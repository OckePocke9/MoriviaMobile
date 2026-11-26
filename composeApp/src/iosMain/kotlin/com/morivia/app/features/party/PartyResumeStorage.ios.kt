package com.morivia.app.features.party

import platform.Foundation.NSUserDefaults

internal actual object PartyResumeStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun load(account: String): String? =
        defaults.stringForKey("watch_party_resume:$account")

    actual fun save(account: String, code: String?) {
        val key = "watch_party_resume:$account"
        if (code == null) defaults.removeObjectForKey(key)
        else defaults.setObject(code, forKey = key)
    }
}
