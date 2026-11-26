package com.morivia.app.features.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.morivia.app.core.sync.decodeSyncBoolean
import com.morivia.app.core.sync.decodeSyncString
import com.morivia.app.core.sync.encodeSyncBoolean
import com.morivia.app.core.sync.encodeSyncString
import com.morivia.app.core.storage.ProfileScopedKey
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

actual object ThemeSettingsStorage {
    private const val preferencesName = "nuvio_theme_settings"
    private const val selectedThemeKey = "selected_theme"
    private const val customThemeColorsKey = "custom_theme_colors"
    private const val amoledEnabledKey = "amoled_enabled"
    private const val liquidGlassNativeTabBarEnabledKey = "liquid_glass_native_tab_bar_enabled"
    private const val selectedAppLanguageKey = "selected_app_language"
    private const val NAV_BAR_STYLE_KEY = "nav_bar_style"
    private const val navBarGlowEnabledKey = "nav_bar_glow_enabled"
    private val profileScopedSyncKeys = listOf(
        selectedThemeKey,
        customThemeColorsKey,
        amoledEnabledKey,
        navBarGlowEnabledKey,
        liquidGlassNativeTabBarEnabledKey,
        NAV_BAR_STYLE_KEY,
    )

    private var preferences: SharedPreferences? = null

    fun initialize(context: Context) {
        preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
        applySelectedAppLanguage(loadSelectedAppLanguage() ?: AppLanguage.DEVICE.code)
    }

    actual fun loadSelectedTheme(): String? =
        preferences?.getString(ProfileScopedKey.of(selectedThemeKey), null)

    actual fun saveSelectedTheme(themeName: String) {
        preferences
            ?.edit()
            ?.putString(ProfileScopedKey.of(selectedThemeKey), themeName)
            ?.apply()
    }

    actual fun loadCustomThemeColors(): String? =
        preferences?.getString(ProfileScopedKey.of(customThemeColorsKey), null)

    actual fun saveCustomThemeColors(colors: String) {
        preferences
            ?.edit()
            ?.putString(ProfileScopedKey.of(customThemeColorsKey), colors)
            ?.apply()
    }

    actual fun loadAmoledEnabled(): Boolean? =
        preferences?.let { prefs ->
            val key = ProfileScopedKey.of(amoledEnabledKey)
            if (prefs.contains(key)) prefs.getBoolean(key, false) else null
        }

    actual fun saveAmoledEnabled(enabled: Boolean) {
        preferences
            ?.edit()
            ?.putBoolean(ProfileScopedKey.of(amoledEnabledKey), enabled)
            ?.apply()
    }

    // Morivia: "Match app icon to theme" (kept on this device, not synced).
    actual fun loadMatchAppIconToTheme(): Boolean? =
        preferences?.let { prefs ->
            val key = ProfileScopedKey.of("match_app_icon_to_theme")
            if (prefs.contains(key)) prefs.getBoolean(key, true) else null
        }

    actual fun saveMatchAppIconToTheme(enabled: Boolean) {
        preferences?.edit()?.putBoolean(ProfileScopedKey.of("match_app_icon_to_theme"), enabled)?.apply()
    }

    actual fun loadMatchThemeToAppIcon(): Boolean =
        preferences?.getBoolean(ProfileScopedKey.of("match_theme_to_app_icon"), false) ?: false

    actual fun saveMatchThemeToAppIcon(enabled: Boolean) {
        preferences?.edit()?.putBoolean(ProfileScopedKey.of("match_theme_to_app_icon"), enabled)?.apply()
    }

    actual fun loadNavBarGlowEnabled(): Boolean? =
        preferences?.let { prefs ->
            val key = ProfileScopedKey.of(navBarGlowEnabledKey)
            if (prefs.contains(key)) prefs.getBoolean(key, false) else null
        }

    actual fun saveNavBarGlowEnabled(enabled: Boolean) {
        preferences
            ?.edit()
            ?.putBoolean(ProfileScopedKey.of(navBarGlowEnabledKey), enabled)
            ?.apply()
    }

    actual fun loadLiquidGlassNativeTabBarEnabled(): Boolean? =
        preferences?.let { prefs ->
            val key = ProfileScopedKey.of(liquidGlassNativeTabBarEnabledKey)
            if (prefs.contains(key)) prefs.getBoolean(key, false) else null
        }

    actual fun saveLiquidGlassNativeTabBarEnabled(enabled: Boolean) {
        preferences
            ?.edit()
            ?.putBoolean(ProfileScopedKey.of(liquidGlassNativeTabBarEnabledKey), enabled)
            ?.apply()
    }

    actual fun loadSelectedAppLanguage(): String? {
        val value = preferences?.getString(selectedAppLanguageKey, null)
        if (value != null) return value
        val legacy = preferences?.getString(ProfileScopedKey.of(selectedAppLanguageKey), null)
        if (legacy != null) saveSelectedAppLanguage(legacy)
        return legacy
    }

    actual fun saveSelectedAppLanguage(languageCode: String) {
        preferences
            ?.edit()
            ?.putString(selectedAppLanguageKey, languageCode)
            ?.apply()
    }

    actual fun applySelectedAppLanguage(languageCode: String) {
        if (languageCode.equals("device", ignoreCase = true)) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        } else {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(languageCode),
            )
        }
    }

    // The account the saved themes and app icons belong to (device-wide).
    actual fun loadAppearanceOwner(): String? = preferences?.getString("appearance_owner", null)

    actual fun saveAppearanceOwner(userId: String) {
        preferences?.edit()?.putString("appearance_owner", userId)?.apply()
    }

    // A new profile starts with the Original theme and app icon (owner, 2026-11-23).
    actual fun resetProfileAppearance(profileId: Int) {
        preferences?.edit()
            ?.putString(ProfileScopedKey.of(selectedThemeKey, profileId), "ORIGINAL")
            ?.putString(ProfileScopedKey.of("profile_app_icon", profileId), "original")
            ?.apply()
    }

    // The app icon each profile chose, so it comes back with that profile (owner, 2026-11-23).
    actual fun loadProfileAppIcon(): String? =
        preferences?.getString(ProfileScopedKey.of("profile_app_icon"), null)

    actual fun saveProfileAppIcon(key: String) {
        preferences?.edit()?.putString(ProfileScopedKey.of("profile_app_icon"), key)?.apply()
    }

    // The profile view background (owner, 2026-11-23): device-wide, so the profile picker shows it too.
    actual fun loadProfileViewBackground(): String? =
        preferences?.getString("profile_view_background", null)

    actual fun saveProfileViewBackground(value: String) {
        preferences?.edit()?.putString("profile_view_background", value)?.apply()
    }

    actual fun loadNavBarStyle(): String? =
        preferences?.getString(ProfileScopedKey.of(NAV_BAR_STYLE_KEY), null)

    actual fun saveNavBarStyle(styleKey: String) {
        preferences
            ?.edit()
            ?.putString(ProfileScopedKey.of(NAV_BAR_STYLE_KEY), styleKey)
            ?.apply()
    }

    actual fun exportToSyncPayload(): JsonObject = buildJsonObject {
        loadSelectedTheme()?.let { put(selectedThemeKey, encodeSyncString(it)) }
        loadCustomThemeColors()?.let { put(customThemeColorsKey, encodeSyncString(it)) }
        loadNavBarGlowEnabled()?.let { put(navBarGlowEnabledKey, encodeSyncBoolean(it)) }
        loadAmoledEnabled()?.let { put(amoledEnabledKey, encodeSyncBoolean(it)) }
        loadLiquidGlassNativeTabBarEnabled()?.let { put(liquidGlassNativeTabBarEnabledKey, encodeSyncBoolean(it)) }
        loadNavBarStyle()?.let { put(NAV_BAR_STYLE_KEY, encodeSyncString(it)) }
    }

    actual fun replaceFromSyncPayload(payload: JsonObject) {
        preferences?.edit()?.apply {
            profileScopedSyncKeys.forEach { remove(ProfileScopedKey.of(it)) }
        }?.apply()

        payload.decodeSyncString(selectedThemeKey)?.let(::saveSelectedTheme)
        payload.decodeSyncString(customThemeColorsKey)?.let(::saveCustomThemeColors)
        payload.decodeSyncBoolean(navBarGlowEnabledKey)?.let(::saveNavBarGlowEnabled)
        payload.decodeSyncBoolean(amoledEnabledKey)?.let(::saveAmoledEnabled)
        payload.decodeSyncBoolean(liquidGlassNativeTabBarEnabledKey)?.let(::saveLiquidGlassNativeTabBarEnabled)
        payload.decodeSyncString(NAV_BAR_STYLE_KEY)?.let(::saveNavBarStyle)
        applySelectedAppLanguage(loadSelectedAppLanguage() ?: AppLanguage.DEVICE.code)
    }
}
