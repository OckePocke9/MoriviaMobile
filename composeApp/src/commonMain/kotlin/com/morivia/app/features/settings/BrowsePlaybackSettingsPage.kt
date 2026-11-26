package com.morivia.app.features.settings

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.morivia.app.core.build.AppFeaturePolicy
import morivia.composeapp.generated.resources.Res
import morivia.composeapp.generated.resources.compose_settings_root_content_discovery_description
import morivia.composeapp.generated.resources.settings_playback_subtitle
import org.jetbrains.compose.resources.stringResource

internal object BrowsePlaybackSections {
    var discoveryOpen by mutableStateOf(false)
    var playbackOpen by mutableStateOf(false)
}

internal fun LazyListScope.browsePlaybackSettingsContent(
    isTablet: Boolean,
    onAddonsClick: () -> Unit,
    onPluginsClick: () -> Unit,
    onPageChange: (SettingsPage) -> Unit,
) {
    val discoveryOpen = BrowsePlaybackSections.discoveryOpen
    val playbackOpen = BrowsePlaybackSections.playbackOpen
    expandableSettingsSections(
        isTablet = isTablet,
        sections = listOf(
            ExpandableSettingsSection(
                key = "browse_playback_discovery",
                open = discoveryOpen,
                header = {
                    ConnectionsSectionHeader(
                        title = stringResource(SettingsPage.ContentDiscovery.titleRes),
                        description = stringResource(Res.string.compose_settings_root_content_discovery_description),
                        icon = SettingsPage.ContentDiscovery.icon,
                        expanded = discoveryOpen,
                        isTablet = isTablet,
                        onToggle = { BrowsePlaybackSections.discoveryOpen = !BrowsePlaybackSections.discoveryOpen },
                    )
                },
                content = {
                    contentDiscoveryContent(
                        isTablet = isTablet,
                        showPluginsEntry = AppFeaturePolicy.pluginsEnabled,
                        onAddonsClick = onAddonsClick,
                        onPluginsClick = onPluginsClick,
                        onMediaServersClick = { onPageChange(SettingsPage.MediaServers) },
                    )
                },
            ),
            ExpandableSettingsSection(
                key = "browse_playback_player",
                open = playbackOpen,
                header = {
                    ConnectionsSectionHeader(
                        title = stringResource(SettingsPage.Playback.titleRes),
                        description = stringResource(Res.string.settings_playback_subtitle),
                        icon = SettingsPage.Playback.icon,
                        expanded = playbackOpen,
                        isTablet = isTablet,
                        onToggle = { BrowsePlaybackSections.playbackOpen = !BrowsePlaybackSections.playbackOpen },
                    )
                },
                content = {
                    playbackSettingsContent(
                        isTablet = isTablet,
                        page = SettingsPage.Playback,
                        onPageChange = onPageChange,
                    )
                },
            ),
        ),
    )
}
