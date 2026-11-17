package com.nuvio.app.features.autosync.speech

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlin.math.max
import kotlin.math.min

/**
 * Loudness of the audio that has actually been played, in fixed bins of media time.
 *
 * Players feed it while they play: ExoPlayer from an audio processor, libmpv (Android and iOS)
 * from an FFmpeg filter that prints the level of every short audio frame. Only the voice band
 * is measured where the player allows it, so speech stands out from music and effects.
 * [AudioSubtitleAligner] compares the resulting speech pattern with a subtitle's cues.
 */
internal class SpeechActivityTimeline(
    val sourceKey: String,
) {
    private val lock = SynchronizedObject()
    private var levels = FloatArray(INITIAL_BINS) { Float.NaN }
    private var observedBins = 0
    private var firstObservedBin = Int.MAX_VALUE
    private var lastObservedBin = -1

    /** Records [levelDb] for the media time [startMs] until [startMs] + [durationMs]. */
    fun record(startMs: Long, durationMs: Long, levelDb: Float) {
        if (startMs < 0L || durationMs <= 0L || levelDb.isNaN()) return
        if (startMs > MAX_MEDIA_TIME_MS) return
        val level = levelDb.coerceIn(SILENCE_DB, 0f)
        val firstBin = (startMs / BIN_MS).toInt()
        val lastBin = ((startMs + durationMs - 1L) / BIN_MS).toInt()
        synchronized(lock) {
            ensureCapacity(lastBin + 1)
            for (bin in firstBin..lastBin) {
                if (levels[bin].isNaN()) observedBins++
                levels[bin] = level
            }
            firstObservedBin = min(firstObservedBin, firstBin)
            lastObservedBin = max(lastObservedBin, lastBin)
        }
    }

    /** Media time covered by measured audio, in milliseconds. */
    fun observedDurationMs(): Long = synchronized(lock) { observedBins.toLong() * BIN_MS }

    /** A copy of the measured bins from the first to the last observed one. */
    fun snapshot(): Snapshot? = synchronized(lock) {
        if (lastObservedBin < 0) return@synchronized null
        Snapshot(
            firstBin = firstObservedBin,
            levelsDb = levels.copyOfRange(firstObservedBin, lastObservedBin + 1),
            observedBins = observedBins,
        )
    }

    private fun ensureCapacity(bins: Int) {
        if (bins <= levels.size) return
        var size = levels.size
        while (size < bins) size *= 2
        val grown = FloatArray(size) { Float.NaN }
        levels.copyInto(grown)
        levels = grown
    }

    /** Levels for consecutive bins starting at [firstBin]; NaN where nothing was measured. */
    class Snapshot(
        val firstBin: Int,
        val levelsDb: FloatArray,
        val observedBins: Int,
    )

    companion object {
        /** One bin is 20 ms of media time. */
        const val BIN_MS = 20L
        const val SILENCE_DB = -100f
        private const val INITIAL_BINS = 30_000 // 10 minutes; grows as needed
        private const val MAX_MEDIA_TIME_MS = 8L * 60L * 60L * 1_000L
    }
}

/** The speech timeline of the source that is playing. Only one source plays at a time. */
internal object SpeechActivityRegistry {
    private val lock = SynchronizedObject()
    private var current: SpeechActivityTimeline? = null

    /** Starts a new, empty timeline for [sourceKey] unless one is already collecting for it. */
    fun begin(sourceKey: String): SpeechActivityTimeline = synchronized(lock) {
        current?.takeIf { it.sourceKey == sourceKey } ?: SpeechActivityTimeline(sourceKey).also { current = it }
    }

    fun forSource(sourceKey: String): SpeechActivityTimeline? = synchronized(lock) {
        current?.takeIf { it.sourceKey == sourceKey }
    }

    fun end(timeline: SpeechActivityTimeline) {
        synchronized(lock) {
            if (current === timeline) current = null
        }
    }
}
