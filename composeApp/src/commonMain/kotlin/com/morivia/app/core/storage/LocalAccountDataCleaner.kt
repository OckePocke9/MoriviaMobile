package com.morivia.app.core.storage

import com.morivia.app.core.build.AppFeaturePolicy
import com.morivia.app.core.sync.SyncManager
import com.morivia.app.core.sync.ProfileSettingsSync
import com.morivia.app.core.tracking.ensureTrackingProvidersRegistered
import com.morivia.app.features.addons.AddonRepository
import com.morivia.app.features.catalog.CatalogRepository
import com.morivia.app.features.collection.CollectionMobileSettingsRepository
import com.morivia.app.features.collection.CollectionRepository
import com.morivia.app.features.details.MetaDetailsRepository
import com.morivia.app.features.details.MetaScreenSettingsRepository
import com.morivia.app.features.home.HomeCatalogSettingsRepository
import com.morivia.app.features.home.HomeRepository
import com.morivia.app.features.library.LibraryRepository
import com.morivia.app.features.membership.MemberAccessRepository
import com.morivia.app.features.library.LibraryDisplaySettingsRepository
import com.morivia.app.features.notifications.EpisodeReleaseNotificationsRepository
import com.morivia.app.features.player.PlayerLaunchStore
import com.morivia.app.features.player.PlayerSettingsRepository
import com.morivia.app.features.p2p.P2pSettingsRepository
import com.morivia.app.features.plugins.PluginRepository
import com.morivia.app.features.player.SubtitleRepository
import com.morivia.app.features.profiles.ProfileRepository
import com.morivia.app.features.profiles.MAX_PROFILES
import com.morivia.app.features.search.SearchRepository
import com.morivia.app.features.servers.ServerRepository
import com.morivia.app.features.settings.ThemeSettingsRepository
import com.morivia.app.features.streams.StreamContextStore
import com.morivia.app.features.streams.StreamBadgeSettingsRepository
import com.morivia.app.features.streams.StreamLaunchStore
import com.morivia.app.features.streams.StreamsRepository
import com.morivia.app.features.tracking.TrackingProviderRegistry
import com.morivia.app.features.tracking.TrackingSettingsRepository
import com.morivia.app.core.ui.CardDepthStyleRepository
import com.morivia.app.core.ui.PosterCardStyleRepository
import com.morivia.app.features.watchprogress.ContinueWatchingPreferencesRepository
import com.morivia.app.features.watchprogress.ContinueWatchingEnrichmentCache
import com.morivia.app.features.watchprogress.WatchProgressRepository
import com.morivia.app.features.watchprogress.WatchProgressSourceCoordinator
import com.morivia.app.features.watched.WatchedRepository

internal object LocalAccountDataCleaner {
    fun wipe() {
        ensureTrackingProvidersRegistered()
        TrackingProviderRegistry.removeStoredProfiles(1..MAX_PROFILES)
        SyncManager.cancelAccountSync()
        WatchProgressSourceCoordinator.clearLocalState()
        ProfileSettingsSync.clearAccountState()
        ContinueWatchingEnrichmentCache.clearLocalState()
        WatchProgressRepository.clearLocalState()
        WatchedRepository.clearLocalState()
        LibraryRepository.runAccountStorageWipe {
            wipePlatformStorage()
        }

        ProfileRepository.clearInMemory()
        MemberAccessRepository.clearLocalState()
        AddonRepository.clearLocalState()
        ServerRepository.clearLocalState()
        if (AppFeaturePolicy.pluginsEnabled) {
            PluginRepository.clearLocalState()
        }
        HomeRepository.clear()
        HomeCatalogSettingsRepository.clearLocalState()
        MetaScreenSettingsRepository.clearLocalState()
        com.morivia.app.features.shuffle.EpisodeShuffleRepository.clearLocalState()
        LibraryRepository.clearLocalState()
        LibraryDisplaySettingsRepository.clearLocalState()
        ContinueWatchingPreferencesRepository.clearLocalState()
        EpisodeReleaseNotificationsRepository.clearLocalState()
        CollectionMobileSettingsRepository.clearLocalState()
        CollectionRepository.clearLocalState()
        ThemeSettingsRepository.clearLocalState()
        PosterCardStyleRepository.clearLocalState()
        com.morivia.app.features.settings.MoriviaLayoutSettingsRepository.clearLocalState()
        CardDepthStyleRepository.clearLocalState()
        TrackingProviderRegistry.clearLocalState()
        TrackingSettingsRepository.clearLocalState()
        PlayerSettingsRepository.clearLocalState()
        StreamBadgeSettingsRepository.clearLocalState()
        P2pSettingsRepository.clearLocalState()
        CatalogRepository.clear()
        StreamsRepository.clear()
        MetaDetailsRepository.clear()
        SearchRepository.reset()
        SubtitleRepository.clear()
        PlayerLaunchStore.clear()
        StreamLaunchStore.clear()
        StreamContextStore.clear()
    }

    internal fun wipePlatformStorage(wipeStorage: () -> Unit = PlatformLocalAccountDataCleaner::wipe) {
        try {
            wipeStorage()
        } finally {
            ContinueWatchingEnrichmentCache.clearLocalState()
        }
    }
}

internal expect object PlatformLocalAccountDataCleaner {
    fun wipe()
}
