package com.morivia.app.features.downloads

internal expect object DownloadsClock {
    fun nowEpochMs(): Long
}
