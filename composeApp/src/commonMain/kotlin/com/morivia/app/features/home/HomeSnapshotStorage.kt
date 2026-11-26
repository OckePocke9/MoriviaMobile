package com.morivia.app.features.home

/**
 * What Home showed last time, kept on the device per profile (owner, 2026-11-25): the rows appear
 * the moment a profile is picked and are then refreshed quietly.
 */
internal expect object HomeSnapshotStorage {
    fun load(key: String): String?
    fun save(key: String, payload: String)
}
