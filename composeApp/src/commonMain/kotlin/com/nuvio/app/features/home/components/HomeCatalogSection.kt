package com.nuvio.app.features.home.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.layout.layout
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nuvio.app.core.ui.NuvioShelfSection
import com.nuvio.app.core.ui.NuvioViewAllPillSize
import com.nuvio.app.core.ui.landscapePosterWidth
import com.nuvio.app.core.ui.rememberPosterCardStyleUiState
import com.nuvio.app.features.home.HomeCatalogSection
import com.nuvio.app.features.home.MetaPreview
import com.nuvio.app.features.home.stableKey
import com.nuvio.app.features.watching.application.WatchingState
import kotlin.math.roundToInt

/** The gap between posters in a Home row. */
internal val HomePosterSpacing = 12.dp

/**
 * Morivia's larger Home posters: about two and a half across the screen, so the next poster
 * peeks in and shows the row can be swiped.
 */
internal fun homePosterWidth(screenWidth: Dp, sectionPadding: Dp): Dp =
    ((screenWidth - sectionPadding - HomePosterSpacing * 2) / 2.5f).coerceIn(110.dp, 220.dp)

/** The large landscape cards of the main Featured row: one card with the next one peeking in. */
internal fun homeFeaturedCardWidth(screenWidth: Dp, sectionPadding: Dp): Dp =
    ((screenWidth - sectionPadding * 2) * 0.86f).coerceAtMost(520.dp)

/** Fit full cards plus a visible portion of the next one, near the selected card size. */
internal fun homeCatalogCardWidth(
    screenWidth: Dp,
    startPadding: Dp,
    basePosterWidthDp: Int,
    landscape: Boolean,
): Dp {
    val availableWidth = screenWidth - startPadding
    val preferredWidth = if (landscape) landscapePosterWidth(basePosterWidthDp) else basePosterWidthDp.dp
    val cardsAcross = (availableWidth + HomePosterSpacing) / (preferredWidth + HomePosterSpacing)
    val fullCards = (cardsAcross - 0.35f).roundToInt().coerceAtLeast(1)
    val cardSpace = (availableWidth - HomePosterSpacing * fullCards).coerceAtLeast(1.dp)
    return preferredWidth.coerceIn(
        minimumValue = cardSpace / (fullCards + 0.45f),
        maximumValue = cardSpace / (fullCards + 0.25f),
    )
}

@Composable
fun HomeCatalogRowSection(
    section: HomeCatalogSection,
    modifier: Modifier = Modifier,
    entries: List<MetaPreview> = section.items,
    watchedKeys: Set<String> = emptySet(),
    fullyWatchedSeriesKeys: Set<String> = emptySet(),
    sectionPadding: Dp? = null,
    onViewAllClick: (() -> Unit)? = null,
    onPosterClick: ((MetaPreview) -> Unit)? = null,
    onPosterLongClick: ((MetaPreview) -> Unit)? = null,
    posterWidth: Dp? = null,
    landscape: Boolean = false,
    landscapeOverride: Boolean? = null,
    useHomePill: Boolean = false,
    // The Home-pill style Show All with the theme-coloured arrow (owner, 2026-11-25).
    accentShowAll: Boolean = false,
    // Titles glide into place when the row's list changes (owner, 2026-11-25).
    animateItems: Boolean = false,
    // Top Row: a large rank number beside every poster (owner, 2026-11-25).
    ranked: Boolean = false,
) {
    if (sectionPadding != null) {
        HomeCatalogRowSectionContent(
            section = section,
            entries = entries,
            watchedKeys = watchedKeys,
            fullyWatchedSeriesKeys = fullyWatchedSeriesKeys,
            modifier = modifier.fillMaxWidth(),
            sectionPadding = sectionPadding,
            onViewAllClick = onViewAllClick,
            onPosterClick = onPosterClick,
            onPosterLongClick = onPosterLongClick,
            posterWidth = posterWidth,
            landscape = landscape,
            landscapeOverride = landscapeOverride,
            useHomePill = useHomePill,
            accentShowAll = accentShowAll,
            animateItems = animateItems,
            ranked = ranked,
        )
    } else {
        BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
            HomeCatalogRowSectionContent(
                section = section,
                entries = entries,
                watchedKeys = watchedKeys,
                fullyWatchedSeriesKeys = fullyWatchedSeriesKeys,
                modifier = Modifier.fillMaxWidth(),
                sectionPadding = homeSectionHorizontalPaddingForWidth(maxWidth.value),
                onViewAllClick = onViewAllClick,
                onPosterClick = onPosterClick,
                onPosterLongClick = onPosterLongClick,
                posterWidth = posterWidth,
                landscape = landscape,
                landscapeOverride = landscapeOverride,
                useHomePill = useHomePill,
                accentShowAll = accentShowAll,
                animateItems = animateItems,
                ranked = ranked,
            )
        }
    }
}

@Composable
private fun HomeCatalogRowSectionContent(
    section: HomeCatalogSection,
    entries: List<MetaPreview>,
    watchedKeys: Set<String>,
    fullyWatchedSeriesKeys: Set<String>,
    modifier: Modifier,
    sectionPadding: Dp,
    onViewAllClick: (() -> Unit)?,
    onPosterClick: ((MetaPreview) -> Unit)?,
    onPosterLongClick: ((MetaPreview) -> Unit)?,
    posterWidth: Dp?,
    landscape: Boolean,
    landscapeOverride: Boolean?,
    useHomePill: Boolean,
    accentShowAll: Boolean = false,
    animateItems: Boolean = false,
    ranked: Boolean = false,
) {
    val posterCardStyle = rememberPosterCardStyleUiState()

    NuvioShelfSection(
        title = section.title,
        entries = entries,
        modifier = modifier,
        headerHorizontalPadding = sectionPadding,
        rowContentPadding = PaddingValues(horizontal = sectionPadding),
        itemSpacing = HomePosterSpacing,
        onViewAllClick = onViewAllClick,
        viewAllPillSize = when {
            accentShowAll -> NuvioViewAllPillSize.Accent
            useHomePill -> NuvioViewAllPillSize.Home
            else -> NuvioViewAllPillSize.Compact
        },
        titleMaxLines = Int.MAX_VALUE, // the whole name, on as many lines as it needs (owner, 2026-11-25)
        animatePlacement = animateItems,
        key = { item -> item.stableKey() },
    ) { item ->
        val card: @Composable () -> Unit = {
            HomePosterCard(
            item = item,
            useLandscapeBackdropMode = landscape || posterCardStyle.catalogLandscapeModeEnabled,
            landscapeOverride = landscapeOverride,
            isWatched = WatchingState.isPosterWatched(
                watchedKeys = watchedKeys,
                item = item,
                fullyWatchedSeriesKeys = fullyWatchedSeriesKeys,
            ),
            onClick = onPosterClick?.let { { it(item) } },
            onLongClick = onPosterLongClick?.let { { it(item) } },
            width = posterWidth,
        )
        }
        if (ranked) {
            RankedCard(
                item = item,
                rank = entries.indexOf(item) + 1,
                width = posterWidth,
                isWatched = WatchingState.isPosterWatched(
                    watchedKeys = watchedKeys,
                    item = item,
                    fullyWatchedSeriesKeys = fullyWatchedSeriesKeys,
                ),
                onClick = onPosterClick?.let { { it(item) } },
                onLongClick = onPosterLongClick?.let { { it(item) } },
            )
        } else {
            card()
        }
    }
}

/**
 * A Top Row card (owner, 2026-11-25): always landscape, and under the picture the rank on the left
 * with the name and "genre · year" to its right; the number is no taller than those two lines.
 */
@Composable
private fun RankedCard(
    item: MetaPreview,
    rank: Int,
    width: Dp?,
    isWatched: Boolean,
    onClick: (() -> Unit)?,
    onLongClick: (() -> Unit)?,
) {
    androidx.compose.foundation.layout.Column(
        modifier = (if (width != null) Modifier.width(width) else Modifier)
            // The rank line is taller than its text; 1.5 dp less keeps the 97 px to the next heading,
            // measured on the phone (owner, 2026-11-25).
            .then(
                Modifier.layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    val trimmed = (placeable.height - 1.5.dp.roundToPx()).coerceAtLeast(0)
                    layout(placeable.width, trimmed) { placeable.place(0, 0) }
                },
            ),
    ) {
        HomePosterCard(
            item = item,
            isWatched = isWatched,
            onClick = onClick,
            onLongClick = onLongClick,
            showLandscapeOverlay = false,
            width = width,
            landscapeOverride = true,
            showTitle = false,
        )
        androidx.compose.foundation.layout.Spacer(Modifier.height(com.nuvio.app.core.ui.NuvioTokens.Space.s6))
        androidx.compose.foundation.layout.Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
        ) {
            androidx.compose.material3.Text(
                text = rank.toString(),
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 44.sp,
                    lineHeight = 44.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.White,
                    platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false),
                    lineHeightStyle = androidx.compose.ui.text.style.LineHeightStyle(
                        alignment = androidx.compose.ui.text.style.LineHeightStyle.Alignment.Center,
                        trim = androidx.compose.ui.text.style.LineHeightStyle.Trim.Both,
                    ),
                ),
                maxLines = 1,
            )
            androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
                androidx.compose.material3.Text(
                    text = item.name,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                    color = androidx.compose.ui.graphics.Color.White,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
                val detail = listOfNotNull(
                    item.genres.firstOrNull()?.takeIf { it.isNotBlank() },
                    item.releaseInfo?.take(4)?.takeIf { it.isNotBlank() },
                ).joinToString(" · ")
                if (detail.isNotEmpty()) {
                    androidx.compose.material3.Text(
                        text = detail,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
