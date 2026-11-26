package com.morivia.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.morivia.app.core.ui.AmoledBlackBackground
import com.morivia.app.core.ui.DisintegrationRequest
import com.morivia.app.core.ui.MoriviaLoadingIndicator
import com.morivia.app.core.ui.MoriviaTokens
import com.morivia.app.core.ui.nuvio
import com.morivia.app.features.cloud.CloudLibraryContentType
import com.morivia.app.features.cloud.CloudLibraryFile
import com.morivia.app.features.cloud.CloudLibraryItem
import com.morivia.app.features.home.HomeCatalogSection
import com.morivia.app.features.home.HomeScreen
import com.morivia.app.features.home.MetaPreview
import com.morivia.app.features.library.LibraryItem
import com.morivia.app.features.library.LibraryScreen
import com.morivia.app.features.library.LibrarySection
import com.morivia.app.features.library.LibrarySortOption
import com.morivia.app.features.profiles.MoriviaProfile
import com.morivia.app.features.profiles.ProfileBackgroundBackdrop
import com.morivia.app.features.search.SearchScreen
import com.morivia.app.features.settings.AppBrandWordmark
import com.morivia.app.features.settings.SettingsScreen
import com.morivia.app.features.settings.ProfileScreen
import androidx.compose.runtime.LaunchedEffect
import com.morivia.app.features.watchprogress.ContinueWatchingItem
import com.morivia.app.navigation.AppRoute
import com.morivia.app.navigation.MoriviaNavigator
import kotlinx.coroutines.flow.Flow
import morivia.composeapp.generated.resources.Res
import morivia.composeapp.generated.resources.app_brand_name
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun rememberGuardedPopBackStack(
    navController: MoriviaNavigator,
    route: AppRoute,
    beforePop: () -> Unit = {},
): () -> Unit {
    var popHandled by remember(route) { mutableStateOf(false) }

    return remember(navController, route, popHandled, beforePop) {
        {
            if (!popHandled && navController.currentRoute == route) {
                popHandled = true
                beforePop()
                navController.popBackStack(expectedRoute = route)
            }
        }
    }
}

internal data class AppTabState(
    val searchListState: LazyListState,
    val homeContentGeneration: Int = 0,
    val profileId: Int? = null,
    val searchFocusRequestCount: Int = 0,
    val rootActionsEnabled: Boolean = true,
    val animateHomeCollectionGifs: Boolean = true,
    val libraryDisintegrationRequest: DisintegrationRequest<String>? = null,
    val continueWatchingDisintegrationRequest: DisintegrationRequest<String>? = null,
    val requestedSettingsPageName: String? = null,
)

internal data class AppTabRequests(
    val homeScrollToTopRequests: Flow<Unit>,
    val searchScrollToTopRequests: Flow<Unit>,
    val libraryScrollToTopRequests: Flow<Unit>,
    val settingsRootActionRequests: Flow<Unit>,
)

internal data class AppTabActions(
    val onCatalogClick: ((HomeCatalogSection) -> Unit)? = null,
    val onPosterClick: ((MetaPreview) -> Unit)? = null,
    val onPosterLongClick: ((MetaPreview) -> Unit)? = null,
    val onLibraryPosterClick: ((LibraryItem) -> Unit)? = null,
    val onLibraryPosterLongClick: ((LibraryItem, LibrarySection) -> Unit)? = null,
    val onLibrarySectionViewAllClick: ((LibrarySection, LibrarySortOption) -> Unit)? = null,
    val onCloudFilePlay: ((CloudLibraryItem, CloudLibraryFile) -> Unit)? = null,
    val onConnectCloudClick: (() -> Unit)? = null,
    val onDownloadsClick: () -> Unit = {},
    val onContinueWatchingClick: ((ContinueWatchingItem) -> Unit)? = null,
    val onContinueWatchingLongPress: ((ContinueWatchingItem) -> Unit)? = null,
    val onSwitchProfile: (() -> Unit)? = null,
    val onSettingsPageClick: ((pageName: String, title: String) -> Unit)? = null,
    val onHomescreenSettingsClick: () -> Unit = {},
    val onMetaScreenSettingsClick: () -> Unit = {},
    val onContinueWatchingSettingsClick: () -> Unit = {},
    val onAddonsSettingsClick: () -> Unit = {},
    val onPluginsSettingsClick: () -> Unit = {},
    val onAccountSettingsClick: () -> Unit = {},
    val onProfileSettingsClick: (String) -> Unit = {},
    val onSupportersContributorsSettingsClick: () -> Unit = {},
    val onLicensesAttributionsSettingsClick: () -> Unit = {},
    val onCheckForUpdatesClick: (() -> Unit)? = null,
    val onTestUpdateBannerClick: (() -> Unit)? = null,
    val onCollectionsSettingsClick: () -> Unit = {},
    val onWatchPartySettingsClick: () -> Unit = {},
    val onFolderClick: ((collectionId: String, folderId: String) -> Unit)? = null,
    val onRequestedSettingsPageConsumed: () -> Unit = {},
    val onInitialHomeContentRendered: () -> Unit = {},
)

@Composable
internal fun AppTabHost(
    selectedTab: AppScreenTab,
    requests: AppTabRequests,
    state: AppTabState,
    actions: AppTabActions,
    modifier: Modifier = Modifier,
) {
    RootTabHost(
        selectedTab = selectedTab,
        modifier = modifier,
        active = state.rootActionsEnabled,
        profileId = state.profileId,
    ) { tab ->
        when (tab) {
            AppScreenTab.Home -> AmoledBlackBackground {
                key(state.homeContentGeneration) {
                    HomeScreen(
                        modifier = Modifier.fillMaxSize(),
                        animateCollectionGifs = state.animateHomeCollectionGifs,
                        scrollToTopRequests = requests.homeScrollToTopRequests,
                        onCatalogClick = actions.onCatalogClick,
                        onPosterClick = actions.onPosterClick,
                        onPosterLongClick = actions.onPosterLongClick,
                        onContinueWatchingClick = actions.onContinueWatchingClick,
                        onContinueWatchingLongPress = actions.onContinueWatchingLongPress,
                        continueWatchingDisintegrationRequest = state.continueWatchingDisintegrationRequest,
                        onFolderClick = actions.onFolderClick,
                        onFirstCatalogRendered = actions.onInitialHomeContentRendered,
                        onAddAddon = actions.onAddonsSettingsClick,
                    )
                }
            }

            AppScreenTab.Search -> AmoledBlackBackground {
                SearchScreen(
                    modifier = Modifier.fillMaxSize(),
                    listState = state.searchListState,
                    onPosterClick = actions.onPosterClick,
                    onPosterLongClick = actions.onPosterLongClick,
                    searchFocusRequestCount = state.searchFocusRequestCount,
                    scrollToTopRequests = requests.searchScrollToTopRequests,
                    onAddAddon = actions.onAddonsSettingsClick,
                )
            }

            AppScreenTab.Library -> AmoledBlackBackground {
                LibraryScreen(
                    modifier = Modifier.fillMaxSize(),
                    scrollToTopRequests = requests.libraryScrollToTopRequests,
                    onPosterClick = actions.onLibraryPosterClick,
                    onPosterLongClick = actions.onLibraryPosterLongClick,
                    onSectionViewAllClick = actions.onLibrarySectionViewAllClick,
                    onCloudFilePlay = actions.onCloudFilePlay,
                    onConnectCloudClick = actions.onConnectCloudClick,
                    onCatalogClick = actions.onCatalogClick,
                    onPreviewClick = actions.onPosterClick,
                    onPreviewLongClick = actions.onPosterLongClick,
                    onDownloadsClick = actions.onDownloadsClick,
                    disintegrationRequest = state.libraryDisintegrationRequest,
                    onAddAddon = actions.onAddonsSettingsClick,
                )
            }

            AppScreenTab.Settings -> {
                LaunchedEffect(state.requestedSettingsPageName) {
                    state.requestedSettingsPageName?.let {
                        actions.onProfileSettingsClick(it)
                        actions.onRequestedSettingsPageConsumed()
                    }
                }
                ProfileScreen(
                    modifier = Modifier.fillMaxSize(),
                    onSwitchProfile = actions.onSwitchProfile,
                    onAccount = actions.onAccountSettingsClick,
                    onSettings = { actions.onProfileSettingsClick("Root") },
                )
            }
        }
    }
}

internal fun ContinueWatchingItem.isCloudLibraryContinueWatchingItem(): Boolean =
    parentMetaType.equals(CloudLibraryContentType, ignoreCase = true)

@Composable
internal fun AppLoadingContent(
    modifier: Modifier = Modifier,
) {
    val tokens = MaterialTheme.nuvio
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // The loading-screen wordmark is 51 dp tall on a phone.
            AppBrandWordmark(
                contentDescription = stringResource(Res.string.app_brand_name),
                modifier = Modifier.height(51.dp),
            )
            Spacer(modifier = Modifier.height(tokens.spacing.sectionGap))
            MoriviaLoadingIndicator()
        }
    }
}

@Composable
internal fun AppLaunchOverlay(
    profile: MoriviaProfile?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.zIndex(MoriviaTokens.Z.dialog),
    ) {
        ProfileBackgroundBackdrop(
            profile = profile,
            modifier = Modifier.fillMaxSize(),
        )
        AppLoadingContent(modifier = Modifier.fillMaxSize())
    }
}
