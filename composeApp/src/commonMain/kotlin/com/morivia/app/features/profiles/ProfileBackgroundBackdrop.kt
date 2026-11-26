package com.morivia.app.features.profiles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.morivia.app.features.membership.MemberAccessRepository
import com.morivia.app.features.membership.ProfileBackgroundRepository
import com.morivia.app.features.membership.ProfileBackgroundSelection
import com.morivia.app.features.membership.resolveProfileBackground

@Composable
fun ProfileBackgroundBackdrop(
    profile: MoriviaProfile?,
    modifier: Modifier = Modifier,
) {
    val memberAccess by remember {
        MemberAccessRepository.ensureStarted()
        MemberAccessRepository.access
    }.collectAsStateWithLifecycle()
    val backgroundCatalog by ProfileBackgroundRepository.catalog.collectAsStateWithLifecycle()
    val backgroundSelection = remember(
        profile?.profileBackgroundId,
        profile?.profileBackgroundUrl,
        memberAccess.entitlements,
    ) {
        profile?.let { resolveProfileBackground(it, memberAccess.entitlements) }
    }

    BoxWithConstraints(modifier = modifier) {
        val isPortrait = maxHeight > maxWidth
        LaunchedEffect(backgroundSelection, isPortrait) {
            val selectedId = (backgroundSelection as? ProfileBackgroundSelection.Catalog)?.id
            if (selectedId != null) {
                ProfileBackgroundRepository.loadSelectedAndPreload(selectedId, isPortrait)
            }
        }
        val backgroundModel: Any? = when (backgroundSelection) {
            is ProfileBackgroundSelection.Catalog -> backgroundCatalog
                .firstOrNull { it.id == backgroundSelection.id }
                ?.let { background ->
                    if (isPortrait) {
                        background.portraitImageBytes ?: background.landscapeImageBytes
                    } else {
                        background.landscapeImageBytes
                    }
                }
            is ProfileBackgroundSelection.Custom -> backgroundSelection.url
            null -> null
        }

        if (backgroundModel == null) {
            Box(modifier = Modifier.fillMaxSize().earlierMoriviaBackground())
        } else {
            AsyncImage(
                model = backgroundModel,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.28f)),
            )
        }
    }
}

/**
 * The background of the earlier Morivia app (1.4.130) with its Original theme, used there for the
 * profile picker, the profile editor and the Profile page:
 * `radial-gradient(ellipse 100% 65% at 5% 5%, #920cfc·32% 0%, transparent 70%),
 *  radial-gradient(ellipse 90% 65% at 95% 15%, #00c8fc·32% 0%, #000 75%)` — a violet glow at the top
 * left and a cyan glow at the top right on black.
 */
@Composable
internal fun Modifier.earlierMoriviaBackground(): Modifier {
    val (leftGlow, rightGlow) = rememberProfileViewGlowColors()
    return earlierMoriviaBackground(leftGlow, rightGlow)
}

/**
 * The earlier Morivia app's theme background (the glows in the theme colours) behind [content],
 * with the screen background made transparent so it shows through. Cards keep their own colour
 * (black with AMOLED Black on).
 */
@Composable
internal fun MoriviaThemedBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val tokens = com.morivia.app.core.ui.LocalMoriviaThemeTokens.current
    val colorScheme = androidx.compose.material3.MaterialTheme.colorScheme
    Box(modifier = modifier.earlierMoriviaBackground()) {
        androidx.compose.runtime.CompositionLocalProvider(
            com.morivia.app.core.ui.LocalMoriviaThemeTokens provides tokens.copy(
                colors = tokens.colors.copy(background = Color.Transparent),
            ),
        ) {
            androidx.compose.material3.MaterialTheme(
                colorScheme = colorScheme.copy(background = Color.Transparent),
                typography = androidx.compose.material3.MaterialTheme.typography,
                shapes = androidx.compose.material3.MaterialTheme.shapes,
            ) {
                ProvideMoriviaHeaderBackdrop(content)
            }
        }
    }
}

/**
 * The two glows in the selected theme's colours. The Original theme keeps the earlier app's exact
 * violet and cyan; other themes use their accent colours at the same 32 % strength.
 */
internal fun profileGlowColors(
    theme: com.morivia.app.core.ui.AppTheme,
    palette: com.morivia.app.core.ui.ThemeColorPalette,
): Pair<Color, Color> {
    if (theme == com.morivia.app.core.ui.AppTheme.ORIGINAL) return Color(0xFF2F0451) to Color(0xFF004051)
    // The theme's colour made a little greyer, so the clear colour of the profile picture stands
    // out against it (owner, 2026-11-23).
    val glow = androidx.compose.ui.graphics.lerp(palette.secondary, Color(0xFF2A2A2A), 0.45f)
    return glow to glow
}

internal fun Modifier.earlierMoriviaBackground(leftGlow: Color, rightGlow: Color): Modifier = drawBehind {
    drawMoriviaGlow(size, leftGlow, rightGlow)
}

/**
 * Lets sticky headers on a see-through screen draw the same background behind themselves, as it
 * is at their place on the screen, so content scrolling under a header is hidden.
 */
@Composable
internal fun ProvideMoriviaHeaderBackdrop(content: @Composable () -> Unit) {
    val (leftGlow, rightGlow) = rememberProfileViewGlowColors()
    val backdrop: androidx.compose.ui.graphics.drawscope.DrawScope.(Size) -> Unit = remember(leftGlow, rightGlow) {
        { screenSize -> drawMoriviaGlow(screenSize, leftGlow, rightGlow) }
    }
    androidx.compose.runtime.CompositionLocalProvider(
        com.morivia.app.core.ui.LocalHeaderBackdrop provides backdrop,
        content = content,
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMoriviaGlow(
    canvas: Size,
    leftGlow: Color,
    rightGlow: Color,
) {
    val w = canvas.width
    val h = canvas.height
    drawRect(Color.Black, size = canvas)
    if (rightGlow.alpha < 1f) {
        // A theme colour (owner, 2026-11-23): a soft, dark glow of the colour from the
        // top left across the top of the screen, fading into black about halfway down.
        drawRect(Color.Black, size = canvas)
        drawRect(
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                0f to rightGlow.copy(alpha = rightGlow.alpha * 0.55f),
                0.55f to Color.Transparent,
                1f to Color.Transparent,
                startY = 0f,
                endY = h,
            ),
            size = canvas,
        )
        ellipticalGlow(
            center = Offset(0f, 0f),
            radiusX = w * 1.25f,
            radiusY = h * 0.55f,
            stops = arrayOf(0f to rightGlow, 0.6f to rightGlow.copy(alpha = rightGlow.alpha * 0.3f), 1f to Color.Transparent),
            canvas = canvas,
        )
        return
    }
    // Bottom layer: right glow at 95 % / 15 %, ellipse 90 % x 65 %, fading to black at 75 %.
    ellipticalGlow(
        center = Offset(w * 0.95f, h * 0.15f),
        radiusX = w * 0.90f,
        radiusY = h * 0.65f,
        stops = arrayOf(0f to rightGlow, 0.75f to Color.Black, 1f to Color.Black),
        canvas = canvas,
    )
    // Top layer: left glow at 5 % / 5 %, ellipse 100 % x 65 %, fading to transparent at 70 %.
    ellipticalGlow(
        center = Offset(w * 0.05f, h * 0.05f),
        radiusX = w * 1.00f,
        radiusY = h * 0.65f,
        stops = arrayOf(0f to leftGlow, 0.70f to Color.Transparent, 1f to Color.Transparent),
        canvas = canvas,
    )
}

/** A CSS-style elliptical radial gradient: a circle of [radiusX] squashed to [radiusY] vertically. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.ellipticalGlow(
    center: Offset,
    radiusX: Float,
    radiusY: Float,
    stops: Array<Pair<Float, Color>>,
    canvas: Size,
) {
    val squash = radiusY / radiusX
    withTransform({ scale(scaleX = 1f, scaleY = squash, pivot = center) }) {
        drawRect(
            brush = Brush.radialGradient(*stops, center = center, radius = radiusX),
            topLeft = Offset(-canvas.width, center.y - canvas.height * 2f / squash),
            size = Size(canvas.width * 3f, canvas.height * 4f / squash),
        )
    }
}

/**
 * The glows of the profile view background chosen under Edit Profile (owner, 2026-11-23):
 * Original, Black (no glow) or one of the theme colours.
 */
@Composable
internal fun rememberProfileViewGlowColors(): Pair<Color, Color> {
    // The Appearance theme decides the profile background (owner, 2026-11-23).
    val appearanceTheme by remember {
        com.morivia.app.features.settings.ThemeSettingsRepository.ensureLoaded()
        com.morivia.app.features.settings.ThemeSettingsRepository.selectedTheme
    }.collectAsStateWithLifecycle()
    val previewOriginal by com.morivia.app.features.settings.ThemeSettingsRepository.pickerPreviewOriginal.collectAsStateWithLifecycle()
    val effective = if (previewOriginal) com.morivia.app.core.ui.AppTheme.ORIGINAL.name else appearanceTheme.name
    // Settings' dark base (pure black with AMOLED Black) with a faint glow of the colour behind the
    // logo, fading out downwards (owner, 2026-11-23).
    val base = com.morivia.app.features.settings.settingsBackgroundColor()
    val target = remember(effective, base) {
        if (effective == com.morivia.app.features.settings.PROFILE_BACKGROUND_BLACK) {
            Color.Black to Color.Black
        } else {
            val theme = com.morivia.app.core.ui.AppTheme.entries.firstOrNull { it.name == effective }
                ?: com.morivia.app.core.ui.AppTheme.ORIGINAL
            if (theme == com.morivia.app.core.ui.AppTheme.ORIGINAL) {
                profileGlowColors(theme, com.morivia.app.core.ui.ThemeColors.getColorPalette(theme))
            } else {
                base to com.morivia.app.core.ui.ThemeColors.getColorPalette(theme).secondary.copy(alpha = 0.42f)
            }
        }
    }
    // A new profile's theme fades in instead of jumping (owner, 2026-11-23).
    val left by androidx.compose.animation.animateColorAsState(target.first, androidx.compose.animation.core.tween(600), label = "profileGlowLeft")
    val right by androidx.compose.animation.animateColorAsState(target.second, androidx.compose.animation.core.tween(600), label = "profileGlowRight")
    return left to right
}
