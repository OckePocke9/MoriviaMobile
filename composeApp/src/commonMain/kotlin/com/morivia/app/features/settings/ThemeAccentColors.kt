package com.morivia.app.features.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.morivia.app.core.ui.AppTheme
import com.morivia.app.core.ui.ThemeColors

/**
 * The chosen theme colour (owner's request, 2026-11-23). It only colours the icons on the left
 * of Settings cards, on/off switches, the Appearance buttons and Watch Party's Join button; the
 * rest of the app keeps Morivia's own colours.
 */
internal data class ThemeAccentColors(val accent: Color, val onAccent: Color)

@Composable
internal fun rememberThemeAccent(): ThemeAccentColors {
    LocalAccentOverride.current?.let { override ->
        return ThemeAccentColors(override, if (override.luminance() > 0.45f) Color(0xFF111111) else Color.White)
    }
    val theme by remember {
        ThemeSettingsRepository.ensureLoaded()
        ThemeSettingsRepository.selectedTheme
    }.collectAsStateWithLifecycle()
    val customColors by ThemeSettingsRepository.customThemeColors.collectAsStateWithLifecycle()
    val target = remember(theme, customColors) {
        // Original: the violet in the middle of the new app icon.
        val accent = if (theme == AppTheme.ORIGINAL) Color(0xFF7A2BFF)
        else ThemeColors.getColorPalette(theme, customColors).secondary
        ThemeAccentColors(
            accent = accent,
            onAccent = if (accent.luminance() > 0.45f) Color(0xFF111111) else Color.White,
        )
    }
    // Fades to a new profile's theme colour instead of jumping (owner, 2026-11-23).
    val accent by androidx.compose.animation.animateColorAsState(target.accent, androidx.compose.animation.core.tween(600), label = "themeAccent")
    val onAccent by androidx.compose.animation.animateColorAsState(target.onAccent, androidx.compose.animation.core.tween(600), label = "themeOnAccent")
    return ThemeAccentColors(accent, onAccent)
}

/** An accent set by an outer [ThemedAccent], e.g. a profile's initial colour. */
internal val LocalAccentOverride = androidx.compose.runtime.staticCompositionLocalOf<Color?> { null }

/**
 * Gives [content] the theme colour as its accent (switches, buttons, selection rings, PIN dots),
 * used on Edit/Add Profile and the PIN lock (owner, 2026-11-23).
 */
@Composable
internal fun ThemedAccent(accentOverride: Color? = null, content: @Composable () -> Unit) {
    val override = accentOverride ?: LocalAccentOverride.current
    val theme = rememberThemeAccent()
    val themeAccent = if (override == null) theme else ThemeAccentColors(
        accent = override,
        onAccent = if (override.luminance() > 0.45f) Color(0xFF111111) else Color.White,
    )
    val tokens = com.morivia.app.core.ui.LocalMoriviaThemeTokens.current
    val colorScheme = androidx.compose.material3.MaterialTheme.colorScheme
    androidx.compose.runtime.CompositionLocalProvider(
        LocalAccentOverride provides override,
        com.morivia.app.core.ui.LocalMoriviaThemeTokens provides tokens.copy(
            colors = tokens.colors.copy(
                accent = themeAccent.accent,
                accentStrong = themeAccent.accent,
                onAccent = themeAccent.onAccent,
                borderSelected = themeAccent.accent,
            ),
        ),
    ) {
        androidx.compose.material3.MaterialTheme(
            colorScheme = colorScheme.copy(primary = themeAccent.accent, onPrimary = themeAccent.onAccent),
            typography = androidx.compose.material3.MaterialTheme.typography,
            shapes = androidx.compose.material3.MaterialTheme.shapes,
            content = content,
        )
    }
}
