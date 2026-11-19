package com.nuvio.app.features.home

/**
 * "Movies Based on Your Genres" and "Series Based on Your Genres": titles that share genres with
 * the movie (or series) watched last. The two rows are independent: watching a movie changes
 * only the movie row, watching a series only the series row.
 */
internal data class WatchedTitle(
    val id: String,
    val type: String,
    val name: String,
    val watchedAtEpochMs: Long,
)

internal data class GenreRecommendation(
    val key: String,
    val isSeries: Boolean,
    val source: WatchedTitle,
    /** Every match, best first; Home shows the first [GENRE_ROW_HOME_LIMIT]. */
    val items: List<MetaPreview>,
)

internal const val GENRE_ROW_HOME_LIMIT = 30

internal fun String.isSeriesLikeType(): Boolean =
    lowercase().let { it == "series" || it == "tv" || it == "show" || it == "anime" }

/** The title of the given kind watched most recently. */
internal fun latestWatched(history: List<WatchedTitle>, isSeries: Boolean): WatchedTitle? =
    history
        .filter { it.id.isNotBlank() && it.type.isSeriesLikeType() == isSeries }
        .maxByOrNull { it.watchedAtEpochMs }

internal fun buildGenreRecommendation(
    isSeries: Boolean,
    history: List<WatchedTitle>,
    sections: List<HomeCatalogSection>,
    genresById: Map<String, List<String>>,
): GenreRecommendation? {
    val source = latestWatched(history, isSeries) ?: return null
    val genres = genresById[source.id].orEmpty()
        .map { it.trim().lowercase() }
        .filter { it.isNotEmpty() }
        .toSet()
    if (genres.isEmpty()) return null
    val matches = sections.asSequence()
        .flatMap { it.items.asSequence() }
        .filter { it.id != source.id && it.type.isSeriesLikeType() == isSeries }
        .distinctBy { it.id }
        .map { item -> item to item.genres.count { it.trim().lowercase() in genres } }
        .filter { (_, shared) -> shared > 0 }
        .toList()
        // Titles sharing more genres first; otherwise the catalogs' own order.
        .sortedByDescending { (_, shared) -> shared }
        .map { (item, _) -> item }
    if (matches.isEmpty()) return null
    return GenreRecommendation(
        key = if (isSeries) "morivia_series_by_genres" else "morivia_movies_by_genres",
        isSeries = isSeries,
        source = source,
        items = matches,
    )
}
