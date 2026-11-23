package com.nuvio.app.features.settings

import androidx.compose.ui.graphics.toArgb
import com.nuvio.app.core.ui.AppTheme
import com.nuvio.app.core.ui.CustomThemeColors
import com.nuvio.app.core.ui.NativeTabBridge
import com.nuvio.app.core.ui.ThemeColors
import com.nuvio.app.features.membership.MemberAccessRepository
import com.nuvio.app.features.membership.availableAppThemes
import com.nuvio.app.features.membership.resolveAppTheme
import com.nuvio.app.features.membership.resolveCustomThemeColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object ThemeSettingsRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _selectedThemePreference = MutableStateFlow<AppTheme?>(null)
    val selectedThemePreference: StateFlow<AppTheme?> = _selectedThemePreference.asStateFlow()
    private val _selectedTheme = MutableStateFlow(AppTheme.ORIGINAL)
    val selectedTheme: StateFlow<AppTheme> = _selectedTheme.asStateFlow()

    private val _customThemePreference = MutableStateFlow(CustomThemeColors.Default)
    val customThemePreference: StateFlow<CustomThemeColors> = _customThemePreference.asStateFlow()
    private val _customThemeColors = MutableStateFlow(CustomThemeColors.solid(CustomThemeColors.Default.second))
    val customThemeColors: StateFlow<CustomThemeColors> = _customThemeColors.asStateFlow()

    // Morivia: AMOLED Black is on by default, as in the earlier Morivia app.
    private val _amoledEnabled = MutableStateFlow(true)
    val amoledEnabled: StateFlow<Boolean> = _amoledEnabled.asStateFlow()

    private val _liquidGlassNativeTabBarEnabled = MutableStateFlow(false)
    val liquidGlassNativeTabBarEnabled: StateFlow<Boolean> = _liquidGlassNativeTabBarEnabled.asStateFlow()

    private val _selectedAppLanguage = MutableStateFlow(AppLanguage.DEVICE)
    val selectedAppLanguage: StateFlow<AppLanguage> = _selectedAppLanguage.asStateFlow()

    private val _navBarStyle = MutableStateFlow(NavBarStyle.ADAPTIVE)
    val navBarStyle: StateFlow<NavBarStyle> = _navBarStyle.asStateFlow()

    private val _navBarGlowEnabled = MutableStateFlow(true)
    val navBarGlowEnabled: StateFlow<Boolean> = _navBarGlowEnabled.asStateFlow()

    // Morivia: the app icon follows the theme unless the user turns this off (like the earlier app).
    private val _matchAppIconToTheme = MutableStateFlow(false)
    val matchAppIconToTheme: StateFlow<Boolean> = _matchAppIconToTheme.asStateFlow()

    private val _matchThemeToAppIcon = MutableStateFlow(false)
    val matchThemeToAppIcon: StateFlow<Boolean> = _matchThemeToAppIcon.asStateFlow()

    // The profile view background (profile picker, editor and Profile page): "ORIGINAL", "BLACK"
    // or a theme name. Device-wide, and separate from the theme colour (owner, 2026-11-23).
    private val _profileViewBackground = MutableStateFlow(PROFILE_BACKGROUND_ORIGINAL)
    val profileViewBackground: StateFlow<String> = _profileViewBackground.asStateFlow()

    fun setProfileViewBackground(value: String) {
        if (_profileViewBackground.value == value) return
        _profileViewBackground.value = value
        ThemeSettingsStorage.saveProfileViewBackground(value)
    }

    private var hasLoaded = false
    private var observesMembership = false

    fun ensureLoaded() {
        observeMembership()
        if (hasLoaded) return
        loadFromDisk()
    }

    fun onProfileChanged() {
        loadFromDisk()
    }

    fun clearLocalState() {
        hasLoaded = false
        _selectedThemePreference.value = null
        _selectedTheme.value = AppTheme.ORIGINAL
        _customThemePreference.value = CustomThemeColors.Default
        _customThemeColors.value = CustomThemeColors.solid(CustomThemeColors.Default.second)
        _amoledEnabled.value = true
        _liquidGlassNativeTabBarEnabled.value = false
        NativeTabBridge.publishAccentColor(ThemeColors.Original.nativeAccentHex)
        NativeTabBridge.publishLiquidGlassEnabled(false)
        _selectedAppLanguage.value = AppLanguage.DEVICE
        _navBarGlowEnabled.value = true
        _navBarStyle.value = NavBarStyle.ADAPTIVE
        _matchAppIconToTheme.value = false
        _matchThemeToAppIcon.value = false
        // Without an account (signed out) the app shows the Original theme and app icon.
        scope.launch(Dispatchers.Main) { AppIconRepository.select(AppIconOption.ORIGINAL) }
    }

    private fun loadFromDisk() {
        hasLoaded = true
        _profileViewBackground.value = ThemeSettingsStorage.loadProfileViewBackground() ?: PROFILE_BACKGROUND_ORIGINAL
        val stored = ThemeSettingsStorage.loadSelectedTheme()
        val theme = if (stored != null) {
            try {
                AppTheme.valueOf(stored)
            } catch (_: IllegalArgumentException) {
                null
            }
        } else {
            null
        }
        _selectedThemePreference.value = theme
        _customThemePreference.value = CustomThemeColors.decode(ThemeSettingsStorage.loadCustomThemeColors())
        _matchThemeToAppIcon.value = false
        _matchAppIconToTheme.value = false
        applyEffectiveTheme()
        _amoledEnabled.value = ThemeSettingsStorage.loadAmoledEnabled() ?: true
        val liquidGlassEnabled = ThemeSettingsStorage.loadLiquidGlassNativeTabBarEnabled() ?: false
        _liquidGlassNativeTabBarEnabled.value = liquidGlassEnabled
        NativeTabBridge.publishLiquidGlassEnabled(liquidGlassEnabled)
        val appLanguage = AppLanguage.fromCode(ThemeSettingsStorage.loadSelectedAppLanguage())
        ThemeSettingsStorage.applySelectedAppLanguage(appLanguage.code)
        _selectedAppLanguage.value = appLanguage
        _navBarGlowEnabled.value = ThemeSettingsStorage.loadNavBarGlowEnabled() ?: true
        _navBarStyle.value = NavBarStyle.fromKey(ThemeSettingsStorage.loadNavBarStyle())
    }

    fun setTheme(theme: AppTheme) {
        ensureLoaded()
        val access = MemberAccessRepository.access.value
        if (theme !in availableAppThemes(access.entitlements)) return
        saveTheme(theme)
    }

    private fun saveTheme(theme: AppTheme) {
        if (_selectedThemePreference.value != theme) {
            _selectedThemePreference.value = theme
            ThemeSettingsStorage.saveSelectedTheme(theme.name)
        }
        applyEffectiveTheme()
    }

    fun setCustomTheme(colors: CustomThemeColors) {
        ensureLoaded()
        saveCustomTheme(colors)
    }

    private fun saveCustomTheme(colors: CustomThemeColors) {
        val access = MemberAccessRepository.access.value
        val selectedColors = resolveCustomThemeColors(colors, access.tier)
        ThemeSettingsStorage.saveCustomThemeColors(selectedColors.encode())
        ThemeSettingsStorage.saveSelectedTheme(AppTheme.CUSTOM.name)
        _customThemePreference.value = selectedColors
        _selectedThemePreference.value = AppTheme.CUSTOM
        applyEffectiveTheme()
    }

    fun setMatchAppIconToTheme(enabled: Boolean) {
        ensureLoaded()
        if (_matchAppIconToTheme.value == enabled) return
        _matchAppIconToTheme.value = enabled
        ThemeSettingsStorage.saveMatchAppIconToTheme(enabled)
        if (enabled) {
            setMatchThemeToAppIcon(false)
            val icon = iconForTheme(_selectedTheme.value, _customThemeColors.value)
            scope.launch(Dispatchers.Main) { AppIconRepository.select(icon) }
        }
    }

    fun setMatchThemeToAppIcon(enabled: Boolean) {
        ensureLoaded()
        if (_matchThemeToAppIcon.value == enabled) return
        _matchThemeToAppIcon.value = enabled
        ThemeSettingsStorage.saveMatchThemeToAppIcon(enabled)
        if (enabled) {
            setMatchAppIconToTheme(false)
            AppIconRepository.ensureLoaded()
            applyIconTheme(AppIconRepository.state.value.selected)
        }
    }

    internal suspend fun selectAppIcon(icon: AppIconOption) {
        ensureLoaded()
        AppIconRepository.select(icon)
    }

    private fun applyIconTheme(icon: AppIconOption) {
        val theme = icon.theme ?: return
        if (theme in availableAppThemes(MemberAccessRepository.access.value.entitlements)) {
            saveTheme(theme)
        } else {
            val accent = ThemeColors.getColorPalette(theme).secondary
            saveCustomTheme(CustomThemeColors.solid(accent.toArgb() and 0xFFFFFF))
        }
    }

    fun setAmoled(enabled: Boolean) {
        ensureLoaded()
        if (_amoledEnabled.value == enabled) return
        _amoledEnabled.value = enabled
        ThemeSettingsStorage.saveAmoledEnabled(enabled)
    }

    fun setLiquidGlassNativeTabBar(enabled: Boolean) {
        ensureLoaded()
        if (_liquidGlassNativeTabBarEnabled.value == enabled) return
        _liquidGlassNativeTabBarEnabled.value = enabled
        ThemeSettingsStorage.saveLiquidGlassNativeTabBarEnabled(enabled)
        NativeTabBridge.publishLiquidGlassEnabled(enabled)
    }

    fun setAppLanguage(language: AppLanguage) {
        ensureLoaded()
        if (_selectedAppLanguage.value == language) return
        ThemeSettingsStorage.saveSelectedAppLanguage(language.code)
        ThemeSettingsStorage.applySelectedAppLanguage(language.code)
        _selectedAppLanguage.value = language
    }

    fun setNavBarStyle(style: NavBarStyle) {
        ensureLoaded()
        if (_navBarStyle.value == style) return
        _navBarStyle.value = style
        ThemeSettingsStorage.saveNavBarStyle(style.key)
    }

    fun setNavBarGlowEnabled(enabled: Boolean) {
        ensureLoaded()
        if (_navBarGlowEnabled.value == enabled) return
        _navBarGlowEnabled.value = enabled
        ThemeSettingsStorage.saveNavBarGlowEnabled(enabled)
    }

    private fun observeMembership() {
        if (observesMembership) return
        observesMembership = true
        MemberAccessRepository.ensureStarted()
        scope.launch {
            MemberAccessRepository.access.collect {
                if (hasLoaded) applyEffectiveTheme()
            }
        }
    }

    private fun applyEffectiveTheme() {
        val access = MemberAccessRepository.access.value
        val effective = resolveAppTheme(
            selectedTheme = _selectedThemePreference.value,
            entitlements = access.entitlements,
        )
        _customThemeColors.value = resolveCustomThemeColors(_customThemePreference.value, access.tier)
        _selectedTheme.value = effective
        if (_matchAppIconToTheme.value) {
            val icon = iconForTheme(effective, _customThemeColors.value)
            scope.launch(Dispatchers.Main) { AppIconRepository.select(icon) }
        }
        NativeTabBridge.publishAccentColor(ThemeColors.Original.nativeAccentHex)
    }
}

internal const val PROFILE_BACKGROUND_ORIGINAL = "ORIGINAL"
internal const val PROFILE_BACKGROUND_BLACK = "BLACK"
