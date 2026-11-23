package com.nuvio.app.features.membership

import com.nuvio.app.core.ui.AppTheme
import com.nuvio.app.core.ui.CustomThemeColors

private val supporterThemes = linkedMapOf(
    AppTheme.GOLD to CosmeticEntitlement.GOLD_THEME,
    AppTheme.JADE to CosmeticEntitlement.JADE_THEME,
    AppTheme.ROSE_GOLD to CosmeticEntitlement.ROSE_GOLD_THEME,
    AppTheme.ARCTIC_BLUE to CosmeticEntitlement.ARCTIC_BLUE_THEME,
    AppTheme.GRAPHITE to CosmeticEntitlement.GRAPHITE_THEME,
)

private val standardThemes = listOf(AppTheme.ORIGINAL, AppTheme.WHITE) + AppTheme.entries.filterNot {
    it == AppTheme.ORIGINAL || it == AppTheme.WHITE || it == AppTheme.CUSTOM || it in supporterThemes
}

fun availableAppThemes(entitlements: CosmeticEntitlements): List<AppTheme> {
    val supporter = supporterThemes
        .filterValues(entitlements::includes)
        .keys
        .toList()
    return supporter + AppTheme.CUSTOM + standardThemes
}

fun resolveAppTheme(
    selectedTheme: AppTheme?,
    entitlements: CosmeticEntitlements,
): AppTheme {
    // A profile without a saved theme starts on Original (owner, 2026-11-23).
    if (selectedTheme == null) return AppTheme.ORIGINAL
    return selectedTheme.takeIf { it in availableAppThemes(entitlements) } ?: AppTheme.ORIGINAL
}

fun resolveCustomThemeColors(colors: CustomThemeColors, memberTier: MemberTier?): CustomThemeColors =
    if (memberTier == null) CustomThemeColors.solid(colors.second) else colors
