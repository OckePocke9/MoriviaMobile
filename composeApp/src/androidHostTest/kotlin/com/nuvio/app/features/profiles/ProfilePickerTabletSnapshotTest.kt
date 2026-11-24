package com.nuvio.app.features.profiles

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Renders the profile picker at iPad size, landscape and portrait, to PNG files. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], application = Application::class, qualifiers = "w1366dp-h1024dp-xhdpi")
class ProfilePickerTabletSnapshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun ipadLandscape() = render("norivia-ipad-profiles")

    @Test
    @Config(sdk = [34], application = Application::class, qualifiers = "w1024dp-h1366dp-xhdpi")
    fun ipadPortrait() = render("norivia-ipad-portrait-profiles")

    private fun render(name: String) {
        // Two profiles with initials, as on the owner's phone, in the app's own theme.
        val field = ProfileRepository::class.java.getDeclaredField("_state").apply { isAccessible = true }
        @Suppress("UNCHECKED_CAST")
        (field.get(ProfileRepository) as kotlinx.coroutines.flow.MutableStateFlow<ProfileState>).value = ProfileState(
            profiles = listOf(
                NuvioProfile(id = "1", profileIndex = 1, name = "Oscar", avatarColorHex = "#6B4BF5"),
                NuvioProfile(id = "2", profileIndex = 2, name = "Victor", avatarColorHex = "#6B4BF5"),
            ),
            isLoaded = true,
        )
        compose.setContent {
            com.nuvio.app.core.ui.NuvioTheme(appTheme = com.nuvio.app.core.ui.AppTheme.ORIGINAL, amoled = true) {
                ProfileSelectionScreen(onProfileSelected = {}, onEditProfile = {}, onAddProfile = {})
            }
        }
        compose.waitForIdle()
        compose.mainClock.advanceTimeBy(2_000)
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        val out = File(System.getProperty("java.io.tmpdir"), "$name.png")
        out.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
