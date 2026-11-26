package com.morivia.app.core.tracking

import com.morivia.app.features.simkl.SimklAuthRepository
import com.morivia.app.features.simkl.SimklMutationRepository
import com.morivia.app.features.simkl.SimklLibraryRepository
import com.morivia.app.features.simkl.SimklProgressRepository
import com.morivia.app.features.simkl.SimklRatings
import com.morivia.app.features.simkl.SimklTrackingLibraryProvider
import com.morivia.app.features.simkl.SimklTrackingProgressProvider
import com.morivia.app.features.simkl.SimklWatchedSyncAdapter
import com.morivia.app.features.simkl.SimklSyncRepository
import com.morivia.app.features.tracking.TrackingProviderRegistry
import com.morivia.app.features.mdblist.MdbListTracker
import com.morivia.app.features.trakt.TraktAuthRepository
import com.morivia.app.features.trakt.TraktScrobbleRepository
import com.morivia.app.features.trakt.TraktTrackingLibraryProvider
import com.morivia.app.features.trakt.TraktTrackingProgressProvider
import com.morivia.app.features.watching.sync.TraktWatchedSyncAdapter

fun ensureTrackingProvidersRegistered() {
    MdbListTracker.register()
    TraktAuthRepository.descriptor
    TraktScrobbleRepository.ensureRegistered()
    SimklAuthRepository.descriptor
    SimklSyncRepository.state
    SimklLibraryRepository.uiState
    SimklProgressRepository.uiState
    SimklMutationRepository.ensureRegistered()
    TrackingProviderRegistry.registerLibraryProvider(TraktTrackingLibraryProvider)
    TrackingProviderRegistry.registerLibraryProvider(SimklTrackingLibraryProvider)
    TrackingProviderRegistry.registerWatchedProvider(TraktWatchedSyncAdapter)
    TrackingProviderRegistry.registerWatchedProvider(SimklWatchedSyncAdapter)
    TrackingProviderRegistry.registerProgressProvider(TraktTrackingProgressProvider)
    TrackingProviderRegistry.registerProgressProvider(SimklTrackingProgressProvider)
    TrackingProviderRegistry.registerRatingProvider(SimklRatings)
}
