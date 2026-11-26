package com.morivia.app.features.mdblist

import com.morivia.app.features.tracking.TrackingHistoryItem
import com.morivia.app.features.tracking.TrackingHistoryWriter
import com.morivia.app.features.tracking.TrackingMediaReference
import com.morivia.app.features.tracking.TrackingProviderId
import com.morivia.app.features.tracking.TrackingScrobbleAction
import com.morivia.app.features.tracking.TrackingScrobbleEvent
import com.morivia.app.features.tracking.TrackingScrobbler
import com.morivia.app.features.tracking.TrackingSeekScrobblePolicy
import kotlinx.coroutines.CancellationException

class MdbListTrackingWrites(
    private val sync: MdbListSyncRepository,
    private val history: MdbListHistoryService,
    private val scrobble: MdbListScrobbleService,
    private val episodeMappings: MdbListEpisodeMappingService,
) : TrackingHistoryWriter, TrackingScrobbler {
    override val providerId = TrackingProviderId.MDBLIST
    override val seekScrobblePolicy = TrackingSeekScrobblePolicy.STOP_AND_RESTART

    // MDBList reads episode numbers in TMDB's numbering, which can differ from the addon's.
    override suspend fun addToHistory(profileId: Int, items: Collection<TrackingHistoryItem>) =
        scope(profileId).let { scope -> history.add(scope, items.map { it.copy(media = it.media.inMdbListNumbering()) }) }

    override suspend fun removeFromHistory(profileId: Int, items: Collection<TrackingMediaReference>) =
        scope(profileId).let { scope -> history.remove(scope, items.map { it.inMdbListNumbering() }) }

    override suspend fun scrobble(profileId: Int, action: TrackingScrobbleAction, event: TrackingScrobbleEvent) =
        scope(profileId).let { scope -> scrobble.scrobble(scope, action, event.copy(media = event.media.inMdbListNumbering())) }

    private suspend fun TrackingMediaReference.inMdbListNumbering() =
        episodeMappings.toMdbListNumbering(this, sync.currentProjection())

    private fun scope(profileId: Int): MdbListAuthScope = sync.currentScope().also {
        if (it.profileId != profileId) throw CancellationException("MDBList profile changed")
    }
}
