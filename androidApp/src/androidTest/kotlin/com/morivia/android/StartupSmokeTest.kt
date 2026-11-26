package com.morivia.android

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.morivia.app.MainActivity
import com.morivia.app.core.auth.AuthRepository
import com.morivia.app.core.auth.AuthState
import com.morivia.app.features.profiles.ProfileRepository
import com.morivia.app.features.addons.AddonRepository
import com.morivia.app.features.addons.AddAddonResult
import com.morivia.app.features.search.SearchRepository
import com.morivia.app.features.details.MetaDetailsRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.rules.ExternalResource
import android.app.UiAutomation
import android.view.Surface
import androidx.test.platform.app.InstrumentationRegistry

/** Run on a fresh English-language emulator; never signs into a real account. */
@RunWith(AndroidJUnit4::class)
class StartupSmokeTest {
    @get:Rule(order = 0) val orientation = object : ExternalResource() {
        override fun before() {
            InstrumentationRegistry.getInstrumentation().uiAutomation.setRotation(Surface.ROTATION_0)
        }
        override fun after() {
            InstrumentationRegistry.getInstrumentation().uiAutomation.setRotation(UiAutomation.ROTATION_UNFREEZE)
        }
    }
    @get:Rule(order = 1) val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun freshInstallCanContinueWithoutAccountAndOpenAProfile() {
        compose.waitUntil(30_000) {
            compose.onAllNodesWithText("Continue Without Account").fetchSemanticsNodes().isNotEmpty() &&
                compose.onNodeWithText("Continue Without Account").isDisplayed()
        }
        compose.onNodeWithText("Continue Without Account").assertIsDisplayed().performClick()
        compose.waitUntil(30_000) {
            ProfileRepository.state.value.isLoaded && ProfileRepository.state.value.profiles.isNotEmpty()
        }
        assertTrue((AuthRepository.state.value as? AuthState.Authenticated)?.isAnonymous == true)
        compose.waitUntil(30_000) { ProfileRepository.state.value.hasEverSelectedProfile }
        compose.waitUntil(30_000) {
            compose.onAllNodesWithText("No content sources").fetchSemanticsNodes().isNotEmpty() &&
                compose.onNodeWithText("No content sources").isDisplayed()
        }
        compose.onNodeWithText("No content sources").assertIsDisplayed()

        // Verify a real public addon through the app's own network/parsing stack.
        val manifestUrl = "https://v3-cinemeta.strem.io/manifest.json"
        try {
            val added = runBlocking { withTimeout(30_000) { AddonRepository.addAddon(manifestUrl) } }
            assertTrue("Cinemeta installation failed: $added", added is AddAddonResult.Success)
            SearchRepository.search("Matrix", AddonRepository.uiState.value.addons)
            compose.waitUntil(45_000) {
                !SearchRepository.uiState.value.isLoading &&
                    SearchRepository.uiState.value.sections.any { it.items.isNotEmpty() }
            }
            val details = runBlocking {
                withTimeout(30_000) { MetaDetailsRepository.fetch("movie", "tt0133093") }
            }
            assertNotNull("The Matrix metadata should load through Cinemeta", details)
        } finally {
            AddonRepository.removeAddon(manifestUrl)
            SearchRepository.reset()
        }
    }
}
