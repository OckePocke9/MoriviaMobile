package com.nuvio.app.features.autosync.speech

import com.nuvio.app.features.player.SubtitleSyncCue
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AudioSubtitleAlignerTest {

    /** Dialogue lines at irregular times, like a film: 1-4 s lines with 1-8 s gaps. */
    private fun dialogue(durationMs: Long, seed: Int = 7): List<SubtitleSyncCue> {
        val random = Random(seed)
        val cues = ArrayList<SubtitleSyncCue>()
        var time = 5_000L
        while (time < durationMs) {
            val length = 1_000L + random.nextLong(3_000L)
            cues += SubtitleSyncCue(startTimeMs = time, endTimeMs = time + length, text = "line ${cues.size}")
            time += length + 1_000L + random.nextLong(7_000L)
        }
        return cues
    }

    /**
     * Audio where the cues are spoken: loud in the voice band while a line is spoken, with
     * pauses between words, quiet background otherwise, music bursts and random noise.
     */
    private fun audioFor(
        spoken: List<SubtitleSyncCue>,
        fromMs: Long,
        toMs: Long,
        seed: Int = 11,
    ): SpeechActivityTimeline {
        val random = Random(seed)
        val timeline = SpeechActivityTimeline("test")
        var time = fromMs
        while (time < toMs) {
            val speaking = spoken.any { time >= it.startTimeMs && time < it.endTimeMs }
            val wordPause = speaking && random.nextInt(8) == 0
            val music = !speaking && (time / 15_000L) % 4 == 0L
            val level = when {
                speaking && !wordPause -> -24f + random.nextFloat() * 6f
                music -> -36f + random.nextFloat() * 8f
                else -> -52f + random.nextFloat() * 8f
            }
            timeline.record(time, SpeechActivityTimeline.BIN_MS, level)
            time += SpeechActivityTimeline.BIN_MS
        }
        return timeline
    }

    @Test
    fun `finds a constant delay`() {
        val subtitle = dialogue(30 * 60_000L)
        // The speech comes 3.4 s after the subtitle's lines.
        val spoken = subtitle.map { it.copy(startTimeMs = it.startTimeMs + 3_400, endTimeMs = it.endTimeMs + 3_400) }
        val snapshot = assertNotNull(audioFor(spoken, 0L, 5 * 60_000L).snapshot())

        val alignment = assertNotNull(AudioSubtitleAligner.align(snapshot, subtitle))

        assertEquals(1.0, alignment.scale)
        assertTrue(abs(alignment.shiftMs - 3_400L) <= 40L, "shift was ${alignment.shiftMs}")
    }

    @Test
    fun `finds a negative delay from the middle of the film`() {
        val subtitle = dialogue(60 * 60_000L, seed = 3)
        val spoken = subtitle.map { it.copy(startTimeMs = it.startTimeMs - 12_000, endTimeMs = it.endTimeMs - 12_000) }
        val snapshot = assertNotNull(audioFor(spoken, 30 * 60_000L, 34 * 60_000L).snapshot())

        val alignment = assertNotNull(AudioSubtitleAligner.align(snapshot, subtitle))

        assertTrue(abs(alignment.shiftMs + 12_000L) <= 40L, "shift was ${alignment.shiftMs}")
    }

    @Test
    fun `finds a frame rate mismatch`() {
        val subtitle = dialogue(60 * 60_000L, seed = 5)
        // A subtitle timed for 25 fps on a 23.976 fps video: speech runs 25 / 23.976 slower.
        val scale = 25.0 / 23.976
        val spoken = subtitle.map {
            it.copy(startTimeMs = (it.startTimeMs * scale).toLong(), endTimeMs = (it.endTimeMs * scale).toLong())
        }
        val snapshot = assertNotNull(audioFor(spoken, 20 * 60_000L, 30 * 60_000L).snapshot())

        val retimed = assertNotNull(AudioSubtitleAligner.retime(snapshot, subtitle))

        assertTrue(abs(retimed.alignmentScale - scale) < 1e-6, "scale was ${retimed.alignmentScale}")
        val cue = subtitle.first { it.startTimeMs > 25 * 60_000L }
        val mapped = retimed.cues[subtitle.indexOf(cue)].startTimeMs
        assertTrue(abs(mapped - (cue.startTimeMs * scale).toLong()) <= 60L, "mapped $mapped")
    }

    @Test
    fun `leaves a subtitle that is already in sync`() {
        val subtitle = dialogue(30 * 60_000L, seed = 9)
        val snapshot = assertNotNull(audioFor(subtitle, 0L, 5 * 60_000L).snapshot())

        assertNull(AudioSubtitleAligner.retime(snapshot, subtitle))
    }

    @Test
    fun `refuses a subtitle that belongs to another film`() {
        val subtitle = dialogue(30 * 60_000L, seed = 21)
        val otherFilm = dialogue(30 * 60_000L, seed = 99)
        val snapshot = assertNotNull(audioFor(otherFilm, 0L, 5 * 60_000L).snapshot())

        assertNull(AudioSubtitleAligner.align(snapshot, subtitle))
    }

    @Test
    fun `waits for enough audio`() {
        val subtitle = dialogue(30 * 60_000L)
        val snapshot = assertNotNull(audioFor(subtitle, 0L, 60_000L).snapshot())

        assertNull(AudioSubtitleAligner.align(snapshot, subtitle))
    }

    @Test
    fun `reads levels written by FFmpeg ametadata`() {
        val timeline = SpeechActivityTimeline("mpv")
        val reader = AmetadataSpeechReader(timeline)
        val text = buildString {
            for (frame in 0 until 100) {
                append("frame:$frame    pts:${frame * 1024}    pts_time:${frame * 0.021333}\n")
                append("lavfi.astats.Overall.RMS_level=${if (frame % 2 == 0) "-20.5" else "-inf"}\n")
            }
        }
        // Text arrives in arbitrary pieces.
        text.chunked(37).forEach(reader::consume)

        val snapshot = assertNotNull(timeline.snapshot())
        assertEquals(0, snapshot.firstBin)
        assertEquals(-20.5f, snapshot.levelsDb[0])
        // The 99 complete frames cover about 2.1 s; the last waits for the next frame's time.
        assertTrue(timeline.observedDurationMs() in 2_000L..2_200L, "observed ${timeline.observedDurationMs()}")
    }
}
