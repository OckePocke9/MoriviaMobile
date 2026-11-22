package com.nuvio.app.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.nuvio.app.core.ui.AppTheme
import com.nuvio.app.core.ui.LocalAmoled
import com.nuvio.app.core.ui.LocalNuvioThemeTokens
import com.nuvio.app.core.ui.NuvioTheme

/** Nuvio's original Settings colours, measured from the owner's reference screenshot. */
internal val SettingsNuvioBackground = Color(0xFF0E0C0E)
internal val SettingsNuvioCard = Color(0xFF1A191B)

/**
 * Settings looks like Nuvio's original app whatever theme is picked (owner's request,
 * 2026-11-22): background #0E0C0E, grey cards #1A191B and white icons on grey tiles (Nuvio's
 * neutral White accent). The AMOLED setting only turns the background pure black; the cards
 * stay grey. The rest of the app keeps following the chosen theme.
 */
@Composable
internal fun SettingsBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val amoled = LocalAmoled.current
    NuvioTheme(darkTheme = true, appTheme = AppTheme.WHITE, amoled = false) {
        val tokens = LocalNuvioThemeTokens.current
        val colorScheme = MaterialTheme.colorScheme
        val background = if (amoled) Color.Black else SettingsNuvioBackground
        CompositionLocalProvider(
            LocalNuvioThemeTokens provides tokens.copy(
                colors = tokens.colors.copy(
                    background = background,
                    surface = SettingsNuvioCard,
                    surfaceCard = SettingsNuvioCard,
                ),
            ),
        ) {
            MaterialTheme(
                colorScheme = colorScheme.copy(background = background, surface = SettingsNuvioCard),
                typography = MaterialTheme.typography,
                shapes = MaterialTheme.shapes,
            ) {
                Box(modifier = modifier.background(background)) {
                    content()
                }
            }
        }
    }
}

/** Card colour for Settings groups: Nuvio's grey, also with AMOLED. */
@Composable
internal fun settingsCardColor(): Color = SettingsNuvioCard
