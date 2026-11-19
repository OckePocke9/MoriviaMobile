package com.nuvio.app.features.party

internal expect object PartyResumeStorage {
    fun load(account: String): String?
    fun save(account: String, code: String?)
}
