package com.nuvio.app.features.home.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
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
        viewAllPillSize = if (useHomePill) NuvioViewAllPillSize.Home else NuvioViewAllPillSize.Compact,
        titleMaxLines = if (useHomePill) 2 else 1,
        key = { item -> item.stableKey() },
    ) { item ->
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
}
