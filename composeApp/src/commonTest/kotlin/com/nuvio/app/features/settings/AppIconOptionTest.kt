package com.nuvio.app.features.settings

import com.nuvio.app.core.ui.AppTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import com.nuvio.app.core.ui.CustomThemeColors
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.app_logo_wordmark_gold

class AppIconOptionTest {
    @Test
    fun primaryIconUsesPlatformDefault() {
        assertEquals(null, AppIconOption.ORIGINAL.platformName)
        assertEquals(AppIconOption.ORIGINAL, AppIconOption.fromPlatformName(null))
    }

    @Test
    fun alternateIconNamesRoundTrip() {
        AppIconOption.entries.drop(1).forEach { icon ->
            assertEquals(icon, AppIconOption.fromPlatformName(icon.platformName))
        }
    }

    @Test
    fun everyPresetThemeHasAMatchingLauncherPalette() {
        AppTheme.entries.filter { it != AppTheme.CUSTOM }.forEach { theme ->
            val icon = iconForTheme(theme, CustomThemeColors.Default)
            assertEquals(theme, icon.theme)
            // The Original theme uses the default launcher entry, which has no alternate name.
            if (theme == AppTheme.ORIGINAL) assertEquals(AppIconOption.ORIGINAL, icon) else assertNotNull(icon.platformName)
        }
        assertEquals(AppIconOption.entries.size, AppIconOption.entries.map { it.platformName }.toSet().size)
    }

    @Test
    fun unknownIconFallsBackToOriginal() {
        assertEquals(AppIconOption.ORIGINAL, AppIconOption.fromPlatformName("UnknownIcon"))
    }

    @Test
    fun goldThemeUsesGoldWordmark() {
        assertEquals(
            Res.drawable.app_logo_wordmark_gold,
            AppTheme.GOLD.wordmarkResource(AppIconOption.COPPER),
        )
    }
}
