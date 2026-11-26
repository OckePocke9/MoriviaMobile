package com.morivia.app.core.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Typography
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import morivia.composeapp.generated.resources.Res
import morivia.composeapp.generated.resources.jetbrains_sans_bold
import morivia.composeapp.generated.resources.jetbrains_sans_regular
import morivia.composeapp.generated.resources.jetbrains_sans_semibold
import org.jetbrains.compose.resources.Font

val LocalAppTheme = staticCompositionLocalOf { AppTheme.ORIGINAL }
val LocalThemePalette = staticCompositionLocalOf { ThemeColors.Original }

/** True when AMOLED black is on for the current theme: cards and other surfaces are pure black. */
val LocalAmoled = staticCompositionLocalOf { false }

/** A card colour: pure black with AMOLED black on, otherwise [default]. */
@Composable
@ReadOnlyComposable
fun amoledCardColor(default: Color): Color = if (LocalAmoled.current) Color.Black else default

/**
 * Home, Search and Library: with AMOLED black on, the screen background is pure black instead of
 * the theme-tinted AMOLED background that Profile and Settings keep.
 */
@Composable
fun AmoledBlackBackground(content: @Composable () -> Unit) {
    if (!LocalAmoled.current) {
        content()
        return
    }
    val tokens = MaterialTheme.nuvio
    val colorScheme = MaterialTheme.colorScheme
    CompositionLocalProvider(
        LocalMoriviaThemeTokens provides tokens.copy(colors = tokens.colors.copy(background = Color.Black)),
    ) {
        MaterialTheme(
            colorScheme = colorScheme.copy(background = Color.Black),
            typography = MaterialTheme.typography,
            shapes = MaterialTheme.shapes,
            content = content,
        )
    }
}

val MaterialTheme.themePalette: ThemeColorPalette
    @Composable
    @ReadOnlyComposable
    get() = LocalThemePalette.current

val MaterialTheme.appTheme: AppTheme
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTheme.current

private fun contentColorFor(background: Color): Color =
    if (background.luminance() > 0.5f) Color(0xFF111111) else Color(0xFFF5F7F8)

private fun buildColorScheme(palette: ThemeColorPalette, amoled: Boolean = false) = darkColorScheme(
    primary = palette.secondary,
    onPrimary = palette.onSecondary,
    primaryContainer = palette.focusBackground,
    onPrimaryContainer = contentColorFor(palette.focusBackground),
    secondary = palette.secondaryVariant,
    onSecondary = palette.onSecondaryVariant,
    background = if (amoled) palette.amoledBackground() else palette.background,
    onBackground = Color(0xFFF5F7F8),
    surface = if (amoled) Color.Black else palette.backgroundElevated,
    onSurface = Color(0xFFF5F7F8),
    surfaceVariant = if (amoled) Color.Black else palette.backgroundCard,
    onSurfaceVariant = Color(0xFF969CA3),
    outline = Color(0xFF252A2A),
    error = Color(0xFFE36A8A),
    onError = Color(0xFFFCE5EC),
)

private val JetBrainsSans: FontFamily
    @Composable
    get() = FontFamily(
        Font(Res.font.jetbrains_sans_bold, FontWeight.Bold, FontStyle.Normal),
        Font(Res.font.jetbrains_sans_semibold, FontWeight.SemiBold, FontStyle.Normal),
        Font(Res.font.jetbrains_sans_regular, FontWeight.Normal, FontStyle.Normal),
    )

private val MoriviaTypography: Typography
    @Composable
    get() = Typography(
        displayLarge = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = MoriviaTokens.Type.pageDisplay,
            lineHeight = MoriviaTokens.LineHeight.pageDisplay,
            fontWeight = FontWeight.Bold,
            letterSpacing = MoriviaTokens.LetterSpacing.pageDisplay,
        ),
        headlineLarge = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = MoriviaTokens.Type.headline,
            lineHeight = MoriviaTokens.LineHeight.headline,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = MoriviaTokens.LetterSpacing.headline,
        ),
        titleLarge = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = MoriviaTokens.Type.titleSm,
            lineHeight = MoriviaTokens.LineHeight.materialTitleLarge,
            fontWeight = FontWeight.SemiBold,
        ),
        titleMedium = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = MoriviaTokens.Type.bodyLg,
            lineHeight = MoriviaTokens.LineHeight.bodyMd,
            fontWeight = FontWeight.SemiBold,
        ),
        bodyLarge = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = MoriviaTokens.Type.bodyApp,
            lineHeight = MoriviaTokens.LineHeight.bodyApp,
            fontWeight = FontWeight.Normal,
        ),
        bodyMedium = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = MoriviaTokens.Type.bodyMd,
            lineHeight = MoriviaTokens.LineHeight.bodyMd,
            fontWeight = FontWeight.Normal,
        ),
        labelLarge = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = MoriviaTokens.Type.bodyMd,
            lineHeight = MoriviaTokens.LineHeight.bodySm,
            fontWeight = FontWeight.SemiBold,
        ),
        labelMedium = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = MoriviaTokens.Type.labelSm,
            lineHeight = MoriviaTokens.LineHeight.labelXs,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = MoriviaTokens.LetterSpacing.label,
        ),
    )

private val MoriviaTypeTokens: MoriviaTypeScale
    @Composable
    get() = MoriviaTypeScale(
        labelXs = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = MoriviaTokens.Type.labelXs,
            lineHeight = MoriviaTokens.LineHeight.labelXs,
            fontWeight = FontWeight.SemiBold,
        ),
        labelSm = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = MoriviaTokens.Type.labelSm,
            lineHeight = MoriviaTokens.LineHeight.labelSm,
            fontWeight = FontWeight.SemiBold,
        ),
        bodySm = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = MoriviaTokens.Type.bodySm,
            lineHeight = MoriviaTokens.LineHeight.bodySm,
            fontWeight = FontWeight.Normal,
        ),
        bodyMd = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = MoriviaTokens.Type.bodyMd,
            lineHeight = MoriviaTokens.LineHeight.bodyMd,
            fontWeight = FontWeight.Normal,
        ),
        bodyLg = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = MoriviaTokens.Type.bodyLg,
            lineHeight = MoriviaTokens.LineHeight.bodyLg,
            fontWeight = FontWeight.Normal,
        ),
        titleSm = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = MoriviaTokens.Type.titleSm,
            lineHeight = MoriviaTokens.LineHeight.titleSm,
            fontWeight = FontWeight.SemiBold,
        ),
        titleMd = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = MoriviaTokens.Type.titleMd,
            lineHeight = MoriviaTokens.LineHeight.titleMd,
            fontWeight = FontWeight.SemiBold,
        ),
        titleLg = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = MoriviaTokens.Type.titleLg,
            lineHeight = MoriviaTokens.LineHeight.titleLg,
            fontWeight = FontWeight.SemiBold,
        ),
        displaySm = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = MoriviaTokens.Type.displaySm,
            lineHeight = MoriviaTokens.LineHeight.displaySm,
            fontWeight = FontWeight.Bold,
        ),
        displayMd = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = MoriviaTokens.Type.displayMd,
            lineHeight = MoriviaTokens.LineHeight.displayMd,
            fontWeight = FontWeight.Bold,
        ),
    )

private val MoriviaRippleConfiguration = RippleConfiguration(
    color = Color.Black,
)

@Composable
fun MoriviaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    appTheme: AppTheme = AppTheme.ORIGINAL,
    amoled: Boolean = false,
    customThemeColors: CustomThemeColors = CustomThemeColors.Default,
    content: @Composable () -> Unit,
) {
    val palette = remember(appTheme, customThemeColors) {
        ThemeColors.getColorPalette(appTheme, customThemeColors)
    }
    val colorScheme = buildColorScheme(palette, amoled = amoled)
    val tokens = defaultMoriviaThemeTokens(palette, amoled = amoled, colorScheme = colorScheme)

    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(
            density = density.density,
            fontScale = 1f,
        ),
        LocalMoriviaThemeTokens provides tokens,
        LocalMoriviaTypeScale provides MoriviaTypeTokens,
        LocalRippleConfiguration provides MoriviaRippleConfiguration,
        LocalAppTheme provides appTheme,
        LocalThemePalette provides palette,
        LocalAmoled provides amoled,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MoriviaTypography,
        ) {
            SkeletonAnimationProvider(content = content)
        }
    }
}
