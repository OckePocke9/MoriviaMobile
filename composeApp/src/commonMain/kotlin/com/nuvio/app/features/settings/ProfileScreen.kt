package com.nuvio.app.features.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nuvio.app.core.auth.AuthRepository
import com.nuvio.app.core.auth.AuthState
import com.nuvio.app.core.ui.NuvioScreen
import com.nuvio.app.core.ui.NuvioScreenHeader
import com.nuvio.app.features.profiles.ActiveProfileMiniAvatar
import com.nuvio.app.features.profiles.AvatarRepository
import com.nuvio.app.features.profiles.ProfileRepository
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/** The profile tab owns account/profile actions. Settings is a separate destination. */
@Composable
internal fun ProfileScreen(
    modifier: Modifier = Modifier,
    onSwitchProfile: (() -> Unit)?,
    onAccount: () -> Unit,
    onSettings: () -> Unit,
) {
    val profiles by ProfileRepository.state.collectAsStateWithLifecycle()
    val avatars by AvatarRepository.avatars.collectAsStateWithLifecycle()
    val auth by AuthRepository.state.collectAsStateWithLifecycle()
    val profile = profiles.activeProfile
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 600.dp
        NuvioScreen(modifier = Modifier.fillMaxSize()) {
            item {
                NuvioScreenHeader(title = stringResource(Res.string.compose_nav_profile))
            }
            item {
                SettingsGroup(isTablet = isTablet) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = profile?.name ?: stringResource(Res.string.compose_nav_profile),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = stringResource(
                                    if (auth is AuthState.Authenticated && !(auth as AuthState.Authenticated).isAnonymous)
                                        Res.string.morivia_profile_synced else Res.string.morivia_profile_local,
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                        ActiveProfileMiniAvatar(profile, avatars, selected = false, size = 72)
                    }
                }
            }
            item {
                SettingsGroup(isTablet = isTablet) {
                    onSwitchProfile?.let { switch ->
                        SettingsNavigationRow(
                            title = stringResource(Res.string.compose_settings_root_switch_profile_title),
                            description = stringResource(Res.string.compose_settings_root_switch_profile_description),
                            icon = Icons.Default.People,
                            isTablet = isTablet,
                            onClick = switch,
                        )
                        SettingsGroupDivider(isTablet = isTablet)
                    }
                    SettingsNavigationRow(
                        title = stringResource(Res.string.compose_settings_page_account),
                        description = stringResource(Res.string.compose_settings_root_account_description),
                        icon = SettingsPage.Account.icon,
                        isTablet = isTablet,
                        onClick = onAccount,
                    )
                }
            }
            item {
                SettingsGroup(isTablet = isTablet) {
                    SettingsNavigationRow(
                        title = stringResource(Res.string.compose_settings_page_root),
                        description = stringResource(Res.string.morivia_profile_settings_description),
                        icon = Icons.Rounded.Settings,
                        isTablet = isTablet,
                        onClick = onSettings,
                    )
                }
            }
            settingsFooterContent(isTablet = isTablet)
        }
    }
}
