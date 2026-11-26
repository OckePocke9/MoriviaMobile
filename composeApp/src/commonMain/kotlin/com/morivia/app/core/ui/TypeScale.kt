package com.morivia.app.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

data class MoriviaTypeScale(
    val labelXs: TextStyle,
    val labelSm: TextStyle,
    val bodySm: TextStyle,
    val bodyMd: TextStyle,
    val bodyLg: TextStyle,
    val titleSm: TextStyle,
    val titleMd: TextStyle,
    val titleLg: TextStyle,
    val displaySm: TextStyle,
    val displayMd: TextStyle,
)

internal val LocalMoriviaTypeScale = staticCompositionLocalOf {
    MoriviaTypeScale(
        labelXs = TextStyle(fontSize = MoriviaTokens.Type.labelXs, lineHeight = MoriviaTokens.LineHeight.labelXs, fontWeight = FontWeight.Medium),
        labelSm = TextStyle(fontSize = MoriviaTokens.Type.labelSm, lineHeight = MoriviaTokens.LineHeight.labelSm, fontWeight = FontWeight.Medium),
        bodySm = TextStyle(fontSize = MoriviaTokens.Type.bodySm, lineHeight = MoriviaTokens.LineHeight.bodySm, fontWeight = FontWeight.Normal),
        bodyMd = TextStyle(fontSize = MoriviaTokens.Type.bodyMd, lineHeight = MoriviaTokens.LineHeight.bodyMd, fontWeight = FontWeight.Normal),
        bodyLg = TextStyle(fontSize = MoriviaTokens.Type.bodyLg, lineHeight = MoriviaTokens.LineHeight.bodyLg, fontWeight = FontWeight.Medium),
        titleSm = TextStyle(fontSize = MoriviaTokens.Type.titleSm, lineHeight = MoriviaTokens.LineHeight.titleSm, fontWeight = FontWeight.Bold),
        titleMd = TextStyle(fontSize = MoriviaTokens.Type.titleMd, lineHeight = MoriviaTokens.LineHeight.titleMd, fontWeight = FontWeight.Bold),
        titleLg = TextStyle(fontSize = MoriviaTokens.Type.titleLg, lineHeight = MoriviaTokens.LineHeight.titleLg, fontWeight = FontWeight.Bold),
        displaySm = TextStyle(fontSize = MoriviaTokens.Type.displaySm, lineHeight = MoriviaTokens.LineHeight.displaySm, fontWeight = FontWeight.ExtraBold),
        displayMd = TextStyle(fontSize = MoriviaTokens.Type.displayMd, lineHeight = MoriviaTokens.LineHeight.displayMd, fontWeight = FontWeight.ExtraBold),
    )
}

val MaterialTheme.moriviaTypeScale: MoriviaTypeScale
    @Composable
    get() = LocalMoriviaTypeScale.current
