@file:androidx.annotation.OptIn(UnstableApi::class)

package com.nuvio.app.features.player

import android.os.SystemClock
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import com.nuvio.app.features.autosync.speech.SpeechActivityTimeline
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Tells [SpeechTapAudioProcessor] where in media time its audio starts.
 *
 * ExoPlayer flushes its audio processors when playback starts and on every seek, and the first
 * sample after a flush is the start or seek position. The player screen calls [startsAt] with
 * that position before preparing a source and on every position jump. A flush takes the newest
 * position nobody has used yet: one set before the flush, or one arriving shortly after it,
 * because the seek callback can come after the playback thread has flushed. A flush with no
 * new position (for example a format change) is not measured.
 */
internal class SpeechTapAnchor {
    @Volatile var timeline: SpeechActivityTimeline? = null
    private var positionMs = 0L
    private var setAtMs = 0L
    private var consumed = true

    @Synchronized
    fun startsAt(positionMs: Long) {
        this.positionMs = positionMs.coerceAtLeast(0L)
        setAtMs = SystemClock.elapsedRealtime()
        consumed = false
    }

    /** The unused start position for a flush at [flushedAtMs], or null while there is none. */
    @Synchronized
    fun take(flushedAtMs: Long): Long? {
        if (consumed) return null
        // A position set long after the flush belongs to a later flush.
        if (setAtMs - flushedAtMs > ANCHOR_AFTER_FLUSH_MS) return null
        consumed = true
        return positionMs
    }

    companion object {
        const val ANCHOR_AFTER_FLUSH_MS = 3_000L
    }
}

/**
 * Measures the voice band of the playing audio for subtitle sync and passes the audio on
 * unchanged. Every [SpeechActivityTimeline.BIN_MS] of mono audio, band-passed to the range of
 * the human voice, becomes one level in the anchor's [SpeechActivityTimeline]. Encoded
 * passthrough audio is not measured. Speed changes and audio delay are applied after this
 * processor, so frames here are in media time.
 */
internal class SpeechTapAudioProcessor(
    private val anchor: SpeechTapAnchor,
) : BaseAudioProcessor() {
    private var channels = 0
    private var sampleRate = 0
    private var framesPerBin = 1
    private var flushedAtMs = 0L
    private var startMs: Long? = null
    private var continuationStartMs: Long? = null
    private var framesSinceStart = 0L
    private var binFrames = 0
    private var binEnergy = 0.0
    private var measuring = false
    private val pendingLevels = ArrayList<Float>()
    private val highPass = Biquad()
    private val lowPass = Biquad()

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        return when (inputAudioFormat.encoding) {
            C.ENCODING_PCM_16BIT, C.ENCODING_PCM_FLOAT -> inputAudioFormat
            else -> AudioProcessor.AudioFormat.NOT_SET
        }
    }

    override fun onFlush() {
        channels = inputAudioFormat.channelCount
        sampleRate = inputAudioFormat.sampleRate
        framesPerBin = (sampleRate * SpeechActivityTimeline.BIN_MS / 1_000L).toInt().coerceAtLeast(1)
        // Without a new position (a format change, not a seek) the audio continues where it was.
        continuationStartMs = startMs?.let { start -> if (sampleRate > 0) start + framesSinceStart * 1_000L / sampleRate else null }
        flushedAtMs = SystemClock.elapsedRealtime()
        startMs = anchor.take(flushedAtMs)
        framesSinceStart = 0L
        binFrames = 0
        binEnergy = 0.0
        pendingLevels.clear()
        measuring = channels > 0 && sampleRate > 0 && anchor.timeline != null
        highPass.setHighPass(HIGH_PASS_HZ, sampleRate)
        lowPass.setLowPass(LOW_PASS_HZ, sampleRate)
    }

    override fun onReset() {
        measuring = false
        pendingLevels.clear()
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        if (!inputBuffer.hasRemaining()) return
        if (measuring) measure(inputBuffer.duplicate().order(ByteOrder.nativeOrder()))
        val outputBuffer = replaceOutputBuffer(inputBuffer.remaining())
        outputBuffer.put(inputBuffer)
        outputBuffer.flip()
    }

    private fun measure(buffer: ByteBuffer) {
        val pcm16 = inputAudioFormat.encoding == C.ENCODING_PCM_16BIT
        val bytesPerFrame = channels * if (pcm16) 2 else 4
        while (buffer.remaining() >= bytesPerFrame) {
            var mono = 0.0
            for (channel in 0 until channels) {
                mono += if (pcm16) buffer.short / 32_768.0 else buffer.float.toDouble()
            }
            mono /= channels
            val voice = lowPass.process(highPass.process(mono))
            binEnergy += voice * voice
            binFrames++
            if (binFrames == framesPerBin) {
                val rms = sqrt(binEnergy / binFrames)
                val levelDb = if (rms > 1e-9) (20.0 * log10(rms)).toFloat() else SpeechActivityTimeline.SILENCE_DB
                binFrames = 0
                binEnergy = 0.0
                if (!emit(levelDb)) {
                    measuring = false
                    return
                }
            }
        }
    }

    /** Records one bin, holding bins back until the start position is known. False stops measuring. */
    private fun emit(levelDb: Float): Boolean {
        val timeline = anchor.timeline ?: return false
        var start = startMs
        if (start == null) {
            start = anchor.take(flushedAtMs)
            if (start == null) {
                val waitedMs = SystemClock.elapsedRealtime() - flushedAtMs
                if (waitedMs <= SpeechTapAnchor.ANCHOR_AFTER_FLUSH_MS) {
                    if (pendingLevels.size < MAX_PENDING_BINS) pendingLevels += levelDb
                    return true
                }
                start = continuationStartMs
                if (start == null) {
                    pendingLevels.clear()
                    return false
                }
            }
            startMs = start
            for (pending in pendingLevels) record(timeline, start, pending)
            pendingLevels.clear()
        }
        record(timeline, start, levelDb)
        return true
    }

    private fun record(timeline: SpeechActivityTimeline, start: Long, levelDb: Float) {
        val binStartMs = start + framesSinceStart * 1_000L / sampleRate
        timeline.record(binStartMs, SpeechActivityTimeline.BIN_MS, levelDb)
        framesSinceStart += framesPerBin
    }

    /** A second-order filter (RBJ audio EQ cookbook) in direct form I. */
    private class Biquad {
        private var b0 = 1.0
        private var b1 = 0.0
        private var b2 = 0.0
        private var a1 = 0.0
        private var a2 = 0.0
        private var x1 = 0.0
        private var x2 = 0.0
        private var y1 = 0.0
        private var y2 = 0.0

        fun setHighPass(frequencyHz: Double, sampleRate: Int) = set(frequencyHz, sampleRate, highPass = true)
        fun setLowPass(frequencyHz: Double, sampleRate: Int) = set(frequencyHz, sampleRate, highPass = false)

        private fun set(frequencyHz: Double, sampleRate: Int, highPass: Boolean) {
            x1 = 0.0; x2 = 0.0; y1 = 0.0; y2 = 0.0
            if (sampleRate <= 0 || frequencyHz >= sampleRate / 2.0) {
                b0 = 1.0; b1 = 0.0; b2 = 0.0; a1 = 0.0; a2 = 0.0
                return
            }
            val omega = 2.0 * PI * frequencyHz / sampleRate
            val alpha = sin(omega) / (2.0 * Q)
            val cosine = cos(omega)
            val a0 = 1.0 + alpha
            if (highPass) {
                b0 = (1.0 + cosine) / 2.0 / a0
                b1 = -(1.0 + cosine) / a0
                b2 = (1.0 + cosine) / 2.0 / a0
            } else {
                b0 = (1.0 - cosine) / 2.0 / a0
                b1 = (1.0 - cosine) / a0
                b2 = (1.0 - cosine) / 2.0 / a0
            }
            a1 = -2.0 * cosine / a0
            a2 = (1.0 - alpha) / a0
        }

        fun process(x: Double): Double {
            val y = b0 * x + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2
            x2 = x1; x1 = x
            y2 = y1; y1 = y
            return y
        }

        private companion object {
            const val Q = 0.7071
        }
    }

    private companion object {
        const val HIGH_PASS_HZ = 250.0
        const val LOW_PASS_HZ = 3_500.0
        const val MAX_PENDING_BINS = 1_000 // 20 s
    }
}
