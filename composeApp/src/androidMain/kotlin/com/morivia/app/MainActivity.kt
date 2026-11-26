package com.morivia.app

import android.content.Intent
import android.content.res.Configuration
import com.morivia.app.features.autosync.AutoSyncPreferencesAndroid
import android.os.Bundle
import androidx.activity.compose.setContent

import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.morivia.app.core.auth.AuthStorage
import com.morivia.app.core.network.ServerConfigurationStorage
import com.morivia.app.core.diagnostics.SentryInitializer
import com.morivia.app.core.deeplink.handleAppUrl
import com.morivia.app.core.storage.PlatformLocalAccountDataCleaner
import com.morivia.app.core.sync.SyncClientIdentityStorage
import com.morivia.app.features.addons.AddonHttpClientProvider
import com.morivia.app.features.addons.AddonStorage
import com.morivia.app.features.collection.CollectionMobileSettingsStorage
import com.morivia.app.features.collection.CollectionStorage
import com.morivia.app.features.debrid.DebridSettingsStorage
import com.morivia.app.features.downloads.DownloadsLiveStatusPlatform
import com.morivia.app.features.downloads.DownloadsPlatformDownloader
import com.morivia.app.features.downloads.DownloadsStorage
import com.morivia.app.features.library.LibraryDisplaySettingsStorage
import com.morivia.app.features.membership.MemberAssetStorage
import com.morivia.app.features.library.LibraryStorage
import com.morivia.app.features.details.MetaScreenSettingsStorage
import com.morivia.app.features.home.HomeCatalogSettingsStorage
import com.morivia.app.features.mdblist.MdbListSettingsStorage
import com.morivia.app.features.notifications.EpisodeReleaseNotificationPlatform
import com.morivia.app.features.notifications.EpisodeReleaseNotificationsStorage
import com.morivia.app.features.player.PlayerSettingsStorage
import com.morivia.app.features.player.PlayerTrackPreferenceStorage
import com.morivia.app.features.player.ExternalPlayerPlatform
import com.morivia.app.features.player.SubtitleFileCache
import com.morivia.app.features.player.PlayerPictureInPictureManager
import com.morivia.app.features.player.PipRemoteActionReceiver
import com.morivia.app.features.p2p.P2pSettingsStorage
import com.morivia.app.features.p2p.P2pStreamingEngine
import com.morivia.app.features.plugins.PluginStorage
import com.morivia.app.features.profiles.AvatarStorage
import com.morivia.app.features.profiles.ProfilePinCacheStorage
import com.morivia.app.features.profiles.ProfileStorage
import com.morivia.app.features.details.SeasonViewModeStorage
import com.morivia.app.features.search.DiscoverSelectionStorage
import com.morivia.app.features.search.SearchHistoryStorage
import com.morivia.app.features.settings.SentrySettingsStorage
import com.morivia.app.features.settings.AppIconPlatform
import com.morivia.app.features.settings.ThemeSettingsStorage
import com.morivia.app.features.trakt.TraktAuthStorage
import com.morivia.app.features.trakt.TraktCommentsStorage
import com.morivia.app.features.trakt.TraktLibraryStorage
import com.morivia.app.features.trakt.TraktSettingsStorage
import com.morivia.app.features.mdblist.PlatformMdbListAuthPersistence
import com.morivia.app.features.mdblist.PlatformMdbListSyncStorage
import com.morivia.app.features.servers.ServerStorage
import com.morivia.app.features.simkl.SimklAuthStorage
import com.morivia.app.features.simkl.SimklSyncStorage
import com.morivia.app.features.tmdb.TmdbSettingsStorage
import com.morivia.app.features.updater.AndroidAppUpdaterPlatform
import com.morivia.app.core.ui.CardDepthStyleStorage
import com.morivia.app.core.ui.PosterCardStyleStorage
import com.morivia.app.core.poster.CustomPosterUrlStorage
import com.morivia.app.features.watched.WatchedStorage
import com.morivia.app.features.streams.StreamLinkCacheStorage
import com.morivia.app.features.streams.StreamBadgeSettingsStorage
import com.morivia.app.features.streams.BingeGroupCacheStorage
import com.morivia.app.features.watchprogress.ContinueWatchingEnrichmentStorage
import com.morivia.app.features.watchprogress.ContinueWatchingPreferencesStorage
import com.morivia.app.features.watchprogress.ResumePromptStorage
import com.morivia.app.features.watchprogress.WatchProgressStorage

open class MainActivity : AppCompatActivity() {
    private var pipRemoteActionReceiver: PipRemoteActionReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.dark(
                scrim = 0xFF020404.toInt(),
            ),
        )
        ThemeSettingsStorage.initialize(applicationContext)
        AppIconPlatform.initialize(applicationContext)
        SentrySettingsStorage.initialize(applicationContext)
        SentryInitializer.start(application)
        super.onCreate(savedInstanceState)
        AutoSyncPreferencesAndroid.initialize(applicationContext) // AutoSync hook
        window.setBackgroundDrawableResource(R.color.morivia_background)
        pipRemoteActionReceiver = PipRemoteActionReceiver.register(this)
        SyncClientIdentityStorage.initialize(applicationContext)
        com.morivia.app.features.party.PartyResumeStorage.initialize(applicationContext)
        AddonHttpClientProvider.initialize(applicationContext)
        AddonStorage.initialize(applicationContext)
        AuthStorage.initialize(applicationContext)
        ServerConfigurationStorage.initialize(applicationContext)
        LibraryStorage.initialize(applicationContext)
        WatchedStorage.initialize(applicationContext)
        MetaScreenSettingsStorage.initialize(applicationContext)
        com.morivia.app.features.shuffle.EpisodeShuffleStorage.initialize(applicationContext)
        HomeCatalogSettingsStorage.initialize(applicationContext)
        com.morivia.app.features.settings.MoriviaLayoutSettingsStorage.initialize(applicationContext)
        PlayerSettingsStorage.initialize(applicationContext)
        PlayerTrackPreferenceStorage.initialize(applicationContext)
        P2pSettingsStorage.initialize(applicationContext)
        P2pStreamingEngine.initialize(applicationContext)
        ExternalPlayerPlatform.initialize(applicationContext)
        SubtitleFileCache.initialize(applicationContext)
        ProfileStorage.initialize(applicationContext)
        AvatarStorage.initialize(applicationContext)
        ProfilePinCacheStorage.initialize(applicationContext)
        MemberAssetStorage.initialize(applicationContext)
        DiscoverSelectionStorage.initialize(applicationContext)
        SearchHistoryStorage.initialize(applicationContext)
        SeasonViewModeStorage.initialize(applicationContext)
        PosterCardStyleStorage.initialize(applicationContext)
        CustomPosterUrlStorage.initialize(applicationContext)
        CardDepthStyleStorage.initialize(applicationContext)
        com.morivia.app.features.home.HomeSnapshotStorage.initialize(applicationContext)
        DebridSettingsStorage.initialize(applicationContext)
        TmdbSettingsStorage.initialize(applicationContext)
        MdbListSettingsStorage.initialize(applicationContext)
        TraktAuthStorage.initialize(applicationContext)
        TraktCommentsStorage.initialize(applicationContext)
        TraktLibraryStorage.initialize(applicationContext)
        TraktSettingsStorage.initialize(applicationContext)
        PlatformMdbListAuthPersistence.initialize(applicationContext)
        ServerStorage.initialize(applicationContext)
        PlatformMdbListSyncStorage.initialize(applicationContext)
        SimklAuthStorage.initialize(applicationContext)
        SimklSyncStorage.initialize(applicationContext)
        LibraryDisplaySettingsStorage.initialize(applicationContext)
        ContinueWatchingPreferencesStorage.initialize(applicationContext)
        ResumePromptStorage.initialize(applicationContext)
        ContinueWatchingEnrichmentStorage.initialize(applicationContext)
        EpisodeReleaseNotificationsStorage.initialize(applicationContext)
        WatchProgressStorage.initialize(applicationContext)
        StreamLinkCacheStorage.initialize(applicationContext)
        StreamBadgeSettingsStorage.initialize(applicationContext)
        BingeGroupCacheStorage.initialize(applicationContext)
        PluginStorage.initialize(applicationContext)
        CollectionMobileSettingsStorage.initialize(applicationContext)
        CollectionStorage.initialize(applicationContext)
        DownloadsStorage.initialize(applicationContext)
        DownloadsPlatformDownloader.initialize(applicationContext)
        DownloadsLiveStatusPlatform.initialize(applicationContext)
        AndroidAppUpdaterPlatform.initialize(applicationContext)
        PlatformLocalAccountDataCleaner.initialize(applicationContext)
        EpisodeReleaseNotificationPlatform.initialize(applicationContext)
        EpisodeReleaseNotificationPlatform.bindActivity(this)
        handleIncomingAppIntent(intent)

        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingAppIntent(intent)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        PlayerPictureInPictureManager.onUserLeaveHint(this)
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration,
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        PlayerPictureInPictureManager.onPictureInPictureModeChanged(this, isInPictureInPictureMode)
    }

    override fun onDestroy() {
        EpisodeReleaseNotificationPlatform.unbindActivity(this)
        val receiver = pipRemoteActionReceiver
        if (receiver != null) {
            runCatching { unregisterReceiver(receiver) }
            pipRemoteActionReceiver = null
        }
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        if (EpisodeReleaseNotificationPlatform.handlePermissionRequestResult(requestCode, grantResults)) {
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun handleIncomingAppIntent(intent: Intent?) {
        val appUrl = (intent?.dataString ?: intent?.getStringExtra(LinkActivity.EXTRA_LINK))?.trim().orEmpty()
        if (appUrl.isBlank()) return
        handleAppUrl(appUrl)
    }
}
