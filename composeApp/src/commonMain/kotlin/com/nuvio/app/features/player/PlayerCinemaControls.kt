package com.nuvio.app.features.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.Toc
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.nuvio.app.core.format.formatLocalTime
import com.nuvio.app.core.ui.AppIconResource
import com.nuvio.app.core.ui.appIconPainter
import com.nuvio.app.core.ui.accentBrush
import com.nuvio.app.core.ui.themePalette
import com.nuvio.app.features.player.skip.NextEpisodeInfo
import com.nuvio.app.features.streams.StreamBadge
import kotlinx.coroutines.delay
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

/** What the "Bio Pro" controls show next to the buttons: the current choices and the next episode. */
internal data class PlayerCinemaInfo(
    val subtitleLabel: String? = null,
    val audioLabel: String? = null,
    val nextEpisode: NextEpisodeInfo? = null,
)

private val CinemaDim = Color.White.copy(alpha = 0.68f)
private val CinemaGlass = Color.White.copy(alpha = 0.12f)
private val CinemaGlassBorder = Color.White.copy(alpha = 0.16f)

/**
 * Morivia's "Bio Pro" player controls, shared by ExoPlayer, libmpv and iOS: back, clock and actions
 * at the top, an Up next card, a large title with season, episode, year and quality badges at the
 * bottom left, labelled buttons that show the current subtitle, audio, source, speed and picture
 * choices, and a play row with the chapter timeline and the time the film ends.
 */
@Composable
internal fun PlayerCinemaControls(
    title: String,
    providerName: String,
    seasonNumber: Int?,
    episodeNumber: Int?,
    episodeTitle: String?,
    releaseInfo: String?,
    badges: List<StreamBadge>,
    info: PlayerCinemaInfo,
    playbackSnapshot: PlayerPlaybackSnapshot,
    displayedPositionMs: Long,
    metrics: PlayerLayoutMetrics,
    resizeMode: PlayerResizeMode,
    isLocked: Boolean,
    hideDetails: Boolean,
    showRemainingTime: Boolean,
    onRuntimeClick: () -> Unit,
    showPlaybackControls: Boolean,
    chapters: List<PlayerChapter>,
    parentalWarnings: List<ParentalWarning>,
    showParentalGuide: Boolean,
    onParentalGuideAnimationComplete: () -> Unit,
    onInteraction: () -> Unit,
    onBack: () -> Unit,
    onLockToggle: () -> Unit,
    onPartyClick: (() -> Unit)?,
    partyActive: Boolean,
    onSwitchEngineClick: (() -> Unit)?,
    onTogglePlayback: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onSubtitleClick: () -> Unit,
    onAudioClick: () -> Unit,
    onSourcesClick: (() -> Unit)?,
    onEpisodesClick: (() -> Unit)?,
    onChaptersClick: (() -> Unit)?,
    onNextEpisodeClick: (() -> Unit)?,
    onSpeedClick: () -> Unit,
    onResizeModeClick: () -> Unit,
    onVideoSettingsClick: (() -> Unit)?,
    onOpenInExternalPlayer: (() -> Unit)?,
    onStreamInfoClick: () -> Unit,
    onSubmitIntroClick: (() -> Unit)?,
    onScrubChange: (Long) -> Unit,
    onScrubFinished: (Long) -> Unit,
    horizontalSafePadding: Dp,
    modifier: Modifier = Modifier,
) {
    fun act(action: () -> Unit): () -> Unit = {
        onInteraction()
        action()
    }

    var nowMs by remember { mutableLongStateOf(Clock.System.now().toEpochMilliseconds()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(15_000L)
            nowMs = Clock.System.now().toEpochMilliseconds()
        }
    }
    val remainingMs = (playbackSnapshot.durationMs - displayedPositionMs).coerceAtLeast(0L)
    val endsAtMs = nowMs + (remainingMs / playbackSnapshot.playbackSpeed.coerceAtLeast(0.25f)).toLong()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val wide = maxWidth >= 700.dp
        // Shade: left side and bottom for the text, top for the bar.
        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(listOf(Color.Black.copy(0.78f), Color.Black.copy(0.30f), Color.Transparent)),
            ),
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Black.copy(0.50f), Color.Transparent, Color.Transparent, Color.Black.copy(0.85f))),
            ),
        )

        Box(Modifier.fillMaxSize().padding(horizontal = horizontalSafePadding)) {
            // Top bar
            if (showPlaybackControls) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.safeContent.only(WindowInsetsSides.Top))
                        .padding(horizontal = metrics.horizontalPadding, vertical = metrics.verticalPadding / 2),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CinemaGlassButton(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(Res.string.compose_player_go_back), onBack)
                    Spacer(Modifier.weight(1f))
                    Text(formatLocalTime(nowMs), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(10.dp))
                    onPartyClick?.let {
                        CinemaIconButton(Icons.Rounded.Groups, stringResource(Res.string.party_title), act(it), highlighted = partyActive)
                    }
                    onSwitchEngineClick?.let { CinemaIconButton(Icons.Rounded.SwapHoriz, stringResource(Res.string.cd_switch_player_engine), act(it)) }
                    onOpenInExternalPlayer?.let {
                        CinemaIconButton(Icons.AutoMirrored.Rounded.OpenInNew, stringResource(Res.string.streams_open_external_player), act(it))
                    }
                    onVideoSettingsClick?.let {
                        CinemaIconButton(Icons.Rounded.Build, stringResource(Res.string.player_action_video_settings), act(it))
                    }
                    onSubmitIntroClick?.let { CinemaIconButton(Icons.Rounded.Flag, stringResource(Res.string.submit_intro_action), act(it)) }
                    CinemaIconButton(Icons.Rounded.Info, stringResource(Res.string.cd_stream_info), act(onStreamInfoClick))
                    Spacer(Modifier.width(6.dp))
                    CinemaGlassButton(
                        if (isLocked) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                        stringResource(if (isLocked) Res.string.compose_player_unlock_controls else Res.string.compose_player_lock_controls),
                        onLockToggle,
                    )
                }
            }

            ParentalGuideOverlay(
                warnings = parentalWarnings,
                isVisible = showParentalGuide,
                onAnimationComplete = onParentalGuideAnimationComplete,
                contentPadding = PaddingValues(horizontal = metrics.horizontalPadding, vertical = metrics.verticalPadding),
                modifier = Modifier.align(Alignment.TopStart),
            )

            // Up next card
            val next = info.nextEpisode
            if (showPlaybackControls && wide && next != null && onNextEpisodeClick != null) {
                UpNextCard(
                    next = next,
                    remainingMs = remainingMs,
                    onClick = act(onNextEpisodeClick),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .windowInsetsPadding(WindowInsets.safeContent.only(WindowInsetsSides.Top))
                        .padding(top = 72.dp, end = metrics.horizontalPadding),
                )
            }

            // Bottom block
            if (showPlaybackControls) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .windowInsetsPadding(playerTimelineBottomInsets(metrics))
                        .padding(horizontal = metrics.horizontalPadding),
                ) {
                    val rail = @Composable {
                        CinemaRail(
                            subtitleLabel = info.subtitleLabel,
                            audioLabel = info.audioLabel,
                            providerName = providerName,
                            seasonNumber = seasonNumber,
                            playbackSpeed = playbackSnapshot.playbackSpeed,
                            resizeMode = resizeMode,
                            onSubtitleClick = act(onSubtitleClick),
                            onAudioClick = act(onAudioClick),
                            onSourcesClick = onSourcesClick?.let(::act),
                            onEpisodesClick = onEpisodesClick?.let(::act),
                            onChaptersClick = onChaptersClick?.let(::act),
                            onNextEpisodeClick = onNextEpisodeClick?.takeUnless { wide && next != null }?.let(::act),
                            onSpeedClick = act(onSpeedClick),
                            onResizeModeClick = act(onResizeModeClick),
                        )
                    }
                    if (wide) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Box(Modifier.weight(1f)) {
                                if (!hideDetails) CinemaTitleBlock(title, seasonNumber, episodeNumber, episodeTitle, releaseInfo, badges)
                            }
                            rail()
                        }
                    } else {
                        if (!hideDetails) CinemaTitleBlock(title, seasonNumber, episodeNumber, episodeTitle, releaseInfo, badges)
                        Spacer(Modifier.height(8.dp))
                        rail()
                    }
                    PlayerChapterTitle(chapters = chapters, positionMs = displayedPositionMs)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable(onClick = act(onTogglePlayback)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                if (playbackSnapshot.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        CinemaIconButton(Icons.Rounded.Replay10, null, act(onSeekBack))
                        CinemaIconButton(Icons.Rounded.Forward10, null, act(onSeekForward))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            formatPlaybackTime(displayedPositionMs),
                            color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                        )
                        PlayerTimeline(
                            snapshot = playbackSnapshot,
                            displayedPositionMs = displayedPositionMs,
                            onScrubChange = onScrubChange,
                            onScrubFinished = {
                                onInteraction()
                                onScrubFinished(it)
                            },
                            chapters = chapters,
                            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                        )
                        Text(
                            text = if (showRemainingTime) "-${formatPlaybackTime(remainingMs)}" else formatPlaybackTime(playbackSnapshot.durationMs),
                            color = CinemaDim, fontSize = 13.sp,
                            modifier = Modifier.clickable(onClick = onRuntimeClick),
                        )
                        if (playbackSnapshot.durationMs > 0L) {
                            Text(
                                "  ·  " + stringResource(Res.string.morivia_player_ends_at, formatLocalTime(endsAtMs)),
                                color = CinemaDim, fontSize = 11.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CinemaTitleBlock(
    title: String,
    seasonNumber: Int?,
    episodeNumber: Int?,
    episodeTitle: String?,
    releaseInfo: String?,
    badges: List<StreamBadge>,
) {
    Column(Modifier.padding(end = 16.dp)) {
        if (seasonNumber != null && episodeNumber != null) {
            Text(
                stringResource(Res.string.morivia_player_season_episode, seasonNumber, episodeNumber).uppercase(),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp,
            )
        }
        Text(
            title, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
        )
        episodeTitle?.takeIf { it.isNotBlank() }?.let {
            Text(it, color = Color.White.copy(0.8f), fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (!releaseInfo.isNullOrBlank() || badges.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                releaseInfo?.takeIf { it.isNotBlank() }?.let { Text(it, color = CinemaDim, fontSize = 11.sp) }
                if (badges.isNotEmpty()) PlayerBadges(badges = badges)
            }
        }
    }
}

@Composable
private fun CinemaRail(
    subtitleLabel: String?,
    audioLabel: String?,
    providerName: String,
    seasonNumber: Int?,
    playbackSpeed: Float,
    resizeMode: PlayerResizeMode,
    onSubtitleClick: () -> Unit,
    onAudioClick: () -> Unit,
    onSourcesClick: (() -> Unit)?,
    onEpisodesClick: (() -> Unit)?,
    onChaptersClick: (() -> Unit)?,
    onNextEpisodeClick: (() -> Unit)?,
    onSpeedClick: () -> Unit,
    onResizeModeClick: () -> Unit,
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        RailButton(
            painter = appIconPainter(AppIconResource.PlayerSubtitles),
            label = stringResource(Res.string.compose_player_subtitles),
            value = subtitleLabel ?: stringResource(Res.string.compose_player_none),
            highlighted = subtitleLabel != null,
            onClick = onSubtitleClick,
        )
        RailButton(
            painter = appIconPainter(AppIconResource.PlayerAudioFilled),
            label = stringResource(Res.string.compose_player_audio),
            value = audioLabel,
            onClick = onAudioClick,
        )
        onSourcesClick?.let {
            RailButton(
                painter = appIconPainter(AppIconResource.PlayerSource),
                label = stringResource(Res.string.compose_player_sources),
                value = providerName.takeIf { name -> name.isNotBlank() },
                onClick = it,
            )
        }
        onEpisodesClick?.let {
            RailButton(
                painter = appIconPainter(AppIconResource.PlayerEpisodes),
                label = stringResource(Res.string.compose_player_episodes),
                value = seasonNumber?.let { season -> "S$season" },
                onClick = it,
            )
        }
        onChaptersClick?.let {
            RailButton(icon = Icons.AutoMirrored.Rounded.Toc, label = stringResource(Res.string.compose_player_chapters), onClick = it)
        }
        RailButton(
            icon = Icons.Rounded.Speed,
            label = stringResource(Res.string.morivia_player_speed),
            value = formatPlaybackSpeedLabel(playbackSpeed),
            onClick = onSpeedClick,
        )
        RailButton(
            painter = appIconPainter(AppIconResource.PlayerAspectRatio),
            label = stringResource(Res.string.morivia_player_picture),
            value = stringResource(resizeMode.labelRes),
            onClick = onResizeModeClick,
        )
        onNextEpisodeClick?.let {
            RailButton(icon = Icons.Rounded.SkipNext, label = stringResource(Res.string.player_next_episode), onClick = it)
        }
    }
}

@Composable
private fun RailButton(
    label: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    painter: Painter? = null,
    value: String? = null,
    highlighted: Boolean = false,
) {
    Column(
        modifier = Modifier
            .width(68.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .then(
                    if (highlighted) Modifier.background(MaterialTheme.themePalette.accentBrush())
                    else Modifier.background(CinemaGlass),
                ),
            contentAlignment = Alignment.Center,
        ) {
            when {
                painter != null -> Icon(painter, contentDescription = label, tint = Color.White, modifier = Modifier.size(21.dp))
                icon != null -> Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(21.dp))
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (!value.isNullOrBlank()) {
            Text(value, color = CinemaDim, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun UpNextCard(
    next: NextEpisodeInfo,
    remainingMs: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.55f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!next.thumbnail.isNullOrBlank()) {
            AsyncImage(
                model = next.thumbnail,
                contentDescription = null,
                modifier = Modifier.width(96.dp).height(54.dp).clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop,
            )
        }
        Column(Modifier.padding(horizontal = 10.dp).width(150.dp)) {
            Text(
                stringResource(Res.string.morivia_player_up_next).uppercase(),
                color = MaterialTheme.colorScheme.primary, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp,
            )
            Text(
                "E${next.episode} · ${next.title}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            val minutes = ((remainingMs + 59_999L) / 60_000L).toInt()
            Text(stringResource(Res.string.morivia_player_starts_in, minutes), color = CinemaDim, fontSize = 10.sp)
        }
    }
}

@Composable
private fun CinemaGlassButton(icon: ImageVector, description: String?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(CinemaGlass)
            .border(1.dp, CinemaGlassBorder, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = Color.White, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun CinemaIconButton(icon: ImageVector, description: String?, onClick: () -> Unit, highlighted: Boolean = false) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .then(if (highlighted) Modifier.background(MaterialTheme.themePalette.accentBrush()) else Modifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = Color.White, modifier = Modifier.size(22.dp))
    }
}
