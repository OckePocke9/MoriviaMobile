package com.nuvio.app.features.profiles

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

const val MAX_PROFILES = 6

@Serializable
data class NuvioProfile(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("profile_index") val profileIndex: Int = 1,
    val name: String = "",
    @SerialName("avatar_color_hex") val avatarColorHex: String = "#1E88E5",
    @SerialName("avatar_id") val avatarId: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("profile_background_id") val profileBackgroundId: String? = null,
    @SerialName("profile_background_url") val profileBackgroundUrl: String? = null,
    @SerialName("uses_primary_addons") val usesPrimaryAddons: Boolean = false,
    @SerialName("uses_primary_plugins") val usesPrimaryPlugins: Boolean = false,
    @SerialName("pin_enabled") val pinEnabled: Boolean = false,
    @SerialName("pin_locked_until") val pinLockedUntil: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
)

@Serializable
data class ProfilePushPayload(
    @SerialName("profile_index") val profileIndex: Int,
    val name: String,
    @SerialName("avatar_color_hex") val avatarColorHex: String,
    @SerialName("uses_primary_addons") val usesPrimaryAddons: Boolean = false,
    @SerialName("uses_primary_plugins") val usesPrimaryPlugins: Boolean = false,
    @SerialName("avatar_id") val avatarId: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("profile_background_id") val profileBackgroundId: String? = null,
    @SerialName("profile_background_url") val profileBackgroundUrl: String? = null,
)

@Serializable
data class PinVerifyResult(
    val unlocked: Boolean = false,
    @SerialName("retry_after_seconds") val retryAfterSeconds: Int = 0,
    val message: String? = null,
)

data class ProfileState(
    val profiles: List<NuvioProfile> = emptyList(),
    val activeProfile: NuvioProfile? = null,
    val isLoaded: Boolean = false,
    val hasEverSelectedProfile: Boolean = false,
    val rememberLastProfileEnabled: Boolean = false,
)

@Serializable
data class AvatarCatalogItem(
    val id: String,
    @SerialName("display_name") val displayName: String = "",
    @SerialName("storage_path") val storagePath: String = "",
    val category: String = "character",
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("bg_color") val bgColor: String? = null,
    @Transient val localImageUrl: String? = null,
    @Transient val memberOnly: Boolean = false,
)

fun parseHexColor(hex: String): Color {
    val cleaned = hex.removePrefix("#")
    return runCatching {
        when (cleaned.length) {
            6 -> Color(("FF$cleaned").toLong(16))
            8 -> Color(cleaned.toLong(16))
            else -> Color(0xFF1E88E5)
        }
    }.getOrDefault(Color(0xFF1E88E5))
}

/**
 * The profile colours: the main colour of each Morivia theme, in the order of the themes, with
 * Original first as the default. A profile colour is saved on the Nuvio profile as a plain
 * #RRGGBB, which Nuvio accepts for any colour, so these sync with Nuvio.
 */
val PROFILE_COLORS = listOf(
    "#6B4BF5", // Original
    "#E8A91C", // Gold
    "#22D37C", // Jade
    "#EC70A9", // Rose Gold
    "#3185F5", // Arctic Blue
    "#AAB2BE", // Graphite
    "#E53935", // Crimson
    "#1E88E5", // Ocean
    "#8E24AA", // Violet
    "#43A047", // Emerald
    "#FB8C00", // Amber
    "#D81B60", // Rose
    "#F5F5F5", // White
)

/** White on dark colours, near-black on light ones (Graphite, White), so initials stay readable. */
fun initialsTextColor(background: androidx.compose.ui.graphics.Color): androidx.compose.ui.graphics.Color =
    if (background.luminance() > 0.6f) androidx.compose.ui.graphics.Color(0xFF111111) else androidx.compose.ui.graphics.Color.White

fun avatarStorageUrl(storagePath: String): String =
    if (storagePath.startsWith("https://") || storagePath.startsWith("http://")) {
        storagePath
    } else {
        "${com.nuvio.app.core.network.ServerConfigurationRepository.active.value.backendUrl}/storage/v1/object/public/avatars/$storagePath"
    }

fun avatarImageUrl(avatar: AvatarCatalogItem): String? =
    avatar.localImageUrl
        ?: avatar.storagePath.takeIf { it.isNotBlank() && !avatar.memberOnly }?.let(::avatarStorageUrl)

fun normalizedAvatarUrl(url: String?): String? =
    url?.trim()?.takeIf { it.isValidAvatarUrl() }

fun String.isValidAvatarUrl(): Boolean {
    val value = trim()
    return value.length <= 2048 &&
        !value.any { it.isWhitespace() } &&
        (value.startsWith("https://") || value.startsWith("http://"))
}

fun profileAvatarImageUrl(profile: NuvioProfile, avatar: AvatarCatalogItem?): String? =
    normalizedAvatarUrl(profile.avatarUrl)
        ?: avatar?.let(::avatarImageUrl)
