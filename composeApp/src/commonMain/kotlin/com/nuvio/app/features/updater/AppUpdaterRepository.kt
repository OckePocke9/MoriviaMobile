package com.nuvio.app.features.updater

import com.nuvio.app.features.addons.httpRequestRaw
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.updates_github_api_error
import org.jetbrains.compose.resources.getString

@Serializable
internal data class GitHubReleaseDto(
    @SerialName("tag_name") val tagName: String? = null,
    val name: String? = null,
    val body: String? = null,
    val draft: Boolean = false,
    val prerelease: Boolean = false,
    @SerialName("html_url") val htmlUrl: String? = null,
    val assets: List<GitHubAssetDto> = emptyList(),
)

@Serializable
internal data class GitHubAssetDto(
    val name: String,
    @SerialName("browser_download_url") val browserDownloadUrl: String,
    val size: Long? = null,
    @SerialName("content_type") val contentType: String? = null,
)

internal class NoChannelReleaseException : IllegalStateException()
internal class UpdateRepositoryUnavailableException : IllegalStateException()

internal object AppUpdaterRepository {
    // MoriviaMobile is private, so GitHub hides its releases from the app. Every release is also
    // published to the public MoriviaUpdates repository (APK and notes only) by
    // scripts/publish-update-mirror.ps1, and the updater reads that one.
    const val UPDATE_REPOSITORY = "OckePocke9/MoriviaUpdates"
    const val RELEASES_URL = "https://github.com/$UPDATE_REPOSITORY/releases"
    internal fun browserReleaseUrl(channel: UpdateChannel): String =
        if (channel == UpdateChannel.STABLE) "$RELEASES_URL/latest" else RELEASES_URL
    internal fun releaseApiUrl(channel: UpdateChannel): String =
        "https://api.github.com/repos/$UPDATE_REPOSITORY/${releasePath(channel)}"
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun getLatestChannelUpdate(channel: UpdateChannel): Result<AppUpdate> = runCatching {
        val response = httpRequestRaw(
            method = "GET",
            url = releaseApiUrl(channel),
            headers = mapOf(
                "Accept" to "application/vnd.github+json",
                "User-Agent" to "Morivia-Android",
            ),
            body = "",
        )
        currentCoroutineContext().ensureActive()
        // GitHub also returns 404 for private repositories without authentication.
        // This is not evidence that the installed app is up to date.
        if (response.status == 404) throw UpdateRepositoryUnavailableException()
        if (response.status !in 200..299) {
            error(getString(Res.string.updates_github_api_error, response.status))
        }
        selectUpdate(response.body, channel, AppUpdaterPlatform.getSupportedAbis())
            ?: throw NoChannelReleaseException()
    }

    internal fun releasePath(channel: UpdateChannel): String = when (channel) {
        UpdateChannel.STABLE -> "releases/latest"
        UpdateChannel.BETA -> "releases?per_page=100"
    }

    internal fun selectUpdate(
        responseBody: String,
        channel: UpdateChannel,
        supportedAbis: List<String>,
    ): AppUpdate? {
        val releases = when (channel) {
            UpdateChannel.STABLE -> listOf(json.decodeFromString<GitHubReleaseDto>(responseBody))
            UpdateChannel.BETA -> json.decodeFromString<List<GitHubReleaseDto>>(responseBody)
        }
        return ReleaseSelector.eligibleReleases(releases, channel).firstNotNullOfOrNull { release ->
            val asset = chooseBestApkAsset(release.assets, supportedAbis) ?: return@firstNotNullOfOrNull null
            val tag = release.tagName?.takeIf { VersionUtils.parse(it) != null }
                ?: release.name.orEmpty()
            AppUpdate(
                tag = tag,
                title = release.name?.takeIf { it.isNotBlank() } ?: tag,
                notes = release.body.orEmpty(),
                releaseUrl = release.htmlUrl,
                assetName = asset.name,
                assetUrl = asset.browserDownloadUrl,
                assetSizeBytes = asset.size,
            )
        }
    }

    private fun chooseBestApkAsset(
        assets: List<GitHubAssetDto>,
        supportedAbis: List<String>,
    ): GitHubAssetDto? {
        val apkAssets = assets.filter { asset ->
            asset.name.endsWith(".apk", ignoreCase = true) ||
                asset.contentType == "application/vnd.android.package-archive"
        }
        for (abi in supportedAbis) {
            apkAssets.firstOrNull { asset ->
                Regex("(?i)(?<![a-z0-9])${Regex.escape(abi)}(?![a-z0-9]|_64)")
                    .containsMatchIn(asset.name)
            }?.let { return it }
        }
        return apkAssets.firstOrNull { asset ->
            val name = asset.name.lowercase()
            Regex("(^|[._-])(universal|all)([._-]|$)").containsMatchIn(name)
        } ?: apkAssets.firstOrNull { asset ->
            // Untagged APKs may be universal, but never offer a known incompatible ABI.
            listOf("arm64-v8a", "armeabi-v7a", "x86_64", "x86").none {
                asset.name.contains(it, ignoreCase = true)
            }
        }
    }
}
