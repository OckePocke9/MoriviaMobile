package com.nuvio.app.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nuvio.app.core.build.AppFeaturePolicy

/** The top-level card a page belongs to (its ancestor right under the Settings root). */
internal fun SettingsPage.topLevelPage(): SettingsPage {
    var current = this
    while (true) {
        val parent = current.parentPage ?: return current
        if (parent == SettingsPage.Root) return current
        current = parent
    }
}

/**
 * Landscape/tablet sidebar list (owner's request, 2026-11-22): the same Settings list as in
 * portrait, with search on top and every card with its description, on the sidebar's own
 * background. Tapping a card opens it on the right side.
 */
@Composable
internal fun SettingsSidebarList(
    onPageClick: (SettingsPage) -> Unit,
    onSwitchProfile: (() -> Unit)?,
    onWatchPartyClick: (() -> Unit)?,
    onCollectionsClick: () -> Unit,
    onCheckForUpdatesClick: (() -> Unit)?,
    liquidGlassNativeTabBarSupported: Boolean,
) {
    var query by rememberSaveable { mutableStateOf("") }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        settingsSearchRootContent(
            query = query,
            entries = {
                settingsSearchEntries(
                    isTablet = false,
                    pluginsEnabled = AppFeaturePolicy.pluginsEnabled,
                    supportersContributorsPageEnabled = AppFeaturePolicy.supportersContributorsPageEnabled,
                    accountDeletionEnabled = AppFeaturePolicy.accountDeletionEnabled,
                    personalMediaAddonCopyEnabled = AppFeaturePolicy.personalMediaAddonCopyEnabled,
                    liquidGlassNativeTabBarSupported = liquidGlassNativeTabBarSupported,
                    switchProfileAvailable = onSwitchProfile != null,
                    checkForUpdatesAvailable = onCheckForUpdatesClick != null,
                )
            },
            isTablet = false,
            showSearchField = true,
            animateSearchField = false,
            onQueryChange = { query = it },
            onTargetClick = { target ->
                when (target) {
                    is SettingsSearchTarget.Page -> if (target.page.isEnabledByPolicy()) onPageClick(target.page)
                    SettingsSearchTarget.Collections -> onCollectionsClick()
                    SettingsSearchTarget.SwitchProfile -> onSwitchProfile?.invoke()
                    SettingsSearchTarget.CheckForUpdates -> onCheckForUpdatesClick?.invoke()
                }
            },
        )
        if (query.isBlank()) {
            settingsRootContent(
                isTablet = false,
                onPageClick = onPageClick,
                onWatchPartyClick = onWatchPartyClick,
            )
        }
    }
}
