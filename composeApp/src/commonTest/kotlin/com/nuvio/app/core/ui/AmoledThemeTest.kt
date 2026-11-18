package com.nuvio.app.core.ui

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class AmoledThemeTest {
    @Test
    fun amoledMakesCardsBlackAndTintsTheBackgroundWithTheTheme() {
        val palette = ThemeColors.Original
        val colors = defaultNuvioThemeTokens(palette, amoled = true, colorScheme = null).colors

        assertEquals(Color.Black, colors.surface)
        assertEquals(Color.Black, colors.surfaceCard)
        assertEquals(palette.amoledBackground(), colors.background)
        assertNotEquals(Color.Black, colors.background)
    }

    @Test
    fun withoutAmoledThePaletteColoursAreUnchanged() {
        val palette = ThemeColors.Original
        val colors = defaultNuvioThemeTokens(palette, amoled = false, colorScheme = null).colors

        assertEquals(palette.background, colors.background)
        assertEquals(palette.backgroundElevated, colors.surface)
        assertEquals(palette.backgroundCard, colors.surfaceCard)
    }

    @Test
    fun originalIsTheFirstStandardThemeAndUsesTheLogoColours() {
        assertEquals(AppTheme.ORIGINAL, AppTheme.entries.first())
        assertEquals(Color(0xFF6B4BF5), ThemeColors.getColorPalette(AppTheme.ORIGINAL).secondary)
    }
}
