package com.nuvio.app.features.settings

import com.nuvio.app.isIos
import com.nuvio.app.features.player.AndroidPlaybackEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsNavigationTest {
    @Test
    fun layoutChildrenReturnToTheirPlatformSettingsPage() {
        val parent = if (isIos) SettingsPage.Layout else SettingsPage.Appearance
        listOf(
            SettingsPage.MetaScreen,
            SettingsPage.Streams,
            SettingsPage.MoriviaSearch,
            SettingsPage.MoriviaLibrary,
        ).forEach { page ->
            assertEquals(parent, page.previousPage())
            assertEquals(SettingsCategory.Look, page.category)
        }
        assertEquals(SettingsPage.Root, parent.previousPage())
    }

    @Test
    fun playbackChildrenReturnToPlaybackInTheWatchGroup() {
        PlaybackSection.entries.forEach { section ->
            assertEquals(SettingsPage.Playback, section.page.previousPage())
            assertEquals(SettingsCategory.Watch, section.page.category)
        }
        val parent = if (isIos) SettingsPage.Root else SettingsPage.BrowsePlayback
        assertEquals(parent, SettingsPage.Playback.previousPage())
        assertEquals(parent, SettingsPage.ContentDiscovery.previousPage())
    }

    @Test
    fun bufferOptionsFollowThePlatformAndPlaybackEngine() {
        AndroidPlaybackEngine.entries.forEach { engine ->
            val android = visiblePlaybackSections(ios = false, engine = engine, p2pVisible = false)
            val ios = visiblePlaybackSections(ios = true, engine = engine, p2pVisible = false)
            assertEquals(engine != AndroidPlaybackEngine.Libmpv, PlaybackSection.Buffer in android)
            assertFalse(PlaybackSection.Buffer in ios)
            assertFalse(PlaybackSection.P2p in android)
            assertFalse(PlaybackSection.P2p in ios)
            assertTrue(PlaybackSection.Interface in android)
            assertTrue(PlaybackSection.Interface in ios)
        }
        assertTrue(PlaybackSection.P2p in visiblePlaybackSections(false, AndroidPlaybackEngine.Auto, true))
    }

    @Test
    fun oldTabletCategoriesRestoreToTheirNewGroups() {
        assertEquals(SettingsCategory.You, settingsCategoryForName("Account"))
        assertEquals(SettingsCategory.Look, settingsCategoryForName("General"))
        assertEquals(SettingsCategory.System, settingsCategoryForName("About"))
        assertEquals(SettingsCategory.System, settingsCategoryForName("Advanced"))
        assertEquals(SettingsCategory.Services, settingsCategoryForName("Services"))
        assertEquals(SettingsCategory.Look, settingsCategoryForName("unknown"))
    }
}
