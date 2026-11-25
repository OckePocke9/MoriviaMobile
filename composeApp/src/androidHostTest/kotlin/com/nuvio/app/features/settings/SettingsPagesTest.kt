package com.nuvio.app.features.settings

import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import com.nuvio.app.core.ui.NuvioTheme
import com.nuvio.app.features.player.AndroidPlaybackEngine
import com.nuvio.app.features.player.PlayerSettingsRepository
import com.nuvio.app.features.player.PlayerSettingsStorage
import com.nuvio.app.features.player.PlayerSettingsUiState
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w411dp-h891dp")
class SettingsPagesTest {
    @get:Rule
    val compose = createComposeRule()

    @BeforeTest
    fun initialize() {
        val context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences("nuvio_player_settings", Context.MODE_PRIVATE).edit().clear().commit()
        PlayerSettingsStorage.initialize(context)
        PlayerSettingsRepository.clearLocalState()
        ThemeSettingsStorage.initialize(context)
        ThemeSettingsRepository.clearLocalState()
        ThemeSettingsRepository.ensureLoaded()
    }

    @AfterTest
    fun clearState() {
        LayoutSections.appearanceOpen = false
        LayoutSections.layoutOpen = false
        BrowsePlaybackSections.discoveryOpen = false
        BrowsePlaybackSections.playbackOpen = false
        PlayerSettingsRepository.clearLocalState()
        ThemeSettingsRepository.clearLocalState()
    }

    @Test
    fun profileActionsOpenTheirExistingDestinations() {
        var opened = ""
        compose.setContent {
            NuvioTheme {
                ProfileScreen(
                    onSwitchProfile = { opened = "switch" },
                    onAccount = { opened = "account" },
                    onSettings = { opened = "settings" },
                )
            }
        }
        compose.onNodeWithText("Switch Profile").performScrollTo().performClick()
        assertEquals("switch", opened)
        compose.onNodeWithText("Account").performScrollTo().performClick()
        assertEquals("account", opened)
        compose.onNodeWithText("Settings").performScrollTo().performClick()
        assertEquals("settings", opened)
    }

    @Test
    fun settingsRootDoesNotDuplicateProfileActions() {
        compose.setContent {
            NuvioTheme {
                LazyColumn {
                    settingsRootContent(isTablet = false, onPageClick = {}, onSwitchProfileClick = {})
                }
            }
        }
        compose.onNodeWithText("Switch Profile").assertDoesNotExist()
        compose.onNodeWithText("Account").assertDoesNotExist()
    }

    @Test
    fun phoneRootRoutesLayoutAndBrowsePlayback() {
        var opened: SettingsPage? = null
        compose.setContent {
            NuvioTheme {
                LazyColumn(Modifier.fillMaxSize()) {
                    settingsRootContent(isTablet = false, onPageClick = { opened = it })
                }
            }
        }
        compose.onNodeWithText("Layout").performScrollTo().performClick()
        assertEquals(SettingsPage.Appearance, opened)
        compose.onNodeWithText("Browse & Playback").performScrollTo().performClick()
        assertEquals(SettingsPage.BrowsePlayback, opened)
        compose.onNodeWithText("Playback").assertDoesNotExist()
        compose.onNodeWithText("Content & Discovery").assertDoesNotExist()
        compose.onNodeWithText("Connections").performScrollTo().performClick()
        assertEquals(SettingsPage.Integrations, opened)
    }

    @Test
    fun browseAndPlaybackCanBeExpandedTogether() {
        var opened: SettingsPage? = null
        compose.setContent {
            NuvioTheme {
                LazyColumn(Modifier.fillMaxSize()) {
                    browsePlaybackSettingsContent(
                        isTablet = false,
                        onAddonsClick = { opened = SettingsPage.Addons },
                        onPluginsClick = {},
                        onPageChange = { opened = it },
                    )
                }
            }
        }

        compose.onNodeWithText("Addons").assertDoesNotExist()
        compose.onNodeWithText("Player").assertDoesNotExist()
        compose.onNodeWithText("Content & Discovery").performClick()
        compose.onNodeWithText("Playback").performScrollTo().performClick()
        assertTrue(BrowsePlaybackSections.discoveryOpen && BrowsePlaybackSections.playbackOpen)
        compose.onNodeWithText("Addons").performScrollTo().performClick()
        assertEquals(SettingsPage.Addons, opened)
        compose.onNodeWithText("Player").performScrollTo().performClick()
        assertEquals(SettingsPage.PlaybackPlayer, opened)
    }

    @Test
    fun tabletServicesGroupHasOneConnectionsCard() {
        var opened: SettingsPage? = null
        compose.setContent {
            NuvioTheme {
                LazyColumn(Modifier.fillMaxSize()) {
                    settingsRootContent(
                        isTablet = true,
                        category = SettingsCategory.Services,
                        onPageClick = { opened = it },
                    )
                }
            }
        }
        // Integrations and Tracking are one "Connections" card; there is no separate Tracking row.
        compose.onNodeWithText("Connections").performClick()
        assertEquals(SettingsPage.Integrations, opened)
        compose.onNodeWithText("Tracking").assertDoesNotExist()
        compose.onNodeWithText("Appearance").assertDoesNotExist()
        compose.onNodeWithText("Playback").assertDoesNotExist()
    }

    @Test
    fun settingsSearchCanBeRevealedAgainAfterAnUpwardSwipe() {
        compose.setContent {
            NuvioTheme {
                var visible by remember { mutableStateOf(false) }
                val listState = rememberLazyListState()
                val connection = rememberSettingsSearchScrollConnection(listState, visible) { visible = it }
                val searchVisibility = rememberSettingsSearchVisibility(visible)
                LazyColumn(Modifier.fillMaxSize().nestedScroll(connection), state = listState) {
                    settingsSearchRootContent(
                        query = "",
                        entries = { emptyList() },
                        isTablet = false,
                        searchVisibility = searchVisibility,
                        onQueryChange = {},
                        onTargetClick = {},
                    )
                    item { Text("Settings options") }
                }
            }
        }

        val search = compose.onNodeWithText("Search settings...")
        val list = compose.onNode(hasScrollAction())
        search.assertDoesNotExist()
        list.performTouchInput { swipeDown() }
        search.assertIsDisplayed()
        list.performTouchInput { swipeUp() }
        search.assertDoesNotExist()
        list.performTouchInput { swipeDown() }
        search.assertIsDisplayed()
    }

    @Test
    fun layoutKeepsSelectedChildPagesAndMobileNavigationControls() {
        var opened = ""
        compose.setContent {
            NuvioTheme {
                LazyColumn(Modifier.fillMaxSize()) {
                    expandableLayoutSettingsContent(
                        isTablet = false,
                        appearanceContent = { item { Text("Appearance options") } },
                    ) {
                        layoutSettingsContent(
                            isTablet = false,
                            liquidGlassNativeTabBarSupported = false,
                            onMetaScreenClick = { opened = "details" },
                            onStreamsClick = { opened = "streams" },
                            onContinueWatchingClick = { opened = "continue-watching" },
                            onPosterCustomizationClick = { opened = "poster-style" },
                            onCollectionsClick = { opened = "collections" },
                            onMoriviaPageClick = { opened = it.name },
                        )
                    }
                }
            }
        }
        compose.onNodeWithText("Appearance options").assertDoesNotExist()
        compose.onNodeWithText("Hero").assertDoesNotExist()
        compose.onNode(hasText("Appearance", substring = false) and hasClickAction()).performClick()
        compose.onNode(hasText("Layout", substring = false) and hasClickAction()).performClick()
        compose.onNodeWithText("Appearance options").assertIsDisplayed()
        compose.onNodeWithText("Hero").assertDoesNotExist()
        compose.onNodeWithText("Home Catalogs").assertDoesNotExist()
        mapOf(
            "Home Layout" to "MoriviaHomeLayout",
            "Collections" to "collections",
            "Continue Watching" to "continue-watching",
            "Poster Card Style" to "poster-style",
            "Streams" to "streams",
            "Search" to "MoriviaSearch",
            "Library" to "MoriviaLibrary",
            "Detail Page" to "details",
        ).forEach { (label, destination) ->
            compose.onNode(hasScrollAction()).performScrollToNode(hasText(label, substring = false) and hasClickAction())
            compose.onNode(hasText(label, substring = false) and hasClickAction()).performClick()
            assertEquals(destination, opened)
        }
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("Navigation Bar") and hasClickAction())
        compose.onNode(hasText("Navigation Bar") and hasClickAction()).performClick()
        compose.onNodeWithText("Classic").performScrollTo().performClick()
        assertEquals(NavBarStyle.CLASSIC, ThemeSettingsRepository.navBarStyle.value)
        compose.runOnIdle { ThemeSettingsRepository.onProfileChanged() }
        assertEquals(NavBarStyle.CLASSIC, ThemeSettingsRepository.navBarStyle.value)
    }

    @Test
    fun playbackNavigationKeepsMobileControlsAndSavesTheirChanges() {
        val page = mutableStateOf(SettingsPage.Playback)
        compose.setContent {
            NuvioTheme {
                LazyColumn(Modifier.fillMaxSize()) {
                    if (page.value != SettingsPage.Playback) {
                        item {
                            Button(onClick = { page.value = page.value.previousPage()!! }) { Text("Back") }
                        }
                    }
                    playbackSettingsContent(false, page.value) { page.value = it }
                }
            }
        }
        compose.onNodeWithText("Player interface").performScrollTo().performClick()
        compose.onNodeWithText("Touch Gestures").performScrollTo().performClick()
        assertFalse(PlayerSettingsRepository.uiState.value.touchGesturesEnabled)
        assertEquals(false, PlayerSettingsStorage.loadTouchGesturesEnabled())
        compose.onNodeWithText("Back").performScrollTo().performClick()
        compose.onNodeWithText("Audio").performScrollTo().performClick()
        compose.onNodeWithText("Preferred Audio Language").performClick()
        compose.onNodeWithText("Original language").performClick()
        assertEquals("original", PlayerSettingsRepository.uiState.value.preferredAudioLanguage)
        compose.runOnIdle { PlayerSettingsRepository.onProfileChanged() }
        assertFalse(PlayerSettingsRepository.uiState.value.touchGesturesEnabled)
        assertEquals("original", PlayerSettingsRepository.uiState.value.preferredAudioLanguage)
        compose.onNodeWithText("Back").performScrollTo().performClick()
        compose.onNodeWithText("Player").performScrollTo().performClick()
        compose.onNodeWithText("Auto-switch engine on playback error").performScrollTo().performClick()
        assertFalse(PlayerSettingsRepository.uiState.value.autoSwitchPlaybackEngineOnError)
        assertEquals(false, PlayerSettingsStorage.loadAutoSwitchPlaybackEngineOnError())
        compose.runOnIdle { PlayerSettingsRepository.onProfileChanged() }
        assertFalse(PlayerSettingsRepository.uiState.value.autoSwitchPlaybackEngineOnError)
        compose.onNodeWithText("Playback Engine").performClick()
        compose.onNodeWithText("libmpv").performClick()
        assertEquals(AndroidPlaybackEngine.Libmpv, PlayerSettingsRepository.uiState.value.androidPlaybackEngine)
        compose.onNodeWithText("Back").performScrollTo().performClick()
        compose.onNodeWithText("Buffer and cache").assertDoesNotExist()
    }

    @Test
    fun subtitleRenderModePersistsAndRespectsTunneledPlayback() {
        PlayerSettingsRepository.setUseLibass(true)
        compose.setContent {
            NuvioTheme {
                LazyColumn(Modifier.fillMaxSize()) {
                    playbackSettingsContent(false, SettingsPage.PlaybackSubtitles) {}
                }
            }
        }
        compose.onNodeWithText("ASS Render Mode").performScrollTo().performClick()
        compose.onNodeWithText("Video Effect").performClick()
        assertEquals("VIDEO_EFFECT", PlayerSettingsRepository.uiState.value.libassRenderMode)
        assertEquals("VIDEO_EFFECT", PlayerSettingsStorage.loadLibassRenderMode())
        compose.runOnIdle { PlayerSettingsRepository.onProfileChanged() }
        compose.onNodeWithText("Video Effect").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { PlayerSettingsRepository.setTunnelingEnabled(true) }
        compose.onNodeWithText("ASS Render Mode").performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun externalPlayersStillDisableMobileGestures() {
        compose.setContent {
            NuvioTheme {
                LazyColumn(Modifier.fillMaxSize()) {
                    item { PlaybackInterfaceSettings(false, PlayerSettingsUiState(externalPlayerEnabled = true)) }
                }
            }
        }
        compose.onNodeWithText("Touch Gestures").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("Hold To Speed").performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun searchOpensTheMovedControlsAndFiltersUnsupportedBufferOptions() {
        var entries = emptyList<SettingsSearchEntry>()
        compose.setContent {
            NuvioTheme {
                entries = settingsSearchEntries(
                    isTablet = false,
                    pluginsEnabled = true,
                    supportersContributorsPageEnabled = true,
                    accountDeletionEnabled = true,
                    personalMediaAddonCopyEnabled = true,
                    liquidGlassNativeTabBarSupported = true,
                    switchProfileAvailable = true,
                    checkForUpdatesAvailable = true,
                )
            }
        }
        compose.runOnIdle {
            assertEquals(entries.size, entries.map { it.key }.distinct().size)
            mapOf(
                "playback-touch-gestures" to SettingsPage.PlaybackInterface,
                "playback-preferred-audio" to SettingsPage.PlaybackAudio,
                "playback-preferred-subtitles" to SettingsPage.PlaybackSubtitles,
                "playback-auto-switch-engine" to SettingsPage.PlaybackPlayer,
                "playback-libass-render-mode" to SettingsPage.PlaybackSubtitles,
                "playback-custom-buffers" to SettingsPage.PlaybackBuffer,
                "liquid-glass" to SettingsPage.Appearance,
                "navigation-bar" to SettingsPage.Appearance,
                "app-icon" to SettingsPage.MoriviaAppearance,
                "auto-rotate-hero" to SettingsPage.MoriviaHero,
                "hero-trailers" to SettingsPage.MoriviaHero,
            ).forEach { (key, page) ->
                assertEquals(SettingsSearchTarget.Page(page), entries.single { it.key == key }.target)
            }
            assertTrue(entries.none { (it.target as? SettingsSearchTarget.Page)?.page?.isLegacyLayoutPage() == true })
            assertEquals("Services", entries.single { it.key == "tracking" }.category)
            PlayerSettingsRepository.setAndroidPlaybackEngine(AndroidPlaybackEngine.Libmpv)
        }
        compose.runOnIdle {
            assertTrue(entries.none { it.target == SettingsSearchTarget.Page(SettingsPage.PlaybackBuffer) })
            assertTrue(entries.any { it.target == SettingsSearchTarget.Page(SettingsPage.PlaybackVideo) })
        }
    }
}
