package com.morivia.app.core.storage

import android.content.Context

internal actual object PlatformLocalAccountDataCleaner {
    private val preferenceNames = listOf(
        "episode_shuffle",
        "morivia_addons",
        "morivia_library",
        "morivia_library_display_settings",
        "morivia_home_catalog_settings",
        "morivia_player_settings",
        "torrent_settings",
        "morivia_profile_cache",
        "morivia_avatar_cache",
        "morivia_profile_pin_cache",
        "morivia_theme_settings",
        "morivia_poster_card_style",
        "morivia_debrid_settings",
        "morivia_mdblist_settings",
        "morivia_mdblist_auth",
        "morivia_mdblist_sync",
        "morivia_auth",
        "morivia_trakt_auth",
        "morivia_simkl_auth",
        "morivia_simkl_sync",
        "morivia_trakt_library",
        "morivia_trakt_settings",
        "morivia_watched",
        "morivia_stream_link_cache",
        "morivia_stream_badge_settings",
        "morivia_continue_watching_preferences",
        "morivia_cw_enrichment",
        "morivia_episode_release_notifications",
        "morivia_episode_release_notifications_platform",
        "morivia_watch_progress",
        "morivia_discover_selection",
        "morivia_collection_mobile_settings",
        "morivia_collections",
        "morivia_plugins",
        "morivia_member_access",
    )

    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    actual fun wipe() {
        val context = appContext ?: return
        preferenceNames.forEach { name ->
            context.getSharedPreferences(name, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()
        }
        context.filesDir.resolve("morivia_plugin_scrapers").deleteRecursively()
    }
}
