package com.morivia.app.features.home

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** What Home saves for the next profile pick comes back exactly (owner, 2026-11-25). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class HomeSnapshotTest {
    @Before
    fun setUp() {
        HomeSnapshotStorage.initialize(ApplicationProvider.getApplicationContext())
    }

    private val items = listOf(
        MetaPreview(id = "tt1", type = "movie", name = "One", poster = "p1", genres = listOf("Drama"), releaseInfo = "2026"),
        MetaPreview(id = "tt2", type = "series", name = "Two", banner = "b2", posterShape = PosterShape.Landscape),
    )

    @Test
    fun listsComeBackUnchanged() {
        HomeSnapshot.saveList("test", items)
        assertEquals(items, HomeSnapshot.loadList("test"))
    }

    @Test
    fun rowsComeBackByKey() {
        HomeSnapshot.saveSections(mapOf("row|sig" to HomeSnapshot.SavedSection(items, availableItemCount = 40, hasMore = true)))
        val restored = HomeSnapshot.loadSections()["row|sig"]
        assertEquals(items, restored?.items)
        assertEquals(40, restored?.availableItemCount)
        assertTrue(restored?.hasMore == true)
    }

    @Test
    fun manifestsComeBackByUrl() {
        HomeSnapshot.saveManifests(mapOf("https://addon/manifest.json" to "{\"id\":\"a\"}"))
        assertEquals("{\"id\":\"a\"}", HomeSnapshot.loadManifests()["https://addon/manifest.json"])
    }

    @Test
    fun missingSnapshotIsEmpty() {
        assertEquals(null, HomeSnapshot.loadList("never-saved"))
    }
}
