package com.morivia.app.features.player

import androidx.compose.animation.AnimatedVisibility
import com.morivia.app.features.party.PartyMember
import com.morivia.app.features.party.AvatarStack
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.touchlab.kermit.Logger
import com.morivia.app.features.party.ActiveParty
import com.morivia.app.features.party.PartyAction
import com.morivia.app.core.sync.AppForegroundMonitor
import com.morivia.app.core.sync.AppVisibility
import com.morivia.app.core.ui.MoriviaToastController
import com.morivia.app.features.party.PartyChat
import com.morivia.app.features.party.PartyCorrection
import com.morivia.app.features.party.PartyEvent
import com.morivia.app.features.party.PartyError
import com.morivia.app.features.party.PartyMedia
import com.morivia.app.features.party.PartyMemberStatus
import com.morivia.app.features.party.PartyNotice
import com.morivia.app.features.party.PartyPanel
import com.morivia.app.features.party.PartyPlayback
import com.morivia.app.features.party.PartyRepository
import com.morivia.app.features.party.PartySource
import com.morivia.app.features.party.PartyJoinPlayback
import com.morivia.app.features.party.PartyStream
import com.morivia.app.features.party.isShareableStreamUrl
import com.morivia.app.features.party.PartySync
import com.morivia.app.features.party.UnreadBadge
import com.morivia.app.features.party.messageRes
import com.morivia.app.features.party.partyMediaKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import morivia.composeapp.generated.resources.Res
import morivia.composeapp.generated.resources.party_action_paused
import morivia.composeapp.generated.resources.party_action_resumed
import morivia.composeapp.generated.resources.party_action_seeked
import morivia.composeapp.generated.resources.party_action_speed
import morivia.composeapp.generated.resources.party_behind
import morivia.composeapp.generated.resources.party_caught_up
import morivia.composeapp.generated.resources.party_host_only_note
import morivia.composeapp.generated.resources.party_paused_for_you
import morivia.composeapp.generated.resources.party_reconnecting
import morivia.composeapp.generated.resources.party_waiting
import morivia.composeapp.generated.resources.party_waiting_for
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

internal val PlayerScreenRuntime.partyKey: String
    get() = partyMediaKey(parentMetaType, parentMetaId, activeSeasonNumber, activeEpisodeNumber)

internal val PlayerScreenRuntime.isPartySynced: Boolean
    get() = PartyRepository.state.value.party?.media?.key == partyKey

internal val PlayerScreenRuntime.isPartyControlLocked: Boolean
    get() = PartyRepository.state.value.party?.let { party -> !party.canControl && party.media.key == partyKey } == true

internal fun PlayerScreenRuntime.partyBlocksControl(): Boolean {
    if (!isPartyControlLocked) return false
    scope.launch { showGestureMessage(getString(Res.string.party_host_only_note)) }
    return true
}

internal fun PlayerScreenRuntime.openPartyPanel() {
    if (PartyChat.unread.value > 0) showPartyChat = true
    showPartyPanel = true
    controlsActivityTick += 1
}

internal fun PlayerScreenRuntime.toggleOwnPlayback() {
    controlsVisible = true
    when {
        shouldPlay -> {
            partyBehind = true
            shouldPlay = false
            playerController?.pause()
        }
        partyBehind -> {
            shouldPlay = true
            playerController?.play()
        }
        else -> partyBlocksControl()
    }
}

internal fun PlayerScreenRuntime.privatePartySeek(targetMs: Long): Long {
    if (isPartySynced) {
        partyPrivatePosition = true
        partyBehind = false
        partyBehindMs = null
        clearPartyNudge()
    }
    return targetMs
}

internal fun PlayerScreenRuntime.currentPartyMedia(): PartyMedia = PartyMedia(
    type = contentType ?: parentMetaType,
    metaId = parentMetaId,
    metaType = parentMetaType,
    videoId = activeVideoId,
    season = activeSeasonNumber,
    episode = activeEpisodeNumber,
    title = title,
    episodeTitle = activeEpisodeTitle,
    description = activePauseDescription?.take(DESCRIPTION_LIMIT),
    poster = poster,
    background = background,
    logo = logo,
    source = PartySource(
        infoHash = activeTorrentInfoHash,
        fileIdx = activeTorrentFileIdx,
        filename = activeSourceFilename ?: activeTorrentFilename,
        bingeGroup = currentStreamBingeGroup,
        addonId = activeProviderAddonId,
        name = activeStreamTitle,
    ),
)

internal fun PlayerScreenRuntime.currentPartyStream(): PartyStream? {
    val stream = activeStream ?: return null
    if (!isShareableStreamUrl(activeSourceUrl) || activeSourceAudioUrl != null) return null
    return PartyStream(
        mediaKey = partyKey,
        url = activeSourceUrl,
        headers = activeSourceHeaders,
        responseHeaders = activeSourceResponseHeaders,
        name = stream.name,
        description = stream.description,
        addonName = stream.addonName,
        addonId = stream.addonId,
        addonLogo = stream.addonLogo,
        filename = activeSourceFilename ?: stream.behaviorHints.filename,
        videoSize = stream.behaviorHints.videoSize,
        bingeGroup = stream.behaviorHints.bingeGroup,
        streamType = activeStreamType,
        badges = stream.badges,
    )
}

internal fun PlayerScreenRuntime.partyBaseRate(): Float =
    PartyRepository.playback.value?.rate ?: playbackSnapshot.playbackSpeed

internal fun PlayerScreenRuntime.currentPlaybackPositionMs(): Long {
    val snapshot = playbackSnapshot
    if (!snapshot.isPlaying || snapshot.isLoading) return snapshot.positionMs
    val elapsedMs = playbackSnapshotMark?.elapsedNow()?.inWholeMilliseconds?.coerceIn(0L, 1_000L) ?: 0L
    return snapshot.positionMs + (elapsedMs * snapshot.playbackSpeed).toLong()
}

internal fun PlayerScreenRuntime.publishPartyPlayback(
    positionMs: Long = currentPlaybackPositionMs(),
    rate: Float = partyBaseRate(),
) {
    if (!isPartySynced) return
    val sharedPosition = if (partyPrivatePosition) {
        val playback = PartyRepository.playback.value ?: return
        PartySync.expectedPositionMs(playback, PartyRepository.nowMs())
    } else positionMs
    PartyRepository.publishPlayback(paused = !shouldPlay, positionMs = sharedPosition, rate = rate)
}

internal fun PlayerScreenRuntime.startParty() {
    scope.launch {
        val error = PartyRepository.create(currentPartyMedia()) ?: return@launch
        MoriviaToastController.show(getString(error.messageRes))
    }
}

private fun PlayerScreenRuntime.applyPartyPaused(paused: Boolean, force: Boolean) {
    if (!force && shouldPlay == !paused) return
    shouldPlay = !paused
    if (paused) playerController?.pause() else playerController?.play()
}

private fun PlayerScreenRuntime.handlePartyMediaChange(media: PartyMedia) {
    if (media.key == partyKey) return
    val season = media.season
    val episode = media.episode
    if (media.metaId == parentMetaId && isSeries && season != null && episode != null) {
        val next = nextEpisodeInfo
        if (next != null && next.hasAired && next.season == season && next.episode == episode) {
            playNextEpisode()
            return
        }
        val video = playerMetaVideos.firstOrNull { it.season == season && it.episode == episode }
        if (video != null) {
            episodeStreamsPanelState = EpisodeStreamsPanelState(showStreams = true, selectedEpisode = video)
            showEpisodesPanel = true
            showPartyPanel = false
            return
        }
    }
    showPartyChat = false
    showPartyPanel = true
}

@Composable
internal fun PlayerScreenRuntime.BindPartyEffects() {
    // This binding belongs to the player session, including background PiP.
    val partyState by PartyRepository.state.collectAsState()
    val partyId = partyState.party?.id
    var pausedForPartyWait by remember(key1 = partyKey) { mutableStateOf(false) }
    val key = partyKey
    val sharing = partyState.party?.let { it.isHost && it.sharing } == true
    val sharedStream = if (sharing) currentPartyStream() else null

    DisposableEffect(playerController) {
        val source = playerController as? PlayerSystemControlSource
        // System controls follow the same party rules as the in-player controls.
        source?.setSystemControlHandler(object : PlayerSystemControlHandler {
            override fun handleSetPlaying(playing: Boolean): Boolean {
                if (!isPartySynced) return false
                setPlaybackPlaying(playing)
                return true
            }

            override fun handleSeekTo(positionMs: Long): Boolean {
                if (!isPartySynced) return false
                val targetMs = privatePartySeek(positionMs)
                playerController?.seekTo(targetMs)
                scheduleProgressSyncAfterSeek()
                return true
            }

            override fun handleSeekBy(offsetMs: Long): Boolean {
                if (!isPartySynced) return false
                this@BindPartyEffects.seekBy(offsetMs)
                return true
            }
        })
        onDispose { source?.setSystemControlHandler(null) }
    }

    val isHost = partyState.party?.isHost == true
    DisposableEffect(partyId, key, isHost) {
        if (isHost) {
            PartyRepository.setHostPlaybackForJoin {
                if (!isPartySynced || !initialLoadCompleted) null else {
                    val audio = serverAudioTracks.firstOrNull { it.isSelected }
                        ?: audioTracks.firstOrNull { it.index == selectedAudioIndex }
                    val subtitle = serverSubtitleTracks.firstOrNull { it.isSelected }
                        ?: subtitleTracks.firstOrNull { it.index == selectedSubtitleIndex }
                    PartyJoinPlayback(
                        playback = PartyPlayback(
                            seq = 0, paused = !shouldPlay, positionMs = currentPlaybackPositionMs(),
                            rate = playbackSnapshot.playbackSpeed, at = PartyRepository.nowMs(), mediaKey = key,
                        ),
                        audioLanguage = audio?.language,
                        audioLabel = audio?.label,
                        subtitleLanguage = selectedAddonSubtitle?.language ?: subtitle?.language,
                        subtitleLabel = subtitle?.label,
                        subtitlesOff = selectedAddonSubtitle == null && subtitle == null,
                        privatePosition = partyPrivatePosition,
                    )
                }
            }
        }
        onDispose { if (isHost) PartyRepository.setHostPlaybackForJoin(null) }
    }

    LaunchedEffect(sharing, sharedStream) {
        if (sharing) PartyRepository.shareStream(sharedStream)
    }

    LaunchedEffect(key, partyId) {
        partyStartReady = false
        partyPrivatePosition = false
        partyBehind = false
        val previous = partyPreviousKey
        partyPreviousKey = key
        val party = PartyRepository.state.value.party ?: return@LaunchedEffect
        if (previous != null && previous != key && party.media.key == previous) {
            PartyRepository.publishMedia(currentPartyMedia(), positionMs = 0L)
        }
    }

    LaunchedEffect(Unit) {
        PartyRepository.events.collect { event ->
            when {
                event is PartyEvent.MediaChanged -> handlePartyMediaChange(event.media)
                event is PartyEvent.EndPlayback && event.mediaKey == partyKey -> {
                    shouldPlay = false
                    playerController?.pause()
                    flushWatchProgress()
                    args.onBack()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        AppForegroundMonitor.events().collect { visibility ->
            partyAppInBackground = visibility == AppVisibility.Background
        }
    }

    LaunchedEffect(partyId) {
        if (partyId == null) {
            clearPartyNudge()
            partyBehind = false
            partyBehindMs = null
            if (pausedForPartyWait && !playbackSnapshot.isEnded && errorMessage == null) {
                applyPartyPaused(paused = false, force = false)
            }
            pausedForPartyWait = false
            return@LaunchedEffect
        }
        pausedForPartyWait = false
        val clock = TimeSource.Monotonic
        var bufferingSince: TimeMark? = null
        var mismatchSince: TimeMark? = null
        var lastSeek: TimeMark? = null
        var lastCorrection: TimeMark? = null
        var lastSeq: Pair<Long, String?>? = null
        var lastStatus: Triple<PartyMemberStatus, String, Long?>? = null
        var wasInBackground = false
        var publishedInitial = false
        var seekLeadMs = 0L
        var measureSeekLead = false
        var wasBehind = false
        var joinPositionApplied = false
        var joinAudioApplied = false
        var joinSubtitleApplied = false
        while (isActive) {
            delay(PARTY_TICK)
            val party = PartyRepository.state.value.party ?: break
            val snapshot = playbackSnapshot
            val awayFromPlayback = partyAppInBackground &&
                (playerController as? PlayerSystemControlSource)?.keepsPlaybackInBackground != true
            val resumedFromBackground = wasInBackground && !partyAppInBackground
            wasInBackground = partyAppInBackground
            if (resumedFromBackground) lastSeq = null
            val behindGap = PartyRepository.playback.value
                ?.takeIf { partyBehind && party.media.key == partyKey }
                ?.let { PartySync.expectedPositionMs(it, PartyRepository.nowMs(), snapshot.durationMs) - currentPlaybackPositionMs() }
            val status = when {
                errorMessage != null || awayFromPlayback -> PartyMemberStatus.Browsing
                behindGap != null -> PartyMemberStatus.Behind
                !initialLoadCompleted -> PartyMemberStatus.Loading
                partyPrivatePosition -> PartyMemberStatus.Ready
                snapshot.isLoading && !snapshot.isEnded -> {
                    val since = bufferingSince ?: clock.markNow().also { bufferingSince = it }
                    if (since.elapsedNow() > BUFFERING_REPORT_DELAY) PartyMemberStatus.Buffering else PartyMemberStatus.Ready
                }
                else -> {
                    bufferingSince = null
                    PartyMemberStatus.Ready
                }
            }
            val reportedStatus = Triple(
                status,
                partyKey,
                behindGap?.takeIf { status == PartyMemberStatus.Behind }?.let(PartySync::reportedGapMs),
            )
            if (reportedStatus != lastStatus) {
                lastStatus = reportedStatus
                PartyRepository.setLocalStatus(status, partyKey, reportedStatus.third)
            }

            val controller = playerController
            if (party.media.key != partyKey || controller == null || errorMessage != null || awayFromPlayback) {
                clearPartyNudge()
                continue
            }
            val playback = PartyRepository.playback.value
            if (playback == null) {
                if (initialLoadCompleted && !publishedInitial && party.isHost) {
                    publishedInitial = true
                    PartyRepository.publishPlayback(
                        paused = !shouldPlay,
                        positionMs = snapshot.positionMs,
                        rate = snapshot.playbackSpeed,
                    )
                }
                continue
            }
            publishedInitial = true
            if (!initialLoadCompleted) continue
            val join = PartyRepository.joinPlayback.value
                ?.takeIf { !party.isHost && it.playback.mediaKey == partyKey }
            if (join != null) {
                if (!joinPositionApplied) {
                    joinPositionApplied = true
                    if (!partyPrivatePosition && join.privatePosition) {
                        val position = PartySync.expectedPositionMs(join.playback, PartyRepository.nowMs(), snapshot.durationMs)
                        controller.seekTo(position)
                        partyPrivatePosition = true
                    }
                }
                if (!joinAudioApplied) {
                    joinAudioApplied = isUserExplicitAudioSelection || applyPartyAudio(join, controller)
                }
                if (!joinSubtitleApplied) {
                    joinSubtitleApplied = isUserExplicitSubtitleSelection || applyPartySubtitle(join, controller)
                }
            }
            if (partyBehind && party.canControl) partyBehind = false
            if (partyBehind) {
                clearPartyNudge(playback)
                mismatchSince = null
                val gap = PartySync.expectedPositionMs(playback, PartyRepository.nowMs(), snapshot.durationMs) - currentPlaybackPositionMs()
                if (PartySync.shouldRejoin(gap, playing = shouldPlay && !snapshot.isLoading)) {
                    partyBehind = false
                } else {
                    val shown = partyBehindMs
                    if (shown == null || abs(gap - shown) >= BEHIND_DISPLAY_STEP_MS) partyBehindMs = gap.coerceAtLeast(0L)
                    wasBehind = true
                    continue
                }
            }
            if (wasBehind) {
                wasBehind = false
                lastSeq = null
                partyBehindMs = null
                if (!party.canControl) showGestureMessage(getString(Res.string.party_caught_up))
            }
            val expected = PartySync.expectedPositionMs(playback, PartyRepository.nowMs(), snapshot.durationMs)
            val identity = playback.seq to playback.memberId
            if (identity != lastSeq) {
                val forced = lastSeq == null
                if (!playback.paused && playback.at > PartyRepository.nowMs()) {
                    applyPartyPaused(paused = true, force = false)
                    continue
                }
                lastSeq = identity
                pausedForPartyWait = playback.paused && playback.waiting
                val own = playback.memberId == party.memberId && !forced
                if (!partyPrivatePosition && (!own || playback.paused) && (forced || abs(snapshot.positionMs - expected) > PartySync.PAUSED_TOLERANCE_MS)) {
                    controller.seekTo(if (playback.paused) expected else expected + seekLeadMs)
                    measureSeekLead = !playback.paused
                }
                applyPartyPaused(playback.paused, force = forced)
                if (!playback.waiting) partyStartReady = true
                lastSeek = clock.markNow()
                if (partyRateNudge == null && abs(snapshot.playbackSpeed - playback.rate) > 0.01f) {
                    controller.setPlaybackSpeed(playback.rate)
                }
                mismatchSince = null
                continue
            }

            if (snapshot.isLoading || isScrubbingTimeline || snapshot.isEnded) {
                mismatchSince = null
                continue
            }

            if (snapshot.isPlaying != shouldPlay) {
                val since = mismatchSince ?: clock.markNow().also { mismatchSince = it }
                if (since.elapsedNow() > OUTSIDE_TOGGLE_DELAY) {
                    mismatchSince = null
                    when {
                        party.canControl -> {
                            shouldPlay = snapshot.isPlaying
                            publishPartyPlayback(positionMs = snapshot.positionMs, rate = playback.rate)
                        }
                        snapshot.isPlaying -> applyPartyPaused(playback.paused, force = true)
                        else -> {
                            partyBehind = true
                            shouldPlay = false
                        }
                    }
                }
                continue
            }
            mismatchSince = null

            if (partyPrivatePosition) continue

            val settled = lastSeek?.let { it.elapsedNow() > SEEK_SETTLE } ?: true
            val due = lastCorrection?.let { it.elapsedNow() >= CORRECTION_INTERVAL } ?: true
            if (!settled || !due) continue
            lastCorrection = clock.markNow()
            val drift = snapshot.positionMs - expected
            if (measureSeekLead && !playback.paused) {
                measureSeekLead = false
                if (abs(drift) < MAX_SEEK_LEAD_MS) seekLeadMs = (seekLeadMs - drift).coerceIn(0L, MAX_SEEK_LEAD_MS)
            }
            when (val correction = PartySync.correction(snapshot.positionMs, expected, playback, partyRateNudge != null)) {
                is PartyCorrection.Seek -> {
                    partyLog.i { "Seeking $drift ms back into sync with ${seekLeadMs} ms lead" }
                    clearPartyNudge(playback)
                    val target = if (playback.paused) correction.positionMs else correction.positionMs + seekLeadMs
                    controller.seekTo(if (snapshot.durationMs > 0L) target.coerceAtMost(snapshot.durationMs) else target)
                    lastSeek = clock.markNow()
                    measureSeekLead = !playback.paused
                }
                is PartyCorrection.Nudge -> {
                    if (partyRateNudge != correction.rate) {
                        partyLog.i { "Nudging speed to ${correction.rate} for ${snapshot.positionMs - expected} ms drift" }
                        partyRateNudge = correction.rate
                        controller.setPlaybackSpeed(correction.rate)
                    }
                }
                PartyCorrection.InSync -> clearPartyNudge(playback)
            }
        }
        clearPartyNudge()
    }

    DisposableEffect(Unit) {
        onDispose {
            val party = PartyRepository.state.value.party ?: return@onDispose
            val playback = PartyRepository.playback.value
            val othersWatching = party.presence.values.any {
                it.memberId != party.memberId && it.mediaKey == party.media.key && it.status != PartyMemberStatus.Browsing
            }
            if (party.media.key == partyKey && playback != null && !playback.paused && !othersWatching && !partyPrivatePosition) {
                PartyRepository.publishPlayback(paused = true, positionMs = playbackSnapshot.positionMs, rate = playback.rate)
            }
            PartyRepository.setLocalStatus(PartyMemberStatus.Browsing, null)
        }
    }
}

private fun PlayerScreenRuntime.applyPartyAudio(join: PartyJoinPlayback, controller: PlayerEngineController): Boolean {
    if (join.audioLanguage == null && join.audioLabel == null) return true
    val tracks = serverAudioTracks.ifEmpty { audioTracks }
    val track = tracks.firstOrNull { join.audioLabel != null && it.label == join.audioLabel }
        ?: tracks.firstOrNull { join.audioLanguage?.let { language -> languageMatchesPreference(it.language, language) } == true }
        ?: return false
    preferredAudioSelectionApplied = true
    if (serverAudioTracks.isNotEmpty()) selectServerAudioTrack(track.index) else {
        controller.selectAudioTrack(track.index)
        selectedAudioIndex = track.index
    }
    return true
}

private fun PlayerScreenRuntime.applyPartySubtitle(join: PartyJoinPlayback, controller: PlayerEngineController): Boolean {
    val tracks = serverSubtitleTracks.ifEmpty { subtitleTracks }
    val track = tracks.firstOrNull { join.subtitleLabel != null && it.label == join.subtitleLabel }
        ?: tracks.firstOrNull { join.subtitleLanguage?.let { language -> languageMatchesPreference(it.language, language) } == true }
    if (track != null || join.subtitlesOff) {
        preferredSubtitleSelectionApplied = true
        if (track != null && serverSubtitleTracks.isNotEmpty()) selectServerSubtitleTrack(track.index) else {
            controller.clearExternalSubtitleAndSelect(track?.index ?: -1)
            if (hasBurnedInServerSubtitle) clearServerSubtitleTrack()
            selectedSubtitleIndex = track?.index ?: -1
            selectedAddonSubtitleId = null
            useCustomSubtitles = false
        }
        return true
    }
    val addon = visibleAddonSubtitles.firstOrNull {
        join.subtitleLanguage?.let { language -> languageMatchesPreference(it.language, language) } == true
    } ?: return join.subtitleLanguage == null && join.subtitleLabel == null
    preferredSubtitleSelectionApplied = true
    selectedSubtitleIndex = -1
    selectedAddonSubtitleId = addon.selectionKey
    useCustomSubtitles = true
    attachSelectedAddonSubtitleWithAutoSync(addon.url)
    return true
}

private fun PlayerScreenRuntime.clearPartyNudge(playback: PartyPlayback? = PartyRepository.playback.value) {
    if (partyRateNudge == null) return
    partyRateNudge = null
    playerController?.setPlaybackSpeed(playback?.rate ?: 1f)
}

@Composable
internal fun BoxScope.RenderParty(
    runtime: PlayerScreenRuntime,
    panelWidth: Dp,
    modal: Boolean,
    panelVisible: Boolean,
) {
    val partyState by PartyRepository.state.collectAsStateWithLifecycle()
    val playback by PartyRepository.playback.collectAsStateWithLifecycle()
    val unread by PartyChat.unread.collectAsStateWithLifecycle()
    var notice by remember { mutableStateOf<PartyNotice?>(null) }
    LaunchedEffect(Unit) {
        PartyRepository.notices.collectLatest { next ->
            notice = next
            delay(NOTICE_DURATION)
            notice = null
        }
    }
    val party = partyState.party
    runtime.run {
        if (party != null) {
            val waitingNames = if (playback?.paused == true && playback?.waiting == true) {
                party.waitingNames()
            } else {
                emptyList()
            }
            val noticeText = notice?.text()
            PartyStatusPill(
                visible = (controlsVisible || waitingNames.isNotEmpty() || !party.connected || noticeText != null || (partyBehind && !shouldPlay)) &&
                    !playerControlsLocked && !showPartyPanel,
                members = party.members,
                unread = unread,
                message = when {
                    !party.connected -> stringResource(Res.string.party_reconnecting)
                    waitingNames.isNotEmpty() -> stringResource(Res.string.party_waiting_for, waitingNames.joinToString())
                    playback?.waiting == true -> stringResource(Res.string.party_waiting)
                    noticeText != null -> noticeText
                    partyBehind && !shouldPlay -> stringResource(Res.string.party_paused_for_you)
                    partyBehind -> partyBehindMs?.let { stringResource(Res.string.party_behind, formatPlaybackTime(it)) }
                    else -> null
                },
                onClick = { openPartyPanel() },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.safeContent.only(WindowInsetsSides.Top))
                    .padding(top = metrics.verticalPadding / 4 + 8.dp),
            )
        }
        PartyPanel(
            visible = panelVisible,
            width = panelWidth,
            modal = modal,
            state = partyState,
            showChat = showPartyChat,
            onShowChatChange = { showPartyChat = it },
            playerMedia = currentPartyMedia(),
            isSupported = PartyRepository.available.collectAsStateWithLifecycle().value,
            canStart = PartyRepository.canUse,
            onStart = { startParty() },
            onSwitchParty = {
                PartyRepository.publishMedia(currentPartyMedia(), positionMs = playbackSnapshot.positionMs)
            },
            onWatchWithParty = {
                party?.media?.let { media ->
                    showPartyPanel = false
                    flushWatchProgress()
                    args.onOpenPartyMedia(media)
                }
            },
            onLeave = {
                showPartyPanel = false
                PartyRepository.leave()
            },
            onEnd = {
                showPartyPanel = false
                PartyRepository.end()
            },
            onRemove = { member -> PartyRepository.removeMember(member.id) },
            onHostOnlyChange = { enabled -> PartyRepository.setHostOnly(enabled) },
            canShareStream = currentPartyStream() != null,
            onSharingChange = { enabled -> PartyRepository.setSharing(enabled) },
            behindMs = if (partyBehind) partyBehindMs ?: 0L else null,
            canReturnToSync = party?.isHost == false && party.media.key == partyKey && partyPrivatePosition,
            onCatchUp = {
                showPartyPanel = false
                scope.launch {
                    val roomId = party?.id ?: return@launch
                    val controller = playerController ?: return@launch
                    val host = PartyRepository.currentHostPlayback()
                    if (PartyRepository.state.value.party?.id != roomId || playerController !== controller) return@launch
                    if (host == null || host.playback.mediaKey != partyKey) {
                        showGestureMessage(getString(PartyError.Network.messageRes))
                        return@launch
                    }
                    val position = PartySync.expectedPositionMs(host.playback, PartyRepository.nowMs(), playbackSnapshot.durationMs)
                    partyBehind = false
                    partyBehindMs = null
                    partyPrivatePosition = host.privatePosition
                    clearPartyNudge()
                    controller.seekTo(position)
                    applyPartyPaused(host.playback.paused, force = true)
                    scheduleProgressSyncAfterSeek()
                }
            },
            onDismiss = { showPartyPanel = false },
        )
    }
}

@Composable
private fun PartyNotice.text(): String = when (action) {
    PartyAction.Paused -> stringResource(Res.string.party_action_paused, name)
    PartyAction.Resumed -> stringResource(Res.string.party_action_resumed, name)
    PartyAction.Seeked -> stringResource(Res.string.party_action_seeked, name, formatPlaybackTime(positionMs))
    PartyAction.Speed -> stringResource(Res.string.party_action_speed, name, formatPlaybackSpeedLabel(rate))
}

private fun ActiveParty.waitingNames(): List<String> =
    PartySync.waitingMemberIds(this, emptySet())
        .filter { it != memberId }
        .mapNotNull { id -> member(id)?.displayName }

@Composable
private fun PartyStatusPill(
    visible: Boolean,
    members: List<PartyMember>,
    unread: Int,
    message: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onClick)
                .animateContentSize()
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AvatarStack(
                members = members,
                size = 28.dp,
                ringColor = Color.Black.copy(alpha = 0.5f),
            )
            UnreadBadge(count = unread, icon = true)
            AnimatedContent(
                targetState = message,
                transitionSpec = {
                    (fadeIn(tween(200)) + slideInVertically(tween(240)) { it / 2 }) togetherWith
                        (fadeOut(tween(120)) + slideOutVertically(tween(160)) { -it / 2 })
                },
            ) { text ->
                if (text != null) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge.copy(
                            shadow = Shadow(color = Color.Black.copy(alpha = 0.8f), blurRadius = 12f),
                        ),
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

private val partyLog = Logger.withTag("Party")
private val PARTY_TICK = 250.milliseconds
private val NOTICE_DURATION = 3_000.milliseconds
private val BUFFERING_REPORT_DELAY = 1_000.milliseconds
private val OUTSIDE_TOGGLE_DELAY = 1_200.milliseconds
private val SEEK_SETTLE = 2_500.milliseconds
private val CORRECTION_INTERVAL = 500.milliseconds
private const val MAX_SEEK_LEAD_MS = 3_000L
private const val BEHIND_DISPLAY_STEP_MS = 1_000L
private const val DESCRIPTION_LIMIT = 600
