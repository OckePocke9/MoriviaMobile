package com.nuvio.android

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nuvio.app.features.player.SubtitleFileCache
import com.nuvio.app.features.player.SubtitleInput
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Uses Android's real FileProvider; Windows host paths cannot model its URI roots. */
@RunWith(AndroidJUnit4::class)
class LocalSubtitleSharingTest {
    @Test
    fun localSubtitleCanBeReadThroughTheProductionFileProvider(): Unit = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val text = "1\n00:00:01,000 --> 00:00:02,000\nMorivia subtitle\n"
        val original = File.createTempFile("morivia-subtitle-", ".srt", context.filesDir)
        try {
            original.writeText(text)
            SubtitleFileCache.initialize(context)
            val tracks = requireNotNull(SubtitleFileCache.cacheSubtitles(
                listOf(SubtitleInput(original.toURI().toString(), "English", "en")),
            ))
            val uri = Uri.parse(tracks.single().url)
            assertEquals("content", uri.scheme)
            assertEquals("${context.packageName}.fileprovider", uri.authority)
            val cachedText = requireNotNull(context.contentResolver.openInputStream(uri))
                .bufferedReader().use { it.readText() }
            assertEquals(text, cachedText)
            SubtitleFileCache.clearCache()
            assertTrue(original.exists())
            assertEquals(text, original.readText())
        } finally {
            original.delete()
            SubtitleFileCache.clearCache()
        }
    }
}
