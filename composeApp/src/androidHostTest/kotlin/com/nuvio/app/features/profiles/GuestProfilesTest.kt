package com.nuvio.app.features.profiles

import android.app.Application
import android.content.Context
import com.nuvio.app.core.auth.AuthRepository
import com.nuvio.app.core.auth.AuthState
import com.nuvio.app.core.auth.AuthStorage
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class GuestProfilesTest {
    @Before
    fun initialize() {
        val context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences("nuvio_profile_cache", Context.MODE_PRIVATE).edit().clear().commit()
        AuthStorage.initialize(context)
        ProfileStorage.initialize(context)
        ProfilePinCacheStorage.initialize(context)
        AuthRepository.signInAnonymously()
        ProfileRepository.clearInMemory()
        ProfileRepository.ensureLoaded((AuthRepository.state.value as AuthState.Authenticated).userId)
    }

    @org.junit.After
    fun cleanUp() {
        // Leave no guest profiles behind for tests that run later in the same process.
        ProfileRepository.clearInMemory()
    }

    @Test
    fun newGuestStartsWithNoProfilesAndCreatedProfileSurvivesReload(): Unit = runBlocking {
        // A new guest gets no profile of its own: the picker shows only Add Profile.
        ProfileRepository.pullProfiles()
        assertTrue(ProfileRepository.state.value.profiles.isEmpty())
        assertTrue(ProfileRepository.state.value.isLoaded)

        assertEquals(1, ProfileRepository.createProfile(name = "Oscar", avatarColorHex = "#1E88E5"))
        val original = ProfileRepository.state.value.profiles.single()
        assertEquals("Oscar", original.name)

        ProfileRepository.clearInMemory()
        ProfileRepository.ensureLoaded(original.userId)
        ProfileRepository.pullProfiles()
        assertEquals(listOf(original), ProfileRepository.state.value.profiles)
    }

    @Test
    fun existingGuestProfileIsNotReplaced(): Unit = runBlocking {
        ProfileRepository.pushProfiles(listOf(ProfilePushPayload(2, "Personal", "#123456")))
        ProfileRepository.pullProfiles()
        val profile = ProfileRepository.state.value.profiles.single()
        assertEquals(2, profile.profileIndex)
        assertEquals("Personal", profile.name)
    }
}
