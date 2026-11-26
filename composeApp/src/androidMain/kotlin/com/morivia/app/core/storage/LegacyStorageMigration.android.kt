package com.morivia.app.core.storage

import android.app.NotificationManager
import android.app.job.JobScheduler
import android.content.Context
import android.os.Build
import java.io.File

/**
 * Saved data used to be named after the app Morivia was forked from. Since local test build 198
 * (owner, 2026-11-26) it is named Morivia. This moves everything once, before anything reads it,
 * so people keep their profiles, settings, sign-ins, library and downloads when they update.
 *
 * The old prefix is only written here, because this is the one place that has to find the old
 * files. It runs from [com.morivia.app.MoriviaApplication.attachBaseContext], which comes before
 * every content provider and every screen.
 */
internal object LegacyStorageMigration {
    private const val OLD = "nuvio"
    private const val NEW = "morivia"
    /** Earlier names: the original app's, and Norivia, which the app was briefly called (build 199). */
    private val OLD_NAMES = listOf(OLD, "norivia")
    private const val DONE_MARKER = "morivia_storage_migration_v2"

    fun run(context: Context) {
        val dataDir = File(context.applicationInfo.dataDir)
        val marker = File(dataDir, "no_backup/$DONE_MARKER")
        if (marker.exists()) return
        runCatching { moveSharedPreferences(File(dataDir, "shared_prefs")) }
        runCatching { moveDirectory(File(dataDir, "files"), "${OLD}_plugin_scrapers", "${NEW}_plugin_scrapers") }
        runCatching {
            File(dataDir, "cache").listFiles()
                ?.filter { it.name.startsWith("${OLD}_vod_cache") }
                ?.forEach { it.deleteRecursively() }
        }
        runCatching {
            marker.parentFile?.mkdirs()
            marker.writeText("done")
        }
    }

    /** Old notification channel and download jobs, which need the system services of a ready app. */
    fun cleanUpSystemEntries(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            runCatching {
                context.getSystemService(NotificationManager::class.java)
                    ?.deleteNotificationChannel("${OLD}_playback")
            }
        }
        if (Build.VERSION.SDK_INT >= 34) {
            runCatching {
                context.getSystemService(JobScheduler::class.java)
                    ?.forNamespace("$OLD-downloads")
                    ?.cancelAll()
            }
        }
    }

    private fun moveSharedPreferences(directory: File) {
        val files = directory.listFiles() ?: return
        for (file in files) {
            if (!file.isFile || !(file.name.endsWith(".xml") || file.name.endsWith(".xml.bak"))) continue
            val text = file.readText(Charsets.UTF_8)
            // Keys that start with an old prefix, for example the resume-prompt keys.
            val renamedKeys = OLD_NAMES.fold(text) { content, old -> content.replace("name=\"${old}_", "name=\"${NEW}_") }
            val oldName = OLD_NAMES.firstOrNull { file.name.startsWith("${it}_") }
            val target = if (oldName != null) {
                File(directory, NEW + file.name.removePrefix(oldName))
            } else {
                file
            }
            if (target != file && target.exists()) {
                // Already moved earlier; the old copy is stale.
                file.delete()
                continue
            }
            if (renamedKeys != text) target.writeText(renamedKeys, Charsets.UTF_8)
            if (target != file) {
                if (renamedKeys == text) file.renameTo(target) else file.delete()
            }
        }
    }

    private fun moveDirectory(parent: File, oldName: String, newName: String) {
        val old = File(parent, oldName)
        val new = File(parent, newName)
        if (old.exists() && !new.exists()) old.renameTo(new)
    }
}
