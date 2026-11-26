package com.morivia.app

import androidx.compose.runtime.Composable
import com.morivia.app.features.downloads.DownloadItem
import com.morivia.app.features.downloads.DownloadsScreen
import com.morivia.app.navigation.DownloadShowRoute
import com.morivia.app.navigation.DownloadsRoute
import com.morivia.app.navigation.MoriviaNavigator

@Composable
internal fun DownloadsDestination(
    route: DownloadsRoute,
    navController: MoriviaNavigator,
    useNativeNavigation: Boolean,
    onOpenDownload: (DownloadItem) -> Unit,
) {
    val onBack = rememberGuardedPopBackStack(navController, route)
    DownloadsScreen(
        onBack = onBack,
        onOpenDownload = onOpenDownload,
        onNavigateToShow = if (useNativeNavigation) {
            { showId, title -> navController.navigate(DownloadShowRoute(showId, title)) }
        } else {
            null
        },
    )
}

@Composable
internal fun DownloadShowDestination(
    route: DownloadShowRoute,
    navController: MoriviaNavigator,
    onOpenDownload: (DownloadItem) -> Unit,
) {
    val onBack = rememberGuardedPopBackStack(navController, route)
    DownloadsScreen(
        onBack = onBack,
        onOpenDownload = onOpenDownload,
        initialShowId = route.showId,
        onBackFromShow = onBack,
    )
}
