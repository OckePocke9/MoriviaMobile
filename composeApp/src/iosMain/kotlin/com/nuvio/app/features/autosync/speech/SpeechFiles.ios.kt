package com.nuvio.app.features.autosync.speech

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSASCIIStringEncoding
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileHandle
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSUserDomainMask
import platform.Foundation.closeFile
import platform.Foundation.create
import platform.Foundation.fileHandleForReadingAtPath
import platform.Foundation.readDataOfLength
import platform.Foundation.seekToFileOffset

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal actual fun readSpeechFile(path: String, offset: Long, maxBytes: Int): SpeechFileChunk? {
    val manager = NSFileManager.defaultManager
    if (!manager.fileExistsAtPath(path)) return null
    val size = when (val value = manager.attributesOfItemAtPath(path, error = null)?.get("NSFileSize")) {
        is Long -> value
        is Number -> value.toLong()
        else -> return null
    }
    if (size <= offset) return SpeechFileChunk("", size)
    val handle = NSFileHandle.fileHandleForReadingAtPath(path) ?: return null
    return try {
        handle.seekToFileOffset(offset.toULong())
        val data = handle.readDataOfLength(minOf(maxBytes.toLong(), size - offset).toULong())
        val text = NSString.create(data = data, encoding = NSASCIIStringEncoding) as String?
        SpeechFileChunk(text.orEmpty(), size)
    } catch (_: Throwable) {
        null
    } finally {
        handle.closeFile()
    }
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun deleteSpeechFile(path: String) {
    NSFileManager.defaultManager.removeItemAtPath(path, error = null)
}

/** Caches/speech, created when missing. */
@OptIn(ExperimentalForeignApi::class)
internal fun iosSpeechFileDirectory(): String? {
    val caches = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true)
        .firstOrNull() as? String ?: return null
    val directory = "$caches/speech"
    return if (NSFileManager.defaultManager.createDirectoryAtPath(directory, true, null, null)) directory else null
}
