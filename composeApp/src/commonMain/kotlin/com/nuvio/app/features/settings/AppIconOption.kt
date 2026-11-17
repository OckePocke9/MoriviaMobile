package com.nuvio.app.features.settings

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
    ;

    val theme: com.nuvio.app.core.ui.AppTheme?
        get() = when (this) {
            ORIGINAL, COPPER -> null
            else -> com.nuvio.app.core.ui.AppTheme.valueOf(name)
        }

    companion object {
        fun fromPlatformName(name: String?): AppIconOption =
            entries.firstOrNull { it.platformName == name } ?: ORIGINAL
    }
}
