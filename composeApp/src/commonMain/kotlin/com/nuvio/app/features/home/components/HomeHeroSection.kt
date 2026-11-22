package com.nuvio.app.features.home.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.stopScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.nuvio.app.core.ui.focusedAlignment
import com.nuvio.app.core.ui.rememberBackdropFocusState
import com.nuvio.app.core.ui.revealAlpha
import com.nuvio.app.core.ui.heroStretchHeight
import com.nuvio.app.core.ui.LocalVerticalBarPadding
import com.nuvio.app.core.ui.ScreenActivityEffect
import com.nuvio.app.core.ui.heroStretchZoom
import com.nuvio.app.core.ui.centeredVerticalBarPadding
import com.nuvio.app.core.ui.withVerticalBar
import com.nuvio.app.isIos
import com.nuvio.app.features.home.MetaPreview
import com.nuvio.app.features.home.stableKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nuvio.app.features.details.youtubePlaybackUrl
import kotlinx.coroutines.launch
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs

private const val HERO_BACKGROUND_PARALLAX = 0.055f
private const val HERO_BACKGROUND_SCALE = 1.14f
private const val HERO_CONTENT_PARALLAX = 0.18f
private const val HERO_SCROLL_PARALLAX = 0.3f
private const val HERO_SCROLL_DOWN_SCALE_MULTIPLIER = 0.0001f
private const val HERO_SCROLL_UP_SCALE_MULTIPLIER = 0.002f
private const val HERO_SCROLL_MAX_SCALE = 1.3f
private const val HERO_SWIPE_THRESHOLD_FRACTION = 0.16f
private const val HERO_SWIPE_VELOCITY_THRESHOLD = 300f
private const val HERO_AUTO_SCROLL_INTERVAL_MS = 8_000L
private const val HERO_IOS_TRAILER_SCROLL_INTERVAL_MS = 45_000L
private const val HERO_TRAILER_START_TIMEOUT_MS = 20_000L

private enum class HeroTrailerStatus { Loading, Playing, Ended, Unavailable }

/**
 * The gap from View Details to the dots. The hero ends [HERO_BOTTOM_PADDING] below the dots and
 * Home's list adds 12 dp before Continue Watching, so the dots are the same distance from both.
 */
private val HERO_DOTS_GAP = 20.dp
private val HERO_BOTTOM_PADDING = 8.dp
private const val MOBILE_HERO_VIEWPORT_RATIO = 0.82f
private const val MOBILE_HERO_MIN_HEIGHT_DP = 360f
private const val MOBILE_HERO_MAX_HEIGHT_DP = 760f

internal data class HomeHeroLayout(
    val isTablet: Boolean,
    val heroHeight: Dp,
    val contentMaxWidth: Dp,
    val contentWidthFraction: Float,
    val contentHorizontalPadding: Dp,
    val contentVerticalPadding: Dp,
    val bottomFadeHeight: Dp,
    val logoWidthFraction: Float,
)

@Composable
fun HomeHeroSection(
    items: List<MetaPreview>,
    modifier: Modifier = Modifier,
    viewportHeight: Dp? = null,
    mobileBelowSectionHeightHint: Dp? = null,
    listState: LazyListState? = null,
    stretchPx: () -> Float = { 0f },
    showImdbRating: Boolean = true,
    showMdbListRatings: Boolean = false,
    onItemClick: ((MetaPreview) -> Unit)? = null,
) {
    if (items.isEmpty()) return

    val pagerState = key(items.size) {
        rememberPagerState(
            initialPage = if (items.size > 1) {
                Int.MAX_VALUE / 2 - (Int.MAX_VALUE / 2) % items.size
            } else {
                0
            },
            pageCount = { if (items.size > 1) Int.MAX_VALUE else 1 },
        )
    }
    val coroutineScope = rememberCoroutineScope()
    val autoScrollPage = pagerState.settledPage

    LaunchedEffect(pagerState) {
        pagerState.scrollToPage(pagerState.currentPage)
    }

    ScreenActivityEffect(pagerState) { active ->
        if (!active) {
            pagerState.stopScroll(MutatePriority.PreventUserInput)
            pagerState.scrollToPage(pagerState.currentPage)
        }
    }

    val moriviaLayout by com.nuvio.app.features.settings.MoriviaLayoutSettingsRepository.settings
        .collectAsStateWithLifecycle()
    // Hybrid stops rolling after the first swipe; a swipe is a scroll the hero did not start itself.
    var autoScrolling by remember { mutableStateOf(false) }
    var userSwiped by remember { mutableStateOf(false) }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.isScrollInProgress }.collect { scrolling ->
            if (scrolling && !autoScrolling) userSwiped = true
        }
    }
    val trailerEnabled = moriviaLayout.heroVideo &&
        com.nuvio.app.core.build.AppFeaturePolicy.heroTrailerPlaybackSupported &&
        com.nuvio.app.core.build.AppFeaturePolicy.trailerPlaybackMode == com.nuvio.app.core.build.TrailerPlaybackMode.IN_APP
    val trailerItem = items[autoScrollPage % items.size]
    val trailerStatus = remember(pagerState, autoScrollPage, trailerItem.id, trailerItem.type, trailerEnabled) {
        mutableStateOf(if (trailerEnabled) HeroTrailerStatus.Loading else HeroTrailerStatus.Unavailable)
    }
    val rolls = when (moriviaLayout.heroLayout) {
        com.nuvio.app.features.settings.MoriviaHeroLayout.ROLLING -> true
        com.nuvio.app.features.settings.MoriviaHeroLayout.PINNED -> false
        com.nuvio.app.features.settings.MoriviaHeroLayout.HYBRID -> !isIos || !userSwiped
    }

    val advanceDelay = if (isIos) {
        if (trailerStatus.value == HeroTrailerStatus.Playing) HERO_IOS_TRAILER_SCROLL_INTERVAL_MS
        else HERO_AUTO_SCROLL_INTERVAL_MS
    } else {
        when (trailerStatus.value) {
            HeroTrailerStatus.Loading -> HERO_TRAILER_START_TIMEOUT_MS
            HeroTrailerStatus.Playing -> null
            HeroTrailerStatus.Ended -> 0L
            HeroTrailerStatus.Unavailable -> HERO_AUTO_SCROLL_INTERVAL_MS
        }
    }
    ScreenActivityEffect(autoScrollPage, items.size, rolls, advanceDelay) { active ->
        if (!active || items.size <= 1 || !rolls || advanceDelay == null) return@ScreenActivityEffect
        delay(advanceDelay)
        while (pagerState.isScrollInProgress) {
            delay(100L)
        }
        if (pagerState.settledPage != autoScrollPage ||
            (!isIos && trailerStatus.value == HeroTrailerStatus.Playing)
        ) return@ScreenActivityEffect

        val nextPage = autoScrollPage + 1
        autoScrolling = true
        try {
            pagerState.animateScrollToPage(nextPage)
        } finally {
            autoScrolling = false
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .homeHeroPagerGesture(
                pagerState = pagerState,
                itemCount = items.size,
                coroutineScope = coroutineScope,
            )
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)),
    ) {
        val layout = homeHeroLayout(
            maxWidthDp = maxWidth.value,
            viewportHeightDp = viewportHeight?.value,
            mobileBelowSectionHeightHintDp = mobileBelowSectionHeightHint?.value,
        )
        val heroWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val heroHeightPx = with(LocalDensity.current) { layout.heroHeight.toPx() }
        val scrollOffsetPx by remember(listState, heroHeightPx) {
            derivedStateOf {
                when {
                    listState == null -> 0f
                    listState.firstVisibleItemIndex > 0 -> heroHeightPx
                    else -> listState.firstVisibleItemScrollOffset.toFloat()
                }
            }
        }
        val currentPage = pagerState.currentPage
        val visiblePages = listOf(
            currentPage,
            (currentPage - 1).coerceAtLeast(0),
            (currentPage + 1).coerceAtMost(pagerState.pageCount - 1),
        ).distinct()
            .mapNotNull { index ->
                val pageOffset = heroPageOffset(pagerState, index)
                val visibility = (1f - abs(pageOffset)).coerceIn(0f, 1f)
                if (visibility <= 0f) {
                    null
                } else {
                    HeroPageLayer(
                        itemIndex = index % items.size,
                        visibility = visibility,
                        offset = pageOffset,
                    )
                }
            }
            .sortedBy(HeroPageLayer::visibility)
        val currentItem = visiblePages
            .lastOrNull()
            ?.itemIndex
            ?.let(items::get)
            ?: items[currentPage % items.size]

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heroStretchHeight(layout.heroHeight, stretchPx),
        ) {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 0.01f },
            ) {
                Box(modifier = Modifier.fillMaxSize())
            }

            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(layout.heroHeight)
                        .heroStretchZoom(stretchPx),
                ) {
                    visiblePages.forEach { layer ->
                        key(layer.itemIndex) {
                            val layerImageUrl = items[layer.itemIndex].banner ?: items[layer.itemIndex].poster
                            val backdropFocus = rememberBackdropFocusState(layerImageUrl)
                            AsyncImage(
                                model = layerImageUrl,
                                contentDescription = items[layer.itemIndex].name,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        val offset = scrollOffsetPx
                                        val scrollScale = heroBackgroundScrollScale(offset)
                                        alpha = layer.visibility * backdropFocus.revealAlpha
                                        translationX = -layer.offset * heroWidthPx * HERO_BACKGROUND_PARALLAX
                                        translationY = heroBackgroundScrollTranslationY(offset)
                                        scaleX = HERO_BACKGROUND_SCALE * scrollScale
                                        scaleY = HERO_BACKGROUND_SCALE * scrollScale
                                    },
                                alignment = backdropFocus.focusedAlignment(
                                    if (layout.isTablet) Alignment.TopCenter else Alignment.Center,
                                ),
                                contentScale = ContentScale.Crop,
                                onSuccess = { state -> backdropFocus.onImageLoaded(state.result.image) },
                                onError = { backdropFocus.onImageFailed() },
                            )
                        }
                    }
                    // Hero video (Home & Layout): the title's trailer plays behind the hero.
                    if (trailerEnabled) {
                        key(autoScrollPage, trailerItem.id, trailerItem.type) {
                            HomeHeroTrailer(
                                item = trailerItem,
                                muted = !moriviaLayout.trailerSound,
                                onStatusChange = { trailerStatus.value = it },
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.02f),
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.12f),
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.34f),
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.78f),
                                ),
                            ),
                        ),
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(layout.bottomFadeHeight)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.background.copy(alpha = 0f),
                                    MaterialTheme.colorScheme.background,
                                ),
                            ),
                        ),
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        // Morivia: everything left-aligned on one edge, as in the earlier Morivia app.
                        .padding(PaddingValues(horizontal = layout.contentHorizontalPadding).withVerticalBar())
                        .padding(vertical = layout.contentVerticalPadding),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(layout.contentWidthFraction)
                            .widthIn(max = layout.contentMaxWidth),
                        contentAlignment = Alignment.BottomStart,
                    ) {
                        visiblePages.forEach { layer ->
                            val item = items[layer.itemIndex]
                            key(item.stableKey()) {
                                Box(
                                    modifier = Modifier.graphicsLayer {
                                        alpha = layer.visibility
                                        translationX = -layer.offset * heroWidthPx * HERO_CONTENT_PARALLAX
                                    },
                                ) {
                                    HeroContentBlock(
                                        item = item,
                                        layout = layout,
                                        showImdbRating = showImdbRating,
                                        showMdbListRatings = showMdbListRatings,
                                        onItemClick = onItemClick,
                                    )
                                }
                            }
                        }
                    }

                    if (!layout.isTablet) {
                        // The earlier Morivia app's buttons: View Details in the theme colour, an
                        // information button and a plain arrow to the next title.
                        Spacer(modifier = Modifier.height(18.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(40.dp))
                                    .clickable(enabled = onItemClick != null) {
                                        onItemClick?.invoke(currentItem)
                                    },
                                color = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                shape = RoundedCornerShape(40.dp),
                            ) {
                                Text(
                                    text = stringResource(Res.string.home_view_details),
                                    modifier = Modifier.padding(horizontal = 40.dp, vertical = 13.dp),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.22f))
                                    .clickable(enabled = onItemClick != null) { onItemClick?.invoke(currentItem) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Info,
                                    contentDescription = stringResource(Res.string.home_view_details),
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp),
                                )
                            }
                            if (items.size > 1) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                            }
                                        },
                                )
                            }
                        }
                    }

                    if (items.size > 1) {
                        // The same gap above the dots as from the dots to Continue Watching.
                        Spacer(modifier = Modifier.height(if (layout.isTablet) 14.dp else HERO_DOTS_GAP))
                        Row(
                            modifier = if (layout.isTablet) Modifier else Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            items.forEachIndexed { index, _ ->
                                val activeFraction = visiblePages
                                    .firstOrNull { it.itemIndex == index }
                                    ?.visibility ?: 0f
                                Box(
                                    modifier = Modifier
                                        .clickable {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(
                                                    heroPageForItem(pagerState.currentPage, index, items.size),
                                                )
                                            }
                                        }
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.onBackground)
                                        .graphicsLayer {
                                            alpha = 0.35f + (0.57f * activeFraction)
                                        }
                                        .width(8.dp + (24.dp * activeFraction))
                                        .height(8.dp),
                                )
                            }
                        }
                    }
                }

                // The earlier Morivia app's Home pill, below the clock and on the same edge as
                // the hero text.
                if (!layout.isTablet) {
                    val statusBarTop = androidx.compose.foundation.layout.WindowInsets.statusBars
                        .asPaddingValues().calculateTopPadding()
                    com.nuvio.app.core.ui.MoriviaPagePill(
                        label = stringResource(Res.string.compose_nav_home),
                        icon = Icons.Rounded.Home,
                        compact = !isIos,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(PaddingValues(start = if (isIos) layout.contentHorizontalPadding else 12.dp).withVerticalBar())
                            .padding(top = statusBarTop + 12.dp),
                    )
                }
            }
        }
    }
}

/**
 * The trailer of [item] behind the hero, as in the earlier Morivia app: it stays invisible until
 * it plays, so the artwork shows while it loads or when a title has no trailer.
 */
@Composable
private fun HomeHeroTrailer(
    item: MetaPreview,
    muted: Boolean,
    onStatusChange: (HeroTrailerStatus) -> Unit,
) {
    var source by remember(item.id) { mutableStateOf<com.nuvio.app.features.trailer.TrailerPlaybackSource?>(null) }
    var ready by remember(item.id) { mutableStateOf(false) }
    var finished by remember(item.id) { mutableStateOf(false) }
    LaunchedEffect(item.id, item.type) {
        // Wait a moment so quick swipes do not start trailers.
        delay(1_200L)
        val meta = com.nuvio.app.features.details.MetaDetailsRepository.peek(item.type, item.id)
            ?: runCatching { com.nuvio.app.features.details.MetaDetailsRepository.fetch(item.type, item.id) }.getOrNull()
        val trailer = meta?.let { com.nuvio.app.features.details.selectHeroTrailer(it.trailers) }
        if (trailer == null) {
            finished = true
            onStatusChange(HeroTrailerStatus.Unavailable)
            return@LaunchedEffect
        }
        source = runCatching {
            com.nuvio.app.features.trailer.TrailerPlaybackResolver.resolveFromYouTubeUrl(trailer.youtubePlaybackUrl())
        }.getOrNull()
        if (source == null) {
            finished = true
            onStatusChange(HeroTrailerStatus.Unavailable)
        }
    }
    val playing = source
    if (playing == null || finished) return
    val alpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (ready) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(600),
        label = "HomeHeroTrailerAlpha",
    )
    com.nuvio.app.features.details.components.HeroTrailerPlayerSurface(
        sourceUrl = playing.videoUrl,
        sourceAudioUrl = playing.audioUrl,
        playWhenReady = true,
        muted = muted,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                this.alpha = alpha
                scaleX = 1.08f
                scaleY = 1.08f
            },
        onReady = {
            ready = true
            onStatusChange(HeroTrailerStatus.Playing)
        },
        onEnded = {
            finished = true
            onStatusChange(HeroTrailerStatus.Ended)
        },
        onError = {
            finished = true
            onStatusChange(HeroTrailerStatus.Unavailable)
        },
    )
}

private data class HeroPageLayer(
    val itemIndex: Int,
    val visibility: Float,
    val offset: Float,
)

private fun heroPageOffset(
    pagerState: PagerState,
    page: Int,
): Float = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction

internal fun heroPageForItem(currentPage: Int, itemIndex: Int, itemCount: Int): Int {
    val page = currentPage.toLong() - currentPage % itemCount + itemIndex
    return listOf(page - itemCount, page, page + itemCount)
        .filter { it in 0L until Int.MAX_VALUE.toLong() }
        .minBy { abs(it - currentPage) }
        .toInt()
}

@Composable
fun HomeHeroReservedSpace(
    modifier: Modifier = Modifier,
    viewportHeight: Dp? = null,
    mobileBelowSectionHeightHint: Dp? = null,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)),
    ) {
        val layout = homeHeroLayout(
            maxWidthDp = maxWidth.value,
            viewportHeightDp = viewportHeight?.value,
            mobileBelowSectionHeightHintDp = mobileBelowSectionHeightHint?.value,
        )

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(layout.heroHeight),
        )
    }
}

@Composable
private fun HeroContentBlock(
    item: MetaPreview,
    layout: HomeHeroLayout,
    showImdbRating: Boolean,
    showMdbListRatings: Boolean,
    onItemClick: ((MetaPreview) -> Unit)?,
) {
    var logoLoadError by remember(item.type, item.id, item.logo) {
        mutableStateOf(false)
    }
    val logoUrl = item.logo?.takeIf { it.isNotBlank() }

    // Morivia: left-aligned like the earlier Morivia app, without ratings in the hero.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        horizontalAlignment = Alignment.Start,
    ) {
        if (logoUrl != null && !logoLoadError) {
            AsyncImage(
                model = logoUrl,
                contentDescription = item.name,
                modifier = Modifier
                    .fillMaxWidth(layout.logoWidthFraction)
                    .aspectRatio(2.6f)
                    .clickable(enabled = onItemClick != null) {
                        onItemClick?.invoke(item)
                    },
                alignment = Alignment.CenterStart,
                contentScale = ContentScale.Fit,
                onError = { logoLoadError = true },
            )
        } else {
            Text(
                text = item.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = onItemClick != null) {
                        onItemClick?.invoke(item)
                    },
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Start,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        // The badge: the first genre, or "Featured".
        Text(
            text = item.genres.firstOrNull { it.isNotBlank() } ?: stringResource(Res.string.morivia_hero_featured),
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.16f))
                .padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
        )
        val metaLine = listOfNotNull(
            stringResource(
                if (item.type.lowercase().let { it == "series" || it == "tv" || it == "anime" }) {
                    Res.string.morivia_hero_show
                } else {
                    Res.string.morivia_hero_movie
                },
            ),
            item.genres.firstOrNull { it.isNotBlank() },
            item.releaseInfo?.let { Regex("\\d{4}").find(it)?.value },
        ).joinToString(" · ")
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = metaLine,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        val description = item.description?.trim().orEmpty()
        if (description.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.92f),
                textAlign = TextAlign.Start,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

internal fun homeHeroLayout(
    maxWidthDp: Float,
    viewportHeightDp: Float? = null,
    mobileBelowSectionHeightHintDp: Float? = null,
): HomeHeroLayout =
    when {
        maxWidthDp >= 1200f -> HomeHeroLayout(
            isTablet = true,
            heroHeight = (maxWidthDp * 0.42f).dp.coerceIn(360.dp, 440.dp),
            contentMaxWidth = 640.dp,
            contentWidthFraction = 0.56f,
            contentHorizontalPadding = 56.dp,
            contentVerticalPadding = 22.dp,
            bottomFadeHeight = 190.dp,
            logoWidthFraction = 0.58f,
        )
        maxWidthDp >= 840f -> HomeHeroLayout(
            isTablet = true,
            heroHeight = (maxWidthDp * 0.46f).dp.coerceIn(340.dp, 420.dp),
            contentMaxWidth = 560.dp,
            contentWidthFraction = 0.62f,
            contentHorizontalPadding = 40.dp,
            contentVerticalPadding = 20.dp,
            bottomFadeHeight = 180.dp,
            logoWidthFraction = 0.56f,
        )
        maxWidthDp >= 600f -> HomeHeroLayout(
            isTablet = true,
            heroHeight = (maxWidthDp * 0.58f).dp.coerceIn(320.dp, 380.dp),
            contentMaxWidth = 520.dp,
            contentWidthFraction = 0.72f,
            contentHorizontalPadding = 32.dp,
            contentVerticalPadding = 18.dp,
            bottomFadeHeight = 170.dp,
            logoWidthFraction = 0.54f,
        )
        else -> HomeHeroLayout(
            isTablet = false,
            heroHeight = mobileHeroHeight(
                maxWidthDp = maxWidthDp,
                viewportHeightDp = viewportHeightDp,
                mobileBelowSectionHeightHintDp = mobileBelowSectionHeightHintDp,
            ),
            contentMaxWidth = 480.dp,
            contentWidthFraction = 1f,
            // Morivia: the content starts 16 dp from the edge, like the Home pill and the rows.
            contentHorizontalPadding = 16.dp,
            contentVerticalPadding = HERO_BOTTOM_PADDING,
            bottomFadeHeight = 220.dp,
            logoWidthFraction = 0.62f,
        )
    }

private fun mobileHeroHeight(
    maxWidthDp: Float,
    viewportHeightDp: Float?,
    mobileBelowSectionHeightHintDp: Float?,
): Dp {
    val viewportDrivenHeight = viewportHeightDp?.let { (it * MOBILE_HERO_VIEWPORT_RATIO).dp }
    val widthFallbackHeight = (maxWidthDp * 1.16f).dp
    val baseHeight = if (mobileBelowSectionHeightHintDp == null) {
        viewportDrivenHeight?.coerceAtMost(widthFallbackHeight) ?: widthFallbackHeight
    } else {
        viewportDrivenHeight ?: widthFallbackHeight
    }

    val maxAllowedFromViewportDp = if (viewportHeightDp != null && mobileBelowSectionHeightHintDp != null) {
        viewportHeightDp - mobileBelowSectionHeightHintDp
    } else {
        null
    }
    val cappedHeight = if (maxAllowedFromViewportDp != null) {
        val maxAllowedFromViewport = maxAllowedFromViewportDp.dp
        baseHeight.coerceAtMost(maxAllowedFromViewport)
    } else {
        baseHeight
    }
    val minHeight = if (maxAllowedFromViewportDp != null) {
        minOf(MOBILE_HERO_MIN_HEIGHT_DP, maxAllowedFromViewportDp.coerceAtLeast(0f)).dp
    } else {
        MOBILE_HERO_MIN_HEIGHT_DP.dp
    }

    return cappedHeight.coerceIn(minHeight, MOBILE_HERO_MAX_HEIGHT_DP.dp)
}

private fun heroBackgroundScrollScale(scrollOffsetPx: Float): Float {
    val scaleIncrease = if (scrollOffsetPx < 0f) {
        abs(scrollOffsetPx) * HERO_SCROLL_UP_SCALE_MULTIPLIER
    } else {
        scrollOffsetPx * HERO_SCROLL_DOWN_SCALE_MULTIPLIER
    }
    return (1f + scaleIncrease).coerceAtMost(HERO_SCROLL_MAX_SCALE)
}

private fun heroBackgroundScrollTranslationY(scrollOffsetPx: Float): Float {
    return scrollOffsetPx * HERO_SCROLL_PARALLAX
}

private fun Modifier.homeHeroPagerGesture(
    pagerState: PagerState,
    itemCount: Int,
    coroutineScope: CoroutineScope,
): Modifier {
    if (itemCount <= 1) return this

    return pointerInput(pagerState, itemCount) {
        awaitEachGesture {
            val down = awaitFirstDown(pass = PointerEventPass.Initial)
            val widthPx = size.width.toFloat().takeIf { it > 0f } ?: return@awaitEachGesture
            val velocityTracker = VelocityTracker().apply {
                addPosition(down.uptimeMillis, down.position)
            }
            val startPage = pagerState.currentPage
            var totalDx = 0f
            var totalDy = 0f
            var dragging = false

            while (true) {
                val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                velocityTracker.addPosition(change.uptimeMillis, change.position)

                if (!change.pressed) {
                    if (dragging) {
                        val targetPage = resolveHeroTargetPage(
                            startPage = startPage,
                            pageCount = pagerState.pageCount,
                            totalDx = totalDx,
                            velocityX = velocityTracker.calculateVelocity().x,
                            widthPx = widthPx,
                        )
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(targetPage)
                        }
                    }
                    break
                }

                val delta = change.position - change.previousPosition
                totalDx += delta.x
                totalDy += delta.y

                if (!dragging) {
                    val horizontalDrag =
                        abs(totalDx) > viewConfiguration.touchSlop && abs(totalDx) > abs(totalDy)
                    val verticalDrag =
                        abs(totalDy) > viewConfiguration.touchSlop && abs(totalDy) > abs(totalDx)

                    when {
                        verticalDrag -> break
                        horizontalDrag -> dragging = true
                        else -> continue
                    }
                }

                pagerState.dispatchRawDelta(-delta.x)
                change.consume()
            }
        }
    }
}

private fun resolveHeroTargetPage(
    startPage: Int,
    pageCount: Int,
    totalDx: Float,
    velocityX: Float,
    widthPx: Float,
): Int {
    val thresholdPassed = abs(totalDx) > widthPx * HERO_SWIPE_THRESHOLD_FRACTION ||
        abs(velocityX) > HERO_SWIPE_VELOCITY_THRESHOLD
    if (!thresholdPassed) return startPage

    val currentPage = startPage.coerceIn(0, pageCount - 1)
    return when {
        totalDx > 0f -> (currentPage - 1).coerceAtLeast(0)
        totalDx < 0f -> (currentPage + 1).coerceAtMost(pageCount - 1)
        else -> currentPage
    }
}
