package com.morivia.app.features.auth

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.morivia.app.core.auth.DeviceLinkAuthRepository
import com.morivia.app.core.auth.DeviceLinkAuthState
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Renders the sign-in screen at iPad size (1024 x 1366 dp, landscape and portrait) to PNG files. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], application = Application::class, qualifiers = "w1366dp-h1024dp-xhdpi")
class AuthTabletSnapshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun ipadLandscape() = render("norivia-ipad-sign-in")

    @Test
    @Config(sdk = [34], application = Application::class, qualifiers = "w1024dp-h1366dp-xhdpi")
    fun ipadPortrait() = render("norivia-ipad-portrait-sign-in", withCode = false)

    @Test
    @Config(sdk = [34], application = Application::class, qualifiers = "w800dp-h1280dp-xhdpi")
    fun androidTabletPortrait() = render("norivia-android-tablet-sign-in", withCode = false)

    private fun render(name: String, withCode: Boolean = true) {
        @Suppress("UNCHECKED_CAST")
        val field = DeviceLinkAuthRepository::class.java.getDeclaredField("_state").apply { isAccessible = true }
        compose.setContent { AuthScreen() }
        compose.waitForIdle()
        compose.mainClock.advanceTimeBy(500)
        if (withCode) {
            (field.get(DeviceLinkAuthRepository) as MutableStateFlow<DeviceLinkAuthState>).value =
                DeviceLinkAuthState.Waiting(code = "K7Q4-M2XP", verificationUrl = "https://nuvio.tv/link?code=K7Q4-M2XP")
        }
        compose.waitForIdle()
        compose.mainClock.advanceTimeBy(1000)
        save("$name.png")
    }

    private fun save(name: String) {
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        val out = File(System.getProperty("norivia.snapshotDir") ?: System.getProperty("java.io.tmpdir"), name)
        out.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
