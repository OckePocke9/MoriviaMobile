package com.morivia.app.core.storage

import platform.Foundation.NSFileManager
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSUserDefaults

/**
 * Saved data used to be named after the app Morivia was forked from. Since local test build 198
 * (owner, 2026-11-26) it is named Morivia. This moves the old folders and settings once, before
 * the app reads them, so people keep their downloads and settings when they update.
 */
@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
internal object LegacyStorageMigration {
    private const val OLD = "nuvio"
    private const val NEW = "morivia"
    private const val DONE_KEY = "morivia_storage_migration_v2"

    fun run() {
        val defaults = NSUserDefaults.standardUserDefaults
        if (defaults.boolForKey(DONE_KEY)) return

        // Settings saved under keys that start with the old prefix.
        defaults.dictionaryRepresentation().keys.forEach { key ->
            val name = key as? String ?: return@forEach
            val old = listOf(OLD, "norivia").firstOrNull { name.startsWith("${it}_") } ?: return@forEach
            val newName = NEW + name.removePrefix(old)
            if (defaults.objectForKey(newName) == null) {
                defaults.setObject(defaults.objectForKey(name), forKey = newName)
            }
            defaults.removeObjectForKey(name)
        }

        // The MDBList sync settings have their own settings file.
        val oldSuite = "${OLD}_mdblist_sync"
        val newSuite = "${NEW}_mdblist_sync"
        defaults.persistentDomainForName(oldSuite)?.let { saved ->
            if (defaults.persistentDomainForName(newSuite) == null) {
                defaults.setPersistentDomain(saved, forName = newSuite)
            }
            defaults.removePersistentDomainForName(oldSuite)
        }

        val home = NSHomeDirectory().trimEnd('/')
        move("$home/Documents/${OLD}_downloads", "$home/Documents/${NEW}_downloads")
        move("$home/Library/Application Support/${OLD}_plugin_scrapers", "$home/Library/Application Support/${NEW}_plugin_scrapers")
        move("$home/Library/Application Support/NuvioMembership", "$home/Library/Application Support/MoriviaMembership")
        move("$home/Library/Caches/NuvioMemberBackgrounds", "$home/Library/Caches/MoriviaMemberBackgrounds")
        move("$home/Library/Caches/NuvioMemberAvatars", "$home/Library/Caches/MoriviaMemberAvatars")

        defaults.setBool(true, forKey = DONE_KEY)
    }

    private fun move(oldPath: String, newPath: String) {
        val files = NSFileManager.defaultManager
        if (!files.fileExistsAtPath(oldPath) || files.fileExistsAtPath(newPath)) return
        files.moveItemAtPath(oldPath, toPath = newPath, error = null)
    }
}
