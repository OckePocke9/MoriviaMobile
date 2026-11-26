package com.morivia.app.features.autosync.speech

import kotlin.math.roundToLong

/**
 * Reads the text FFmpeg's `ametadata=mode=print` filter writes for libmpv's speech filter
 * ([MpvSpeechFilter]) and records the level of every audio frame in a [SpeechActivityTimeline]:
 *
 * ```
 * frame:12   pts:11520   pts_time:0.24
 * lavfi.astats.Overall.RMS_level=-31.42
 * ```
 *
 * Text arrives in arbitrary pieces while the file grows, so a partial last line is kept until
 * the rest arrives.
 */
internal class AmetadataSpeechReader(
    private val timeline: SpeechActivityTimeline,
) {
    private val pending = StringBuilder()
    private var frameTimeMs: Long? = null
    private var lastFrameDurationMs = DEFAULT_FRAME_MS
    private var pendingLevel: Float? = null
    private var pendingLevelTimeMs: Long? = null

    fun consume(text: String) {
        pending.append(text)
        while (true) {
            val end = pending.indexOf('\n')
            if (end < 0) break
            val line = pending.substring(0, end).trim()
            pending.deleteRange(0, end + 1)
            if (line.isNotEmpty()) consumeLine(line)
        }
        // A file that never ends lines must not grow this buffer without limit.
        if (pending.length > MAX_PENDING_CHARS) pending.clear()
    }

    /** The filter was restarted and its file rewritten from the start. */
    fun restart() {
        pending.clear()
        frameTimeMs = null
        pendingLevel = null
        pendingLevelTimeMs = null
    }

    private fun consumeLine(line: String) {
        if (line.startsWith("frame:")) {
            val ptsTime = PTS_TIME.find(line)?.groupValues?.get(1)?.toDoubleOrNull() ?: return
            val timeMs = (ptsTime * 1_000.0).roundToLong()
            flushPendingLevel(nextFrameTimeMs = timeMs)
            frameTimeMs = timeMs
            return
        }
        val separator = line.indexOf('=')
        if (separator <= 0) return
        if (!line.startsWith(RMS_KEY)) return
        val value = line.substring(separator + 1).trim()
        val level = when {
            value.equals("-inf", ignoreCase = true) -> SpeechActivityTimeline.SILENCE_DB
            else -> value.toFloatOrNull() ?: return
        }
        pendingLevel = level
        pendingLevelTimeMs = frameTimeMs
    }

    /** A frame's duration is known when the next frame's time arrives. */
    private fun flushPendingLevel(nextFrameTimeMs: Long) {
        val level = pendingLevel ?: return
        val startMs = pendingLevelTimeMs ?: return
        pendingLevel = null
        pendingLevelTimeMs = null
        val gapMs = nextFrameTimeMs - startMs
        val durationMs = if (gapMs in 1L..MAX_FRAME_MS) gapMs.also { lastFrameDurationMs = it } else lastFrameDurationMs
        timeline.record(startMs, durationMs, level)
    }

    private companion object {
        const val RMS_KEY = "lavfi.astats.Overall.RMS_level"
        const val DEFAULT_FRAME_MS = 20L
        const val MAX_FRAME_MS = 200L
        const val MAX_PENDING_CHARS = 64 * 1024
        val PTS_TIME = Regex("""pts_time:\s*(-?[0-9.]+)""")
    }
}
