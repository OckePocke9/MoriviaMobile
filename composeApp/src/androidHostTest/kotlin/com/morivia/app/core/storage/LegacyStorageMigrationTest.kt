package com.morivia.app.core.storage

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import java.io.File
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** People keep their saved data when the old file and key names become Morivia (build 198). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class LegacyStorageMigrationTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val dataDir = File(context.applicationInfo.dataDir)
    private val prefsDir = File(dataDir, "shared_prefs")

    @Test
    fun oldPreferencesAndKeysAreRenamedAndKept() {
        prefsDir.mkdirs()
        File(prefsDir, "nuvio_player_settings.xml").writeText(
            """<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
    <string name="subtitle_size">large</string>
    <boolean name="nuvio_resume_prompt_was_in_player" value="true" />
    <string name="link">nuvio://meta?type=movie</string>
</map>
""",
        )
        File(prefsDir, "norivia_home_snapshot.xml").writeText("<map><string name=\"rows\">saved</string></map>")
        File(dataDir, "files/nuvio_plugin_scrapers").mkdirs()
        File(dataDir, "no_backup/morivia_storage_migration_v2").delete()

        LegacyStorageMigration.run(context)

        assertFalse(File(prefsDir, "nuvio_player_settings.xml").exists())
        val prefs = context.getSharedPreferences("morivia_player_settings", Context.MODE_PRIVATE)
        assertEquals("large", prefs.getString("subtitle_size", null))
        assertTrue(prefs.getBoolean("morivia_resume_prompt_was_in_player", false))
        // Values are never changed, only names.
        assertEquals("nuvio://meta?type=movie", prefs.getString("link", null))
        assertTrue(File(dataDir, "files/morivia_plugin_scrapers").isDirectory)
        assertEquals("saved", context.getSharedPreferences("morivia_home_snapshot", Context.MODE_PRIVATE).getString("rows", null))
    }

    @Test
    fun runsOnlyOnce() {
        LegacyStorageMigration.run(context)
        prefsDir.mkdirs()
        File(prefsDir, "nuvio_library.xml").writeText("<map />")

        LegacyStorageMigration.run(context)

        assertTrue(File(prefsDir, "nuvio_library.xml").exists())
    }
}
