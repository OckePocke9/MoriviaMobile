package com.morivia.app.features.watchprogress

import com.morivia.app.features.tracking.WatchProgressSource

internal fun projectWatchProgressSourceEntries(
    source: WatchProgressSource,
    moriviaEntries: Collection<WatchProgressEntry>,
    providerEntries: Collection<WatchProgressEntry>,
): List<WatchProgressEntry> = if (source.providerId == null) {
    moriviaEntries.toList()
} else {
    providerEntries.toList()
}
