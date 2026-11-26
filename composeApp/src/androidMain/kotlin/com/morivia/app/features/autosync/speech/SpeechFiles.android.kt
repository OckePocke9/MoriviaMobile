package com.morivia.app.features.autosync.speech

import java.io.File
import java.io.RandomAccessFile

internal actual fun readSpeechFile(path: String, offset: Long, maxBytes: Int): SpeechFileChunk? {
    val file = File(path)
    if (!file.exists()) return null
    return runCatching {
        RandomAccessFile(file, "r").use { access ->
            val size = access.length()
            if (size <= offset) return@use SpeechFileChunk("", size)
            access.seek(offset)
            val buffer = ByteArray(minOf(maxBytes.toLong(), size - offset).toInt())
            val read = access.read(buffer)
            SpeechFileChunk(if (read > 0) String(buffer, 0, read, Charsets.US_ASCII) else "", size)
        }
    }.getOrNull()
}

internal actual fun deleteSpeechFile(path: String) {
    runCatching { File(path).delete() }
}
