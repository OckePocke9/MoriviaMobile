package com.morivia.app.features.home

import com.morivia.app.features.addons.AddonCatalog
import com.morivia.app.features.addons.AddonRepository
import com.morivia.app.features.addons.enabledAddons
import com.morivia.app.features.catalog.fetchCatalogPage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import com.morivia.app.features.addons.hasPendingEnabledManifests
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * "Movies/Series Based on Your Recent Genre" (owner, 2026-11-25): every title the enabled addons
 * have in the genres of the movie (or series) watched last. Home shows exactly
 * [GENRE_ROW_HOME_LIMIT] of them and Show All shows all of them.
 */
internal data class GenreCatalogLoad(
    /** The row on Home, fixed once the first pages are in so it does not jump while more load. */
    val row: List<MetaPreview>,
    /** Everything found so far, for Show All. */
    val all: List<MetaPreview>,
)

internal object GenreCatalogLoader {
    /** Pages fetched per catalog and genre; addons page in steps of up to 100 titles. */
    private const val MAX_PAGES = 10
    private val log = co.touchlab.kermit.Logger.withTag("GenreCatalogLoader")

    private data class Source(val manifestUrl: String, val catalog: AddonCatalog, val genre: String)

    fun load(isSeries: Boolean, genres: Collection<String>, excludeId: String): Flow<GenreCatalogLoad> = channelFlow {
        val wanted = genres.map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
        if (wanted.isEmpty()) return@channelFlow
        // Home can open before the addons are loaded; wait for them so the row is never left short.
        val addons = withTimeoutOrNull(20_000L) {
            AddonRepository.uiState.first { state ->
                state.addons.any { it.enabled && it.manifest != null } &&
                    (!state.addons.hasPendingEnabledManifests() || state.addons.filter { it.enabled }.all { it.manifest != null })
            }
        }?.addons ?: AddonRepository.uiState.value.addons
        val sources = addons.enabledAddons().flatMap { addon ->
            val manifest = addon.manifest ?: return@flatMap emptyList()
            manifest.catalogs
                .filter { it.type.isSeriesLikeType() == isSeries }
                .filter { catalog -> catalog.extra.none { it.isRequired && it.name != "genre" } }
                .flatMap { catalog ->
                    catalog.extra.firstOrNull { it.name == "genre" }?.options.orEmpty()
                        .filter { it.trim().lowercase() in wanted }
                        .map { option -> Source(addon.manifestUrl, catalog, option) }
                }
        }
        if (sources.isEmpty()) return@channelFlow

        val lock = Mutex()
        // Title id -> the title and the recent genres it was found in; first found first.
        val found = linkedMapOf<String, Pair<MetaPreview, MutableSet<String>>>()
        var row: List<MetaPreview>? = null

        // Titles sharing the most of the recent title's genres first (owner, 2026-11-25): the genres an
        // addon lists for the title and the genre catalogs it was found in both count.
        fun ranked(): List<MetaPreview> =
            found.values.toList()
                .sortedByDescending { (item, inGenres) ->
                    val listed = item.genres.map { it.trim().lowercase() }.filter { it in wanted }.toSet()
                    (listed + inGenres).size
                }
                .map { (item, _) -> item }

        suspend fun add(source: Source, items: List<MetaPreview>): Int = lock.withLock {
            var added = 0
            items.forEach { item ->
                if (item.id == excludeId || item.id.isBlank() || item.type.isSeriesLikeType() != isSeries) return@forEach
                val entry = found.getOrPut(item.id) { item to mutableSetOf() }
                if (entry.second.add(source.genre.lowercase())) added++
            }
            added
        }

        suspend fun publish() {
            val all = lock.withLock { ranked() }
            val fixedRow = row ?: all.take(GENRE_ROW_HOME_LIMIT).also { row = it }
            // Top the row up while fewer than 30 have been found.
            val shownRow = if (fixedRow.size >= GENRE_ROW_HOME_LIMIT) fixedRow
            else (fixedRow + all).distinctBy { it.id }.take(GENRE_ROW_HOME_LIMIT).also { row = it }
            send(GenreCatalogLoad(row = shownRow, all = (shownRow + all).distinctBy { it.id }))
        }

        suspend fun page(source: Source, skip: Int?) = runCatching {
            fetchCatalogPage(
                manifestUrl = source.manifestUrl,
                type = source.catalog.type,
                catalogId = source.catalog.id,
                genre = source.genre,
                skip = skip,
            )
        }.getOrNull()

        coroutineScope {
            // The first page of every catalog together, so the row is the addons' best titles.
            val first = sources.map { source -> async { source to page(source, null) } }.awaitAll()
            first.forEach { (source, result) -> if (result != null) add(source, result.items) }
            log.i { "${if (isSeries) "series" else "movies"} genres=$wanted catalogs=${sources.size} first pages=${found.size}" }
            publish()
            // Then the rest, for Show All.
            first.forEach { (source, result) ->
                var next = result?.nextSkip?.takeIf { result.items.isNotEmpty() } ?: return@forEach
                launch {
                    for (pageNumber in 1 until MAX_PAGES) {
                        val more = page(source, next) ?: break
                        if (more.items.isEmpty() || add(source, more.items) == 0) break
                        publish()
                        next = more.nextSkip ?: break
                    }
                }
            }
        }
    }
}
