package com.morivia.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.morivia.app.features.collection.CollectionEditorPage
import com.morivia.app.features.collection.CollectionEditorScreen
import com.morivia.app.features.collection.CollectionManagementScreen
import com.morivia.app.features.collection.CollectionRepository
import com.morivia.app.features.collection.FolderDetailRepository
import com.morivia.app.features.collection.FolderDetailScreen
import com.morivia.app.features.home.HomeCatalogSection
import com.morivia.app.features.settings.SettingsBackground
import com.morivia.app.features.settings.SettingsScreen
import com.morivia.app.navigation.AppRoute
import com.morivia.app.navigation.CollectionEditorPageRoute
import com.morivia.app.navigation.CollectionEditorRoute
import com.morivia.app.navigation.CollectionsRoute
import com.morivia.app.navigation.FolderDetailRoute
import com.morivia.app.navigation.MoriviaNavigator
import com.morivia.app.navigation.PartyRoute
import com.morivia.app.navigation.SettingsPageRoute
import morivia.composeapp.generated.resources.Res
import morivia.composeapp.generated.resources.party_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingsDestination(
    route: AppRoute,
    navController: MoriviaNavigator,
    content: @Composable (onBack: () -> Unit) -> Unit,
) {
    val onBack = rememberGuardedPopBackStack(navController, route)
    if (isIos) {
        content(onBack)
    } else {
        SettingsBackground(modifier = Modifier.fillMaxSize()) {
            content(onBack)
        }
    }
}

@Composable
internal fun SettingsRootDestination(
    route: SettingsPageRoute,
    navController: MoriviaNavigator,
    useNativeNavigation: Boolean,
    collectionsTitle: String,
    onCheckForUpdates: (() -> Unit)?,
    onTestUpdateBanner: (() -> Unit)?,
    onTabSelected: ((AppScreenTab) -> Unit)? = null,
) {
    val onBack = rememberGuardedPopBackStack(navController, route)
    val watchPartyTitle = stringResource(Res.string.party_title)
    SettingsScreen(
        modifier = Modifier.fillMaxSize(),
        initialPageName = route.pageName,
        rootActionsEnabled = false,
        onNavigatePage = { pageName, title ->
            navController.navigate(SettingsPageRoute(pageName, title))
        },
        onExternalBack = onBack,
        onTabSelected = onTabSelected,
        showInternalHeader = !useNativeNavigation,
        onCollectionsClick = {
            navController.navigate(CollectionsRoute(collectionsTitle))
        },
        onWatchPartyClick = {
            navController.navigate(PartyRoute(title = watchPartyTitle))
        },
        onCheckForUpdatesClick = onCheckForUpdates,
        onTestUpdateBannerClick = onTestUpdateBanner,
    )
}

@Composable
internal fun CollectionsDestination(
    route: CollectionsRoute,
    navController: MoriviaNavigator,
    newCollectionTitle: String,
) {
    val onBack = rememberGuardedPopBackStack(navController, route)
    // The same background and cards as the rest of Settings (owner, 2026-11-26).
    SettingsBackground(modifier = Modifier.fillMaxSize()) {
    CollectionManagementScreen(
        onBack = onBack,
        onNavigateToEditor = { collectionId ->
            val editorTitle = collectionId
                ?.let { id ->
                    CollectionRepository.collections.value.firstOrNull { it.id == id }?.title
                }
                .orEmpty()
            navController.navigate(
                CollectionEditorRoute(
                    collectionId = collectionId,
                    title = editorTitle.ifBlank { newCollectionTitle },
                ),
            )
        },
    )
    }
}

@Composable
internal fun CollectionEditorDestination(
    route: CollectionEditorRoute,
    navController: MoriviaNavigator,
    useNativeNavigation: Boolean,
) {
    val onBack = rememberGuardedPopBackStack(navController, route)
    CollectionEditorScreen(
        collectionId = route.collectionId,
        onBack = onBack,
        initialPage = if (useNativeNavigation) CollectionEditorPage.Root else null,
        onNavigateToPage = if (useNativeNavigation) {
            { page, title ->
                navController.navigate(
                    CollectionEditorPageRoute(
                        collectionId = route.collectionId,
                        pageName = page.name,
                        title = title,
                    ),
                )
            }
        } else {
            null
        },
    )
}

@Composable
internal fun CollectionEditorPageDestination(
    route: CollectionEditorPageRoute,
    navController: MoriviaNavigator,
) {
    val page = remember(route.pageName) {
        runCatching { CollectionEditorPage.valueOf(route.pageName) }.getOrNull()
    }
    val onBack = rememberGuardedPopBackStack(navController, route)
    if (page == null || page == CollectionEditorPage.Root) {
        LaunchedEffect(route) { onBack() }
        return
    }
    CollectionEditorScreen(
        collectionId = route.collectionId,
        initialPage = page,
        initializeRepository = false,
        onBack = onBack,
        onNavigateToPage = { nextPage, title ->
            navController.navigate(
                CollectionEditorPageRoute(
                    collectionId = route.collectionId,
                    pageName = nextPage.name,
                    title = title,
                ),
            )
        },
    )
}

@Composable
internal fun FolderDestination(
    route: FolderDetailRoute,
    navController: MoriviaNavigator,
    onCatalogClick: (HomeCatalogSection) -> Unit,
) {
    val onBack = rememberGuardedPopBackStack(navController, route)
    LaunchedEffect(route.collectionId, route.folderId) {
        FolderDetailRepository.initialize(route.collectionId, route.folderId)
    }
    FolderDetailScreen(
        onBack = onBack,
        onCatalogClick = onCatalogClick,
        onPosterClick = rememberOpenMeta(navController),
    )
}
