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
        AppIconOption.AMETHYST -> Res.string.theme_amethyst
        AppIconOption.APRICOT -> Res.string.theme_apricot
        AppIconOption.AUBERGINE -> Res.string.theme_aubergine
        AppIconOption.AZURE -> Res.string.theme_azure
        AppIconOption.BASIL -> Res.string.theme_basil
        AppIconOption.BERRY -> Res.string.theme_berry
        AppIconOption.BLUSH -> Res.string.theme_blush
        AppIconOption.CERULEAN -> Res.string.theme_cerulean
        AppIconOption.CHERRY -> Res.string.theme_cherry
        AppIconOption.COBALT -> Res.string.theme_cobalt
        AppIconOption.CORAL -> Res.string.theme_coral
        AppIconOption.DENIM -> Res.string.theme_denim
        AppIconOption.FERN -> Res.string.theme_fern
        AppIconOption.GLACIER -> Res.string.theme_glacier
        AppIconOption.GRAPE -> Res.string.theme_grape
        AppIconOption.HEATHER -> Res.string.theme_heather
        AppIconOption.INDIGO -> Res.string.theme_indigo
        AppIconOption.LAGOON -> Res.string.theme_lagoon
        AppIconOption.LAVENDER -> Res.string.theme_lavender
        AppIconOption.LEMON -> Res.string.theme_lemon
        AppIconOption.LILAC -> Res.string.theme_lilac
        AppIconOption.LIME -> Res.string.theme_lime
        AppIconOption.MANGO -> Res.string.theme_mango
        AppIconOption.MINT -> Res.string.theme_mint
        AppIconOption.MOSS -> Res.string.theme_moss
        AppIconOption.ORCHID -> Res.string.theme_orchid
        AppIconOption.PEACH -> Res.string.theme_peach
        AppIconOption.PERIWINKLE -> Res.string.theme_periwinkle
        AppIconOption.PINE -> Res.string.theme_pine
        AppIconOption.SAFFRON -> Res.string.theme_saffron
        AppIconOption.TEAL -> Res.string.theme_teal

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
        AppIconOption.AMETHYST -> Res.drawable.app_icon_amethyst
        AppIconOption.APRICOT -> Res.drawable.app_icon_apricot
        AppIconOption.AUBERGINE -> Res.drawable.app_icon_aubergine
        AppIconOption.AZURE -> Res.drawable.app_icon_azure
        AppIconOption.BASIL -> Res.drawable.app_icon_basil
        AppIconOption.BERRY -> Res.drawable.app_icon_berry
        AppIconOption.BLUSH -> Res.drawable.app_icon_blush
        AppIconOption.CERULEAN -> Res.drawable.app_icon_cerulean
        AppIconOption.CHERRY -> Res.drawable.app_icon_cherry
        AppIconOption.COBALT -> Res.drawable.app_icon_cobalt
        AppIconOption.CORAL -> Res.drawable.app_icon_coral
        AppIconOption.DENIM -> Res.drawable.app_icon_denim
        AppIconOption.FERN -> Res.drawable.app_icon_fern
        AppIconOption.GLACIER -> Res.drawable.app_icon_glacier
        AppIconOption.GRAPE -> Res.drawable.app_icon_grape
        AppIconOption.HEATHER -> Res.drawable.app_icon_heather
        AppIconOption.INDIGO -> Res.drawable.app_icon_indigo
        AppIconOption.LAGOON -> Res.drawable.app_icon_lagoon
        AppIconOption.LAVENDER -> Res.drawable.app_icon_lavender
        AppIconOption.LEMON -> Res.drawable.app_icon_lemon
        AppIconOption.LILAC -> Res.drawable.app_icon_lilac
        AppIconOption.LIME -> Res.drawable.app_icon_lime
        AppIconOption.MANGO -> Res.drawable.app_icon_mango
        AppIconOption.MINT -> Res.drawable.app_icon_mint
        AppIconOption.MOSS -> Res.drawable.app_icon_moss
        AppIconOption.ORCHID -> Res.drawable.app_icon_orchid
        AppIconOption.PEACH -> Res.drawable.app_icon_peach
        AppIconOption.PERIWINKLE -> Res.drawable.app_icon_periwinkle
        AppIconOption.PINE -> Res.drawable.app_icon_pine
        AppIconOption.SAFFRON -> Res.drawable.app_icon_saffron
        AppIconOption.TEAL -> Res.drawable.app_icon_teal

        AppIconOption.ARCTIC_BLUE -> Res.drawable.app_icon_arctic_blue
        AppIconOption.EMERALD -> Res.drawable.app_icon_emerald
        AppIconOption.ROSE_GOLD -> Res.drawable.app_icon_rose_gold
        AppIconOption.COPPER -> Res.drawable.app_icon_copper
        AppIconOption.GRAPHITE -> Res.drawable.app_icon_graphite
    }

internal val AppIconOption.wordmarkResource: DrawableResource
    get() = when (this) {
        AppIconOption.ORIGINAL -> Res.drawable.app_logo_wordmark_original
        AppIconOption.ARCTIC_BLUE -> Res.drawable.app_logo_wordmark_arctic_blue
        AppIconOption.EMERALD -> Res.drawable.app_logo_wordmark_emerald
        AppIconOption.ROSE_GOLD -> Res.drawable.app_logo_wordmark_rose_gold
        AppIconOption.COPPER -> Res.drawable.app_logo_wordmark_copper
        AppIconOption.GRAPHITE -> Res.drawable.app_logo_wordmark_graphite
        AppIconOption.GOLD -> Res.drawable.app_logo_wordmark_gold
        AppIconOption.JADE -> Res.drawable.app_logo_wordmark_jade
        AppIconOption.CRIMSON -> Res.drawable.app_logo_wordmark_crimson
        AppIconOption.OCEAN -> Res.drawable.app_logo_wordmark_ocean
        AppIconOption.VIOLET -> Res.drawable.app_logo_wordmark_violet
        AppIconOption.AMBER -> Res.drawable.app_logo_wordmark_amber
        AppIconOption.ROSE -> Res.drawable.app_logo_wordmark_rose
        AppIconOption.WHITE -> Res.drawable.app_logo_wordmark_white
        AppIconOption.AMETHYST -> Res.drawable.app_logo_wordmark_amethyst
        AppIconOption.APRICOT -> Res.drawable.app_logo_wordmark_apricot
        AppIconOption.AUBERGINE -> Res.drawable.app_logo_wordmark_aubergine
        AppIconOption.AZURE -> Res.drawable.app_logo_wordmark_azure
        AppIconOption.BASIL -> Res.drawable.app_logo_wordmark_basil
        AppIconOption.BERRY -> Res.drawable.app_logo_wordmark_berry
        AppIconOption.BLUSH -> Res.drawable.app_logo_wordmark_blush
        AppIconOption.CERULEAN -> Res.drawable.app_logo_wordmark_cerulean
        AppIconOption.CHERRY -> Res.drawable.app_logo_wordmark_cherry
        AppIconOption.COBALT -> Res.drawable.app_logo_wordmark_cobalt
        AppIconOption.CORAL -> Res.drawable.app_logo_wordmark_coral
        AppIconOption.DENIM -> Res.drawable.app_logo_wordmark_denim
        AppIconOption.FERN -> Res.drawable.app_logo_wordmark_fern
        AppIconOption.GLACIER -> Res.drawable.app_logo_wordmark_glacier
        AppIconOption.GRAPE -> Res.drawable.app_logo_wordmark_grape
        AppIconOption.HEATHER -> Res.drawable.app_logo_wordmark_heather
        AppIconOption.INDIGO -> Res.drawable.app_logo_wordmark_indigo
        AppIconOption.LAGOON -> Res.drawable.app_logo_wordmark_lagoon
        AppIconOption.LAVENDER -> Res.drawable.app_logo_wordmark_lavender
        AppIconOption.LEMON -> Res.drawable.app_logo_wordmark_lemon
        AppIconOption.LILAC -> Res.drawable.app_logo_wordmark_lilac
        AppIconOption.LIME -> Res.drawable.app_logo_wordmark_lime
        AppIconOption.MANGO -> Res.drawable.app_logo_wordmark_mango
        AppIconOption.MINT -> Res.drawable.app_logo_wordmark_mint
        AppIconOption.MOSS -> Res.drawable.app_logo_wordmark_moss
        AppIconOption.ORCHID -> Res.drawable.app_logo_wordmark_orchid
        AppIconOption.PEACH -> Res.drawable.app_logo_wordmark_peach
        AppIconOption.PERIWINKLE -> Res.drawable.app_logo_wordmark_periwinkle
        AppIconOption.PINE -> Res.drawable.app_logo_wordmark_pine
        AppIconOption.SAFFRON -> Res.drawable.app_logo_wordmark_saffron
        AppIconOption.TEAL -> Res.drawable.app_logo_wordmark_teal
    }

/** The wordmark in the theme's colours: the logo keeps its details, only the colour changes. */
internal fun AppTheme.wordmarkResource(fallback: AppIconOption): DrawableResource =
    (AppIconOption.entries.firstOrNull { it.theme == this } ?: fallback).wordmarkResource

/** The earlier Morivia app's logo (profile screens) in the colours of this icon. */
internal val AppIconOption.brandSymbolResource: DrawableResource
    get() = when (this) {
        AppIconOption.ORIGINAL -> Res.drawable.morivia_brand_symbol
        AppIconOption.ARCTIC_BLUE -> Res.drawable.morivia_brand_symbol_arctic_blue
        AppIconOption.EMERALD -> Res.drawable.morivia_brand_symbol_emerald
        AppIconOption.ROSE_GOLD -> Res.drawable.morivia_brand_symbol_rose_gold
        AppIconOption.COPPER -> Res.drawable.morivia_brand_symbol_copper
        AppIconOption.GRAPHITE -> Res.drawable.morivia_brand_symbol_graphite
        AppIconOption.GOLD -> Res.drawable.morivia_brand_symbol_gold
        AppIconOption.JADE -> Res.drawable.morivia_brand_symbol_jade
        AppIconOption.CRIMSON -> Res.drawable.morivia_brand_symbol_crimson
        AppIconOption.OCEAN -> Res.drawable.morivia_brand_symbol_ocean
        AppIconOption.VIOLET -> Res.drawable.morivia_brand_symbol_violet
        AppIconOption.AMBER -> Res.drawable.morivia_brand_symbol_amber
        AppIconOption.ROSE -> Res.drawable.morivia_brand_symbol_rose
        AppIconOption.WHITE -> Res.drawable.morivia_brand_symbol_white
        AppIconOption.AMETHYST -> Res.drawable.morivia_brand_symbol_amethyst
        AppIconOption.APRICOT -> Res.drawable.morivia_brand_symbol_apricot
        AppIconOption.AUBERGINE -> Res.drawable.morivia_brand_symbol_aubergine
        AppIconOption.AZURE -> Res.drawable.morivia_brand_symbol_azure
        AppIconOption.BASIL -> Res.drawable.morivia_brand_symbol_basil
        AppIconOption.BERRY -> Res.drawable.morivia_brand_symbol_berry
        AppIconOption.BLUSH -> Res.drawable.morivia_brand_symbol_blush
        AppIconOption.CERULEAN -> Res.drawable.morivia_brand_symbol_cerulean
        AppIconOption.CHERRY -> Res.drawable.morivia_brand_symbol_cherry
        AppIconOption.COBALT -> Res.drawable.morivia_brand_symbol_cobalt
        AppIconOption.CORAL -> Res.drawable.morivia_brand_symbol_coral
        AppIconOption.DENIM -> Res.drawable.morivia_brand_symbol_denim
        AppIconOption.FERN -> Res.drawable.morivia_brand_symbol_fern
        AppIconOption.GLACIER -> Res.drawable.morivia_brand_symbol_glacier
        AppIconOption.GRAPE -> Res.drawable.morivia_brand_symbol_grape
        AppIconOption.HEATHER -> Res.drawable.morivia_brand_symbol_heather
        AppIconOption.INDIGO -> Res.drawable.morivia_brand_symbol_indigo
        AppIconOption.LAGOON -> Res.drawable.morivia_brand_symbol_lagoon
        AppIconOption.LAVENDER -> Res.drawable.morivia_brand_symbol_lavender
        AppIconOption.LEMON -> Res.drawable.morivia_brand_symbol_lemon
        AppIconOption.LILAC -> Res.drawable.morivia_brand_symbol_lilac
        AppIconOption.LIME -> Res.drawable.morivia_brand_symbol_lime
        AppIconOption.MANGO -> Res.drawable.morivia_brand_symbol_mango
        AppIconOption.MINT -> Res.drawable.morivia_brand_symbol_mint
        AppIconOption.MOSS -> Res.drawable.morivia_brand_symbol_moss
        AppIconOption.ORCHID -> Res.drawable.morivia_brand_symbol_orchid
        AppIconOption.PEACH -> Res.drawable.morivia_brand_symbol_peach
        AppIconOption.PERIWINKLE -> Res.drawable.morivia_brand_symbol_periwinkle
        AppIconOption.PINE -> Res.drawable.morivia_brand_symbol_pine
        AppIconOption.SAFFRON -> Res.drawable.morivia_brand_symbol_saffron
        AppIconOption.TEAL -> Res.drawable.morivia_brand_symbol_teal
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
        AppIconOption.AMETHYST -> Res.drawable.morivia_symbol_amethyst
        AppIconOption.APRICOT -> Res.drawable.morivia_symbol_apricot
        AppIconOption.AUBERGINE -> Res.drawable.morivia_symbol_aubergine
        AppIconOption.AZURE -> Res.drawable.morivia_symbol_azure
        AppIconOption.BASIL -> Res.drawable.morivia_symbol_basil
        AppIconOption.BERRY -> Res.drawable.morivia_symbol_berry
        AppIconOption.BLUSH -> Res.drawable.morivia_symbol_blush
        AppIconOption.CERULEAN -> Res.drawable.morivia_symbol_cerulean
        AppIconOption.CHERRY -> Res.drawable.morivia_symbol_cherry
        AppIconOption.COBALT -> Res.drawable.morivia_symbol_cobalt
        AppIconOption.CORAL -> Res.drawable.morivia_symbol_coral
        AppIconOption.DENIM -> Res.drawable.morivia_symbol_denim
        AppIconOption.FERN -> Res.drawable.morivia_symbol_fern
        AppIconOption.GLACIER -> Res.drawable.morivia_symbol_glacier
        AppIconOption.GRAPE -> Res.drawable.morivia_symbol_grape
        AppIconOption.HEATHER -> Res.drawable.morivia_symbol_heather
        AppIconOption.INDIGO -> Res.drawable.morivia_symbol_indigo
        AppIconOption.LAGOON -> Res.drawable.morivia_symbol_lagoon
        AppIconOption.LAVENDER -> Res.drawable.morivia_symbol_lavender
        AppIconOption.LEMON -> Res.drawable.morivia_symbol_lemon
        AppIconOption.LILAC -> Res.drawable.morivia_symbol_lilac
        AppIconOption.LIME -> Res.drawable.morivia_symbol_lime
        AppIconOption.MANGO -> Res.drawable.morivia_symbol_mango
        AppIconOption.MINT -> Res.drawable.morivia_symbol_mint
        AppIconOption.MOSS -> Res.drawable.morivia_symbol_moss
        AppIconOption.ORCHID -> Res.drawable.morivia_symbol_orchid
        AppIconOption.PEACH -> Res.drawable.morivia_symbol_peach
        AppIconOption.PERIWINKLE -> Res.drawable.morivia_symbol_periwinkle
        AppIconOption.PINE -> Res.drawable.morivia_symbol_pine
        AppIconOption.SAFFRON -> Res.drawable.morivia_symbol_saffron
        AppIconOption.TEAL -> Res.drawable.morivia_symbol_teal
    }
