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

    @Test
    fun firstGuestProfileIsCreatedOnceAndSurvivesReload(): Unit = runBlocking {
        ProfileRepository.pullProfiles()
        val original = ProfileRepository.state.value.profiles.single()
        assertEquals(1, original.profileIndex)
        assertTrue(original.name.isNotBlank())
        assertTrue(ProfileRepository.state.value.isLoaded)

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
