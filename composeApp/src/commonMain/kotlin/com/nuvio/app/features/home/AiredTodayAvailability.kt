package com.nuvio.app.features.home

import co.touchlab.kermit.Logger
import com.nuvio.app.features.addons.AddonRepository
import com.nuvio.app.features.addons.buildAddonResourceUrl
import com.nuvio.app.features.addons.enabledAddons
import com.nuvio.app.features.addons.fetchAddonResponseText
import com.nuvio.app.features.addons.hasPendingEnabledManifests
import kotlinx.coroutines.flow.first
import com.nuvio.app.features.streams.StreamItem
import com.nuvio.app.features.streams.StreamParser
import com.nuvio.app.features.streams.supportsStream
import com.nuvio.app.features.trakt.AiredTodayTitle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * "Movies/Series That Aired Today" (owner, 2026-11-25): only titles the stream addons already have,
 * ordered by the best resolution they have it in: 4K first, then 2K (1440p), 1080p, 720p and last
 * any other. Within a resolution the order from Trakt stays (series in the order they aired).
 * Without any stream addon the list is left as it is.
 */
internal object AiredTodayAvailability {
    private val log = Logger.withTag("AiredTodayAvailability")
    private const val MAX_TITLES = 120
    private const val PARALLEL_REQUESTS = 8
    private const val REQUEST_TIMEOUT_MS = 12_000L

    /** Best resolution found per stream id for the day, so the ten-minute refresh only asks for new titles. */
    private val bestResolution = mutableMapOf<String, Int>()
    private val cacheLock = Mutex()

    suspend fun order(titles: List<AiredTodayTitle>): List<MetaPreview> = withContext(Dispatchers.Default) {
        // Home can open before the addons are loaded; wait for them, or the list would go out unchecked.
        val loaded = withTimeoutOrNull(20_000L) {
            AddonRepository.uiState.first { state ->
                state.addons.any { it.enabled && it.manifest != null } &&
                    (!state.addons.hasPendingEnabledManifests() || state.addons.filter { it.enabled }.all { it.manifest != null })
            }
        }?.addons ?: AddonRepository.uiState.value.addons
        val addons = loaded.enabledAddons().mapNotNull { addon ->
            addon.manifest?.let { addon to it }
        }
        val candidates = titles.take(MAX_TITLES)
        if (candidates.none { title -> addons.any { (_, manifest) -> manifest.supportsStream(title.streamType, title.streamId) } }) {
            return@withContext titles.map { it.preview }
        }
        val gate = Semaphore(PARALLEL_REQUESTS)
        val known = cacheLock.withLock { bestResolution.toMap() }
        val found = coroutineScope {
            candidates.map { title ->
                async {
                    title to (known[title.streamId] ?: run {
                        val sources = addons.filter { (_, manifest) -> manifest.supportsStream(title.streamType, title.streamId) }
                        val best = sources.map { (addon, manifest) ->
                            async {
                                gate.withPermit {
                                    withTimeoutOrNull(REQUEST_TIMEOUT_MS) {
                                        runCatching {
                                            val url = buildAddonResourceUrl(
                                                manifestUrl = manifest.transportUrl,
                                                resource = "stream",
                                                type = title.streamType,
                                                id = title.streamId,
                                            )
                                            StreamParser.parse(
                                                payload = fetchAddonResponseText(url = url, forceRefresh = false),
                                                addonName = addon.displayTitle,
                                                addonId = manifest.id,
                                            )
                                        }.getOrNull()
                                    }.orEmpty()
                                }
                            }
                        }.awaitAll().flatten().let { streams ->
                            if (streams.isEmpty()) NOT_AVAILABLE else streams.maxOf(::resolutionOf)
                        }
                        best
                    })
                }
            }.awaitAll()
        }
        val available = found.filter { (_, best) -> best != NOT_AVAILABLE }
        // Only titles that are out are remembered; one that is missing is asked again on the next refresh.
        cacheLock.withLock { available.forEach { (title, best) -> bestResolution[title.streamId] = best } }
        log.i { "aired today: ${titles.size} titles, ${candidates.size} checked, ${available.size} available" }
        available
            .sortedBy { (_, best) -> tierOf(best) }
            .map { (title, _) -> title.preview }
    }

    private const val NOT_AVAILABLE = -1

    private fun tierOf(resolution: Int): Int = when {
        resolution >= 2160 -> 0
        resolution >= 1440 -> 1
        resolution >= 1080 -> 2
        resolution >= 720 -> 3
        else -> 4
    }

    private val resolutionTokens = listOf(
        token("2160p?", "4k", "uhd") to 2160,
        token("1440p?", "2k") to 1440,
        token("1080p?", "fhd") to 1080,
        token("720p?") to 720,
    )

    private fun token(vararg words: String): Regex =
        Regex("(^|[^a-z0-9])(${words.joinToString("|")})([^a-z0-9]|$)")

    private fun resolutionOf(stream: StreamItem): Int {
        val text = listOfNotNull(stream.name, stream.title, stream.description, stream.behaviorHints.filename)
            .joinToString(" ")
            .lowercase()
        return resolutionTokens.firstOrNull { (regex, _) -> regex.containsMatchIn(text) }?.second ?: 0
    }
}
