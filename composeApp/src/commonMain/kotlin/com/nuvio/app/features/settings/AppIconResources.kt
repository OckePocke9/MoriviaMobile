package com.nuvio.app.features.settings

import com.nuvio.app.core.ui.AppTheme
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

internal val AppIconOption.labelResource: StringResource
    get() = when (this) {
        AppIconOption.ORIGINAL -> Res.string.settings_appearance_app_icon_original
        AppIconOption.GOLD -> Res.string.theme_gold
        AppIconOption.JADE -> Res.string.theme_jade
        AppIconOption.CRIMSON -> Res.string.theme_crimson
        AppIconOption.OCEAN -> Res.string.theme_ocean
        AppIconOption.VIOLET -> Res.string.theme_violet
        AppIconOption.AMBER -> Res.string.theme_amber
        AppIconOption.ROSE -> Res.string.theme_rose
        AppIconOption.WHITE -> Res.string.theme_white

        AppIconOption.ARCTIC_BLUE -> Res.string.settings_appearance_app_icon_arctic_blue
        AppIconOption.EMERALD -> Res.string.settings_appearance_app_icon_emerald
        AppIconOption.ROSE_GOLD -> Res.string.settings_appearance_app_icon_rose_gold
        AppIconOption.COPPER -> Res.string.settings_appearance_app_icon_copper
        AppIconOption.GRAPHITE -> Res.string.settings_appearance_app_icon_graphite
    }

internal val AppIconOption.previewResource: DrawableResource
    get() = when (this) {
        AppIconOption.ORIGINAL -> Res.drawable.app_icon_original
        AppIconOption.GOLD -> Res.drawable.app_icon_gold
        AppIconOption.JADE -> Res.drawable.app_icon_jade
        AppIconOption.CRIMSON -> Res.drawable.app_icon_crimson
        AppIconOption.OCEAN -> Res.drawable.app_icon_ocean
        AppIconOption.VIOLET -> Res.drawable.app_icon_violet
        AppIconOption.AMBER -> Res.drawable.app_icon_amber
        AppIconOption.ROSE -> Res.drawable.app_icon_rose
        AppIconOption.WHITE -> Res.drawable.app_icon_white

        AppIconOption.ARCTIC_BLUE -> Res.drawable.app_icon_arctic_blue
        AppIconOption.EMERALD -> Res.drawable.app_icon_emerald
        AppIconOption.ROSE_GOLD -> Res.drawable.app_icon_rose_gold
        AppIconOption.COPPER -> Res.drawable.app_icon_copper
        AppIconOption.GRAPHITE -> Res.drawable.app_icon_graphite
    }

internal val AppIconOption.wordmarkResource: DrawableResource
    get() = when (this) {
        AppIconOption.ORIGINAL, AppIconOption.GOLD, AppIconOption.JADE, AppIconOption.CRIMSON, AppIconOption.OCEAN, AppIconOption.VIOLET, AppIconOption.AMBER, AppIconOption.ROSE, AppIconOption.WHITE -> Res.drawable.app_logo_wordmark_original
        AppIconOption.ARCTIC_BLUE -> Res.drawable.app_logo_wordmark_arctic_blue
        AppIconOption.EMERALD -> Res.drawable.app_logo_wordmark_emerald
        AppIconOption.ROSE_GOLD -> Res.drawable.app_logo_wordmark_rose_gold
        AppIconOption.COPPER -> Res.drawable.app_logo_wordmark_copper
        AppIconOption.GRAPHITE -> Res.drawable.app_logo_wordmark_graphite
    }

internal fun AppTheme.wordmarkResource(fallback: AppIconOption): DrawableResource =
    when (this) {
        AppTheme.GOLD -> Res.drawable.app_logo_wordmark_gold
        AppTheme.JADE -> AppIconOption.EMERALD.wordmarkResource
        AppTheme.ROSE_GOLD -> AppIconOption.ROSE_GOLD.wordmarkResource
        AppTheme.ARCTIC_BLUE -> AppIconOption.ARCTIC_BLUE.wordmarkResource
        AppTheme.GRAPHITE -> AppIconOption.GRAPHITE.wordmarkResource
        else -> fallback.wordmarkResource
    }

/** Preset themes have identical compiled palettes in the picker and both launchers.
 * Android/iOS launchers require precompiled assets; custom colours select the closest preset.
 */
internal fun iconForTheme(theme: AppTheme, custom: com.nuvio.app.core.ui.CustomThemeColors): AppIconOption {
    AppIconOption.entries.firstOrNull { it.theme == theme }?.let { return it }
    val accent = com.nuvio.app.core.ui.ThemeColors.getColorPalette(theme, custom).secondary
    return AppIconOption.entries.filter { it.theme != null }.minBy { icon ->
        val color = com.nuvio.app.core.ui.ThemeColors.getColorPalette(icon.theme!!).secondary
        val r = color.red - accent.red
        val g = color.green - accent.green
        val b = color.blue - accent.blue
        r * r + g * g + b * b
    }
}

internal val AppIconOption.symbolResource: DrawableResource
    get() = when (this) {
        AppIconOption.ORIGINAL -> Res.drawable.morivia_symbol_original
        AppIconOption.ARCTIC_BLUE -> Res.drawable.morivia_symbol_arctic_blue
        AppIconOption.EMERALD -> Res.drawable.morivia_symbol_emerald
        AppIconOption.ROSE_GOLD -> Res.drawable.morivia_symbol_rose_gold
        AppIconOption.COPPER -> Res.drawable.morivia_symbol_copper
        AppIconOption.GRAPHITE -> Res.drawable.morivia_symbol_graphite
        AppIconOption.GOLD -> Res.drawable.morivia_symbol_gold
        AppIconOption.JADE -> Res.drawable.morivia_symbol_jade
        AppIconOption.CRIMSON -> Res.drawable.morivia_symbol_crimson
        AppIconOption.OCEAN -> Res.drawable.morivia_symbol_ocean
        AppIconOption.VIOLET -> Res.drawable.morivia_symbol_violet
        AppIconOption.AMBER -> Res.drawable.morivia_symbol_amber
        AppIconOption.ROSE -> Res.drawable.morivia_symbol_rose
        AppIconOption.WHITE -> Res.drawable.morivia_symbol_white
    }
