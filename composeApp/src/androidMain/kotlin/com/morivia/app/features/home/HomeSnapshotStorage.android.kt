package com.morivia.app.features.home

import android.content.Context
import android.content.SharedPreferences
import com.morivia.app.core.storage.ProfileScopedKey

internal actual object HomeSnapshotStorage {
    private const val preferencesName = "morivia_home_snapshot"

    private var preferences: SharedPreferences? = null

    fun initialize(context: Context) {
        preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
    }

    actual fun load(key: String): String? =
        preferences?.getString(ProfileScopedKey.of(key), null)

    actual fun save(key: String, payload: String) {
        preferences
            ?.edit()
            ?.putString(ProfileScopedKey.of(key), payload)
            ?.apply()
    }
}
