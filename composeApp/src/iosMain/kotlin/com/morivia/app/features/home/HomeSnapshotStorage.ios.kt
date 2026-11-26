package com.morivia.app.features.home

import com.morivia.app.core.storage.ProfileScopedKey
import platform.Foundation.NSUserDefaults

internal actual object HomeSnapshotStorage {
    private const val prefix = "morivia_home_snapshot_"

    actual fun load(key: String): String? =
        NSUserDefaults.standardUserDefaults.stringForKey(ProfileScopedKey.of(prefix + key))

    actual fun save(key: String, payload: String) {
        NSUserDefaults.standardUserDefaults.setObject(payload, forKey = ProfileScopedKey.of(prefix + key))
    }
}
