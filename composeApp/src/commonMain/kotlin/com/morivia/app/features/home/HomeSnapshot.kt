package com.morivia.app.features.home

import co.touchlab.kermit.Logger
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/**
 * Saves and restores what Home showed (owner, 2026-11-25) so the rows are there the moment a
 * profile is picked. Everything is refreshed from the addons right after; a snapshot that cannot
 * be read is simply ignored.
 */
internal object HomeSnapshot {
    private val log = Logger.withTag("HomeSnapshot")
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = false }

    /** At most this many titles are kept per list, so a snapshot stays small. */
    private const val MAX_ITEMS = 120

    @Serializable
    private data class Item(
        val id: String,
        val type: String,
        val name: String,
        val poster: String? = null,
        val banner: String? = null,
        val logo: String? = null,
        val shape: String? = null,
        val description: String? = null,
        val releaseInfo: String? = null,
        val rawReleaseDate: String? = null,
        val imdbRating: String? = null,
        val genres: List<String> = emptyList(),
        val landscapePoster: String? = null,
        val runtime: String? = null,
        val status: String? = null,
        val ageRating: String? = null,
        val language: String? = null,
    )

    @Serializable
    private data class SectionDto(
        val items: List<Item>,
        val availableItemCount: Int,
        val hasMore: Boolean,
    )

    data class SavedSection(val items: List<MetaPreview>, val availableItemCount: Int, val hasMore: Boolean)

    private fun MetaPreview.toItem() = Item(
        id = id,
        type = type,
        name = name,
        poster = poster,
        banner = banner,
        logo = logo,
        shape = posterShape.name.takeIf { posterShape != PosterShape.Poster },
        description = description,
        releaseInfo = releaseInfo,
        rawReleaseDate = rawReleaseDate,
        imdbRating = imdbRating,
        genres = genres,
        landscapePoster = landscapePoster,
        runtime = runtime,
        status = status,
        ageRating = ageRating,
        language = language,
    )

    private fun Item.toPreview() = MetaPreview(
        id = id,
        type = type,
        name = name,
        poster = poster,
        banner = banner,
        logo = logo,
        posterShape = shape?.let { name -> PosterShape.entries.firstOrNull { it.name == name } } ?: PosterShape.Poster,
        description = description,
        releaseInfo = releaseInfo,
        rawReleaseDate = rawReleaseDate,
        imdbRating = imdbRating,
        genres = genres,
        landscapePoster = landscapePoster,
        runtime = runtime,
        status = status,
        ageRating = ageRating,
        language = language,
    )

    private val itemList = ListSerializer(Item.serializer())
    private val sectionMap = MapSerializer(String.serializer(), SectionDto.serializer())
    private val textMap = MapSerializer(String.serializer(), String.serializer())

    fun saveList(key: String, items: List<MetaPreview>) {
        runCatching {
            HomeSnapshotStorage.save("list_$key", json.encodeToString(itemList, items.take(MAX_ITEMS).map { it.toItem() }))
        }.onFailure { log.w(it) { "Could not save $key" } }
    }

    fun loadList(key: String): List<MetaPreview>? = runCatching {
        HomeSnapshotStorage.load("list_$key")?.let { payload -> json.decodeFromString(itemList, payload).map { it.toPreview() } }
    }.getOrNull()

    fun saveSections(sections: Map<String, SavedSection>) {
        runCatching {
            val dto = sections.mapValues { (_, section) ->
                SectionDto(section.items.take(MAX_ITEMS).map { it.toItem() }, section.availableItemCount, section.hasMore)
            }
            HomeSnapshotStorage.save("sections", json.encodeToString(sectionMap, dto))
        }.onFailure { log.w(it) { "Could not save the Home rows" } }
    }

    fun loadSections(): Map<String, SavedSection> = runCatching {
        HomeSnapshotStorage.load("sections")?.let { payload ->
            json.decodeFromString(sectionMap, payload).mapValues { (_, dto) ->
                SavedSection(dto.items.map { it.toPreview() }, dto.availableItemCount, dto.hasMore)
            }
        }
    }.getOrNull().orEmpty()

    /** Raw addon manifests by URL, so the addons are ready before their manifests are fetched again. */
    fun saveManifests(payloads: Map<String, String>) {
        runCatching { HomeSnapshotStorage.save("manifests", json.encodeToString(textMap, payloads)) }
            .onFailure { log.w(it) { "Could not save the addon manifests" } }
    }

    fun loadManifests(): Map<String, String> = runCatching {
        HomeSnapshotStorage.load("manifests")?.let { json.decodeFromString(textMap, it) }
    }.getOrNull().orEmpty()
}
