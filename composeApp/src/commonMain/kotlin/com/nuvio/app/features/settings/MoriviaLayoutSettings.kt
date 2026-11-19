package com.nuvio.app.features.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** How Home's hero moves between titles, as in the earlier Morivia app. */
@Serializable
enum class MoriviaHeroLayout {
    /** Moves to the next title every 8 seconds (45 while a trailer plays). */
    ROLLING,

    /** Moves only when swiped. */
    PINNED,

    /** Rolls until the first swipe, then stays where it was swiped to. */
    HYBRID,
}

/** How collection covers on Home move. */
@Serializable
enum class MoriviaCollectionGifs { ANIMATED, STILL }

/** Which section Library opens on. */
@Serializable
enum class MoriviaLibrarySection { RECENTLY_ADDED, MOVIES, SHOWS, CONTINUE_WATCHING }

/** How Library is sorted. */
@Serializable
enum class MoriviaLibrarySort { ADDED, NAME, RECENTLY_WATCHED }

/**
 * Morivia's own Home, Search and Library settings (from the earlier Morivia app) that Nuvio has
 * no setting for. They are kept per profile on this device and are not synced to Nuvio.
 */
@Serializable
data class MoriviaLayoutSettings(
    // Home: hero
    val heroLayout: MoriviaHeroLayout = MoriviaHeroLayout.ROLLING,
    val heroVideo: Boolean = false,
    val trailerSound: Boolean = false,
    // Home: posters
    val posterBanners: Boolean = true,
    // Home: Continue Watching
    val upcomingCountdown: Boolean = true,
    // Home: personalised rows from the latest watched movie and the latest watched series
    val moviesByGenres: Boolean = true,
    val seriesByGenres: Boolean = true,
    // Home: rows
    val collectionGifs: MoriviaCollectionGifs = MoriviaCollectionGifs.ANIMATED,
    /** Minutes between automatic Home reloads; 0 is off. */
    val autoRefreshMinutes: Int = 0,
    // Search
    val searchTrending: Boolean = true,
    val searchBrowse: Boolean = true,
    val searchHistory: Boolean = true,
    // Library
    val librarySection: MoriviaLibrarySection = MoriviaLibrarySection.RECENTLY_ADDED,
    val librarySort: MoriviaLibrarySort = MoriviaLibrarySort.ADDED,
    val libraryWatchedMarks: Boolean = true,
)

internal expect object MoriviaLayoutSettingsStorage {
    fun loadPayload(): String?
    fun savePayload(payload: String)
}

object MoriviaLayoutSettingsRepository {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val _settings = MutableStateFlow(MoriviaLayoutSettings())
    val settings: StateFlow<MoriviaLayoutSettings> = _settings.asStateFlow()
    private var hasLoaded = false

    fun ensureLoaded() {
        if (hasLoaded) return
        loadFromDisk()
    }

    /** Reads the settings of the profile that is now active. */
    fun onProfileChanged() {
        loadFromDisk()
    }

    fun clearLocalState() {
        hasLoaded = false
        _settings.value = MoriviaLayoutSettings()
    }

    fun update(transform: (MoriviaLayoutSettings) -> MoriviaLayoutSettings) {
        ensureLoaded()
        val updated = transform(_settings.value)
        if (updated == _settings.value) return
        _settings.value = updated
        runCatching { MoriviaLayoutSettingsStorage.savePayload(json.encodeToString(MoriviaLayoutSettings.serializer(), updated)) }
    }

    private fun loadFromDisk() {
        hasLoaded = true
        val payload = runCatching { MoriviaLayoutSettingsStorage.loadPayload() }.getOrNull().orEmpty().trim()
        _settings.value = if (payload.isEmpty()) {
            MoriviaLayoutSettings()
        } else {
            runCatching { json.decodeFromString(MoriviaLayoutSettings.serializer(), payload) }.getOrDefault(MoriviaLayoutSettings())
        }
    }
}
