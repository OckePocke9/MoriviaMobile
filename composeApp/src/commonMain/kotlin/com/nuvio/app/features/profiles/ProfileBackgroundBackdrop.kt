package com.nuvio.app.features.profiles

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
import com.nuvio.app.features.membership.MemberAccessRepository
import com.nuvio.app.features.membership.ProfileBackgroundRepository
import com.nuvio.app.features.membership.ProfileBackgroundSelection
import com.nuvio.app.features.membership.resolveProfileBackground

@Composable
fun ProfileBackgroundBackdrop(
    profile: NuvioProfile?,
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
    val theme = com.nuvio.app.core.ui.LocalAppTheme.current
    val palette = com.nuvio.app.core.ui.LocalThemePalette.current
    val (leftGlow, rightGlow) = remember(theme, palette) { profileGlowColors(theme, palette) }
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
    val tokens = com.nuvio.app.core.ui.LocalNuvioThemeTokens.current
    val colorScheme = androidx.compose.material3.MaterialTheme.colorScheme
    Box(modifier = modifier.earlierMoriviaBackground()) {
        androidx.compose.runtime.CompositionLocalProvider(
            com.nuvio.app.core.ui.LocalNuvioThemeTokens provides tokens.copy(
                colors = tokens.colors.copy(background = Color.Transparent),
            ),
        ) {
            androidx.compose.material3.MaterialTheme(
                colorScheme = colorScheme.copy(background = Color.Transparent),
                typography = androidx.compose.material3.MaterialTheme.typography,
                shapes = androidx.compose.material3.MaterialTheme.shapes,
                content = content,
            )
        }
    }
}

/**
 * The two glows in the selected theme's colours. The Original theme keeps the earlier app's exact
 * violet and cyan; other themes use their accent colours at the same 32 % strength.
 */
internal fun profileGlowColors(
    theme: com.nuvio.app.core.ui.AppTheme,
    palette: com.nuvio.app.core.ui.ThemeColorPalette,
): Pair<Color, Color> {
    if (theme == com.nuvio.app.core.ui.AppTheme.ORIGINAL) return Color(0xFF2F0451) to Color(0xFF004051)
    val left = palette.secondary
    val right = if (palette.accentGradient.size > 1) palette.accentGradient.last() else palette.secondaryVariant
    return androidx.compose.ui.graphics.lerp(Color.Black, left, 0.32f) to
        androidx.compose.ui.graphics.lerp(Color.Black, right, 0.32f)
}

internal fun Modifier.earlierMoriviaBackground(leftGlow: Color, rightGlow: Color): Modifier = drawBehind {
    val w = size.width
    val h = size.height
    drawRect(Color.Black)
    // Bottom layer: right glow at 95 % / 15 %, ellipse 90 % x 65 %, fading to black at 75 %.
    ellipticalGlow(
        center = Offset(w * 0.95f, h * 0.15f),
        radiusX = w * 0.90f,
        radiusY = h * 0.65f,
        stops = arrayOf(0f to rightGlow, 0.75f to Color.Black, 1f to Color.Black),
    )
    // Top layer: left glow at 5 % / 5 %, ellipse 100 % x 65 %, fading to transparent at 70 %.
    ellipticalGlow(
        center = Offset(w * 0.05f, h * 0.05f),
        radiusX = w * 1.00f,
        radiusY = h * 0.65f,
        stops = arrayOf(0f to leftGlow, 0.70f to Color.Transparent, 1f to Color.Transparent),
    )
}

/** A CSS-style elliptical radial gradient: a circle of [radiusX] squashed to [radiusY] vertically. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.ellipticalGlow(
    center: Offset,
    radiusX: Float,
    radiusY: Float,
    stops: Array<Pair<Float, Color>>,
) {
    val squash = radiusY / radiusX
    withTransform({ scale(scaleX = 1f, scaleY = squash, pivot = center) }) {
        drawRect(
            brush = Brush.radialGradient(*stops, center = center, radius = radiusX),
            topLeft = Offset(-size.width, center.y - size.height * 2f / squash),
            size = Size(size.width * 3f, size.height * 4f / squash),
        )
    }
}
