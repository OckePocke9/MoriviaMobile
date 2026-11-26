package com.morivia.app.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.morivia.app.core.ui.AppTheme
import com.morivia.app.core.ui.LocalAmoled
import com.morivia.app.core.ui.LocalMoriviaThemeTokens
import com.morivia.app.core.ui.MoriviaTheme

/** The original Settings colours, measured from the owner's reference screenshot. */
internal val SettingsMoriviaBackground = Color(0xFF0E0C0E)
internal val SettingsMoriviaCard = Color(0xFF1A191B)

@Composable
internal fun settingsBackgroundColor(): Color = if (LocalAmoled.current) Color.Black else SettingsMoriviaBackground

/**
 * Settings keeps its original colours whatever theme is picked (owner's request,
 * 2026-11-22): background #0E0C0E, grey cards #1A191B and white icons on grey tiles (the
 * neutral White accent). The AMOLED setting only turns the background pure black; the cards
 * stay grey. The rest of the app keeps following the chosen theme.
 */
@Composable
internal fun SettingsBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val background = settingsBackgroundColor()
    MoriviaTheme(darkTheme = true, appTheme = AppTheme.WHITE, amoled = false) {
        val tokens = LocalMoriviaThemeTokens.current
        val colorScheme = MaterialTheme.colorScheme
        CompositionLocalProvider(
            LocalMoriviaThemeTokens provides tokens.copy(
                colors = tokens.colors.copy(
                    background = background,
                    surface = SettingsMoriviaCard,
                    surfaceCard = SettingsMoriviaCard,
                ),
            ),
        ) {
            MaterialTheme(
                colorScheme = colorScheme.copy(background = background, surface = SettingsMoriviaCard),
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

/** Card colour for Settings groups: the original grey, also with AMOLED. */
@Composable
internal fun settingsCardColor(): Color = SettingsMoriviaCard
