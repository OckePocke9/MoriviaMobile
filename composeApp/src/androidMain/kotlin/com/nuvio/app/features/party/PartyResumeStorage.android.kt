package com.nuvio.app.features.party

import android.content.Context
import android.content.SharedPreferences

internal actual object PartyResumeStorage {
    private var preferences: SharedPreferences? = null

    fun initialize(context: Context) {
        preferences = context.getSharedPreferences("watch_party_resume", Context.MODE_PRIVATE)
    }

    actual fun load(account: String): String? = preferences?.getString(account, null)

    actual fun save(account: String, code: String?) {
        preferences?.edit()?.putString(account, code)?.apply()
    }
}
