package com.morivia.app.features.details.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAddCheckCircle
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.morivia.app.core.ui.MoriviaBottomSheetActionRow
import com.morivia.app.core.ui.MoriviaBottomSheetDivider
import com.morivia.app.core.ui.MoriviaModalBottomSheet
import com.morivia.app.core.ui.dismissMoriviaBottomSheet
import com.morivia.app.core.ui.nuvio
import com.morivia.app.core.ui.moriviaSafeBottomPadding
import com.morivia.app.core.i18n.localizedSeasonEpisodeCode
import com.morivia.app.features.details.MetaVideo
import com.morivia.app.features.tracking.TrackingMediaReference
import com.morivia.app.features.tracking.TrackingRatingState
import kotlinx.coroutines.launch
import morivia.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import com.morivia.app.core.i18n.localizedEpisodeTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodeWatchedActionSheet(
    episode: MetaVideo,
    seasonLabel: String,
    isEpisodeWatched: Boolean,
    canMarkPreviousEpisodes: Boolean,
    arePreviousEpisodesWatched: Boolean,
    isSeasonWatched: Boolean,
    onDismiss: () -> Unit,
    onToggleWatched: () -> Unit,
    onTogglePreviousWatched: () -> Unit,
    onToggleSeasonWatched: () -> Unit,
    showPlayManually: Boolean = false,
    onPlayManually: (() -> Unit)? = null,
    ratingMedia: TrackingMediaReference? = null,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val ratingState = ratingMedia?.let { rememberRatingState(it).value } ?: TrackingRatingState()
    var showRating by remember { mutableStateOf(false) }
    val dismiss: () -> Unit = {
        coroutineScope.launch {
            dismissMoriviaBottomSheet(sheetState = sheetState, onDismiss = onDismiss)
        }
    }

    MoriviaModalBottomSheet(
        onDismissRequest = dismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = moriviaSafeBottomPadding(16.dp)),
        ) {
            EpisodeActionSheetHeader(
                episode = episode,
                seasonLabel = seasonLabel,
            )
            MoriviaBottomSheetDivider()
            AnimatedContent(
                targetState = showRating && ratingMedia != null,
                transitionSpec = {
                    val forward = targetState
                    val motion = spring<IntOffset>(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
                    (slideInHorizontally(motion) { if (forward) it / 3 else -it / 3 } + fadeIn(tween(180))) togetherWith
                        (slideOutHorizontally(motion) { if (forward) -it / 3 else it / 3 } + fadeOut(tween(120))) using
                        SizeTransform(clip = false)
                },
                label = "episode_sheet_page",
            ) { rating ->
                if (rating && ratingMedia != null) {
                    RatingPanel(
                        media = ratingMedia,
                        state = ratingState,
                        onFinished = dismiss,
                        modifier = Modifier.padding(top = 20.dp),
                    )
                } else {
                    Column {
                        MoriviaBottomSheetActionRow(
                            icon = Icons.Default.CheckCircle,
                            title = if (isEpisodeWatched) {
                                stringResource(Res.string.episode_mark_unwatched)
                            } else {
                                stringResource(Res.string.episode_mark_watched)
                            },
                            onClick = {
                                onToggleWatched()
                                dismiss()
                            },
                        )
                        if (canMarkPreviousEpisodes) {
                            MoriviaBottomSheetActionRow(
                                icon = Icons.Default.DoneAll,
                                title = if (arePreviousEpisodesWatched) {
                                    stringResource(Res.string.episode_mark_previous_unwatched)
                                } else {
                                    stringResource(Res.string.episode_mark_previous_watched)
                                },
                                onClick = {
                                    onTogglePreviousWatched()
                                    dismiss()
                                },
                            )
                        }
                        MoriviaBottomSheetActionRow(
                            icon = Icons.Default.PlaylistAddCheckCircle,
                            title = if (isSeasonWatched) {
                                stringResource(Res.string.episode_mark_season_unwatched, seasonLabel)
                            } else {
                                stringResource(Res.string.episode_mark_season_watched, seasonLabel)
                            },
                            onClick = {
                                onToggleSeasonWatched()
                                dismiss()
                            },
                        )
                        if (ratingState.isAvailable) {
                            val rated = ratingState.rating
                            MoriviaBottomSheetActionRow(
                                icon = if (rated != null) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                                title = stringResource(Res.string.rating_rate_episode),
                                onClick = { showRating = true },
                                trailingContent = if (rated != null) {
                                    {
                                        Text(
                                            text = formatRating(rated),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.nuvio.colors.accent,
                                        )
                                    }
                                } else {
                                    null
                                },
                            )
                        }
                        if (showPlayManually && onPlayManually != null) {
                            MoriviaBottomSheetActionRow(
                                icon = Icons.Default.PlayArrow,
                                title = stringResource(Res.string.play_manually),
                                onClick = {
                                    onPlayManually()
                                    dismiss()
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonWatchedActionSheet(
    seasonLabel: String,
    isSeasonWatched: Boolean,
    canMarkPreviousSeasons: Boolean,
    onDismiss: () -> Unit,
    onToggleSeasonWatched: () -> Unit,
    onMarkPreviousSeasonsWatched: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    MoriviaModalBottomSheet(
        onDismissRequest = {
            coroutineScope.launch {
                dismissMoriviaBottomSheet(sheetState = sheetState, onDismiss = onDismiss)
            }
        },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = moriviaSafeBottomPadding(16.dp)),
        ) {
            Text(
                text = seasonLabel,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            )
            MoriviaBottomSheetDivider()
            MoriviaBottomSheetActionRow(
                icon = Icons.Default.PlaylistAddCheckCircle,
                title = if (isSeasonWatched) {
                    stringResource(Res.string.episode_mark_season_unwatched, seasonLabel)
                } else {
                    stringResource(Res.string.episode_mark_season_watched, seasonLabel)
                },
                onClick = {
                    onToggleSeasonWatched()
                    coroutineScope.launch {
                        dismissMoriviaBottomSheet(sheetState = sheetState, onDismiss = onDismiss)
                    }
                },
            )
            if (canMarkPreviousSeasons) {
                MoriviaBottomSheetActionRow(
                    icon = Icons.Default.DoneAll,
                    title = stringResource(Res.string.episode_mark_previous_seasons_watched),
                    onClick = {
                        onMarkPreviousSeasonsWatched()
                        coroutineScope.launch {
                            dismissMoriviaBottomSheet(sheetState = sheetState, onDismiss = onDismiss)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun EpisodeActionSheetHeader(
    episode: MetaVideo,
    seasonLabel: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = localizedEpisodeTitle(episode.title, episode.episode) ?: episode.title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = buildString {
                localizedSeasonEpisodeCode(
                    seasonNumber = episode.season,
                    episodeNumber = episode.episode,
                )?.let {
                    append(it)
                    append(" • ")
                }
                append(seasonLabel)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
