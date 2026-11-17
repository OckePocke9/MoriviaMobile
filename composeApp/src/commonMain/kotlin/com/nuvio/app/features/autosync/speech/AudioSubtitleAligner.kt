package com.nuvio.app.features.autosync.speech

import com.nuvio.app.features.autosync.AutoSyncRetimedCue
import com.nuvio.app.features.autosync.AutoSyncTimelineRetimeResult
import com.nuvio.app.features.player.SubtitleSyncCue
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong
import kotlin.math.sqrt

/**
 * Lines a subtitle up with the speech in the audio that has been played.
 *
 * The measured audio levels become a speech pattern: +1 where someone speaks, -1 where nobody
 * does, 0 where nothing was measured. For every frame-rate ratio a subtitle is commonly made
 * for (for example 25 fps subtitles on a 23.976 fps video) and every shift within
 * [MAX_SHIFT_MS], the aligner adds up the pattern inside the subtitle's cues. The best
 * combination wins when it stands out clearly from all other shifts; otherwise the subtitle
 * keeps its timing. The result is a linear transform `new = scale * old + shift`.
 */
internal object AudioSubtitleAligner {
    private const val BIN_MS = SpeechActivityTimeline.BIN_MS
    const val MAX_SHIFT_MS = 90_000L

    /** Measured audio needed before an attempt, and the cues it must contain. */
    const val MIN_OBSERVED_MS = 120_000L
    const val MIN_CUES_IN_WINDOW = 20
    const val MIN_SPEECH_BINS = 1_000 // 20 s of speech

    /** Level spread needed to tell speech from quiet passages. */
    private const val MIN_LEVEL_RANGE_DB = 8f
    private const val SPEECH_THRESHOLD_FRACTION = 0.42f
    private const val CLOSE_SPEECH_GAP_BINS = 12 // 240 ms
    private const val MIN_SPEECH_RUN_BINS = 5 // 100 ms

    /** The winning shift must stand this many standard deviations above all shifts. */
    private const val MIN_PEAK_Z = 6.0

    /** ... and this far above the best shift more than [SECOND_PEAK_EXCLUSION_MS] away. */
    private const val MIN_PEAK_MARGIN_Z = 1.2
    private const val SECOND_PEAK_EXCLUSION_MS = 2_000L

    /** A frame-rate correction must beat no correction by this share of its score. */
    private const val SCALE_PREFERENCE = 0.03

    /** Corrections smaller than this everywhere leave the subtitle as it is. */
    const val MIN_USEFUL_SHIFT_MS = 120.0

    /** Ratios between the common frame rates 23.976, 24 and 25 fps, and no change. */
    val SCALES = doubleArrayOf(
        1.0,
        25.0 / 23.976, 23.976 / 25.0,
        25.0 / 24.0, 24.0 / 25.0,
        24.0 / 23.976, 23.976 / 24.0,
    )

    data class Alignment(
        val scale: Double,
        val shiftMs: Long,
        val peakZ: Double,
        val marginZ: Double,
        val cuesInWindow: Int,
    )

    /** The full retime result the AutoSync apply path expects, or null when unsure or already in sync. */
    fun retime(snapshot: SpeechActivityTimeline.Snapshot, cues: List<SubtitleSyncCue>): AutoSyncTimelineRetimeResult? {
        val alignment = align(snapshot, cues) ?: return null
        fun map(timeMs: Long): Long = (alignment.scale * timeMs + alignment.shiftMs).roundToLong().coerceAtLeast(0L)
        val maxShift = cues.maxOfOrNull { cue -> abs(map(cue.startTimeMs) - cue.startTimeMs) } ?: 0L
        if (maxShift < MIN_USEFUL_SHIFT_MS) return null
        // Same order as [cues]: the players map retimed cues to their own cues by index.
        val retimed = cues.map { cue ->
            AutoSyncRetimedCue(
                originalStartTimeMs = cue.startTimeMs,
                originalEndTimeMs = cue.endTimeMs,
                startTimeMs = map(cue.startTimeMs),
                endTimeMs = map(cue.endTimeMs),
            )
        }
        return AutoSyncTimelineRetimeResult(
            cues = retimed,
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
            alignmentSource = "audio",
            alignmentScale = alignment.scale,
            alignmentInterceptMs = alignment.shiftMs.toDouble(),
            activityScore = alignment.peakZ,
            activityMargin = alignment.marginZ,
        )
    }

    fun align(
        snapshot: SpeechActivityTimeline.Snapshot,
        cues: List<SubtitleSyncCue>,
        maxShiftMs: Long = MAX_SHIFT_MS,
        scales: DoubleArray = SCALES,
        minObservedMs: Long = MIN_OBSERVED_MS,
        minCues: Int = MIN_CUES_IN_WINDOW,
        minSpeechBins: Int = MIN_SPEECH_BINS,
        minPeakZ: Double = MIN_PEAK_Z,
        minMarginZ: Double = MIN_PEAK_MARGIN_Z,
    ): Alignment? {
        if (snapshot.observedBins.toLong() * BIN_MS < minObservedMs) return null
        val maxShiftBins = (maxShiftMs / BIN_MS).toInt()
        val pattern = speechPattern(snapshot.levelsDb, minSpeechBins) ?: return null
        val firstBin = snapshot.firstBin
        val binCount = pattern.size
        // prefix[i] = sum of pattern[0 until i]
        val prefix = IntArray(binCount + 1)
        var speechBins = 0
        for (i in 0 until binCount) {
            prefix[i + 1] = prefix[i] + pattern[i]
            if (pattern[i] > 0) speechBins++
        }
        if (speechBins < minSpeechBins) return null
        fun sumOver(startBin: Int, endBin: Int): Int {
            val from = (startBin - firstBin).coerceIn(0, binCount)
            val to = (endBin - firstBin).coerceIn(0, binCount)
            return if (to > from) prefix[to] - prefix[from] else 0
        }

        val validCues = cues.filter { it.endTimeMs > it.startTimeMs && it.startTimeMs >= 0L }
        if (validCues.size < minCues) return null
        val lags = 2 * maxShiftBins + 1
        val windowStartMs = firstBin * BIN_MS
        val windowEndMs = (firstBin + binCount) * BIN_MS

        data class ScaleResult(val scale: Double, val lag: Int, val score: Double, val z: Double, val marginZ: Double, val cues: Int)
        val results = ArrayList<ScaleResult>()
        val scores = DoubleArray(lags)
        for (scale in scales) {
            // Cue bins at this scale, keeping only cues that some shift can move into the window.
            val starts = ArrayList<Int>()
            val ends = ArrayList<Int>()
            for (cue in validCues) {
                val s = (cue.startTimeMs * scale).roundToLong()
                val e = (cue.endTimeMs * scale).roundToLong()
                if (e + maxShiftMs < windowStartMs || s - maxShiftMs > windowEndMs) continue
                starts += (s / BIN_MS).toInt()
                ends += (e / BIN_MS).toInt()
            }
            if (starts.size < minCues) continue
            var bestLag = 0
            var bestScore = Double.NEGATIVE_INFINITY
            var sum = 0.0
            var sumSquares = 0.0
            for (index in 0 until lags) {
                val lag = index - maxShiftBins
                var score = 0
                for (c in starts.indices) score += sumOver(starts[c] + lag, ends[c] + lag)
                val value = score.toDouble()
                scores[index] = value
                sum += value
                sumSquares += value * value
                if (value > bestScore) {
                    bestScore = value
                    bestLag = lag
                }
            }
            val mean = sum / lags
            val deviation = sqrt(max(sumSquares / lags - mean * mean, 1e-9))
            val exclusion = (SECOND_PEAK_EXCLUSION_MS / BIN_MS).toInt()
            var secondScore = Double.NEGATIVE_INFINITY
            for (index in 0 until lags) {
                if (abs(index - maxShiftBins - bestLag) <= exclusion) continue
                secondScore = max(secondScore, scores[index])
            }
            // The cues that actually land in the measured window at the winning shift.
            var cuesInWindow = 0
            for (c in starts.indices) {
                val s = starts[c] + bestLag
                if (s >= firstBin && s < firstBin + binCount) cuesInWindow++
            }
            results += ScaleResult(
                scale = scale,
                lag = bestLag,
                score = bestScore,
                z = (bestScore - mean) / deviation,
                marginZ = (bestScore - secondScore) / deviation,
                cues = cuesInWindow,
            )
        }
        if (results.isEmpty()) return null

        val unscaled = results.firstOrNull { it.scale == 1.0 }
        val bestScaled = results.maxByOrNull { it.score } ?: return null
        val chosen = when {
            unscaled == null -> bestScaled
            bestScaled.scale == 1.0 -> unscaled
            bestScaled.score >= unscaled.score + abs(unscaled.score) * SCALE_PREFERENCE -> bestScaled
            else -> unscaled
        }
        if (chosen.cues < minCues) return null
        if (chosen.z < minPeakZ || chosen.marginZ < minMarginZ) return null
        return Alignment(
            scale = chosen.scale,
            shiftMs = chosen.lag * BIN_MS,
            peakZ = chosen.z,
            marginZ = chosen.marginZ,
            cuesInWindow = chosen.cues,
        )
    }

    /**
     * +1 for speech, -1 for no speech and 0 for unmeasured bins. The threshold sits between the
     * quiet floor and the loud passages of this film, so it adapts to every mix and volume.
     */
    internal fun speechPattern(levelsDb: FloatArray, minMeasuredBins: Int = MIN_SPEECH_BINS): IntArray? {
        val measured = levelsDb.filter { !it.isNaN() }.sorted()
        if (measured.size < minMeasuredBins) return null
        val floor = measured[(measured.size * 0.2).toInt()]
        val loud = measured[min(measured.size - 1, (measured.size * 0.95).toInt())]
        if (loud - floor < MIN_LEVEL_RANGE_DB) return null
        val threshold = floor + (loud - floor) * SPEECH_THRESHOLD_FRACTION

        val pattern = IntArray(levelsDb.size) { index ->
            val level = levelsDb[index]
            when {
                level.isNaN() -> 0
                level >= threshold -> 1
                else -> -1
            }
        }
        // Words are separated by short pauses; a sentence counts as one stretch of speech.
        var lastSpeech = -1
        for (i in pattern.indices) {
            if (pattern[i] != 1) continue
            if (lastSpeech >= 0 && i - lastSpeech in 2..CLOSE_SPEECH_GAP_BINS) {
                for (j in lastSpeech + 1 until i) if (pattern[j] == -1) pattern[j] = 1
            }
            lastSpeech = i
        }
        // Clicks and bangs shorter than a syllable are not speech.
        var runStart = -1
        for (i in 0..pattern.size) {
            val speaking = i < pattern.size && pattern[i] == 1
            if (speaking && runStart < 0) runStart = i
            if (!speaking && runStart >= 0) {
                if (i - runStart < MIN_SPEECH_RUN_BINS) for (j in runStart until i) pattern[j] = -1
                runStart = -1
            }
        }
        return pattern
    }
}
