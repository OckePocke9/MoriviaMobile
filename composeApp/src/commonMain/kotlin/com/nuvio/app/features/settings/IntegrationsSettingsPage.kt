package com.nuvio.app.features.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import com.nuvio.app.core.ui.nuvio
import com.nuvio.app.features.debrid.DebridSettings
import com.nuvio.app.features.simkl.SimklAuthUiState
import com.nuvio.app.features.tracking.TrackingSettingsUiState
import com.nuvio.app.features.trakt.TraktAuthUiState
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.compose_settings_page_integrations
import nuvio.composeapp.generated.resources.compose_settings_page_tracking
import nuvio.composeapp.generated.resources.settings_connections_integrations_description
import nuvio.composeapp.generated.resources.settings_connections_tracking_description
import org.jetbrains.compose.resources.stringResource

/** Which Connections sections are open. Both can be open at once; kept while the app runs. */
internal object ConnectionsSections {
    var integrationsOpen by mutableStateOf(false)
    var trackingOpen by mutableStateOf(false)
}

/**
 * The Connections page (owner's request, 2026-11-22): two cards, Integrations and Tracking,
 * each with its own icon and description. Tapping a card opens its settings right on this page,
 * and both can be open at the same time.
 */
internal fun LazyListScope.connectionsContent(
    isTablet: Boolean,
    debridSettings: DebridSettings,
    onTmdbClick: () -> Unit,
    onMdbListClick: () -> Unit,
    traktUiState: TraktAuthUiState,
    simklUiState: SimklAuthUiState,
    trackingSettingsUiState: TrackingSettingsUiState,
    commentsEnabled: Boolean,
    onCommentsEnabledChange: (Boolean) -> Unit,
    mdbListConnected: Boolean,
) {
    val integrationsOpen = ConnectionsSections.integrationsOpen
    val trackingOpen = ConnectionsSections.trackingOpen

    val integrationsHeader: @Composable () -> Unit = {
        ConnectionsSectionHeader(
            title = stringResource(Res.string.compose_settings_page_integrations),
            description = stringResource(Res.string.settings_connections_integrations_description),
            icon = SettingsPage.Debrid.icon,
            expanded = integrationsOpen,
            isTablet = isTablet,
            onToggle = { ConnectionsSections.integrationsOpen = !ConnectionsSections.integrationsOpen },
        )
    }
    val trackingHeader: @Composable () -> Unit = {
        ConnectionsSectionHeader(
            title = stringResource(Res.string.compose_settings_page_tracking),
            description = stringResource(Res.string.settings_connections_tracking_description),
            icon = SettingsPage.TraktAuthentication.icon,
            expanded = trackingOpen,
            isTablet = isTablet,
            onToggle = { ConnectionsSections.trackingOpen = !ConnectionsSections.trackingOpen },
        )
    }

    expandableSettingsSections(
        isTablet = isTablet,
        sections = listOf(
            ExpandableSettingsSection(
                key = "connections_integrations",
                open = integrationsOpen,
                header = integrationsHeader,
                content = {
                    debridSettingsContent(
                        isTablet = isTablet,
                        settings = debridSettings,
                        onTmdbClick = onTmdbClick,
                        onMdbListClick = onMdbListClick,
                    )
                },
            ),
            ExpandableSettingsSection(
                key = "connections_tracking",
                open = trackingOpen,
                header = trackingHeader,
                content = {
                    trackingSettingsContent(
                        isTablet = isTablet,
                        traktUiState = traktUiState,
                        simklUiState = simklUiState,
                        settingsUiState = trackingSettingsUiState,
                        commentsEnabled = commentsEnabled,
                        onCommentsEnabledChange = onCommentsEnabledChange,
                        mdbListConnected = mdbListConnected,
                        accountsSectionTitle = Res.string.compose_settings_page_tracking,
                    )
                },
            ),
        ),
    )
}

@Composable
internal fun ConnectionsSectionHeader(
    title: String,
    description: String,
    icon: ImageVector,
    expanded: Boolean,
    isTablet: Boolean,
    onToggle: () -> Unit,
) {
    val tokens = MaterialTheme.nuvio
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "connectionsChevron")
    SettingsNavigationRow(
        title = title,
        description = description,
        icon = icon,
        isTablet = isTablet,
        trailingContent = {
            Icon(
                imageVector = Icons.Rounded.ExpandMore,
                contentDescription = null,
                tint = tokens.colors.textSecondary,
                modifier = Modifier.rotate(rotation),
            )
        },
        onClick = onToggle,
    )
}
