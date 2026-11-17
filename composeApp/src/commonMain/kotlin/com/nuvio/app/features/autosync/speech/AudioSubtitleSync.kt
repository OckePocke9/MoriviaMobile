package com.nuvio.app.features.autosync.speech

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
}
