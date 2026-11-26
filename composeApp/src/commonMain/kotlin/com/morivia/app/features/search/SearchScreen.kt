package com.morivia.app.features.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.gestures.stopScroll
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.morivia.app.core.network.NetworkCondition
import com.morivia.app.core.network.NetworkStatusRepository
import com.morivia.app.core.ui.MoriviaInputField
import com.morivia.app.core.ui.MoriviaTokens
import com.morivia.app.core.ui.MoriviaScreen
import com.morivia.app.core.ui.MoriviaNetworkOfflineCard
import com.morivia.app.core.ui.MoriviaScreenHeader
import com.morivia.app.core.ui.moriviaConsumePointerEvents
import com.morivia.app.core.ui.withDuplicateSafeLazyKeys
import com.morivia.app.core.ui.ScreenActivityEffect
import com.morivia.app.features.addons.AddonRepository
import com.morivia.app.features.addons.firstEnabledManifestError
import com.morivia.app.features.addons.hasPendingEnabledManifests
import com.morivia.app.features.home.HomeCatalogSettingsRepository
import com.morivia.app.features.home.MetaPreview
import com.morivia.app.features.home.buildAddonCatalogRefreshSignature
import com.morivia.app.features.home.components.HomeCatalogRowSection
import com.morivia.app.features.home.components.HomeEmptyStateCard
import com.morivia.app.features.home.components.homeSectionHorizontalPaddingForWidth
import com.morivia.app.features.home.components.HomeSkeletonRow
import com.morivia.app.features.home.components.posterGridColumnCountForWidth
import com.morivia.app.features.watched.WatchedRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import morivia.composeapp.generated.resources.Res
import morivia.composeapp.generated.resources.action_retry
import morivia.composeapp.generated.resources.compose_nav_search
import morivia.composeapp.generated.resources.home_empty_no_sources_title
import morivia.composeapp.generated.resources.search_empty_no_sources_message
import morivia.composeapp.generated.resources.compose_search_clear
import morivia.composeapp.generated.resources.compose_search_discover_title
import morivia.composeapp.generated.resources.compose_search_empty_failed_message
import morivia.composeapp.generated.resources.compose_search_empty_failed_title
import morivia.composeapp.generated.resources.compose_search_empty_no_results_message
import morivia.composeapp.generated.resources.compose_search_empty_no_results_title
import morivia.composeapp.generated.resources.compose_search_empty_no_search_catalogs_message
import morivia.composeapp.generated.resources.compose_search_empty_no_search_catalogs_title
import morivia.composeapp.generated.resources.compose_search_placeholder
import morivia.composeapp.generated.resources.compose_search_recent_searches
import morivia.composeapp.generated.resources.compose_search_remove_recent_search
import morivia.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    onPosterClick: ((MetaPreview) -> Unit)? = null,
    onPosterLongClick: ((MetaPreview) -> Unit)? = null,
    searchFocusRequestCount: Int = 0,
    scrollToTopRequests: Flow<Unit> = emptyFlow(),
    onAddAddon: () -> Unit = {},
) {
    // Morivia: without a catalog source Search shows nothing but "No content sources".
    val catalogSources = com.morivia.app.features.home.rememberCatalogSourceState()
    if (catalogSources == com.morivia.app.features.home.CatalogSourceState.Starting) {
        androidx.compose.foundation.layout.Box(modifier = modifier.then(androidx.compose.ui.Modifier.fillMaxSize()))
        return
    }
    if (catalogSources == com.morivia.app.features.home.CatalogSourceState.None) {
        com.morivia.app.features.home.NoCatalogSourcesScreen(onAddAddon = onAddAddon, modifier = modifier)
        return
    }
    SearchScreenWithCatalogs(
        modifier = modifier,
        listState = listState,
        onPosterClick = onPosterClick,
        onPosterLongClick = onPosterLongClick,
        searchFocusRequestCount = searchFocusRequestCount,
        scrollToTopRequests = scrollToTopRequests,
    )
}

/** The normal Search screen, shown when there is a catalog source. */
@Composable
internal fun SearchScreenWithCatalogs(
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    onPosterClick: ((MetaPreview) -> Unit)? = null,
    onPosterLongClick: ((MetaPreview) -> Unit)? = null,
    searchFocusRequestCount: Int = 0,
    scrollToTopRequests: Flow<Unit> = emptyFlow(),
) {
    val focusRequester = remember { FocusRequester() }
    var isSearchFocused by remember { mutableStateOf(false) }

    ScreenActivityEffect(listState) { screenActive ->
        if (!screenActive) {
            isSearchFocused = false
            listState.stopScroll()
        }
    }

    ScreenActivityEffect(searchFocusRequestCount) { screenActive ->
        if (screenActive && searchFocusRequestCount > 0) {
            focusRequester.requestFocus()
        }
    }

    LaunchedEffect(Unit) {
        AddonRepository.initialize()
        WatchedRepository.ensureLoaded()
        SearchHistoryRepository.ensureLoaded()
    }

    val addonsUiState by AddonRepository.uiState.collectAsStateWithLifecycle()
    val uiState by SearchRepository.uiState.collectAsStateWithLifecycle()
    val discoverUiState by SearchRepository.discoverUiState.collectAsStateWithLifecycle()
    val homeCatalogSettingsUiState by remember {
        HomeCatalogSettingsRepository.snapshot()
        HomeCatalogSettingsRepository.uiState
    }.collectAsStateWithLifecycle()
    val recentSearches by SearchHistoryRepository.uiState.collectAsStateWithLifecycle()
    val watchedUiState by WatchedRepository.uiState.collectAsStateWithLifecycle()
    val fullyWatchedSeriesKeys by WatchedRepository.fullyWatchedSeriesKeys.collectAsStateWithLifecycle()
    val networkStatusUiState by NetworkStatusRepository.uiState.collectAsStateWithLifecycle()
    val moriviaLayout by remember {
        com.morivia.app.features.settings.MoriviaLayoutSettingsRepository.ensureLoaded()
        com.morivia.app.features.settings.MoriviaLayoutSettingsRepository.settings
    }.collectAsStateWithLifecycle()
    val homeUiState by com.morivia.app.features.home.HomeRepository.uiState.collectAsStateWithLifecycle()
    // Trending: the "Popular" catalogs of Home (or its first rows), movies and shows taking turns.
    val trendingSections = remember(homeUiState.sections) {
        val popular = homeUiState.sections.filter { it.title.contains("popular", ignoreCase = true) }
        (popular.ifEmpty { homeUiState.sections.take(2) }).filter { it.items.isNotEmpty() }
    }
    val trendingItems = remember(trendingSections) {
        val lists = trendingSections.map { it.items }
        val mixed = mutableListOf<com.morivia.app.features.home.MetaPreview>()
        var index = 0
        while (mixed.size < 20 && lists.any { index < it.size }) {
            lists.forEach { list -> if (index < list.size) mixed += list[index] }
            index++
        }
        mixed.distinctBy { it.id }.take(20)
    }
    val trendingTarget = trendingSections.firstOrNull()?.target
    val browseScope = rememberCoroutineScope()
    var query by rememberSaveable { mutableStateOf("") }
    var lastRequestedQuery by rememberSaveable { mutableStateOf<String?>(null) }
    var observedOfflineState by remember { mutableStateOf(false) }
    val discoverInFocus by remember(query, listState) {
        derivedStateOf {
            query.isBlank() && listState.firstVisibleItemIndex > 0
        }
    }

    ScreenActivityEffect(scrollToTopRequests) { screenActive ->
        if (!screenActive) return@ScreenActivityEffect
        scrollToTopRequests.collect {
            listState.animateScrollToItem(0)
        }
    }

    val addonRefreshKey = remember(addonsUiState.addons) {
        buildAddonCatalogRefreshSignature(addonsUiState.addons)
    }
    val addonManifestsLoading = addonsUiState.addons.hasPendingEnabledManifests()

    ScreenActivityEffect(addonRefreshKey, homeCatalogSettingsUiState.hideUnreleasedContent) { screenActive ->
        if (!screenActive) return@ScreenActivityEffect
        SearchRepository.refreshDiscover(addonsUiState.addons)
    }

    ScreenActivityEffect(query, addonRefreshKey, homeCatalogSettingsUiState.hideUnreleasedContent) { screenActive ->
        if (!screenActive) return@ScreenActivityEffect
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) {
            lastRequestedQuery = null
            SearchRepository.clear()
        } else {
            delay(350)
            lastRequestedQuery = normalizedQuery
            SearchRepository.search(
                query = normalizedQuery,
                addons = addonsUiState.addons,
            )
        }
    }

    ScreenActivityEffect(listState, query, discoverUiState.canLoadMore, discoverUiState.isLoading) { screenActive ->
        if (!screenActive || query.isNotBlank()) return@ScreenActivityEffect

        snapshotFlow { listState.layoutInfo }
            .map { layoutInfo ->
                val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                lastVisible >= layoutInfo.totalItemsCount - 4
            }
            .distinctUntilChanged()
            .filter { it && discoverUiState.canLoadMore && !discoverUiState.isLoading }
            .collect {
                SearchRepository.loadMoreDiscover()
            }
    }

    ScreenActivityEffect(query, lastRequestedQuery, uiState.isLoading, uiState.sections) { screenActive ->
        if (!screenActive) return@ScreenActivityEffect
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) return@ScreenActivityEffect
        if (lastRequestedQuery != normalizedQuery) return@ScreenActivityEffect
        if (uiState.isLoading || uiState.sections.isEmpty()) return@ScreenActivityEffect
        SearchHistoryRepository.recordSearch(normalizedQuery)
    }

    ScreenActivityEffect(networkStatusUiState.condition, query, addonRefreshKey) { screenActive ->
        if (!screenActive) return@ScreenActivityEffect
        when (networkStatusUiState.condition) {
            NetworkCondition.NoInternet,
            NetworkCondition.ServersUnreachable,
            -> {
                observedOfflineState = true
            }

            NetworkCondition.Online -> {
                if (!observedOfflineState) return@ScreenActivityEffect
                observedOfflineState = false

                val normalizedQuery = query.trim()
                if (normalizedQuery.isBlank()) {
                    SearchRepository.refreshDiscover(
                        addons = addonsUiState.addons,
                        forceRefresh = true,
                    )
                } else {
                    SearchRepository.search(
                        query = normalizedQuery,
                        addons = addonsUiState.addons,
                        forceRefresh = true,
                    )
                }
            }

            NetworkCondition.Unknown,
            NetworkCondition.Checking,
            -> Unit
        }
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
    ) {
        val discoverColumns = remember(maxWidth) {
            posterGridColumnCountForWidth(maxWidth)
        }
        val homeSectionPadding = remember(maxWidth) {
            homeSectionHorizontalPaddingForWidth(maxWidth.value)
        }
        val headerTitle = when {
            query.isNotBlank() -> stringResource(Res.string.compose_nav_search)
            discoverInFocus -> stringResource(Res.string.compose_search_discover_title)
            else -> stringResource(Res.string.compose_nav_search)
        }

        MoriviaScreen(
            horizontalPadding = 0.dp,
            listState = listState,
            modifier = Modifier.fillMaxSize(),
        ) {
        stickyHeader {
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(MaterialTheme.colorScheme.background)
                        .moriviaConsumePointerEvents(),
                )
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    // The earlier Morivia app's Search page: the Search pill, then "Movies and Shows".
                    val statusBarTop = androidx.compose.foundation.layout.WindowInsets.statusBars
                        .asPaddingValues().calculateTopPadding()
                    com.morivia.app.core.ui.MoriviaPagePill(
                        label = headerTitle,
                        icon = Icons.Rounded.Search,
                        modifier = Modifier.padding(start = 16.dp, top = statusBarTop + 12.dp),
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(18.dp))
                    androidx.compose.foundation.layout.Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        MoriviaInputField(
                            value = query,
                            onValueChange = { query = it },
                            placeholder = stringResource(Res.string.morivia_search_placeholder),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Content),
                            shape = RoundedCornerShape(MoriviaTokens.Radius.xl),
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            modifier = Modifier
                                .focusRequester(focusRequester)
                                .onFocusChanged { isSearchFocused = it.isFocused },
                            trailingContent = if (query.isNotBlank()) {
                                {
                                    IconButton(onClick = { query = "" }) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = stringResource(Res.string.compose_search_clear),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            } else {
                                null
                            },
                        )
                    }
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }

        if (query.isBlank()) {
            var itemsBeforeDiscover = 1
            if (moriviaLayout.searchHistory && isSearchFocused && recentSearches.isNotEmpty()) {
                itemsBeforeDiscover++
                item(key = "recent_searches") {
                    SearchRecentSection(
                        recentSearches = recentSearches,
                        onSearchPress = { recentQuery -> query = recentQuery },
                        onRemoveSearch = SearchHistoryRepository::removeSearch,
                    )
                }
            }
            // The earlier Morivia app's Trending row: the popular titles of Home's catalogs.
            if (moriviaLayout.searchTrending && trendingItems.isNotEmpty()) {
                itemsBeforeDiscover++
                item(key = "morivia_trending") {
                    HomeCatalogRowSection(
                        section = com.morivia.app.features.home.HomeCatalogSection(
                            key = "morivia_trending",
                            title = stringResource(Res.string.morivia_search_trending),
                            subtitle = "",
                            addonName = "",
                            target = trendingTarget!!,
                            items = trendingItems,
                        ),
                        entries = trendingItems,
                        modifier = Modifier.padding(bottom = 12.dp),
                        sectionPadding = homeSectionPadding,
                        watchedKeys = watchedUiState.watchedKeys,
                        fullyWatchedSeriesKeys = fullyWatchedSeriesKeys,
                        onPosterClick = onPosterClick,
                        onPosterLongClick = onPosterLongClick,
                    )
                }
            }
            // Browse: coloured genre tiles; a tile shows that genre in the grid below.
            if (moriviaLayout.searchBrowse && discoverUiState.genreOptions.isNotEmpty()) {
                itemsBeforeDiscover++
                val discoverIndex = itemsBeforeDiscover
                item(key = "morivia_browse") {
                    MoriviaBrowseTiles(
                        genres = discoverUiState.genreOptions,
                        selectedGenre = discoverUiState.selectedGenre,
                        horizontalPadding = homeSectionPadding,
                        onGenreClick = { genre ->
                            SearchRepository.selectDiscoverGenre(genre)
                            browseScope.launch { listState.animateScrollToItem(discoverIndex) }
                        },
                    )
                }
            }
                discoverContent(
                    state = discoverUiState,
                    isSourceLoading = addonManifestsLoading,
                    columns = discoverColumns,
                    networkCondition = networkStatusUiState.condition,
                    onTypeSelected = SearchRepository::selectDiscoverType,
                    onCatalogSelected = SearchRepository::selectDiscoverCatalog,
                    onGenreSelected = SearchRepository::selectDiscoverGenre,
                    onRetry = {
                        NetworkStatusRepository.requestRefresh(force = true)
                        if (addonsUiState.addons.firstEnabledManifestError() != null) {
                            AddonRepository.refreshAll()
                        } else {
                            SearchRepository.refreshDiscover(
                                addons = addonsUiState.addons,
                                forceRefresh = true,
                            )
                        }
                    },
                    watchedKeys = watchedUiState.watchedKeys,
                    fullyWatchedSeriesKeys = fullyWatchedSeriesKeys,
                    onPosterClick = onPosterClick,
                    onPosterLongClick = onPosterLongClick,
                )
            } else {
                val normalizedQuery = query.trim()
                val isWaitingForSearch = normalizedQuery.isNotBlank() && lastRequestedQuery != normalizedQuery
                when {
                    isWaitingForSearch -> {
                        items(2) {
                            HomeSkeletonRow(
                                horizontalPadding = homeSectionPadding,
                            )
                        }
                    }

                    (uiState.isLoading || addonManifestsLoading) && uiState.sections.isEmpty() -> {
                        items(2) {
                            HomeSkeletonRow(
                                horizontalPadding = homeSectionPadding,
                            )
                        }
                    }

                    uiState.sections.isEmpty() -> {
                        item {
                            SearchEmptyStateCard(
                                reason = uiState.emptyStateReason,
                                errorMessage = uiState.errorMessage,
                                networkCondition = networkStatusUiState.condition,
                                onRetry = {
                                    if (normalizedQuery.isNotBlank()) {
                                        NetworkStatusRepository.requestRefresh(force = true)
                                        if (addonsUiState.addons.firstEnabledManifestError() != null) {
                                            AddonRepository.refreshAll()
                                        } else {
                                            SearchRepository.search(
                                                query = normalizedQuery,
                                                addons = addonsUiState.addons,
                                                forceRefresh = true,
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.padding(horizontal = homeSectionPadding),
                            )
                        }
                    }

                    else -> {
                        items(
                            items = uiState.sections.withDuplicateSafeLazyKeys { section -> section.key },
                            key = { section -> section.lazyKey },
                        ) { keyedSection ->
                            val section = keyedSection.value
                            HomeCatalogRowSection(
                                section = section,
                                modifier = Modifier.padding(bottom = 12.dp),
                                watchedKeys = watchedUiState.watchedKeys,
                                fullyWatchedSeriesKeys = fullyWatchedSeriesKeys,
                                onPosterClick = onPosterClick,
                                onPosterLongClick = onPosterLongClick,
                            )
                        }
                        if (uiState.isLoading) {
                            item(key = "search_loading_more") {
                                HomeSkeletonRow(
                                    horizontalPadding = homeSectionPadding,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchEmptyStateCard(
    reason: SearchEmptyStateReason?,
    errorMessage: String?,
    networkCondition: NetworkCondition,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    if (
        reason == SearchEmptyStateReason.RequestFailed &&
        (networkCondition == NetworkCondition.NoInternet || networkCondition == NetworkCondition.ServersUnreachable)
    ) {
        MoriviaNetworkOfflineCard(
            condition = networkCondition,
            modifier = modifier,
            onRetry = onRetry,
        )
        return
    }

    val title: String
    val message: String

    when (reason) {
        SearchEmptyStateReason.NoActiveAddons -> {
            title = stringResource(Res.string.home_empty_no_sources_title)
            message = stringResource(Res.string.search_empty_no_sources_message)
        }

        SearchEmptyStateReason.NoSearchCatalogs -> {
            title = stringResource(Res.string.compose_search_empty_no_search_catalogs_title)
            message = stringResource(Res.string.compose_search_empty_no_search_catalogs_message)
        }

        SearchEmptyStateReason.RequestFailed -> {
            title = stringResource(Res.string.compose_search_empty_failed_title)
            message = errorMessage ?: stringResource(Res.string.compose_search_empty_failed_message)
        }

        SearchEmptyStateReason.NoResults, null -> {
            title = stringResource(Res.string.compose_search_empty_no_results_title)
            message = stringResource(Res.string.compose_search_empty_no_results_message)
        }
    }

    HomeEmptyStateCard(
        modifier = modifier,
        title = title,
        message = message,
        actionLabel = if (reason == SearchEmptyStateReason.RequestFailed) {
            stringResource(Res.string.action_retry)
        } else {
            null
        },
        onActionClick = if (reason == SearchEmptyStateReason.RequestFailed) onRetry else null,
    )
}

@Composable
private fun SearchRecentSection(
    recentSearches: List<String>,
    onSearchPress: (String) -> Unit,
    onRemoveSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(Res.string.compose_search_recent_searches),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(4.dp))
        recentSearches.forEach { recentQuery ->
            SearchRecentRow(
                query = recentQuery,
                onSearchPress = { onSearchPress(recentQuery) },
                onRemovePress = { onRemoveSearch(recentQuery) },
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
    }
}

@Composable
internal fun SearchRecentRow(
    query: String,
    onSearchPress: () -> Unit,
    onRemovePress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSearchPress)
            .padding(vertical = 2.dp)
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(start = 2.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = query,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Content),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        IconButton(onClick = onRemovePress) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = stringResource(Res.string.compose_search_remove_recent_search),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** The earlier Morivia app's Browse row: wide genre tiles in soft two-colour gradients. */
@Composable
private fun MoriviaBrowseTiles(
    genres: List<String>,
    selectedGenre: String?,
    horizontalPadding: androidx.compose.ui.unit.Dp,
    onGenreClick: (String) -> Unit,
) {
    val gradients = remember {
        listOf(
            listOf(androidx.compose.ui.graphics.Color(0xFF7B2FF7), androidx.compose.ui.graphics.Color(0xFFE0558F)),
            listOf(androidx.compose.ui.graphics.Color(0xFF1C8C8C), androidx.compose.ui.graphics.Color(0xFF4FC3A1)),
            listOf(androidx.compose.ui.graphics.Color(0xFFE0558F), androidx.compose.ui.graphics.Color(0xFFF5A35C)),
            listOf(androidx.compose.ui.graphics.Color(0xFF3949AB), androidx.compose.ui.graphics.Color(0xFF29B6F6)),
            listOf(androidx.compose.ui.graphics.Color(0xFF8E24AA), androidx.compose.ui.graphics.Color(0xFF5E35B1)),
            listOf(androidx.compose.ui.graphics.Color(0xFF00897B), androidx.compose.ui.graphics.Color(0xFF7CB342)),
        )
    }
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(Res.string.morivia_search_browse),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = horizontalPadding),
        )
        androidx.compose.foundation.lazy.LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = horizontalPadding),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
        ) {
            items(genres.size) { index ->
                val genre = genres[index]
                val selected = genre.equals(selectedGenre, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .size(width = 150.dp, height = 82.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(androidx.compose.ui.graphics.Brush.linearGradient(gradients[index % gradients.size]))
                        .then(
                            if (selected) {
                                Modifier.border(2.dp, androidx.compose.ui.graphics.Color.White, RoundedCornerShape(14.dp))
                            } else {
                                Modifier
                            },
                        )
                        .clickable { onGenreClick(genre) }
                        .padding(12.dp),
                    contentAlignment = androidx.compose.ui.Alignment.BottomStart,
                ) {
                    Text(
                        text = genre,
                        style = MaterialTheme.typography.titleSmall,
                        color = androidx.compose.ui.graphics.Color.White,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
