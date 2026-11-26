package com.morivia.app.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.stopScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import morivia.composeapp.generated.resources.Res
import morivia.composeapp.generated.resources.home_view_all
import morivia.composeapp.generated.resources.morivia_catalog_show_all
import morivia.composeapp.generated.resources.morivia_show_all
import morivia.composeapp.generated.resources.morivia_poster_new
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import morivia.composeapp.generated.resources.poster_logo_content_description
import org.jetbrains.compose.resources.stringResource

enum class MoriviaPosterShape {
    Poster,
    Square,
    Landscape,
}

enum class MoriviaViewAllPillSize {
    Default,
    Compact,
    Home,
    /** Like the Home pill: a small dark pill with the arrow in a theme-coloured circle (owner, 2026-11-25). */
    Accent,
}

@Composable
fun <T> MoriviaShelfSection(
    title: String,
    entries: List<T>,
    modifier: Modifier = Modifier,
    headerHorizontalPadding: Dp = 0.dp,
    rowContentPadding: PaddingValues = PaddingValues(0.dp),
    rowModifier: Modifier = Modifier,
    itemSpacing: Dp = 10.dp,
    onViewAllClick: (() -> Unit)? = null,
    viewAllPillSize: MoriviaViewAllPillSize = MoriviaViewAllPillSize.Default,
    key: ((T) -> Any)? = null,
    animatePlacement: Boolean = false,
    state: LazyListState = rememberLazyListState(),
    titleMaxLines: Int = 1,
    itemContent: @Composable (T) -> Unit,
) {
    val tokens = MaterialTheme.nuvio
    // Every row always starts at its first title (owner, 2026-11-25): when it comes into view, when
    // its first title changes, and when the screen is left, so it is at the start on return.
    ScreenActivityEffect(state) { active ->
        if (!active) {
            state.stopScroll()
            state.scrollToItem(0)
        }
    }
    val firstEntryKey = entries.firstOrNull()?.let { first -> key?.invoke(first) ?: first }
    androidx.compose.runtime.LaunchedEffect(state, firstEntryKey) {
        if (state.firstVisibleItemIndex != 0 || state.firstVisibleItemScrollOffset != 0) state.scrollToItem(0)
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(tokens.spacing.controlGap + MoriviaTokens.Space.s2),
    ) {
        if (title.isNotBlank()) {
            MoriviaShelfSectionHeader(
                title = title,
                titleMaxLines = titleMaxLines,
                modifier = Modifier.padding(horizontal = headerHorizontalPadding),
                onViewAllClick = onViewAllClick,
                viewAllPillSize = viewAllPillSize,
            )
        }
        LazyRow(
            modifier = rowModifier.moriviaHorizontalScrollBleed(LocalVerticalBarPadding.current),
            state = state,
            contentPadding = rowContentPadding.withVerticalBar(),
            horizontalArrangement = Arrangement.spacedBy(itemSpacing),
        ) {
            if (key != null) {
                items(
                    items = entries.withDuplicateSafeLazyKeys(key),
                    key = { entry -> entry.lazyKey },
                ) { keyedEntry ->
                    if (animatePlacement) {
                        Box(modifier = Modifier.animateItem()) { itemContent(keyedEntry.value) }
                    } else {
                        itemContent(keyedEntry.value)
                    }
                }
            } else {
                items(entries) { entry ->
                    if (animatePlacement) {
                        Box(modifier = Modifier.animateItem()) { itemContent(entry) }
                    } else {
                        itemContent(entry)
                    }
                }
            }
        }
    }
}

@Composable
fun MoriviaPosterCard(
    title: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    fallbackImageUrl: String? = null,
    shape: MoriviaPosterShape = MoriviaPosterShape.Poster,
    detailLine: String? = null,
    showTitleBelow: Boolean = true,
    bottomLeftLogoUrl: String? = null,
    bottomLeftText: String? = null,
    isWatched: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    /** Shows the earlier Morivia app's "New" banner in the top left corner. */
    showNewBanner: Boolean = false,
    /** A fixed card width (Home's larger posters); null uses Poster Card Style. */
    widthOverride: Dp? = null,
) {
    val posterCardStyle = rememberPosterCardStyleUiState()
    val tokens = MaterialTheme.nuvio
    val cardWidth = widthOverride ?: shape.cardWidth(basePosterWidthDp = posterCardStyle.widthDp)
    val cardShape = RoundedCornerShape(posterCardStyle.cornerRadiusDp.dp)
    val catalogLogoOverlaySize = catalogLogoOverlaySize(
        basePosterWidthDp = posterCardStyle.widthDp,
        shape = shape,
    )
    val shouldShowTitleBelow = showTitleBelow && !posterCardStyle.hideLabelsEnabled

    Column(
        modifier = modifier.width(cardWidth),
        verticalArrangement = Arrangement.spacedBy(MoriviaTokens.Space.s6),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(shape.aspectRatio)
                .clip(cardShape)
                .background(tokens.colors.surface)
                .moriviaCardDepth(
                    shape = cardShape,
                    surface = MoriviaCardDepthSurface.Posters,
                )
                .posterCardClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                    zoomImageUrl = imageUrl,
                    zoomCornerRadius = posterCardStyle.cornerRadiusDp.dp,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (imageUrl != null) {
                val platformContext = coil3.compose.LocalPlatformContext.current
                val hasFallback = !fallbackImageUrl.isNullOrBlank() && fallbackImageUrl != imageUrl
                val imageModel = remember(imageUrl, fallbackImageUrl, platformContext) {
                    if (hasFallback) {
                        coil3.request.ImageRequest.Builder(platformContext)
                            .data(imageUrl)
                            .memoryCacheKeyExtras(
                                mapOf(com.morivia.app.core.poster.CustomPosterFallbackInterceptor.FALLBACK_URL_KEY to fallbackImageUrl!!)
                            )
                            .build()
                    } else {
                        imageUrl
                    }
                }
                AsyncImage(
                    model = imageModel,
                    contentDescription = title,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Text(
                    text = title,
                    modifier = Modifier.padding(horizontal = MoriviaTokens.Space.s14),
                    style = MaterialTheme.typography.titleMedium,
                    color = tokens.colors.textMuted,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (!bottomLeftLogoUrl.isNullOrBlank() || !bottomLeftText.isNullOrBlank()) {
                // Gradient scrim for readability — matching NuvioTV Modern Home style
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .drawWithCache {
                            val gradient = Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color.Transparent,
                                    0.58f to Color.Transparent,
                                    1.0f to Color.Black.copy(alpha = 0.75f)
                                )
                            )
                            onDrawBehind { drawRect(gradient) }
                        }
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = MoriviaTokens.Space.s10, vertical = MoriviaTokens.Space.s10),
                ) {
                    if (!bottomLeftLogoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = bottomLeftLogoUrl,
                            contentDescription = stringResource(Res.string.poster_logo_content_description, title),
                            modifier = Modifier
                                .width(catalogLogoOverlaySize.width)
                                .height(catalogLogoOverlaySize.height),
                            contentScale = ContentScale.Fit,
                            alignment = Alignment.CenterStart,
                        )
                    } else {
                        Text(
                            text = bottomLeftText.orEmpty(),
                            style = MaterialTheme.typography.labelMedium,
                            color = tokens.colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = catalogLogoOverlaySize.textMaxWidth),
                        )
                    }
                }
            }

            if (showNewBanner) {
                // "New" in the Home pill's design, without an icon (owner, 2026-11-25).
                com.morivia.app.features.settings.ThemedAccent {
                    MoriviaPagePill(
                        label = stringResource(Res.string.morivia_poster_new),
                        compact = true,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp),
                    )
                }
            }
            MoriviaPosterWatchedOverlay(isWatched = isWatched)
        }
        if (shouldShowTitleBelow) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = tokens.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!detailLine.isNullOrBlank()) {
                Text(
                    text = detailLine,
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.colors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            // No empty line when there is no year, so every row ends the same distance above the
            // next heading (owner, 2026-11-25).
        } else {
            Box(modifier = Modifier.height(MoriviaTokens.Space.none))
        }
    }
}

@Composable
private fun MoriviaShelfSectionHeader(
    title: String,
    titleMaxLines: Int = 1,
    modifier: Modifier = Modifier,
    onViewAllClick: (() -> Unit)? = null,
    viewAllPillSize: MoriviaViewAllPillSize = MoriviaViewAllPillSize.Default,
) {
    val tokens = MaterialTheme.nuvio
    // A cut title ends in "…"; tapping it shows the whole name, tapping again shortens it (owner,
    // 2026-11-24).
    var showFullTitle by androidx.compose.runtime.saveable.rememberSaveable(title) { androidx.compose.runtime.mutableStateOf(false) }
    var titleCut by androidx.compose.runtime.remember(title) { androidx.compose.runtime.mutableStateOf(false) }
    // A name on several lines has Show All centred on the whole name (owner, 2026-11-25); a one-line
    // name keeps the pill on its baseline.
    var titleLines by androidx.compose.runtime.remember(title) { androidx.compose.runtime.mutableStateOf(1) }
    val baselinePill = (viewAllPillSize == MoriviaViewAllPillSize.Home || viewAllPillSize == MoriviaViewAllPillSize.Accent) && titleLines <= 1
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                // A centred heading on several lines sits 13 px (3.25 dp) lower so the gap above it stays
                // 97 px, as with one-line headings; measured on the phone (owner, 2026-11-25).
                .padding(top = if (!baselinePill && (viewAllPillSize == MoriviaViewAllPillSize.Home || viewAllPillSize == MoriviaViewAllPillSize.Accent)) 3.25.dp else 0.dp),
            horizontalArrangement = Arrangement.spacedBy(tokens.spacing.controlGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f).then(
                    if (baselinePill) Modifier.alignByBaseline() else Modifier,
                ).then(
                    if (titleCut || showFullTitle) {
                        Modifier.clickable { showFullTitle = !showFullTitle }
                    } else {
                        Modifier
                    },
                ),
                style = MaterialTheme.typography.titleLarge,
                color = tokens.colors.textPrimary,
                maxLines = if (showFullTitle) Int.MAX_VALUE else titleMaxLines,
                overflow = if (showFullTitle || titleMaxLines == Int.MAX_VALUE) TextOverflow.Clip else TextOverflow.Ellipsis,
                onTextLayout = { result ->
                    if (!showFullTitle) titleCut = result.hasVisualOverflow
                    titleLines = result.lineCount
                },
            )
            val viewAllPlaceholderModifier = if (onViewAllClick == null) {
                Modifier
                    .alpha(0f)
                    .clearAndSetSemantics { }
            } else {
                Modifier
            }
            MoriviaViewAllPill(
                onClick = onViewAllClick,
                size = viewAllPillSize,
                modifier = viewAllPlaceholderModifier.then(
                    if (baselinePill) Modifier.alignByBaseline() else Modifier,
                ),
            )
        }
    }
}

@Composable
private fun MoriviaViewAllPill(
    onClick: (() -> Unit)?,
    size: MoriviaViewAllPillSize,
    modifier: Modifier = Modifier,
) {
    // The earlier Morivia app's "Show all" button: an outlined pill with the text and a small
    // circle in the theme colour holding the arrow.
    val tokens = MaterialTheme.nuvio
    val compact = size == MoriviaViewAllPillSize.Compact
    val showAllText = stringResource(Res.string.morivia_show_all)
    if (size == MoriviaViewAllPillSize.Accent) {
        // Without Show All the pill is still laid out, invisible, so the heading keeps its height and
        // the gaps around the row stay the same (owner, 2026-11-25).
        com.morivia.app.features.settings.ThemedAccent {
            MoriviaPagePill(
                label = stringResource(Res.string.morivia_catalog_show_all),
                icon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                iconAtEnd = true,
                modifier = modifier
                    // The arrow circle, the part one sees, ends as far from the right edge as the heading
                    // starts from the left (70 px); the faint outline goes a little past it (owner, 2026-11-25).
                    .offset(x = 3.5.dp),
                compact = true,
                onClick = onClick,
            )
        }
        return
    }
    if (size == MoriviaViewAllPillSize.Home) {
        if (onClick != null) {
            MoriviaPagePill(
                label = stringResource(Res.string.morivia_catalog_show_all),
                modifier = modifier,
                compact = true,
                onClick = onClick,
            )
        }
        return
    }
    val pillShape = RoundedCornerShape(50)

    Row(
        modifier = modifier
            .height(if (compact) 34.dp else 38.dp)
            .clip(pillShape)
            .background(Color.Black.copy(alpha = 0.35f), pillShape)
            .border(1.dp, Color.White.copy(alpha = 0.16f), pillShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(start = 14.dp, end = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = showAllText,
            style = MaterialTheme.typography.labelLarge,
            color = tokens.colors.textPrimary,
            maxLines = 1,
        )
        Box(
            modifier = Modifier
                .size(if (compact) 24.dp else 28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(if (compact) 18.dp else 20.dp),
            )
        }
    }
}

private val MoriviaPosterShape.aspectRatio: Float
    get() = when (this) {
        MoriviaPosterShape.Poster -> 0.675f
        MoriviaPosterShape.Square -> 1f
        MoriviaPosterShape.Landscape -> PosterLandscapeAspectRatio
    }

private data class CatalogLogoOverlaySize(
    val width: Dp,
    val height: Dp,
    val textMaxWidth: Dp,
)

private fun catalogLogoOverlaySize(
    basePosterWidthDp: Int,
    shape: MoriviaPosterShape,
): CatalogLogoOverlaySize =
    if (shape == MoriviaPosterShape.Landscape) {
        when {
            basePosterWidthDp <= 108 -> CatalogLogoOverlaySize(width = 92.dp, height = 24.dp, textMaxWidth = 120.dp)
            basePosterWidthDp <= 120 -> CatalogLogoOverlaySize(width = 104.dp, height = 28.dp, textMaxWidth = 132.dp)
            basePosterWidthDp <= 132 -> CatalogLogoOverlaySize(width = 116.dp, height = 30.dp, textMaxWidth = 144.dp)
            else -> CatalogLogoOverlaySize(width = 128.dp, height = 34.dp, textMaxWidth = 156.dp)
        }
    } else {
        when {
            basePosterWidthDp <= 108 -> CatalogLogoOverlaySize(width = 72.dp, height = 18.dp, textMaxWidth = 92.dp)
            basePosterWidthDp <= 120 -> CatalogLogoOverlaySize(width = 80.dp, height = 20.dp, textMaxWidth = 104.dp)
            basePosterWidthDp <= 132 -> CatalogLogoOverlaySize(width = 88.dp, height = 22.dp, textMaxWidth = 112.dp)
            else -> CatalogLogoOverlaySize(width = 96.dp, height = 24.dp, textMaxWidth = 124.dp)
        }
    }

private fun MoriviaPosterShape.cardWidth(basePosterWidthDp: Int): Dp =
    when (this) {
        MoriviaPosterShape.Poster -> basePosterWidthDp.dp
        MoriviaPosterShape.Square -> basePosterWidthDp.dp
        MoriviaPosterShape.Landscape -> landscapePosterWidth(basePosterWidthDp)
    }
