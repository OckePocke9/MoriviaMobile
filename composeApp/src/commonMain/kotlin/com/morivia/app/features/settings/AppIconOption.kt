package com.morivia.app.features.settings

internal enum class AppIconOption(
    val key: String,
    val platformName: String?,
) {
    ORIGINAL(
        key = "original",
        platformName = null,
    ),
    ARCTIC_BLUE(
        key = "arctic_blue",
        platformName = "AppIconArcticBlue",
    ),
    EMERALD(
        key = "emerald",
        platformName = "AppIconEmerald",
    ),
    ROSE_GOLD(
        key = "rose_gold",
        platformName = "AppIconRoseGold",
    ),
    COPPER(
        key = "copper",
        platformName = "AppIconCopper",
    ),
    GRAPHITE(
        key = "graphite",
        platformName = "AppIconGraphite",
    ),
    GOLD(key = "gold", platformName = "AppIconGold"),
    JADE(key = "jade", platformName = "AppIconJade"),
    CRIMSON(key = "crimson", platformName = "AppIconCrimson"),
    OCEAN(key = "ocean", platformName = "AppIconOcean"),
    VIOLET(key = "violet", platformName = "AppIconViolet"),
    AMBER(key = "amber", platformName = "AppIconAmber"),
    ROSE(key = "rose", platformName = "AppIconRose"),
    WHITE(key = "white", platformName = "AppIconWhite"),
    AMETHYST(key = "amethyst", platformName = "AppIconAmethyst"),
    APRICOT(key = "apricot", platformName = "AppIconApricot"),
    AUBERGINE(key = "aubergine", platformName = "AppIconAubergine"),
    AZURE(key = "azure", platformName = "AppIconAzure"),
    BASIL(key = "basil", platformName = "AppIconBasil"),
    BERRY(key = "berry", platformName = "AppIconBerry"),
    BLUSH(key = "blush", platformName = "AppIconBlush"),
    CERULEAN(key = "cerulean", platformName = "AppIconCerulean"),
    CHERRY(key = "cherry", platformName = "AppIconCherry"),
    COBALT(key = "cobalt", platformName = "AppIconCobalt"),
    CORAL(key = "coral", platformName = "AppIconCoral"),
    DENIM(key = "denim", platformName = "AppIconDenim"),
    FERN(key = "fern", platformName = "AppIconFern"),
    GLACIER(key = "glacier", platformName = "AppIconGlacier"),
    GRAPE(key = "grape", platformName = "AppIconGrape"),
    HEATHER(key = "heather", platformName = "AppIconHeather"),
    INDIGO(key = "indigo", platformName = "AppIconIndigo"),
    LAGOON(key = "lagoon", platformName = "AppIconLagoon"),
    LAVENDER(key = "lavender", platformName = "AppIconLavender"),
    LEMON(key = "lemon", platformName = "AppIconLemon"),
    LILAC(key = "lilac", platformName = "AppIconLilac"),
    LIME(key = "lime", platformName = "AppIconLime"),
    MANGO(key = "mango", platformName = "AppIconMango"),
    MINT(key = "mint", platformName = "AppIconMint"),
    MOSS(key = "moss", platformName = "AppIconMoss"),
    ORCHID(key = "orchid", platformName = "AppIconOrchid"),
    PEACH(key = "peach", platformName = "AppIconPeach"),
    PERIWINKLE(key = "periwinkle", platformName = "AppIconPeriwinkle"),
    PINE(key = "pine", platformName = "AppIconPine"),
    SAFFRON(key = "saffron", platformName = "AppIconSaffron"),
    TEAL(key = "teal", platformName = "AppIconTeal"),
    ;

    val theme: com.morivia.app.core.ui.AppTheme?
        get() = when (this) {
            ORIGINAL -> com.morivia.app.core.ui.AppTheme.ORIGINAL
            COPPER -> com.morivia.app.core.ui.AppTheme.COPPER
            else -> com.morivia.app.core.ui.AppTheme.valueOf(name)
        }

    companion object {
        fun fromPlatformName(name: String?): AppIconOption =
            entries.firstOrNull { it.platformName == name } ?: ORIGINAL
    }
}
