package com.nuvio.app.features.home.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import com.nuvio.app.core.format.formatReleaseDateForDisplay
import com.nuvio.app.core.ui.NuvioPosterCard
import com.nuvio.app.core.ui.NuvioPosterShape
import com.nuvio.app.core.ui.rememberPosterCardStyleUiState
import com.nuvio.app.features.home.MetaPreview
import com.nuvio.app.features.home.PosterShape

@Composable
fun HomePosterCard(
    item: MetaPreview,
    modifier: Modifier = Modifier,
    useLandscapeBackdropMode: Boolean = false,
    isWatched: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    showLandscapeOverlay: Boolean = true,
    width: androidx.compose.ui.unit.Dp? = null,
    landscapeOverride: Boolean? = null,
    // Off for Top Row cards, which show the name beside the rank instead (owner, 2026-11-25).
    showTitle: Boolean = true,
) {
    val posterCardStyle = rememberPosterCardStyleUiState()
    val isLandscapeMode = landscapeOverride ?: (useLandscapeBackdropMode || posterCardStyle.catalogLandscapeModeEnabled)
    val effectiveLandscapePoster = if (posterCardStyle.alwaysShowLandscapeClearlogo) null else item.landscapePoster
    val imageUrl = if (isLandscapeMode) {
        effectiveLandscapePoster ?: item.banner ?: item.poster
    } else item.poster
    val fallbackImageUrl = if (isLandscapeMode && !effectiveLandscapePoster.isNullOrBlank()) {
        // Landscape custom poster -> fall back to original backdrop, then portrait
        item.banner ?: item.rawPosterUrl
    } else {
        item.rawPosterUrl
    }

    NuvioPosterCard(
        title = item.name,
        imageUrl = imageUrl,
        modifier = modifier,
        fallbackImageUrl = fallbackImageUrl,
        shape = when {
            isLandscapeMode -> NuvioPosterShape.Landscape
            landscapeOverride == false -> NuvioPosterShape.Poster
            else -> item.posterShape.toNuvioPosterShape()
        },
        detailLine = if (isLandscapeMode || posterCardStyle.hideLabelsEnabled) null else item.releaseInfo?.let { formatReleaseDateForDisplay(it) },
        showTitleBelow = showTitle && !posterCardStyle.hideLabelsEnabled,
        bottomLeftLogoUrl = if (isLandscapeMode && showLandscapeOverlay && effectiveLandscapePoster.isNullOrBlank()) item.logo else null,
        bottomLeftText = if (isLandscapeMode && showLandscapeOverlay && effectiveLandscapePoster.isNullOrBlank() && item.logo.isNullOrBlank() && !posterCardStyle.hideLabelsEnabled) item.name else null,
        isWatched = isWatched,
        onClick = onClick,
        onLongClick = onLongClick,
        showNewBanner = rememberShowsNewBanner(item),
        widthOverride = width,
    )
}

/** "New" on titles released in the last 30 days, when Poster banners is on in Home & Layout. */
@Composable
internal fun rememberShowsNewBanner(item: MetaPreview): Boolean {
    val settings by com.nuvio.app.features.settings.MoriviaLayoutSettingsRepository.settings
        .collectAsStateWithLifecycle()
    if (!settings.posterBanners) return false
    return androidx.compose.runtime.remember(item.rawReleaseDate, item.releaseInfo) {
        isRecentRelease(item.rawReleaseDate ?: item.releaseInfo)
    }
}

internal fun isRecentRelease(date: String?, nowEpochMs: Long = com.nuvio.app.features.watchprogress.WatchProgressClock.nowEpochMs()): Boolean {
    val text = date?.trim().orEmpty()
    val match = Regex("^(\\d{4})-(\\d{2})-(\\d{2})").find(text) ?: return false
    val (year, month, day) = match.destructured
    val releaseDay = epochDay(year.toInt(), month.toInt(), day.toInt())
    val today = nowEpochMs / 86_400_000L
    return releaseDay in (today - 30)..today
}

/** Days since 1970-01-01 for a calendar date (proleptic Gregorian). */
private fun epochDay(year: Int, month: Int, day: Int): Long {
    val y = if (month <= 2) year - 1 else year
    val era = (if (y >= 0) y else y - 399) / 400
    val yoe = y - era * 400
    val m = month + if (month > 2) -3 else 9
    val doy = (153 * m + 2) / 5 + day - 1
    val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
    return era * 146097L + doe - 719468L
}

private fun PosterShape.toNuvioPosterShape(): NuvioPosterShape =
    when (this) {
        PosterShape.Poster -> NuvioPosterShape.Poster
        PosterShape.Square -> NuvioPosterShape.Square
        PosterShape.Landscape -> NuvioPosterShape.Landscape
    }
