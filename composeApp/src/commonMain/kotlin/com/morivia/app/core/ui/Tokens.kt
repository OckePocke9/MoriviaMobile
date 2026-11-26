package com.morivia.app.core.ui

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object MoriviaTokens {
    object Space {
        val none = 0.dp
        val hairline = 0.5.dp
        val s1 = 1.dp
        val s2 = 2.dp
        val s3 = 3.dp
        val s4 = 4.dp
        val s5 = 5.dp
        val s6 = 6.dp
        val s7 = 7.dp
        val s8 = 8.dp
        val s10 = 10.dp
        val s12 = 12.dp
        val s14 = 14.dp
        val s16 = 16.dp
        val s18 = 18.dp
        val s20 = 20.dp
        val s22 = 22.dp
        val s24 = 24.dp
        val s28 = 28.dp
        val s32 = 32.dp
        val s36 = 36.dp
        val s40 = 40.dp
        val s48 = 48.dp
        val s56 = 56.dp
        val s64 = 64.dp
        val s72 = 72.dp
        val s80 = 80.dp
        val s96 = 96.dp
    }

    object Radius {
        val none = Space.none
        val xs = Space.s4
        val sm = Space.s6
        val md = Space.s8
        val lg = Space.s12
        val xl = Space.s16
        val xxl = Space.s24
        val full = 999.dp

        val card = xxl
        val compactCard = lg
        val sheet = xxl
        val dialog = Space.s28
        val button = xl
        val chip = full
        val poster = lg
        val avatar = full
        val playerPanel = xxl
    }

    object Border {
        val hairline = Space.hairline
        val thin = Space.s1
        val medium = Space.s2
    }

    object Elevation {
        val none = Space.none
        val raised = Space.s2
        val modal = Space.s8
        val overlay = Space.s12
        val playerControls = Space.s4
    }

    object Opacity {
        const val invisible = 0f
        const val disabled = 0.38f
        const val secondary = 0.70f
        const val muted = 0.60f
        const val selected = 0.15f
        const val hover = 0.08f
        const val pressed = 0.12f
        const val subtle = 0.06f
        const val medium = 0.52f
        const val strong = 0.75f
        const val overlayLight = 0.35f
        const val overlayMedium = 0.56f
        const val overlayHeavy = 0.82f
        const val visible = 1f
    }

    object Motion {
        const val instantMillis = 0
        const val fastMillis = 150
        const val normalMillis = 220
        const val sheetEnterMillis = 300
        const val sheetExitMillis = 250
        const val slowMillis = 400
        const val cinematicMillis = 700

        val standard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
        val emphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
        val decelerate: Easing = CubicBezierEasing(0f, 0f, 0f, 1f)
        val accelerate: Easing = CubicBezierEasing(0.3f, 0f, 1f, 1f)
    }

    object Icon {
        val xs = Space.s12
        val sm = Space.s16
        val md = Space.s20
        val lg = Space.s24
        val xl = Space.s32
        val xxl = Space.s40
    }

    object Type {
        val labelXs = 11.sp
        val labelSm = 12.sp
        val bodySm = 13.sp
        val bodyMd = 14.sp
        val bodyApp = 15.sp
        val bodyLg = 16.sp
        val titleSm = 18.sp
        val headline = 26.sp
        val titleMd = 22.sp
        val titleLg = 28.sp
        val displaySm = 32.sp
        val pageDisplay = 38.sp
        val displayMd = 48.sp
    }

    object LineHeight {
        val labelXs = 14.sp
        val labelSm = 15.sp
        val bodySm = 18.sp
        val bodyMd = 20.sp
        val bodyApp = 22.sp
        val bodyLg = 22.sp
        val titleSm = 22.sp
        val materialTitleLarge = 24.sp
        val headline = 30.sp
        val titleMd = 26.sp
        val titleLg = 32.sp
        val displaySm = 36.sp
        val pageDisplay = 42.sp
        val displayMd = 52.sp
    }

    object LetterSpacing {
        val none = 0.sp
        val pageDisplay = (-1.2).sp
        val headline = (-0.8).sp
        val label = 0.8.sp
    }

    object Breakpoint {
        val phone = 0.dp
        val largePhone = 420.dp
        val tablet = 600.dp
        val largeTablet = 840.dp
        val desktop = 1024.dp
        val playerWide = 1280.dp
    }

    object Z {
        const val base = 0f
        const val stickyHeader = 2f
        const val navigation = 4f
        const val sheet = 8f
        const val dialog = 10f
        const val playerOverlay = 12f
        const val toast = 16f
    }
}

@Immutable
data class MoriviaColorTokens(
    val background: Color,
    val backgroundAmoled: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceCard: Color,
    val surfaceSheet: Color,
    val surfaceDialog: Color,
    val surfacePopover: Color,
    val nativeChrome: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textDisabled: Color,
    val textInverse: Color,
    val accent: Color,
    val accentStrong: Color,
    val onAccent: Color,
    val focusRing: Color,
    val focusBackground: Color,
    val borderSubtle: Color,
    val borderDefault: Color,
    val borderStrong: Color,
    val borderFocus: Color,
    val borderSelected: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val info: Color,
    val neutral: Color,
    val overlayScrim: Color,
    val overlayHover: Color,
    val overlayPressed: Color,
    val overlaySelected: Color,
    val overlayDisabled: Color,
    val shimmer: Color,
    val skeleton: Color,
    val playerControlsBackground: Color,
    val playerControlsForeground: Color,
    val playerTimelineTrack: Color,
    val playerTimelineFill: Color,
    val playerSubtitlePreview: Color,
    val playerBuffering: Color,
    val parentalGuide: Color,
)

@Immutable
data class MoriviaShapeTokens(
    val card: Shape,
    val compactCard: Shape,
    val sheet: Shape,
    val dialog: Shape,
    val button: Shape,
    val chip: Shape,
    val poster: Shape,
    val avatar: Shape,
    val playerPanel: Shape,
)

@Immutable
data class MoriviaSpacingTokens(
    val screenHorizontal: Dp,
    val screenTop: Dp,
    val screenBottom: Dp,
    val sectionGap: Dp,
    val listGap: Dp,
    val railGap: Dp,
    val cardPadding: Dp,
    val cardPaddingCompact: Dp,
    val controlGap: Dp,
    val dialogPadding: Dp,
    val sheetPadding: Dp,
    val playerOverlayPadding: Dp,
)

@Immutable
data class MoriviaBorderTokens(
    val hairline: Dp,
    val thin: Dp,
    val medium: Dp,
)

@Immutable
data class MoriviaElevationTokens(
    val flat: Dp,
    val raised: Dp,
    val modal: Dp,
    val overlay: Dp,
    val playerControls: Dp,
)

@Immutable
data class MoriviaMotionTokens(
    val instantMillis: Int,
    val fastMillis: Int,
    val normalMillis: Int,
    val sheetEnterMillis: Int,
    val sheetExitMillis: Int,
    val slowMillis: Int,
    val cinematicMillis: Int,
    val standard: Easing,
    val emphasized: Easing,
    val decelerate: Easing,
    val accelerate: Easing,
)

@Immutable
data class MoriviaOpacityTokens(
    val invisible: Float,
    val disabled: Float,
    val secondary: Float,
    val muted: Float,
    val selected: Float,
    val hover: Float,
    val pressed: Float,
    val subtle: Float,
    val medium: Float,
    val strong: Float,
    val overlayLight: Float,
    val overlayMedium: Float,
    val overlayHeavy: Float,
    val visible: Float,
)

@Immutable
data class MoriviaIconTokens(
    val xs: Dp,
    val sm: Dp,
    val md: Dp,
    val lg: Dp,
    val xl: Dp,
    val xxl: Dp,
)

@Immutable
data class MoriviaBreakpointTokens(
    val phone: Dp,
    val largePhone: Dp,
    val tablet: Dp,
    val largeTablet: Dp,
    val desktop: Dp,
    val playerWide: Dp,
)

@Immutable
data class MoriviaComponentTokens(
    val navItemShape: Shape,
    val navIconSize: Dp,
    val navItemMaxWidth: Dp,
    val sheetMaxWidth: Dp,
    val dialogMaxWidth: Dp,
    val chipHorizontalPadding: Dp,
    val chipVerticalPadding: Dp,
    val posterRadius: Dp,
    val avatarSize: Dp,
    val playerPanelMaxWidth: Dp,
)

@Immutable
data class MoriviaThemeTokens(
    val colors: MoriviaColorTokens,
    val spacing: MoriviaSpacingTokens,
    val shapes: MoriviaShapeTokens,
    val borders: MoriviaBorderTokens,
    val elevation: MoriviaElevationTokens,
    val motion: MoriviaMotionTokens,
    val opacity: MoriviaOpacityTokens,
    val icons: MoriviaIconTokens,
    val breakpoints: MoriviaBreakpointTokens,
    val components: MoriviaComponentTokens,
)

internal val LocalMoriviaThemeTokens = staticCompositionLocalOf {
    defaultMoriviaThemeTokens(ThemeColors.White, amoled = false, colorScheme = null)
}

val MaterialTheme.nuvio: MoriviaThemeTokens
    @Composable
    @Stable
    get() = LocalMoriviaThemeTokens.current

internal fun defaultMoriviaThemeTokens(
    palette: ThemeColorPalette,
    amoled: Boolean,
    colorScheme: ColorScheme?,
): MoriviaThemeTokens {
    val background = if (amoled) palette.amoledBackground() else palette.background
    val textPrimary = Color(0xFFF5F7F8)
    val textSecondary = Color(0xFFB8BEC5)
    val textMuted = Color(0xFF969CA3)
    val surface = if (amoled) Color.Black else palette.backgroundElevated
    val surfaceCard = if (amoled) Color.Black else palette.backgroundCard
    val accent = palette.secondary
    val accentStrong = palette.secondaryVariant
    val borderSubtle = Color(0xFF252A2A).copy(alpha = 0.55f)
    val borderDefault = Color(0xFF252A2A)
    val overlayScrim = Color.Black.copy(alpha = MoriviaTokens.Opacity.overlayMedium)

    return MoriviaThemeTokens(
        colors = MoriviaColorTokens(
            background = background,
            backgroundAmoled = Color.Black,
            surface = surface,
            surfaceElevated = surface,
            surfaceCard = surfaceCard,
            surfaceSheet = surface,
            surfaceDialog = surface,
            surfacePopover = surfaceCard,
            nativeChrome = background,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            textMuted = textMuted,
            textDisabled = textMuted.copy(alpha = MoriviaTokens.Opacity.disabled),
            textInverse = Color(0xFF111111),
            accent = accent,
            accentStrong = accentStrong,
            onAccent = palette.onSecondary,
            focusRing = palette.focusRing,
            focusBackground = palette.focusBackground,
            borderSubtle = borderSubtle,
            borderDefault = borderDefault,
            borderStrong = Color(0xFF3A4040),
            borderFocus = palette.focusRing,
            borderSelected = accent.copy(alpha = MoriviaTokens.Opacity.strong),
            success = Color(0xFF66BB6A),
            warning = Color(0xFFFFC857),
            danger = colorScheme?.error ?: Color(0xFFE36A8A),
            info = Color(0xFF42A5F5),
            neutral = textMuted,
            overlayScrim = overlayScrim,
            overlayHover = Color.White.copy(alpha = MoriviaTokens.Opacity.hover),
            overlayPressed = Color.White.copy(alpha = MoriviaTokens.Opacity.pressed),
            overlaySelected = Color.White.copy(alpha = MoriviaTokens.Opacity.selected),
            overlayDisabled = Color.Black.copy(alpha = MoriviaTokens.Opacity.disabled),
            shimmer = Color.White.copy(alpha = 0.10f),
            skeleton = Color.White.copy(alpha = 0.06f),
            playerControlsBackground = Color.Black.copy(alpha = 0.72f),
            playerControlsForeground = Color.White,
            playerTimelineTrack = Color.White.copy(alpha = 0.30f),
            playerTimelineFill = accent,
            playerSubtitlePreview = Color.Black.copy(alpha = 0.55f),
            playerBuffering = accent,
            parentalGuide = Color(0xFF5D1F1F),
        ),
        spacing = MoriviaSpacingTokens(
            screenHorizontal = MoriviaTokens.Space.s16,
            screenTop = MoriviaTokens.Space.s10,
            screenBottom = MoriviaTokens.Space.s18,
            sectionGap = MoriviaTokens.Space.s24,
            listGap = MoriviaTokens.Space.s12,
            railGap = MoriviaTokens.Space.s14,
            cardPadding = MoriviaTokens.Space.s18,
            cardPaddingCompact = MoriviaTokens.Space.s14,
            controlGap = MoriviaTokens.Space.s8,
            dialogPadding = MoriviaTokens.Space.s20,
            sheetPadding = MoriviaTokens.Space.s20,
            playerOverlayPadding = MoriviaTokens.Space.s24,
        ),
        shapes = MoriviaShapeTokens(
            card = RoundedCornerShape(MoriviaTokens.Radius.card),
            compactCard = RoundedCornerShape(MoriviaTokens.Radius.compactCard),
            sheet = RoundedCornerShape(MoriviaTokens.Radius.sheet),
            dialog = RoundedCornerShape(MoriviaTokens.Radius.dialog),
            button = RoundedCornerShape(MoriviaTokens.Radius.button),
            chip = RoundedCornerShape(MoriviaTokens.Radius.chip),
            poster = RoundedCornerShape(MoriviaTokens.Radius.poster),
            avatar = RoundedCornerShape(MoriviaTokens.Radius.avatar),
            playerPanel = RoundedCornerShape(MoriviaTokens.Radius.playerPanel),
        ),
        borders = MoriviaBorderTokens(
            hairline = MoriviaTokens.Border.hairline,
            thin = MoriviaTokens.Border.thin,
            medium = MoriviaTokens.Border.medium,
        ),
        elevation = MoriviaElevationTokens(
            flat = MoriviaTokens.Elevation.none,
            raised = MoriviaTokens.Elevation.raised,
            modal = MoriviaTokens.Elevation.modal,
            overlay = MoriviaTokens.Elevation.overlay,
            playerControls = MoriviaTokens.Elevation.playerControls,
        ),
        motion = MoriviaMotionTokens(
            instantMillis = MoriviaTokens.Motion.instantMillis,
            fastMillis = MoriviaTokens.Motion.fastMillis,
            normalMillis = MoriviaTokens.Motion.normalMillis,
            sheetEnterMillis = MoriviaTokens.Motion.sheetEnterMillis,
            sheetExitMillis = MoriviaTokens.Motion.sheetExitMillis,
            slowMillis = MoriviaTokens.Motion.slowMillis,
            cinematicMillis = MoriviaTokens.Motion.cinematicMillis,
            standard = MoriviaTokens.Motion.standard,
            emphasized = MoriviaTokens.Motion.emphasized,
            decelerate = MoriviaTokens.Motion.decelerate,
            accelerate = MoriviaTokens.Motion.accelerate,
        ),
        opacity = MoriviaOpacityTokens(
            invisible = MoriviaTokens.Opacity.invisible,
            disabled = MoriviaTokens.Opacity.disabled,
            secondary = MoriviaTokens.Opacity.secondary,
            muted = MoriviaTokens.Opacity.muted,
            selected = MoriviaTokens.Opacity.selected,
            hover = MoriviaTokens.Opacity.hover,
            pressed = MoriviaTokens.Opacity.pressed,
            subtle = MoriviaTokens.Opacity.subtle,
            medium = MoriviaTokens.Opacity.medium,
            strong = MoriviaTokens.Opacity.strong,
            overlayLight = MoriviaTokens.Opacity.overlayLight,
            overlayMedium = MoriviaTokens.Opacity.overlayMedium,
            overlayHeavy = MoriviaTokens.Opacity.overlayHeavy,
            visible = MoriviaTokens.Opacity.visible,
        ),
        icons = MoriviaIconTokens(
            xs = MoriviaTokens.Icon.xs,
            sm = MoriviaTokens.Icon.sm,
            md = MoriviaTokens.Icon.md,
            lg = MoriviaTokens.Icon.lg,
            xl = MoriviaTokens.Icon.xl,
            xxl = MoriviaTokens.Icon.xxl,
        ),
        breakpoints = MoriviaBreakpointTokens(
            phone = MoriviaTokens.Breakpoint.phone,
            largePhone = MoriviaTokens.Breakpoint.largePhone,
            tablet = MoriviaTokens.Breakpoint.tablet,
            largeTablet = MoriviaTokens.Breakpoint.largeTablet,
            desktop = MoriviaTokens.Breakpoint.desktop,
            playerWide = MoriviaTokens.Breakpoint.playerWide,
        ),
        components = MoriviaComponentTokens(
            navItemShape = RoundedCornerShape(MoriviaTokens.Radius.xl),
            navIconSize = MoriviaTokens.Icon.xl,
            navItemMaxWidth = 150.dp,
            sheetMaxWidth = 520.dp,
            dialogMaxWidth = 460.dp,
            chipHorizontalPadding = MoriviaTokens.Space.s14,
            chipVerticalPadding = MoriviaTokens.Space.s8,
            posterRadius = MoriviaTokens.Radius.poster,
            avatarSize = MoriviaTokens.Space.s48,
            playerPanelMaxWidth = 600.dp,
        ),
    )
}
