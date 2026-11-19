package com.nuvio.app.features.profiles

import com.nuvio.app.core.ui.ThemeColorPalette
import com.nuvio.app.core.ui.ThemeColors
import com.nuvio.app.core.ui.accentBrush
import com.nuvio.app.core.ui.labelRes
import com.nuvio.app.features.settings.AppIconOption
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.SolidColor
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.settings_appearance_app_icon_copper
import org.jetbrains.compose.resources.StringResource
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
 * Copper exists only as an app icon, so its palette is sampled from that icon
 * (app_icon_copper.png), built like the Gold theme.
 */
internal val CopperInitialsPalette = ThemeColorPalette(
    secondary = Color(0xFFC9804A),
    secondaryVariant = Color(0xFF914407),
    accentGradient = listOf(
        Color(0xFF914407),
        Color(0xFFC9804A),
        Color(0xFFE6B37E),
        Color(0xFFDB9C62),
        Color(0xFF965529),
    ),
    nativeAccentHex = "#C9804A",
    onSecondary = Color(0xFF111111),
    focusRing = Color(0xFFE6B37E),
    focusBackground = Color(0xFF3D261A),
)

/** One initials colour: the same name, gradient and text colour as its theme and app icon. */
internal data class InitialsColorOption(
    val hex: String,
    val label: StringResource,
    val palette: ThemeColorPalette,
)

private fun ThemeColorPalette.hex(): String =
    "#" + (secondary.toArgb() and 0xFFFFFF).toString(16).uppercase().padStart(6, '0')

/**
 * The initials colours are exactly the app icons in Appearance (the same list and order), each
 * drawn with its Appearance theme's palette, so initials, theme and app icon match everywhere.
 * Original comes first as the default. A colour is saved on the Nuvio profile as a plain
 * #RRGGBB, which Nuvio accepts for any colour, so these sync with Nuvio.
 */
internal val INITIALS_COLOR_OPTIONS: List<InitialsColorOption> = AppIconOption.entries.map { icon ->
    val theme = icon.theme
    if (theme == null) {
        InitialsColorOption(CopperInitialsPalette.hex(), Res.string.settings_appearance_app_icon_copper, CopperInitialsPalette)
    } else {
        val palette = ThemeColors.getColorPalette(theme)
        InitialsColorOption(palette.hex(), theme.labelRes, palette)
    }
}

val PROFILE_COLORS: List<String> = INITIALS_COLOR_OPTIONS.map { it.hex }

internal fun initialsPalette(color: Color): ThemeColorPalette? =
    INITIALS_COLOR_OPTIONS.firstOrNull { it.palette.secondary == color }?.palette

/** The theme gradient behind initials, or the plain colour when it is not a Morivia colour. */
internal fun initialsBrush(color: Color): Brush = initialsPalette(color)?.accentBrush() ?: SolidColor(color)

/**
 * The theme's own text colour (as on the Appearance theme chips); for other colours white on
 * dark and near-black on light ones, so initials stay readable.
 */
fun initialsTextColor(background: Color): Color =
    initialsPalette(background)?.onSecondary
        ?: if (background.luminance() > 0.6f) Color(0xFF111111) else Color.White

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
