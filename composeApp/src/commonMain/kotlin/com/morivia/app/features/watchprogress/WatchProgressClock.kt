package com.morivia.app.features.watchprogress

internal expect object WatchProgressClock {
    fun nowEpochMs(): Long
}
