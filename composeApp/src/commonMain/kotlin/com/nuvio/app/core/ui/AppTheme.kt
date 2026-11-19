package com.nuvio.app.core.ui

import nuvio.composeapp.generated.resources.*
import nuvio.composeapp.generated.resources.theme_amber
import nuvio.composeapp.generated.resources.theme_crimson
import nuvio.composeapp.generated.resources.theme_emerald
import nuvio.composeapp.generated.resources.theme_ocean
import nuvio.composeapp.generated.resources.theme_rose
import nuvio.composeapp.generated.resources.theme_violet
import nuvio.composeapp.generated.resources.theme_white
import nuvio.composeapp.generated.resources.theme_gold
import nuvio.composeapp.generated.resources.theme_jade
import nuvio.composeapp.generated.resources.theme_rose_gold
import nuvio.composeapp.generated.resources.theme_arctic_blue
import nuvio.composeapp.generated.resources.theme_graphite
import nuvio.composeapp.generated.resources.theme_custom
import nuvio.composeapp.generated.resources.theme_original
import org.jetbrains.compose.resources.StringResource

enum class AppTheme {
    // Morivia default: the colours of the original Morivia logo.
    ORIGINAL,
    GOLD,
    JADE,
    ROSE_GOLD,
    ARCTIC_BLUE,
    GRAPHITE,
    CUSTOM,
    CRIMSON,
    OCEAN,
    VIOLET,
    EMERALD,
    AMBER,
    ROSE,
    WHITE,
    COPPER,
    AMETHYST,
    APRICOT,
    AUBERGINE,
    AZURE,
    BASIL,
    BERRY,
    BLUSH,
    CERULEAN,
    CHERRY,
    COBALT,
    CORAL,
    DENIM,
    FERN,
    GLACIER,
    GRAPE,
    HEATHER,
    INDIGO,
    LAGOON,
    LAVENDER,
    LEMON,
    LILAC,
    LIME,
    MANGO,
    MINT,
    MOSS,
    ORCHID,
    PEACH,
    PERIWINKLE,
    PINE,
    SAFFRON,
    TEAL,
}

internal val APPEARANCE_PRESETS = listOf(
    AppTheme.AMETHYST, AppTheme.APRICOT, AppTheme.AUBERGINE, AppTheme.AZURE,
    AppTheme.BASIL, AppTheme.BERRY, AppTheme.BLUSH, AppTheme.CERULEAN,
    AppTheme.CHERRY, AppTheme.COBALT, AppTheme.CORAL, AppTheme.DENIM,
    AppTheme.FERN, AppTheme.GLACIER, AppTheme.GRAPE, AppTheme.HEATHER,
    AppTheme.INDIGO, AppTheme.LAGOON, AppTheme.LAVENDER, AppTheme.LEMON,
    AppTheme.LILAC, AppTheme.LIME, AppTheme.MANGO, AppTheme.MINT,
    AppTheme.MOSS, AppTheme.ORCHID, AppTheme.PEACH, AppTheme.PERIWINKLE,
    AppTheme.PINE, AppTheme.SAFFRON, AppTheme.TEAL,
)

val AppTheme.labelRes: StringResource
    get() = when (this) {
        AppTheme.ORIGINAL -> Res.string.theme_original
        AppTheme.GOLD -> Res.string.theme_gold
        AppTheme.JADE -> Res.string.theme_jade
        AppTheme.ROSE_GOLD -> Res.string.theme_rose_gold
        AppTheme.ARCTIC_BLUE -> Res.string.theme_arctic_blue
        AppTheme.GRAPHITE -> Res.string.theme_graphite
        AppTheme.CUSTOM -> Res.string.theme_custom
        AppTheme.CRIMSON -> Res.string.theme_crimson
        AppTheme.OCEAN -> Res.string.theme_ocean
        AppTheme.VIOLET -> Res.string.theme_violet
        AppTheme.EMERALD -> Res.string.theme_emerald
        AppTheme.AMBER -> Res.string.theme_amber
        AppTheme.ROSE -> Res.string.theme_rose
        AppTheme.WHITE -> Res.string.theme_white
        AppTheme.COPPER -> Res.string.settings_appearance_app_icon_copper
        AppTheme.AMETHYST -> Res.string.theme_amethyst
        AppTheme.APRICOT -> Res.string.theme_apricot
        AppTheme.AUBERGINE -> Res.string.theme_aubergine
        AppTheme.AZURE -> Res.string.theme_azure
        AppTheme.BASIL -> Res.string.theme_basil
        AppTheme.BERRY -> Res.string.theme_berry
        AppTheme.BLUSH -> Res.string.theme_blush
        AppTheme.CERULEAN -> Res.string.theme_cerulean
        AppTheme.CHERRY -> Res.string.theme_cherry
        AppTheme.COBALT -> Res.string.theme_cobalt
        AppTheme.CORAL -> Res.string.theme_coral
        AppTheme.DENIM -> Res.string.theme_denim
        AppTheme.FERN -> Res.string.theme_fern
        AppTheme.GLACIER -> Res.string.theme_glacier
        AppTheme.GRAPE -> Res.string.theme_grape
        AppTheme.HEATHER -> Res.string.theme_heather
        AppTheme.INDIGO -> Res.string.theme_indigo
        AppTheme.LAGOON -> Res.string.theme_lagoon
        AppTheme.LAVENDER -> Res.string.theme_lavender
        AppTheme.LEMON -> Res.string.theme_lemon
        AppTheme.LILAC -> Res.string.theme_lilac
        AppTheme.LIME -> Res.string.theme_lime
        AppTheme.MANGO -> Res.string.theme_mango
        AppTheme.MINT -> Res.string.theme_mint
        AppTheme.MOSS -> Res.string.theme_moss
        AppTheme.ORCHID -> Res.string.theme_orchid
        AppTheme.PEACH -> Res.string.theme_peach
        AppTheme.PERIWINKLE -> Res.string.theme_periwinkle
        AppTheme.PINE -> Res.string.theme_pine
        AppTheme.SAFFRON -> Res.string.theme_saffron
        AppTheme.TEAL -> Res.string.theme_teal
    }
