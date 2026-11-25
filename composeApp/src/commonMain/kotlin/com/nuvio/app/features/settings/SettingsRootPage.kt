package com.nuvio.app.features.settings

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.rounded.Diversity3
import androidx.compose.runtime.Composable
import com.nuvio.app.isIos
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

internal fun LazyListScope.settingsRootContent(
    isTablet: Boolean,
    onPageClick: (SettingsPage) -> Unit,
    onSwitchProfileClick: (() -> Unit)? = null,
    onWatchPartyClick: (() -> Unit)? = null,
    category: SettingsCategory? = null,
) {
    SettingsCategory.entries.filter { category == null || it == category }.forEach { group ->
        item(key = group.name) {
            SettingsGroup(isTablet = isTablet, modifier = settingsItemMotion(fade = false)) {
                when (group) {
                    SettingsCategory.You -> {
                        if (onWatchPartyClick != null) {
                            SettingsNavigationRow(
                                title = stringResource(Res.string.party_title),
                                description = stringResource(Res.string.settings_watch_party_card_description),
                                icon = Icons.Rounded.Diversity3,
                                isTablet = isTablet,
                                onClick = onWatchPartyClick,
                            )
                        }
                    }
                    SettingsCategory.Look -> {
                        SettingsPageRow(
                            page = SettingsPage.Appearance,
                            description = appearanceSettingsDescriptionRes,
                            isTablet = isTablet,
                            onPageClick = onPageClick,
                        )
                        if (isIos) {
                            SettingsGroupDivider(isTablet = isTablet)
                            SettingsPageRow(
                                page = SettingsPage.Layout,
                                description = Res.string.compose_settings_root_layout_description,
                                isTablet = isTablet,
                                onPageClick = onPageClick,
                            )
                        }
                    }
                    SettingsCategory.Watch -> {
                        if (isIos) {
                            SettingsPageRow(
                                page = SettingsPage.ContentDiscovery,
                                description = Res.string.compose_settings_root_content_discovery_description,
                                isTablet = isTablet,
                                onPageClick = onPageClick,
                            )
                            SettingsGroupDivider(isTablet = isTablet)
                            SettingsPageRow(
                                page = SettingsPage.Playback,
                                description = Res.string.settings_playback_subtitle,
                                isTablet = isTablet,
                                onPageClick = onPageClick,
                            )
                        } else {
                            SettingsPageRow(
                                page = SettingsPage.BrowsePlayback,
                                description = Res.string.morivia_browse_playback_description,
                                isTablet = isTablet,
                                onPageClick = onPageClick,
                            )
                        }
                    }
                    SettingsCategory.Services -> {
                        // One "Connections" card: integrations first, tracking underneath.
                        SettingsPageRow(
                            page = SettingsPage.Integrations,
                            description = Res.string.settings_connections_description,
                            isTablet = isTablet,
                            onPageClick = onPageClick,
                        )
                    }
                    SettingsCategory.System -> {
                        // One "System" card under Connections; Notifications, Advanced and About
                        // open inside it (owner's request, 2026-11-22).
                        SettingsPageRow(
                            page = SettingsPage.SystemSettings,
                            description = Res.string.settings_system_description,
                            isTablet = isTablet,
                            onPageClick = onPageClick,
                        )
                    }
                }
            }
        }
    }
    // Morivia's logo, name and version, right under the System card (owner, 2026-11-23).
    if (category == null || category == SettingsCategory.System) {
        noriviaBrandFooter(isTablet = isTablet)
    }
}

@Composable
private fun SettingsPageRow(
    page: SettingsPage,
    description: StringResource,
    isTablet: Boolean,
    onPageClick: (SettingsPage) -> Unit,
) {
    SettingsNavigationRow(
        title = stringResource(page.titleRes),
        description = stringResource(description),
        icon = page.icon,
        isTablet = isTablet,
        onClick = { onPageClick(page) },
    )
}
