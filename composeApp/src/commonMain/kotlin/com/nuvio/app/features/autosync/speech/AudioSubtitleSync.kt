package com.nuvio.app.features.autosync.speech

import com.nuvio.app.features.autosync.AutoSyncRetimedCue
import com.nuvio.app.features.autosync.AutoSyncTimelineRetimeResult
import com.nuvio.app.features.autosync.autoSyncCpuDispatcher
import com.nuvio.app.features.autosync.autoSyncElapsedRealtimeMs
import com.nuvio.app.features.player.SubtitleSyncCue
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Waits while the player measures the audio, then lines the subtitle up with the speech.
 *
 * The first attempt is made after [AudioSubtitleAligner.MIN_OBSERVED_MS] of played audio and
 * repeated as more audio arrives, because a longer stretch makes a frame-rate correction
 * reliable. It gives up silently when no clear match appears; the subtitle then keeps its
 * own timing. Cancelling the calling coroutine (another subtitle, a manual delay, leaving the
 * player) stops it at once.
 */
internal object AudioSubtitleSync {
    private const val POLL_MS = 5_000L
    private const val RETRY_AFTER_MORE_AUDIO_MS = 30_000L
    private const val GIVE_UP_AFTER_OBSERVED_MS = 20L * 60L * 1_000L
    private const val GIVE_UP_AFTER_WAIT_MS = 45L * 60L * 1_000L

    suspend fun awaitRetime(
        timeline: SpeechActivityTimeline,
        cues: List<SubtitleSyncCue>,
    ): AutoSyncTimelineRetimeResult? {
        val startedMs = autoSyncElapsedRealtimeMs()
        var lastAttemptObservedMs = 0L
        while (autoSyncElapsedRealtimeMs() - startedMs < GIVE_UP_AFTER_WAIT_MS) {
            val observedMs = timeline.observedDurationMs()
            val ready = observedMs >= AudioSubtitleAligner.MIN_OBSERVED_MS &&
                (lastAttemptObservedMs == 0L || observedMs - lastAttemptObservedMs >= RETRY_AFTER_MORE_AUDIO_MS)
            if (ready) {
                lastAttemptObservedMs = observedMs
                val snapshot = timeline.snapshot()
                if (snapshot != null) {
                    val result = withContext(autoSyncCpuDispatcher) {
                        AudioSubtitleAligner.retime(snapshot, cues)
                    }
                    if (result != null) return result
                    // Already in sync: nothing to correct, and nothing more to learn.
                    if (withContext(autoSyncCpuDispatcher) { isAlreadyInSync(snapshot, cues) }) return null
                }
                if (observedMs >= GIVE_UP_AFTER_OBSERVED_MS) return null
            }
            delay(POLL_MS)
        }
        return null
    }

    private fun isAlreadyInSync(snapshot: SpeechActivityTimeline.Snapshot, cues: List<SubtitleSyncCue>): Boolean {
        val alignment = AudioSubtitleAligner.align(snapshot, cues) ?: return false
        val first = cues.minOfOrNull { it.startTimeMs } ?: return false
        val last = cues.maxOfOrNull { it.startTimeMs } ?: return false
        fun shift(timeMs: Long) = kotlin.math.abs((alignment.scale - 1.0) * timeMs + alignment.shiftMs)
        return maxOf(shift(first), shift(last)) < AudioSubtitleAligner.MIN_USEFUL_SHIFT_MS
    }

    private const val FOLLOW_POLL_MS = 15_000L
    private const val FOLLOW_EVERY_MORE_AUDIO_MS = 60_000L
    private const val FOLLOW_WINDOW_MS = 4L * 60L * 1_000L
    private const val FOLLOW_MAX_SHIFT_MS = 15_000L
    private const val FOLLOW_MIN_CUES = 10
    private const val FOLLOW_MIN_SPEECH_BINS = 500 // 10 s of speech
    /** A short window has fewer candidate shifts, so its peak stands out less than a whole film's. */
    private const val FOLLOW_MIN_PEAK_Z = 3.5
    private const val FOLLOW_MIN_MARGIN_Z = 1.5
    private const val FOLLOW_AGREEMENT_MS = 100L

    /**
     * The drift between [cues] and the speech in [window] (the last minutes of audio), or null
     * when there is no clear drift worth correcting.
     */
    internal fun followAlignment(
        window: SpeechActivityTimeline.Snapshot,
        cues: List<SubtitleSyncCue>,
    ): AudioSubtitleAligner.Alignment? {
        val alignment = AudioSubtitleAligner.align(
            snapshot = window,
            cues = cues,
            maxShiftMs = FOLLOW_MAX_SHIFT_MS,
            scales = doubleArrayOf(1.0),
            minObservedMs = FOLLOW_WINDOW_MS / 2,
            minCues = FOLLOW_MIN_CUES,
            minSpeechBins = FOLLOW_MIN_SPEECH_BINS,
            minPeakZ = FOLLOW_MIN_PEAK_Z,
            minMarginZ = FOLLOW_MIN_MARGIN_Z,
        ) ?: return null
        return alignment.takeIf { kotlin.math.abs(it.shiftMs) >= AudioSubtitleAligner.MIN_USEFUL_SHIFT_MS }
    }

    /**
     * Keeps the subtitle in sync for the whole film. Every minute of newly played audio it lines
     * the cues of the last [FOLLOW_WINDOW_MS] up with the speech (lyrics and voices alike: every
     * cue counts). When they have drifted apart, for example after a scene that is cut
     * differently in this release, every cue from the start of that window onwards moves by the
     * measured shift; earlier cues keep their timing. [onCorrection] gets the full timeline
     * relative to [originalCues], starting from [initial] (null: the original timing). Runs until
     * the calling coroutine is cancelled.
     */
    suspend fun follow(
        timeline: SpeechActivityTimeline,
        originalCues: List<SubtitleSyncCue>,
        initial: AutoSyncTimelineRetimeResult?,
        onCorrection: suspend (AutoSyncTimelineRetimeResult) -> Unit,
    ) {
        var current = initial
        while (true) {
            val next = awaitCorrection(timeline, originalCues, current) ?: return
            onCorrection(next)
            current = next
        }
    }

    /** Waits for the next drift [follow] would correct and returns the corrected timeline. */
    suspend fun awaitCorrection(
        timeline: SpeechActivityTimeline,
        originalCues: List<SubtitleSyncCue>,
        initial: AutoSyncTimelineRetimeResult?,
    ): AutoSyncTimelineRetimeResult? {
        if (originalCues.isEmpty()) return null
        val starts = LongArray(originalCues.size) { index ->
            initial?.cues?.getOrNull(index)?.startTimeMs ?: originalCues[index].startTimeMs
        }
        val ends = LongArray(originalCues.size) { index ->
            initial?.cues?.getOrNull(index)?.endTimeMs ?: originalCues[index].endTimeMs
        }
        var lastObservedMs = timeline.observedDurationMs()
        var previousShiftMs: Long? = null
        while (true) {
            delay(FOLLOW_POLL_MS)
            val observedMs = timeline.observedDurationMs()
            if (observedMs - lastObservedMs < FOLLOW_EVERY_MORE_AUDIO_MS) continue
            lastObservedMs = observedMs
            val window = timeline.snapshot()?.lastWindow(FOLLOW_WINDOW_MS) ?: continue
            val current = originalCues.indices.map { index ->
                SubtitleSyncCue(startTimeMs = starts[index], endTimeMs = ends[index], text = originalCues[index].text)
            }
            val alignment = withContext(autoSyncCpuDispatcher) { followAlignment(window, current) }
            if (alignment == null) {
                previousShiftMs = null
                continue
            }
            // Only a drift measured twice in a row, a minute apart, is corrected.
            val confirmed = previousShiftMs?.let { kotlin.math.abs(it - alignment.shiftMs) <= FOLLOW_AGREEMENT_MS } == true
            previousShiftMs = alignment.shiftMs
            if (!confirmed) continue
            val fromMs = window.firstBin * SpeechActivityTimeline.BIN_MS
            for (index in starts.indices) {
                if (starts[index] < fromMs) continue
                starts[index] = (starts[index] + alignment.shiftMs).coerceAtLeast(0L)
                ends[index] = (ends[index] + alignment.shiftMs).coerceAtLeast(starts[index] + 1)
            }
            return followResult(originalCues, starts, ends, alignment)
        }
    }

    private fun followResult(
        originalCues: List<SubtitleSyncCue>,
        starts: LongArray,
        ends: LongArray,
        alignment: AudioSubtitleAligner.Alignment,
    ): AutoSyncTimelineRetimeResult = AutoSyncTimelineRetimeResult(
        cues = originalCues.indices.map { index ->
            AutoSyncRetimedCue(
                originalStartTimeMs = originalCues[index].startTimeMs,
                originalEndTimeMs = originalCues[index].endTimeMs,
                startTimeMs = starts[index],
                endTimeMs = ends[index],
            )
        },
        groups = emptyList(),
        targetCoverage = 1.0,
        referenceCoverage = 1.0,
        skippedTargetCues = 0,
        skippedReferenceCues = 0,
        longestTargetSkipRun = 0,
        averageGroupCost = 0.0,
        oneToOneGroups = 0,
        oneToTwoGroups = 0,
        twoToOneGroups = 0,
        oneToThreeGroups = 0,
        threeToOneGroups = 0,
        twoToTwoGroups = 0,
        confident = true,
        alignmentSource = "audio-follow",
        alignmentScale = 1.0,
        alignmentInterceptMs = alignment.shiftMs.toDouble(),
        activityScore = alignment.peakZ,
        activityMargin = alignment.marginZ,
    )
}

/** The last [durationMs] of a speech snapshot, or null when it holds less than that. */
internal fun SpeechActivityTimeline.Snapshot.lastWindow(durationMs: Long): SpeechActivityTimeline.Snapshot? {
    val bins = (durationMs / SpeechActivityTimeline.BIN_MS).toInt()
    if (levelsDb.size < bins) return null
    val from = levelsDb.size - bins
    val levels = levelsDb.copyOfRange(from, levelsDb.size)
    return SpeechActivityTimeline.Snapshot(
        firstBin = firstBin + from,
        levelsDb = levels,
        observedBins = levels.count { !it.isNaN() },
    )
}
