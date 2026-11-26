package com.morivia.app.features.autosync.speech

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Bytes appended to a growing file since the last read. */
internal class SpeechFileChunk(val text: String, val fileSize: Long)

/** Reads up to [maxBytes] from [offset]; null when the file does not exist (yet). */
internal expect fun readSpeechFile(path: String, offset: Long, maxBytes: Int): SpeechFileChunk?

internal expect fun deleteSpeechFile(path: String)

/**
 * The libmpv audio filter that measures speech, shared by Android and iOS.
 *
 * FFmpeg's `astats` measures the level of every short audio frame and `ametadata` writes it with
 * the frame's media time to a file. The preferred graph measures a voice-band copy of the audio
 * and passes the original on untouched; the fallback measures the full band in place, which also
 * leaves the sound unchanged.
 */
internal object MpvSpeechFilter {
    const val LABEL = "moriviaspeech"

    fun voiceBand(path: String): String = labelled(
        "asplit[morivia_out][morivia_probe];" +
            "[morivia_probe]highpass=f=250,lowpass=f=3500,${measure(path)},anullsink;" +
            "[morivia_out]anull",
    )

    fun fullBand(path: String): String = labelled(measure(path))

    private fun measure(path: String): String =
        "asetnsamples=n=1024:p=0,astats=metadata=1:reset=1," +
            "ametadata=mode=print:key=lavfi.astats.Overall.RMS_level:file=$path"

    /** `%length%` quoting lets the graph contain brackets, commas and colons. */
    private fun labelled(graph: String): String =
        "@$LABEL:lavfi=graph=%${graph.encodeToByteArray().size}%$graph"
}

/**
 * Measures speech in libmpv's audio for one source at a time and feeds [SpeechActivityRegistry].
 *
 * Call [attach] once the file has loaded: libmpv then adds the filter to the running audio
 * chain and keeps the old chain when the filter cannot start, so the sound is never lost.
 * [addAudioFilter] and [removeAudioFilter] run on libmpv's thread and return false on failure.
 */
internal class MpvSpeechMonitor(
    private val scope: CoroutineScope,
    /** A writable cache directory for the speech file, or null. */
    private val directory: () -> String?,
    private val addAudioFilter: (String) -> Boolean,
    private val removeAudioFilter: (String) -> Unit,
) {
    private var job: Job? = null
    private var path: String? = null
    private var timeline: SpeechActivityTimeline? = null
    private var attached = false

    fun attach(sourceKey: String) {
        if (attached && timeline?.sourceKey == sourceKey) return
        detach()
        val folder = directory() ?: return
        val filePath = "$folder/mpv-speech-${sourceKey.hashCode().toUInt().toString(16)}.txt"
        deleteSpeechFile(filePath)
        val added = addAudioFilter(MpvSpeechFilter.voiceBand(filePath)) ||
            addAudioFilter(MpvSpeechFilter.fullBand(filePath))
        if (!added) return
        val speech = SpeechActivityRegistry.begin(sourceKey)
        attached = true
        path = filePath
        timeline = speech
        job = scope.launch(Dispatchers.IO) { follow(filePath, AmetadataSpeechReader(speech)) }
    }

    fun detach() {
        job?.cancel()
        job = null
        if (attached) removeAudioFilter("@${MpvSpeechFilter.LABEL}")
        attached = false
        timeline?.let(SpeechActivityRegistry::end)
        timeline = null
        path?.let(::deleteSpeechFile)
        path = null
    }

    private suspend fun follow(filePath: String, reader: AmetadataSpeechReader) {
        var offset = 0L
        while (scope.isActive) {
            val chunk = readSpeechFile(filePath, offset, MAX_READ_BYTES)
            if (chunk != null) {
                if (chunk.fileSize < offset) {
                    // libmpv rebuilt the filter (after a seek) and the file starts over.
                    reader.restart()
                    offset = 0L
                    continue
                }
                if (chunk.text.isNotEmpty()) {
                    reader.consume(chunk.text)
                    offset += chunk.text.encodeToByteArray().size
                    if (chunk.text.length >= MAX_READ_BYTES / 2) continue
                }
            }
            delay(POLL_MS)
        }
    }

    private companion object {
        const val POLL_MS = 1_000L
        const val MAX_READ_BYTES = 256 * 1024
    }
}
