package com.morivia.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.morivia.app.features.player.OpeningOverlay
import com.morivia.app.features.player.subtitleLoadingStatusMessage
import com.morivia.app.features.streams.StreamLaunch
import com.morivia.app.features.streams.StreamsUiState
import morivia.composeapp.generated.resources.Res
import morivia.composeapp.generated.resources.debrid_resolving_stream
import morivia.composeapp.generated.resources.player_loading_preparing
import morivia.composeapp.generated.resources.streams_finding_source
import morivia.composeapp.generated.resources.streams_loading_subtitles
import morivia.composeapp.generated.resources.youtube_resolving_stream
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun StreamLoadingScreen(
    launch: StreamLaunch,
    state: StreamsUiState,
    showStatus: Boolean,
    resolvingDebridStream: Boolean,
    resolvingYouTubeStream: Boolean = false,
    onBack: () -> Unit,
    preparingPlayback: Boolean = false,
) {
    val message = when {
        !showStatus -> null
        resolvingDebridStream -> stringResource(Res.string.debrid_resolving_stream)
        resolvingYouTubeStream -> stringResource(Res.string.youtube_resolving_stream)
        preparingPlayback -> stringResource(Res.string.player_loading_preparing)
        state.overlayMessage == stringResource(Res.string.streams_loading_subtitles) -> subtitleLoadingStatusMessage()
        state.overlayMessage != null -> state.overlayMessage
        state.autoPlayStream != null -> stringResource(Res.string.player_loading_preparing)
        else -> stringResource(Res.string.streams_finding_source)
    }
    OpeningOverlay(
        artwork = launch.background ?: launch.poster,
        logo = launch.logo,
        title = launch.title,
        onBack = onBack,
        horizontalSafePadding = 0.dp,
        message = message,
    )
}
