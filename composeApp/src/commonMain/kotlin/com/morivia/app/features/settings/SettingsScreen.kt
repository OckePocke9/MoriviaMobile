package com.morivia.app.features.settings

import com.morivia.app.core.build.AppFeaturePolicy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.graphics.Color
import morivia.composeapp.generated.resources.party_title
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.gestures.stopScroll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.morivia.app.core.ui.LocalScreenActive
import com.morivia.app.core.ui.ScreenActivityEffect
import com.morivia.app.core.ui.AppTheme
import com.morivia.app.core.ui.LocalMoriviaBottomNavigationOverlayPadding
import com.morivia.app.core.ui.MoriviaScreen
import com.morivia.app.core.ui.MoriviaScreenHeader
import com.morivia.app.core.ui.PlatformBackHandler
import com.morivia.app.core.ui.isLiquidGlassNativeTabBarSupported
import com.morivia.app.features.addons.AddonRepository
import com.morivia.app.features.details.MetaScreenSettingsRepository
import com.morivia.app.features.details.MetaScreenSettingsUiState
import com.morivia.app.core.ui.PosterCardStyleRepository
import com.morivia.app.core.ui.PosterCardStyleUiState
import com.morivia.app.features.collection.CollectionRepository
import com.morivia.app.features.addons.enabledAddons
import com.morivia.app.features.addons.firstEnabledManifestError
import com.morivia.app.features.addons.hasPendingEnabledManifests
import com.morivia.app.features.addons.isWaitingForFirstEnabledManifest
import com.morivia.app.features.debrid.DebridSettings
import com.morivia.app.features.debrid.DebridSettingsRepository
import com.morivia.app.features.home.HomeCatalogSettingsItem
import com.morivia.app.features.home.HomeCatalogSettingsRepository
import com.morivia.app.features.home.buildAddonCatalogRefreshSignature
import com.morivia.app.features.mdblist.MdbListSettings
import com.morivia.app.features.mdblist.MdbListSettingsRepository
import com.morivia.app.features.mdblist.MdbListTracker
import com.morivia.app.features.notifications.EpisodeReleaseNotificationsRepository
import com.morivia.app.features.notifications.EpisodeReleaseNotificationsUiState

import com.morivia.app.features.profiles.ProfileRepository
import com.morivia.app.features.simkl.SimklAuthRepository
import com.morivia.app.features.simkl.SimklAuthUiState
import com.morivia.app.features.trakt.TraktAuthUiState
import com.morivia.app.features.trakt.TraktAuthRepository
import com.morivia.app.features.trakt.TraktCommentsSettings
import com.morivia.app.features.tracking.TrackingSettingsRepository
import com.morivia.app.features.tracking.TrackingSettingsUiState
import com.morivia.app.features.tmdb.TmdbSettings
import com.morivia.app.features.tmdb.TmdbSettingsRepository
import com.morivia.app.features.watchprogress.ContinueWatchingPreferencesRepository
import com.morivia.app.features.watchprogress.ContinueWatchingPreferencesUiState
import com.morivia.app.navigation.LocalUseNativeNavigation
import com.morivia.app.features.party.PartyRepository
import com.morivia.app.features.p2p.P2pSettingsRepository
import com.morivia.app.isIos
import morivia.composeapp.generated.resources.Res
import morivia.composeapp.generated.resources.compose_settings_page_root
import morivia.composeapp.generated.resources.compose_settings_page_tracking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
private fun watchPartyRow(onClick: () -> Unit): (() -> Unit)? {
    val available by PartyRepository.available.collectAsStateWithLifecycle()
    return onClick.takeIf { available }
}

internal fun SettingsPage.isEnabledByPolicy(): Boolean =
    when (this) {
        SettingsPage.SupportersContributors -> AppFeaturePolicy.supportersContributorsPageEnabled
        SettingsPage.PlaybackP2p -> P2pSettingsRepository.isVisible
        SettingsPage.PlaybackBuffer -> !isIos
        else -> true
    }

@Composable
private fun settingsPageTitles(): Map<SettingsPage, String> {
    val titles = mutableMapOf<SettingsPage, String>()
    for (page in SettingsPage.entries) {
        titles[page] = stringResource(page.titleRes)
    }
    return titles
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    rootActionRequests: Flow<Unit> = emptyFlow(),
    initialPageName: String = SettingsPage.Root.name,
    requestedPageName: String? = null,
    onRequestedPageConsumed: () -> Unit = {},
    rootActionsEnabled: Boolean = true,
    isSelectedTab: Boolean = true,
    onNavigatePage: ((pageName: String, title: String) -> Unit)? = null,
    onExternalBack: (() -> Unit)? = null,
    onTabSelected: ((com.morivia.app.AppScreenTab) -> Unit)? = null,
    showInternalHeader: Boolean = true,
    onSwitchProfile: (() -> Unit)? = null,
    onHomescreenClick: () -> Unit = {},
    onMetaScreenClick: () -> Unit = {},
    onContinueWatchingClick: () -> Unit = {},
    onAddonsClick: () -> Unit = {},
    onPluginsClick: () -> Unit = {},
    onAccountClick: () -> Unit = {},
    onSupportersContributorsClick: () -> Unit = {},
    onLicensesAttributionsClick: () -> Unit = {},
    onCheckForUpdatesClick: (() -> Unit)? = null,
    onTestUpdateBannerClick: (() -> Unit)? = null,
    onCollectionsClick: () -> Unit = {},
    onWatchPartyClick: () -> Unit = {},
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
    ) {
        val screenActive = LocalScreenActive.current
        val pageStateHolder = rememberSaveableStateHolder()

        val selectedTheme by remember {
            ThemeSettingsRepository.ensureLoaded()
            ThemeSettingsRepository.selectedTheme
        }.collectAsStateWithLifecycle()
        val amoledEnabled by remember { ThemeSettingsRepository.amoledEnabled }.collectAsStateWithLifecycle()
        val useNativeNavigation = LocalUseNativeNavigation.current
        val liquidGlassNativeTabBarSupported = remember(useNativeNavigation) {
            !useNativeNavigation && isLiquidGlassNativeTabBarSupported()
        }
        val selectedAppLanguage by remember { ThemeSettingsRepository.selectedAppLanguage }.collectAsStateWithLifecycle()
        val appIconState by remember {
            AppIconRepository.ensureLoaded()
            AppIconRepository.state
        }.collectAsStateWithLifecycle()
        val appIconScope = rememberCoroutineScope()
        val onAppIconSelected: (AppIconOption) -> Unit = { icon ->
            appIconScope.launch {
                ThemeSettingsRepository.selectAppIcon(icon)
            }
        }
        val tmdbSettings by remember {
            TmdbSettingsRepository.ensureLoaded()
            TmdbSettingsRepository.uiState
        }.collectAsStateWithLifecycle()
        val mdbListSettings by remember {
            MdbListSettingsRepository.ensureLoaded()
            MdbListSettingsRepository.uiState
        }.collectAsStateWithLifecycle()
        val debridSettings by remember {
            DebridSettingsRepository.ensureLoaded()
            DebridSettingsRepository.uiState
        }.collectAsStateWithLifecycle()
        val traktAuthUiState by remember {
            TraktAuthRepository.ensureLoaded()
            TraktAuthRepository.uiState
        }.collectAsStateWithLifecycle()
        val simklAuthUiState by remember {
            SimklAuthRepository.ensureLoaded()
            SimklAuthRepository.uiState
        }.collectAsStateWithLifecycle()
        val traktCommentsEnabled by remember {
            TraktCommentsSettings.ensureLoaded()
            TraktCommentsSettings.enabled
        }.collectAsStateWithLifecycle()
        val mdbListConnected by remember {
            MdbListTracker.ensureLoaded()
            MdbListTracker.isAuthenticated
        }.collectAsStateWithLifecycle()
        val trackingSettingsUiState by remember {
            TrackingSettingsRepository.ensureLoaded()
            TrackingSettingsRepository.uiState
        }.collectAsStateWithLifecycle()
        val addonsUiState by remember {
            AddonRepository.initialize()
            AddonRepository.uiState
        }.collectAsStateWithLifecycle()
        val homescreenCatalogRefreshKey = remember(addonsUiState.addons) {
            buildAddonCatalogRefreshSignature(addonsUiState.addons)
        }
        val addonManifestsLoading = addonsUiState.addons.hasPendingEnabledManifests()
        val addonManifestErrorMessage = addonsUiState.addons.firstEnabledManifestError()
        val homescreenSettingsUiState by remember {
            HomeCatalogSettingsRepository.snapshot()
            HomeCatalogSettingsRepository.uiState
        }.collectAsStateWithLifecycle()
        val collections by CollectionRepository.collections.collectAsStateWithLifecycle()
        val metaScreenSettingsUiState by remember {
            MetaScreenSettingsRepository.ensureLoaded()
            MetaScreenSettingsRepository.uiState
        }.collectAsStateWithLifecycle()
        val continueWatchingPreferencesUiState by remember {
            ContinueWatchingPreferencesRepository.ensureLoaded()
            ContinueWatchingPreferencesRepository.uiState
        }.collectAsStateWithLifecycle()
        val posterCardStyleUiState by remember {
            PosterCardStyleRepository.ensureLoaded()
            PosterCardStyleRepository.uiState
        }.collectAsStateWithLifecycle()
        val episodeReleaseNotificationsUiState by remember {
            EpisodeReleaseNotificationsRepository.ensureLoaded()
            EpisodeReleaseNotificationsRepository.uiState
        }.collectAsStateWithLifecycle()
        val profileSettingsState by remember {
            ProfileRepository.state
        }.collectAsStateWithLifecycle()

        LaunchedEffect(homescreenCatalogRefreshKey) {
            val enabledAddons = addonsUiState.addons.enabledAddons()
            if (!enabledAddons.isWaitingForFirstEnabledManifest()) {
                HomeCatalogSettingsRepository.syncCatalogs(enabledAddons)
            }
        }

        LaunchedEffect(Unit) {
            CollectionRepository.initialize()
        }

        LaunchedEffect(collections) {
            HomeCatalogSettingsRepository.syncCollections(collections)
        }

        val initialPage = remember(initialPageName) {
            runCatching { SettingsPage.valueOf(initialPageName) }
                .getOrDefault(SettingsPage.Root)
                .takeIf { it.isEnabledByPolicy() }
                ?: SettingsPage.Root
        }
        var currentPage by rememberSaveable(initialPageName) { mutableStateOf(initialPage.name) }
        val scrollToTopRequests = remember { MutableSharedFlow<Unit>(extraBufferCapacity = 1) }
        val pageTitles = if (onNavigatePage != null) settingsPageTitles() else emptyMap()
        val page = remember(currentPage) {
            runCatching { SettingsPage.valueOf(currentPage) }
                .getOrDefault(SettingsPage.Root)
                .takeIf { it.isEnabledByPolicy() }
                ?: SettingsPage.Root
        }
        val previousPage = page.previousPage()

        fun inlineSettingsDestination(targetPage: SettingsPage): SettingsPage =
            if (isIos) targetPage else when (targetPage) {
                SettingsPage.MoriviaAppearance -> {
                    LayoutSections.appearanceOpen = true
                    SettingsPage.Appearance
                }
                SettingsPage.ContentDiscovery -> {
                    BrowsePlaybackSections.discoveryOpen = true
                    SettingsPage.BrowsePlayback
                }
                SettingsPage.Playback -> {
                    BrowsePlaybackSections.playbackOpen = true
                    SettingsPage.BrowsePlayback
                }
                else -> targetPage
            }

        fun openPage(targetPage: SettingsPage) {
            if (!targetPage.isEnabledByPolicy()) return
            val destination = inlineSettingsDestination(targetPage)
            val externalNavigator = onNavigatePage
            if (externalNavigator == null) {
                currentPage = destination.name
                return
            }
            if (destination == SettingsPage.Root && onExternalBack != null) {
                onExternalBack()
                return
            }
            externalNavigator(
                destination.name,
                pageTitles.getValue(destination),
            )
        }

        fun navigateBack() {
            if (page == SettingsPage.Root && onExternalBack != null) {
                onExternalBack()
                return
            }
            val parentPage = previousPage ?: return
            if (onNavigatePage != null && onExternalBack != null) {
                onExternalBack()
            } else {
                currentPage = inlineSettingsDestination(parentPage).name
            }
        }

        val openHomescreen = if (onNavigatePage != null) {
            { openPage(SettingsPage.Homescreen) }
        } else {
            onHomescreenClick
        }
        val openMetaScreen = if (onNavigatePage != null) {
            { openPage(SettingsPage.MetaScreen) }
        } else {
            onMetaScreenClick
        }
        val openContinueWatching = if (onNavigatePage != null) {
            { openPage(SettingsPage.ContinueWatching) }
        } else {
            onContinueWatchingClick
        }
        val openAddons = if (onNavigatePage != null) {
            { openPage(SettingsPage.Addons) }
        } else {
            onAddonsClick
        }
        val openPlugins = if (onNavigatePage != null) {
            { openPage(SettingsPage.Plugins) }
        } else {
            onPluginsClick
        }
        val openAccount = if (onNavigatePage != null) {
            { openPage(SettingsPage.Account) }
        } else {
            onAccountClick
        }
        val openSupportersContributors = if (onNavigatePage != null) {
            { openPage(SettingsPage.SupportersContributors) }
        } else {
            onSupportersContributorsClick
        }
        val openLicensesAttributions = if (onNavigatePage != null) {
            { openPage(SettingsPage.LicensesAttributions) }
        } else {
            onLicensesAttributionsClick
        }

        LaunchedEffect(page, currentPage) {
            if (page.name != currentPage) {
                currentPage = page.name
            }
        }

        ScreenActivityEffect(rootActionRequests, rootActionsEnabled, page) { active ->
            if (!active || !rootActionsEnabled) return@ScreenActivityEffect
            rootActionRequests.collect {
                val pageToOpen = page.previousPage()
                if (pageToOpen != null) {
                    navigateBack()
                } else {
                    scrollToTopRequests.tryEmit(Unit)
                }
            }
        }

        ScreenActivityEffect(requestedPageName, rootActionsEnabled) { active ->
            if (!active || !rootActionsEnabled) return@ScreenActivityEffect
            val requestedPage = requestedPageName ?: return@ScreenActivityEffect
            val targetPage = runCatching { SettingsPage.valueOf(requestedPage) }.getOrNull()
            if (targetPage == null || !targetPage.isEnabledByPolicy()) {
                onRequestedPageConsumed()
                return@ScreenActivityEffect
            }
            openPage(targetPage)
            onRequestedPageConsumed()
        }

        PlatformBackHandler(
            enabled = screenActive && (previousPage != null || onExternalBack != null) && (rootActionsEnabled || onExternalBack != null),
            onBack = ::navigateBack,
        )

        if (isSelectedTab || page == SettingsPage.Root) {
            pageStateHolder.SaveableStateProvider("content") {
                SettingsBackground(modifier = Modifier.fillMaxSize()) {
                if (maxWidth >= 768.dp) {
                    TabletSettingsScreen(
                        page = page,
                        scrollToTopRequests = scrollToTopRequests,
                        onPageChange = ::openPage,
                        onNavigateBack = ::navigateBack,
                        showRootBack = onExternalBack != null,
                        onExitSettings = onExternalBack,
                        onTabSelected = onTabSelected,
                        showInternalHeader = showInternalHeader,
                        rememberLastProfileEnabled = profileSettingsState.rememberLastProfileEnabled,
                        selectedTheme = selectedTheme,
                        onThemeSelected = ThemeSettingsRepository::setTheme,
                        amoledEnabled = amoledEnabled,
                        onAmoledToggle = ThemeSettingsRepository::setAmoled,
                        liquidGlassNativeTabBarSupported = liquidGlassNativeTabBarSupported,
                        appIconState = appIconState,
                        onAppIconSelected = onAppIconSelected,
                        onAppIconFailureDismissed = AppIconRepository::clearFailure,
                        selectedAppLanguage = selectedAppLanguage,
                        onAppLanguageSelected = ThemeSettingsRepository::setAppLanguage,
                        episodeReleaseNotificationsUiState = episodeReleaseNotificationsUiState,
                        tmdbSettings = tmdbSettings,
                        mdbListSettings = mdbListSettings,
                        debridSettings = debridSettings,
                        traktAuthUiState = traktAuthUiState,
                        simklAuthUiState = simklAuthUiState,
                        traktCommentsEnabled = traktCommentsEnabled,
                        mdbListConnected = mdbListConnected,
                        trackingSettingsUiState = trackingSettingsUiState,
                        homescreenHeroEnabled = homescreenSettingsUiState.heroEnabled,
                        homescreenShowCatalogType = homescreenSettingsUiState.showCatalogType,
                        homescreenHideUnreleasedContent = homescreenSettingsUiState.hideUnreleasedContent,
                        homescreenItems = homescreenSettingsUiState.items,
                        homescreenCatalogLoading = addonManifestsLoading,
                        homescreenCatalogErrorMessage = addonManifestErrorMessage,
                        metaScreenSettingsUiState = metaScreenSettingsUiState,
                        continueWatchingPreferencesUiState = continueWatchingPreferencesUiState,
                        posterCardStyleUiState = posterCardStyleUiState,
                        onSwitchProfile = onSwitchProfile,
                        onSupportersContributorsClick = openSupportersContributors,
                        onLicensesAttributionsClick = openLicensesAttributions,
                        onCheckForUpdatesClick = onCheckForUpdatesClick,
                        onTestUpdateBannerClick = onTestUpdateBannerClick,
                        onCollectionsClick = onCollectionsClick,
                        onWatchPartyClick = onWatchPartyClick,
                    )
                } else {
                    MobileSettingsScreen(
                        page = page,
                        scrollToTopRequests = scrollToTopRequests,
                        onPageChange = ::openPage,
                        onNavigateBack = ::navigateBack,
                        showRootBack = onExternalBack != null,
                        showInternalHeader = showInternalHeader,
                        rememberLastProfileEnabled = profileSettingsState.rememberLastProfileEnabled,
                        selectedTheme = selectedTheme,
                        onThemeSelected = ThemeSettingsRepository::setTheme,
                        amoledEnabled = amoledEnabled,
                        onAmoledToggle = ThemeSettingsRepository::setAmoled,
                        liquidGlassNativeTabBarSupported = liquidGlassNativeTabBarSupported,
                        appIconState = appIconState,
                        onAppIconSelected = onAppIconSelected,
                        onAppIconFailureDismissed = AppIconRepository::clearFailure,
                        selectedAppLanguage = selectedAppLanguage,
                        onAppLanguageSelected = ThemeSettingsRepository::setAppLanguage,
                        episodeReleaseNotificationsUiState = episodeReleaseNotificationsUiState,
                        tmdbSettings = tmdbSettings,
                        mdbListSettings = mdbListSettings,
                        debridSettings = debridSettings,
                        traktAuthUiState = traktAuthUiState,
                        simklAuthUiState = simklAuthUiState,
                        traktCommentsEnabled = traktCommentsEnabled,
                        mdbListConnected = mdbListConnected,
                        trackingSettingsUiState = trackingSettingsUiState,
                        homescreenHeroEnabled = homescreenSettingsUiState.heroEnabled,
                        homescreenShowCatalogType = homescreenSettingsUiState.showCatalogType,
                        homescreenHideUnreleasedContent = homescreenSettingsUiState.hideUnreleasedContent,
                        homescreenItems = homescreenSettingsUiState.items,
                        homescreenCatalogLoading = addonManifestsLoading,
                        homescreenCatalogErrorMessage = addonManifestErrorMessage,
                        metaScreenSettingsUiState = metaScreenSettingsUiState,
                        continueWatchingPreferencesUiState = continueWatchingPreferencesUiState,
                        posterCardStyleUiState = posterCardStyleUiState,
                        onSwitchProfile = onSwitchProfile,
                        onHomescreenClick = openHomescreen,
                        onMetaScreenClick = openMetaScreen,
                        onContinueWatchingClick = openContinueWatching,
                        onAddonsClick = openAddons,
                        onPluginsClick = openPlugins,
                        onAccountClick = openAccount,
                        onSupportersContributorsClick = openSupportersContributors,
                        onLicensesAttributionsClick = openLicensesAttributions,
                        onCheckForUpdatesClick = onCheckForUpdatesClick,
                        onTestUpdateBannerClick = onTestUpdateBannerClick,
                        onCollectionsClick = onCollectionsClick,
                        onWatchPartyClick = onWatchPartyClick,
                    )
                }
                }
            }
        }
    }
}

@Composable
private fun MobileSettingsScreen(
    page: SettingsPage,
    scrollToTopRequests: Flow<Unit>,
    onPageChange: (SettingsPage) -> Unit,
    onNavigateBack: () -> Unit,
    showRootBack: Boolean = false,
    showInternalHeader: Boolean,
    rememberLastProfileEnabled: Boolean,
    selectedTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    amoledEnabled: Boolean,
    onAmoledToggle: (Boolean) -> Unit,
    liquidGlassNativeTabBarSupported: Boolean,
    appIconState: AppIconSettingsState,
    onAppIconSelected: (AppIconOption) -> Unit,
    onAppIconFailureDismissed: () -> Unit,
    selectedAppLanguage: AppLanguage,
    onAppLanguageSelected: (AppLanguage) -> Unit,
    episodeReleaseNotificationsUiState: EpisodeReleaseNotificationsUiState,
    tmdbSettings: TmdbSettings,
    mdbListSettings: MdbListSettings,
    debridSettings: DebridSettings,
    traktAuthUiState: TraktAuthUiState,
    simklAuthUiState: SimklAuthUiState,
    traktCommentsEnabled: Boolean,
    mdbListConnected: Boolean,
    trackingSettingsUiState: TrackingSettingsUiState,
    homescreenHeroEnabled: Boolean,
    homescreenShowCatalogType: Boolean,
    homescreenHideUnreleasedContent: Boolean,
    homescreenItems: List<HomeCatalogSettingsItem>,
    homescreenCatalogLoading: Boolean,
    homescreenCatalogErrorMessage: String?,
    metaScreenSettingsUiState: MetaScreenSettingsUiState,
    continueWatchingPreferencesUiState: ContinueWatchingPreferencesUiState,
    posterCardStyleUiState: PosterCardStyleUiState,
    onSwitchProfile: (() -> Unit)? = null,
    onHomescreenClick: () -> Unit = {},
    onMetaScreenClick: () -> Unit = {},
    onContinueWatchingClick: () -> Unit = {},
    onAddonsClick: () -> Unit = {},
    onPluginsClick: () -> Unit = {},
    onAccountClick: () -> Unit = {},
    onSupportersContributorsClick: () -> Unit = {},
    onLicensesAttributionsClick: () -> Unit = {},
    onCheckForUpdatesClick: (() -> Unit)? = null,
    onTestUpdateBannerClick: (() -> Unit)? = null,
    onCollectionsClick: () -> Unit = {},
    onWatchPartyClick: () -> Unit = {},
) {
    val saveableStateHolder = rememberSaveableStateHolder()
    saveableStateHolder.SaveableStateProvider(page.name) {
        var settingsSearchQuery by rememberSaveable { mutableStateOf("") }
        var rootSearchVisible by rememberSaveable { mutableStateOf(true) }
        val searchVisibility = rememberSettingsSearchVisibility(rootSearchVisible || (isIos && settingsSearchQuery.isNotBlank()))
        val listState = rememberLazyListState()
        val catalogRowsState = if (!isIos && page == SettingsPage.PosterCustomization) {
            rememberHomeCatalogRowsState(listState)
        } else {
            null
        }
        ScreenActivityEffect(listState) { screenActive ->
            if (!screenActive) listState.stopScroll()
        }
        val rootSearchRevealConnection = rememberSettingsSearchScrollConnection(
            listState = listState,
            visible = rootSearchVisible,
            enabled = page == SettingsPage.Root,
        ) { visible ->
            rootSearchVisible = visible
        }
        fun openSearchTarget(target: SettingsSearchTarget) {
            when (target) {
                is SettingsSearchTarget.Page -> when (target.page) {
                    SettingsPage.Account -> onAccountClick()
                    SettingsPage.SupportersContributors -> {
                        if (AppFeaturePolicy.supportersContributorsPageEnabled) {
                            onSupportersContributorsClick()
                        }
                    }
                    SettingsPage.LicensesAttributions -> onLicensesAttributionsClick()
                    SettingsPage.ContinueWatching -> onContinueWatchingClick()
                    SettingsPage.Addons -> onAddonsClick()
                    SettingsPage.Plugins -> {
                        if (AppFeaturePolicy.pluginsEnabled) {
                            onPluginsClick()
                        }
                    }
                    SettingsPage.Homescreen -> onHomescreenClick()
                    SettingsPage.MetaScreen -> onMetaScreenClick()
                    else -> onPageChange(target.page)
                }
                SettingsSearchTarget.Collections -> onCollectionsClick()
                SettingsSearchTarget.SwitchProfile -> onSwitchProfile?.invoke()
                SettingsSearchTarget.CheckForUpdates -> onCheckForUpdatesClick?.invoke()
            }
        }

        LaunchedEffect(scrollToTopRequests) {
            scrollToTopRequests.collect {
                listState.animateScrollToItem(0)
            }
        }

        val watchPartyRowClick = watchPartyRow(onWatchPartyClick)
        MoriviaScreen(
            modifier = Modifier.nestedScroll(rootSearchRevealConnection),
            listState = listState,
            // On the Settings start page the space under the version matches the space above the
            // search field (owner, 2026-11-23).
            bottomPadding = if (page == SettingsPage.Root) com.morivia.app.core.ui.moriviaSafeBottomPadding(22.dp) else null,
        ) {
            if (showInternalHeader) {
                stickyHeader {
                    val previousPage = page.previousPage()
                    MoriviaScreenHeader(
                        title = stringResource(page.titleRes),
                        onBack = if (previousPage != null || showRootBack) onNavigateBack else null,
                    )
                }
            } else {
                item { Spacer(modifier = Modifier.height(44.dp)) }
            }

            when (page) {
                SettingsPage.Root -> {
                    settingsSearchRootContent(
                        query = settingsSearchQuery,
                        entries = {
                            settingsSearchEntries(
                                isTablet = false,
                                pluginsEnabled = AppFeaturePolicy.pluginsEnabled,
                                supportersContributorsPageEnabled = AppFeaturePolicy.supportersContributorsPageEnabled,
                                accountDeletionEnabled = AppFeaturePolicy.accountDeletionEnabled,
                                personalMediaAddonCopyEnabled = AppFeaturePolicy.personalMediaAddonCopyEnabled,
                                liquidGlassNativeTabBarSupported = liquidGlassNativeTabBarSupported,
                                switchProfileAvailable = false,
                                checkForUpdatesAvailable = onCheckForUpdatesClick != null,
                            )
                        },
                        isTablet = false,
                        searchVisibility = searchVisibility,
                        onQueryChange = { settingsSearchQuery = it },
                        onTargetClick = { openSearchTarget(it) },
                    )
                    if (settingsSearchQuery.isBlank()) {
                        settingsRootContent(
                            isTablet = false,
                            onPageClick = { openSearchTarget(SettingsSearchTarget.Page(it)) },
                            onSwitchProfileClick = onSwitchProfile,
                            onWatchPartyClick = watchPartyRowClick,
                        )
                    }
                }
                SettingsPage.Account -> accountSettingsContent(
                    isTablet = false,
                )
                SettingsPage.SupportersContributors -> {
                    if (AppFeaturePolicy.supportersContributorsPageEnabled) {
                        supportersContributorsContent(isTablet = false)
                    }
                }
                SettingsPage.LicensesAttributions -> licensesAttributionsContent(
                    isTablet = false,
                )
                SettingsPage.Playback,
                SettingsPage.PlaybackPlayer,
                SettingsPage.PlaybackStreamSelection,
                SettingsPage.PlaybackUpNext,
                SettingsPage.PlaybackSkipSegments,
                SettingsPage.PlaybackInterface,
                SettingsPage.PlaybackAudio,
                SettingsPage.PlaybackSubtitles,
                SettingsPage.PlaybackVideo,
                SettingsPage.PlaybackBuffer,
                SettingsPage.PlaybackP2p -> playbackSettingsContent(
                    isTablet = false,
                    page = page,
                    onPageChange = onPageChange,
                )
                SettingsPage.Streams -> streamsSettingsContent(
                    isTablet = false,
                )
                SettingsPage.Appearance, SettingsPage.MoriviaAppearance -> {
                    val appearanceContent: LazyListScope.() -> Unit = {
                        appearanceSettingsContent(
                            isTablet = false,
                            selectedTheme = selectedTheme,
                            onThemeSelected = onThemeSelected,
                            amoledEnabled = amoledEnabled,
                            onAmoledToggle = onAmoledToggle,
                            appIconState = appIconState,
                            onAppIconSelected = onAppIconSelected,
                            onAppIconFailureDismissed = onAppIconFailureDismissed,
                            selectedAppLanguage = selectedAppLanguage,
                            onAppLanguageSelected = onAppLanguageSelected,
                        )
                    }
                    if (isIos || page == SettingsPage.MoriviaAppearance) {
                        appearanceContent()
                    } else {
                        expandableLayoutSettingsContent(
                            isTablet = false,
                            appearanceContent = appearanceContent,
                        ) {
                            layoutSettingsContent(
                                isTablet = false,
                                liquidGlassNativeTabBarSupported = liquidGlassNativeTabBarSupported,
                                onMetaScreenClick = { onPageChange(SettingsPage.MetaScreen) },
                                onStreamsClick = { onPageChange(SettingsPage.Streams) },
                                onCollectionsClick = onCollectionsClick,
                                onContinueWatchingClick = { onPageChange(SettingsPage.ContinueWatching) },
                                onPosterCustomizationClick = { onPageChange(SettingsPage.PosterCustomization) },
                                onMoriviaPageClick = { onPageChange(it) },
                            )
                        }
                    }
                }
                SettingsPage.MoriviaHero -> moriviaHeroSettingsContent(isTablet = false)
                SettingsPage.MoriviaHomeCatalogs -> moriviaHomeCatalogsSettingsContent(
                    isTablet = false,
                    onPosterCustomizationClick = { onPageChange(SettingsPage.PosterCustomization) },
                )
                SettingsPage.MoriviaHome -> moriviaHomeSettingsContent(
                    isTablet = false,
                    onCollectionsClick = onCollectionsClick,
                )
                SettingsPage.Layout -> layoutSettingsContent(
                    isTablet = false,
                    liquidGlassNativeTabBarSupported = liquidGlassNativeTabBarSupported,
                    onMetaScreenClick = onMetaScreenClick,
                    onStreamsClick = { onPageChange(SettingsPage.Streams) },
                    onCollectionsClick = onCollectionsClick,
                    onContinueWatchingClick = onContinueWatchingClick,
                    onPosterCustomizationClick = { onPageChange(SettingsPage.PosterCustomization) },
                    onMoriviaPageClick = { onPageChange(it) },
                )
                SettingsPage.About -> aboutSettingsContent(
                    isTablet = false,
                    onSupportersContributorsClick = onSupportersContributorsClick,
                    onLicensesAttributionsClick = onLicensesAttributionsClick,
                    onCheckForUpdatesClick = onCheckForUpdatesClick,
                    onTestUpdateBannerClick = onTestUpdateBannerClick,
                    showSupportersContributorsPage = AppFeaturePolicy.supportersContributorsPageEnabled,
                )
                SettingsPage.Advanced -> advancedSettingsContent(
                    isTablet = false,
                    rememberLastProfileEnabled = rememberLastProfileEnabled,
                )
                SettingsPage.Notifications -> notificationsSettingsContent(
                    isTablet = false,
                    uiState = episodeReleaseNotificationsUiState,
                )
                SettingsPage.SystemSettings -> systemSettingsContent(
                    isTablet = false,
                    episodeReleaseNotificationsUiState = episodeReleaseNotificationsUiState,
                    rememberLastProfileEnabled = rememberLastProfileEnabled,
                    onSupportersContributorsClick = onSupportersContributorsClick,
                    onLicensesAttributionsClick = onLicensesAttributionsClick,
                    onCheckForUpdatesClick = onCheckForUpdatesClick,
                    onTestUpdateBannerClick = onTestUpdateBannerClick,
                    showSupportersContributorsPage = AppFeaturePolicy.supportersContributorsPageEnabled,
                )
                SettingsPage.ContinueWatching -> continueWatchingSettingsContent(
                    isTablet = false,
                    isVisible = continueWatchingPreferencesUiState.isVisible,
                    style = continueWatchingPreferencesUiState.style,
                    upNextFromFurthestEpisode = continueWatchingPreferencesUiState.upNextFromFurthestEpisode,
                    useEpisodeThumbnails = continueWatchingPreferencesUiState.useEpisodeThumbnails,
                    showUnairedNextUp = continueWatchingPreferencesUiState.showUnairedNextUp,
                    blurNextUp = continueWatchingPreferencesUiState.blurNextUp,
                    showResumePromptOnLaunch = continueWatchingPreferencesUiState.showResumePromptOnLaunch,
                    sortMode = continueWatchingPreferencesUiState.sortMode,
                    clickAction = continueWatchingPreferencesUiState.clickAction,
                )
                SettingsPage.PosterCustomization -> posterCustomizationSettingsContent(
                    isTablet = false,
                    uiState = posterCardStyleUiState,
                    catalogRowsState = catalogRowsState,
                )
                SettingsPage.BrowsePlayback -> browsePlaybackSettingsContent(
                    isTablet = false,
                    onAddonsClick = onAddonsClick,
                    onPluginsClick = onPluginsClick,
                    onPageChange = onPageChange,
                )
                SettingsPage.ContentDiscovery -> contentDiscoveryContent(
                    isTablet = false,
                    showPluginsEntry = AppFeaturePolicy.pluginsEnabled,
                    onAddonsClick = onAddonsClick,
                    onPluginsClick = onPluginsClick,
                    onMediaServersClick = { onPageChange(SettingsPage.MediaServers) },
                )
                SettingsPage.Addons -> addonsSettingsContent()
                SettingsPage.MediaServers -> mediaServersSettingsContent(
                    isTablet = false,
                    onServerClick = { onPageChange(SettingsPage.MediaServer) },
                )
                SettingsPage.MediaServer -> mediaServerSettingsContent(
                    isTablet = false,
                    onBack = onNavigateBack,
                )
                SettingsPage.Plugins -> if (AppFeaturePolicy.pluginsEnabled) pluginsSettingsContent() else addonsSettingsContent()
                SettingsPage.MoriviaHomeLayout -> moriviaHomeLayoutContent(
                    isTablet = false,
                    onHeroSourceClick = { onHomescreenClick() },
                    onHomeRowsClick = { onHomescreenClick() },
                    onCollectionsClick = onCollectionsClick,
                )
                SettingsPage.MoriviaSearch -> moriviaSearchSettingsContent(isTablet = false)
                SettingsPage.MoriviaLibrary -> moriviaLibrarySettingsContent(isTablet = false)
                SettingsPage.Homescreen -> homescreenSettingsContent(
                    isTablet = false,
                    heroEnabled = homescreenHeroEnabled,
                    showCatalogType = homescreenShowCatalogType,
                    hideUnreleasedContent = homescreenHideUnreleasedContent,
                    items = homescreenItems,
                    isCatalogLoading = homescreenCatalogLoading,
                    catalogErrorMessage = homescreenCatalogErrorMessage,
                )
                SettingsPage.MetaScreen -> metaScreenSettingsContent(
                    isTablet = false,
                    uiState = metaScreenSettingsUiState,
                )
                // "Connections": two cards, Integrations and Tracking.
                SettingsPage.Integrations -> connectionsContent(
                    isTablet = false,
                    debridSettings = debridSettings,
                    onTmdbClick = { onPageChange(SettingsPage.TmdbEnrichment) },
                    onMdbListClick = { onPageChange(SettingsPage.MdbListRatings) },
                    traktUiState = traktAuthUiState,
                    simklUiState = simklAuthUiState,
                    trackingSettingsUiState = trackingSettingsUiState,
                    commentsEnabled = traktCommentsEnabled,
                    onCommentsEnabledChange = TraktCommentsSettings::setEnabled,
                    mdbListConnected = mdbListConnected,
                )
                SettingsPage.TraktAuthentication -> trackingSettingsContent(
                    isTablet = false,
                    traktUiState = traktAuthUiState,
                    simklUiState = simklAuthUiState,
                    settingsUiState = trackingSettingsUiState,
                    commentsEnabled = traktCommentsEnabled,
                    mdbListConnected = mdbListConnected,
                    onCommentsEnabledChange = TraktCommentsSettings::setEnabled,
                    accountsSectionTitle = Res.string.compose_settings_page_tracking,
                )
                SettingsPage.TmdbEnrichment -> tmdbSettingsContent(
                    isTablet = false,
                    settings = tmdbSettings,
                )
                SettingsPage.MdbListRatings -> mdbListSettingsContent(
                    isTablet = false,
                    settings = mdbListSettings,
                )
                SettingsPage.Debrid -> debridSettingsContent(
                    isTablet = false,
                    settings = debridSettings,
                    onTmdbClick = { onPageChange(SettingsPage.TmdbEnrichment) },
                    onMdbListClick = { onPageChange(SettingsPage.MdbListRatings) },
                )
            }
        }
    }
}

@Composable
private fun TabletSettingsScreen(
    page: SettingsPage,
    scrollToTopRequests: Flow<Unit>,
    onPageChange: (SettingsPage) -> Unit,
    onNavigateBack: () -> Unit,
    showRootBack: Boolean = false,
    onExitSettings: (() -> Unit)? = null,
    onTabSelected: ((com.morivia.app.AppScreenTab) -> Unit)? = null,
    showInternalHeader: Boolean,
    rememberLastProfileEnabled: Boolean,
    selectedTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    amoledEnabled: Boolean,
    onAmoledToggle: (Boolean) -> Unit,
    liquidGlassNativeTabBarSupported: Boolean,
    appIconState: AppIconSettingsState,
    onAppIconSelected: (AppIconOption) -> Unit,
    onAppIconFailureDismissed: () -> Unit,
    selectedAppLanguage: AppLanguage,
    onAppLanguageSelected: (AppLanguage) -> Unit,
    episodeReleaseNotificationsUiState: EpisodeReleaseNotificationsUiState,
    tmdbSettings: TmdbSettings,
    mdbListSettings: MdbListSettings,
    debridSettings: DebridSettings,
    traktAuthUiState: TraktAuthUiState,
    simklAuthUiState: SimklAuthUiState,
    traktCommentsEnabled: Boolean,
    mdbListConnected: Boolean,
    trackingSettingsUiState: TrackingSettingsUiState,
    homescreenHeroEnabled: Boolean,
    homescreenShowCatalogType: Boolean,
    homescreenHideUnreleasedContent: Boolean,
    homescreenItems: List<HomeCatalogSettingsItem>,
    homescreenCatalogLoading: Boolean,
    homescreenCatalogErrorMessage: String?,
    metaScreenSettingsUiState: MetaScreenSettingsUiState,
    continueWatchingPreferencesUiState: ContinueWatchingPreferencesUiState,
    posterCardStyleUiState: PosterCardStyleUiState,
    onSwitchProfile: (() -> Unit)? = null,
    onSupportersContributorsClick: () -> Unit = {},
    onLicensesAttributionsClick: () -> Unit = {},
    onCheckForUpdatesClick: (() -> Unit)? = null,
    onTestUpdateBannerClick: (() -> Unit)? = null,
    onCollectionsClick: () -> Unit = {},
    onWatchPartyClick: () -> Unit = {},
) {
    var selectedCategory by rememberSaveable { mutableStateOf(SettingsCategory.Look.name) }
    val activeCategory = settingsCategoryForName(selectedCategory)
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    // Landscape: the sidebar back arrow sits as far below the status bar clock as from the
    // left edge (measured on the owner's phone: 114 px both ways).
    val topOffset = statusBarPadding + 5.dp

    LaunchedEffect(page) {
        if (page.opensInlineOnTablet) {
            selectedCategory = page.category.name
        }
    }

    fun openInlinePage(page: SettingsPage) {
        selectedCategory = page.category.name
        onPageChange(page)
    }

    val saveableStateHolder = rememberSaveableStateHolder()

    Row(modifier = Modifier.fillMaxSize()) {
        // Landscape: navigation bar on the left (Settings only), one background colour across
        // the screen (black with AMOLED, #0E0C0E without) - owner's request, 2026-11-22.
        if (onTabSelected != null) {
            Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                SettingsNavigationRail(onTabSelected = onTabSelected)
            }
        }
        Surface(
            modifier = Modifier
                .width(344.dp)
                .fillMaxSize(),
            color = Color.Transparent,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = topOffset),
            ) {
                // Landscape sidebar (owner's request, 2026-11-22): a back arrow next to Settings,
                // no divider line, and our own setting cards instead of the category names.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp)
                        .padding(bottom = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.compose_settings_page_root),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }

                SettingsSidebarList(
                    onPageClick = ::openInlinePage,
                    onSwitchProfile = onSwitchProfile,
                    onWatchPartyClick = watchPartyRow(onWatchPartyClick),
                    onCollectionsClick = onCollectionsClick,
                    onCheckForUpdatesClick = onCheckForUpdatesClick,
                    liquidGlassNativeTabBarSupported = liquidGlassNativeTabBarSupported,
                )
            }
        }

        saveableStateHolder.SaveableStateProvider(page.name) {
            val listState = rememberLazyListState()
            val catalogRowsState = if (!isIos && page == SettingsPage.PosterCustomization) {
                rememberHomeCatalogRowsState(listState)
            } else {
                null
            }
            ScreenActivityEffect(listState) { screenActive ->
                if (!screenActive) listState.stopScroll()
            }
            val bottomOverlayPadding = LocalMoriviaBottomNavigationOverlayPadding.current
            LaunchedEffect(scrollToTopRequests) {
                scrollToTopRequests.collect {
                    listState.animateScrollToItem(0)
                }
            }
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 40.dp,
                    top = topOffset,
                    end = 40.dp,
                    bottom = 40.dp + bottomOverlayPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                if (showInternalHeader && page != SettingsPage.Root) {
                    // "Settings" and the exit arrow live only in the sidebar; a page shows its own
                    // back arrow only inside a card (e.g. Tracking back to Connections).
                    item {
                        val previousPage = page.previousPage()
                        TabletPageHeader(
                            title = stringResource(page.titleRes),
                            showBack = previousPage != null && previousPage != SettingsPage.Root,
                            onBack = onNavigateBack,
                        )
                    }
                }
                when (page) {
                    SettingsPage.Root -> {
                        // Landscape: the Settings list and search live in the sidebar; the right
                        // side stays empty until a card is opened (owner's request, 2026-11-22).
                    }
                    SettingsPage.Account -> accountSettingsContent(
                        isTablet = true,
                    )
                    SettingsPage.SupportersContributors -> {
                        if (AppFeaturePolicy.supportersContributorsPageEnabled) {
                            supportersContributorsContent(isTablet = true)
                        }
                    }
                    SettingsPage.LicensesAttributions -> licensesAttributionsContent(
                        isTablet = true,
                    )
                    SettingsPage.Playback,
                    SettingsPage.PlaybackPlayer,
                    SettingsPage.PlaybackStreamSelection,
                    SettingsPage.PlaybackUpNext,
                    SettingsPage.PlaybackSkipSegments,
                    SettingsPage.PlaybackInterface,
                    SettingsPage.PlaybackAudio,
                    SettingsPage.PlaybackSubtitles,
                    SettingsPage.PlaybackVideo,
                    SettingsPage.PlaybackBuffer,
                    SettingsPage.PlaybackP2p -> playbackSettingsContent(
                        isTablet = true,
                        page = page,
                        onPageChange = ::openInlinePage,
                    )
                    SettingsPage.Streams -> streamsSettingsContent(
                        isTablet = true,
                    )
                    SettingsPage.Appearance, SettingsPage.MoriviaAppearance -> {
                        val appearanceContent: LazyListScope.() -> Unit = {
                            appearanceSettingsContent(
                                isTablet = true,
                                selectedTheme = selectedTheme,
                                onThemeSelected = onThemeSelected,
                                amoledEnabled = amoledEnabled,
                                onAmoledToggle = onAmoledToggle,
                                appIconState = appIconState,
                                onAppIconSelected = onAppIconSelected,
                                onAppIconFailureDismissed = onAppIconFailureDismissed,
                                selectedAppLanguage = selectedAppLanguage,
                                onAppLanguageSelected = onAppLanguageSelected,
                            )
                        }
                        if (isIos || page == SettingsPage.MoriviaAppearance) {
                            appearanceContent()
                        } else {
                            expandableLayoutSettingsContent(
                                isTablet = true,
                                appearanceContent = appearanceContent,
                            ) {
                                layoutSettingsContent(
                                    isTablet = true,
                                    liquidGlassNativeTabBarSupported = liquidGlassNativeTabBarSupported,
                                    onMetaScreenClick = { openInlinePage(SettingsPage.MetaScreen) },
                                    onStreamsClick = { openInlinePage(SettingsPage.Streams) },
                                    onCollectionsClick = onCollectionsClick,
                                    onContinueWatchingClick = { openInlinePage(SettingsPage.ContinueWatching) },
                                    onPosterCustomizationClick = { openInlinePage(SettingsPage.PosterCustomization) },
                                    onMoriviaPageClick = { openInlinePage(it) },
                                )
                            }
                        }
                    }
                    SettingsPage.MoriviaHero -> moriviaHeroSettingsContent(isTablet = true)
                    SettingsPage.MoriviaHomeCatalogs -> moriviaHomeCatalogsSettingsContent(
                        isTablet = true,
                        onPosterCustomizationClick = { openInlinePage(SettingsPage.PosterCustomization) },
                    )
                    SettingsPage.MoriviaHome -> moriviaHomeSettingsContent(
                        isTablet = true,
                        onCollectionsClick = onCollectionsClick,
                    )
                    SettingsPage.Layout -> layoutSettingsContent(
                        isTablet = true,
                        liquidGlassNativeTabBarSupported = liquidGlassNativeTabBarSupported,
                        onMetaScreenClick = { openInlinePage(SettingsPage.MetaScreen) },
                        onStreamsClick = { openInlinePage(SettingsPage.Streams) },
                        onCollectionsClick = onCollectionsClick,
                        onContinueWatchingClick = { openInlinePage(SettingsPage.ContinueWatching) },
                        onPosterCustomizationClick = { openInlinePage(SettingsPage.PosterCustomization) },
                        onMoriviaPageClick = { openInlinePage(it) },
                    )
                    SettingsPage.About -> aboutSettingsContent(
                        isTablet = true,
                        onSupportersContributorsClick = { openInlinePage(SettingsPage.SupportersContributors) },
                        onLicensesAttributionsClick = { openInlinePage(SettingsPage.LicensesAttributions) },
                        onCheckForUpdatesClick = onCheckForUpdatesClick,
                        onTestUpdateBannerClick = onTestUpdateBannerClick,
                        showSupportersContributorsPage = AppFeaturePolicy.supportersContributorsPageEnabled,
                    )
                    SettingsPage.Advanced -> advancedSettingsContent(
                        isTablet = true,
                        rememberLastProfileEnabled = rememberLastProfileEnabled,
                    )
                    SettingsPage.Notifications -> notificationsSettingsContent(
                        isTablet = true,
                        uiState = episodeReleaseNotificationsUiState,
                    )
                    SettingsPage.SystemSettings -> systemSettingsContent(
                        isTablet = true,
                        episodeReleaseNotificationsUiState = episodeReleaseNotificationsUiState,
                        rememberLastProfileEnabled = rememberLastProfileEnabled,
                        onSupportersContributorsClick = { openInlinePage(SettingsPage.SupportersContributors) },
                        onLicensesAttributionsClick = { openInlinePage(SettingsPage.LicensesAttributions) },
                        onCheckForUpdatesClick = onCheckForUpdatesClick,
                        onTestUpdateBannerClick = onTestUpdateBannerClick,
                        showSupportersContributorsPage = AppFeaturePolicy.supportersContributorsPageEnabled,
                    )
                    SettingsPage.ContinueWatching -> continueWatchingSettingsContent(
                        isTablet = true,
                        isVisible = continueWatchingPreferencesUiState.isVisible,
                        style = continueWatchingPreferencesUiState.style,
                        upNextFromFurthestEpisode = continueWatchingPreferencesUiState.upNextFromFurthestEpisode,
                        useEpisodeThumbnails = continueWatchingPreferencesUiState.useEpisodeThumbnails,
                        showUnairedNextUp = continueWatchingPreferencesUiState.showUnairedNextUp,
                        blurNextUp = continueWatchingPreferencesUiState.blurNextUp,
                        showResumePromptOnLaunch = continueWatchingPreferencesUiState.showResumePromptOnLaunch,
                        sortMode = continueWatchingPreferencesUiState.sortMode,
                        clickAction = continueWatchingPreferencesUiState.clickAction,
                    )
                    SettingsPage.PosterCustomization -> posterCustomizationSettingsContent(
                        isTablet = true,
                        uiState = posterCardStyleUiState,
                        catalogRowsState = catalogRowsState,
                    )
                    SettingsPage.BrowsePlayback -> browsePlaybackSettingsContent(
                        isTablet = true,
                        onAddonsClick = { openInlinePage(SettingsPage.Addons) },
                        onPluginsClick = { openInlinePage(SettingsPage.Plugins) },
                        onPageChange = ::openInlinePage,
                    )
                    SettingsPage.ContentDiscovery -> contentDiscoveryContent(
                        isTablet = true,
                        showPluginsEntry = AppFeaturePolicy.pluginsEnabled,
                        onAddonsClick = { openInlinePage(SettingsPage.Addons) },
                        onPluginsClick = { openInlinePage(SettingsPage.Plugins) },
                        onMediaServersClick = { openInlinePage(SettingsPage.MediaServers) },
                    )
                    SettingsPage.Addons -> addonsSettingsContent()
                    SettingsPage.MediaServers -> mediaServersSettingsContent(
                        isTablet = true,
                        onServerClick = { openInlinePage(SettingsPage.MediaServer) },
                    )
                    SettingsPage.MediaServer -> mediaServerSettingsContent(
                        isTablet = true,
                        onBack = onNavigateBack,
                    )
                    SettingsPage.Plugins -> if (AppFeaturePolicy.pluginsEnabled) pluginsSettingsContent() else addonsSettingsContent()
                    SettingsPage.MoriviaHomeLayout -> moriviaHomeLayoutContent(
                        isTablet = true,
                        onHeroSourceClick = { openInlinePage(SettingsPage.Homescreen) },
                        onHomeRowsClick = { openInlinePage(SettingsPage.Homescreen) },
                        onCollectionsClick = onCollectionsClick,
                    )
                    SettingsPage.MoriviaSearch -> moriviaSearchSettingsContent(isTablet = true)
                    SettingsPage.MoriviaLibrary -> moriviaLibrarySettingsContent(isTablet = true)
                    SettingsPage.Homescreen -> homescreenSettingsContent(
                        isTablet = true,
                        heroEnabled = homescreenHeroEnabled,
                        showCatalogType = homescreenShowCatalogType,
                        hideUnreleasedContent = homescreenHideUnreleasedContent,
                        items = homescreenItems,
                        isCatalogLoading = homescreenCatalogLoading,
                        catalogErrorMessage = homescreenCatalogErrorMessage,
                    )
                    SettingsPage.MetaScreen -> metaScreenSettingsContent(
                        isTablet = true,
                        uiState = metaScreenSettingsUiState,
                    )
                    SettingsPage.Integrations -> connectionsContent(
                        isTablet = true,
                        debridSettings = debridSettings,
                        onTmdbClick = { onPageChange(SettingsPage.TmdbEnrichment) },
                        onMdbListClick = { onPageChange(SettingsPage.MdbListRatings) },
                        traktUiState = traktAuthUiState,
                        simklUiState = simklAuthUiState,
                        trackingSettingsUiState = trackingSettingsUiState,
                        commentsEnabled = traktCommentsEnabled,
                        onCommentsEnabledChange = TraktCommentsSettings::setEnabled,
                        mdbListConnected = mdbListConnected,
                    )
                    SettingsPage.TraktAuthentication -> trackingSettingsContent(
                        isTablet = true,
                        traktUiState = traktAuthUiState,
                        simklUiState = simklAuthUiState,
                        settingsUiState = trackingSettingsUiState,
                        commentsEnabled = traktCommentsEnabled,
                        mdbListConnected = mdbListConnected,
                        onCommentsEnabledChange = TraktCommentsSettings::setEnabled,
                        accountsSectionTitle = Res.string.compose_settings_page_tracking,
                    )
                    SettingsPage.TmdbEnrichment -> tmdbSettingsContent(
                        isTablet = true,
                        settings = tmdbSettings,
                    )
                    SettingsPage.MdbListRatings -> mdbListSettingsContent(
                        isTablet = true,
                        settings = mdbListSettings,
                    )
                    SettingsPage.Debrid -> debridSettingsContent(
                        isTablet = true,
                        settings = debridSettings,
                        onTmdbClick = { onPageChange(SettingsPage.TmdbEnrichment) },
                        onMdbListClick = { onPageChange(SettingsPage.MdbListRatings) },
                    )
                }
            }
        }
    }
}
