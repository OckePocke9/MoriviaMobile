package com.nuvio.app.features.settings

import kotlinx.serialization.json.JsonObject

internal expect object ThemeSettingsStorage {
    fun loadSelectedTheme(): String?
    fun saveSelectedTheme(themeName: String)
    fun loadCustomThemeColors(): String?
    fun saveCustomThemeColors(colors: String)
    fun loadAmoledEnabled(): Boolean?
    fun saveAmoledEnabled(enabled: Boolean)
    fun loadLiquidGlassNativeTabBarEnabled(): Boolean?
    fun saveLiquidGlassNativeTabBarEnabled(enabled: Boolean)
    fun loadSelectedAppLanguage(): String?
    fun saveSelectedAppLanguage(languageCode: String)
    fun applySelectedAppLanguage(languageCode: String)
    fun loadNavBarStyle(): String?
    fun saveNavBarStyle(styleKey: String)
    fun loadNavBarGlowEnabled(): Boolean?
    fun saveNavBarGlowEnabled(enabled: Boolean)
    fun loadMatchAppIconToTheme(): Boolean?
    fun saveMatchAppIconToTheme(enabled: Boolean)
    fun loadMatchThemeToAppIcon(): Boolean
    fun saveMatchThemeToAppIcon(enabled: Boolean)
    fun resetProfileAppearance(profileId: Int)
    fun loadAppearanceOwner(): String?
    fun saveAppearanceOwner(userId: String)
    fun loadProfileAppIcon(): String?
    fun saveProfileAppIcon(key: String)
    fun loadProfileViewBackground(): String?
    fun saveProfileViewBackground(value: String)
    fun exportToSyncPayload(): JsonObject
    fun replaceFromSyncPayload(payload: JsonObject)
}
