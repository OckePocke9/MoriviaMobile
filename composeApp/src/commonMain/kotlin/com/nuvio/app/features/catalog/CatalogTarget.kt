package com.nuvio.app.features.catalog

import com.nuvio.app.features.library.LibrarySortOption
import kotlinx.serialization.Serializable

sealed interface CatalogTarget {
    val contentType: String
    val supportsPagination: Boolean

    data class Addon(
        val manifestUrl: String,
        override val contentType: String,
        val catalogId: String,
        val genre: String? = null,
        override val supportsPagination: Boolean = false,
    ) : CatalogTarget

    data class Library(
        override val contentType: String,
        val sectionType: String,
        val sortOption: LibrarySortOption = LibrarySortOption.DEFAULT,
    ) : CatalogTarget {
        override val supportsPagination: Boolean = false
    }

    data class Server(
        val connectionId: String,
        val libraryId: String?,
        override val contentType: String,
        val collectionId: String? = null,
    ) : CatalogTarget {
        override val supportsPagination: Boolean = true
    }

    /**
     * A list Morivia builds itself (such as "Movies Based on Your Genres"); its titles are kept
     * in [FixedCatalogStore] under [key] so Show all can show the whole list.
     */
    data class Fixed(
        val key: String,
        override val contentType: String,
    ) : CatalogTarget {
        override val supportsPagination: Boolean = false
    }

    data class CollectionSource(
        val collectionId: String,
        val folderId: String,
        val sourceKey: String,
        override val contentType: String,
        override val supportsPagination: Boolean = false,
    ) : CatalogTarget
}

@Serializable
enum class CatalogTargetKind {
    ADDON,
    LIBRARY,
    COLLECTION_SOURCE,
}

/** The titles of Morivia's own lists, so their Show all page can show every title. */
object FixedCatalogStore {
    private val lists = mutableMapOf<String, List<com.nuvio.app.features.home.MetaPreview>>()

    fun put(key: String, items: List<com.nuvio.app.features.home.MetaPreview>) {
        lists[key] = items
    }

    fun get(key: String): List<com.nuvio.app.features.home.MetaPreview> = lists[key].orEmpty()
}
