package com.morivia.app.features.settings

import androidx.compose.foundation.lazy.LazyListScope
import com.morivia.app.core.build.AppFeaturePolicy
import morivia.composeapp.generated.resources.Res
import morivia.composeapp.generated.resources.compose_settings_page_addons
import morivia.composeapp.generated.resources.compose_settings_page_media_servers
import morivia.composeapp.generated.resources.compose_settings_page_plugins
import morivia.composeapp.generated.resources.settings_content_discovery_addons_description
import morivia.composeapp.generated.resources.settings_content_discovery_addons_description_appstore
import morivia.composeapp.generated.resources.settings_content_discovery_media_servers_description
import morivia.composeapp.generated.resources.settings_content_discovery_plugins_description
import morivia.composeapp.generated.resources.settings_content_discovery_section_sources
import org.jetbrains.compose.resources.stringResource

internal fun LazyListScope.contentDiscoveryContent(
    isTablet: Boolean,
    showPluginsEntry: Boolean,
    onAddonsClick: () -> Unit,
    onPluginsClick: () -> Unit,
    onMediaServersClick: () -> Unit,
) {
    // Recent searches moved to Layout, Search: one switch that saves and shows them (owner, 2026-11-26).
    item {
        SettingsSection(
            title = stringResource(Res.string.settings_content_discovery_section_sources),
            isTablet = isTablet,
        ) {
            SettingsGroup(isTablet = isTablet) {
                SettingsNavigationRow(
                    title = stringResource(Res.string.compose_settings_page_addons),
                    description = stringResource(
                        if (AppFeaturePolicy.personalMediaAddonCopyEnabled) {
                            Res.string.settings_content_discovery_addons_description_appstore
                        } else {
                            Res.string.settings_content_discovery_addons_description
                        },
                    ),
                    isTablet = isTablet,
                    onClick = onAddonsClick,
                )
                SettingsNavigationRow(
                    title = stringResource(Res.string.compose_settings_page_media_servers),
                    description = stringResource(Res.string.settings_content_discovery_media_servers_description),
                    isTablet = isTablet,
                    onClick = onMediaServersClick,
                )
                if (showPluginsEntry) {
                    SettingsNavigationRow(
                        title = stringResource(Res.string.compose_settings_page_plugins),
                        description = stringResource(Res.string.settings_content_discovery_plugins_description),
                        isTablet = isTablet,
                        onClick = onPluginsClick,
                    )
                }
            }
        }
    }
}
