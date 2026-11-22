package com.nuvio.app.features.settings

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.nuvio.app.features.notifications.EpisodeReleaseNotificationsUiState
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.compose_settings_category_about
import nuvio.composeapp.generated.resources.compose_settings_page_advanced
import nuvio.composeapp.generated.resources.compose_settings_page_notifications
import nuvio.composeapp.generated.resources.compose_settings_root_about_description
import nuvio.composeapp.generated.resources.compose_settings_root_advanced_description
import nuvio.composeapp.generated.resources.compose_settings_root_notifications_description
import org.jetbrains.compose.resources.stringResource

/** Which System sections are open. Any number can be open at once; kept while the app runs. */
internal object SystemSections {
    var notificationsOpen by mutableStateOf(false)
    var advancedOpen by mutableStateOf(false)
    var aboutOpen by mutableStateOf(false)
}

/**
 * The System page (owner's request, 2026-11-22), built like Connections: Notifications,
 * Advanced and About open right on the page. Closed sections next to each other share one
 * card with a light line between them.
 */
internal fun LazyListScope.systemSettingsContent(
    isTablet: Boolean,
    episodeReleaseNotificationsUiState: EpisodeReleaseNotificationsUiState,
    rememberLastProfileEnabled: Boolean,
    onSupportersContributorsClick: () -> Unit,
    onLicensesAttributionsClick: () -> Unit,
    onCheckForUpdatesClick: (() -> Unit)?,
    onTestUpdateBannerClick: (() -> Unit)?,
    showSupportersContributorsPage: Boolean,
) {
    val sections = listOf(
        ExpandableSettingsSection(
            key = "system_notifications",
            open = SystemSections.notificationsOpen,
            header = {
                ConnectionsSectionHeader(
                    title = stringResource(Res.string.compose_settings_page_notifications),
                    description = stringResource(Res.string.compose_settings_root_notifications_description),
                    icon = SettingsPage.Notifications.icon,
                    expanded = SystemSections.notificationsOpen,
                    isTablet = isTablet,
                    onToggle = { SystemSections.notificationsOpen = !SystemSections.notificationsOpen },
                )
            },
            content = {
                notificationsSettingsContent(isTablet = isTablet, uiState = episodeReleaseNotificationsUiState)
            },
        ),
        ExpandableSettingsSection(
            key = "system_advanced",
            open = SystemSections.advancedOpen,
            header = {
                ConnectionsSectionHeader(
                    title = stringResource(Res.string.compose_settings_page_advanced),
                    description = stringResource(Res.string.compose_settings_root_advanced_description),
                    icon = SettingsPage.Advanced.icon,
                    expanded = SystemSections.advancedOpen,
                    isTablet = isTablet,
                    onToggle = { SystemSections.advancedOpen = !SystemSections.advancedOpen },
                )
            },
            content = {
                advancedSettingsContent(isTablet = isTablet, rememberLastProfileEnabled = rememberLastProfileEnabled)
            },
        ),
        ExpandableSettingsSection(
            key = "system_about",
            open = SystemSections.aboutOpen,
            header = {
                ConnectionsSectionHeader(
                    title = stringResource(Res.string.compose_settings_category_about),
                    description = stringResource(Res.string.compose_settings_root_about_description),
                    icon = SettingsPage.About.icon,
                    expanded = SystemSections.aboutOpen,
                    isTablet = isTablet,
                    onToggle = { SystemSections.aboutOpen = !SystemSections.aboutOpen },
                )
            },
            content = {
                aboutSettingsContent(
                    isTablet = isTablet,
                    onSupportersContributorsClick = onSupportersContributorsClick,
                    onLicensesAttributionsClick = onLicensesAttributionsClick,
                    onCheckForUpdatesClick = onCheckForUpdatesClick,
                    onTestUpdateBannerClick = onTestUpdateBannerClick,
                    showSupportersContributorsPage = showSupportersContributorsPage,
                )
            },
        ),
    )
    expandableSettingsSections(isTablet = isTablet, sections = sections)
}

internal class ExpandableSettingsSection(
    val key: String,
    val open: Boolean,
    val header: @Composable () -> Unit,
    val content: LazyListScope.() -> Unit,
)

/**
 * Lays out expandable sections: closed headers next to each other share one card with light
 * dividers; an open section's settings follow right under its own header.
 */
internal fun LazyListScope.expandableSettingsSections(
    isTablet: Boolean,
    sections: List<ExpandableSettingsSection>,
) {
    var index = 0
    while (index < sections.size) {
        val section = sections[index]
        if (section.open) {
            item(key = section.key) {
                SettingsGroup(isTablet = isTablet, modifier = settingsItemMotion().animateContentSize()) { section.header() }
            }
            animatedSettingsItems(section.key, section.content)
            index++
            continue
        }
        // A run of headers: closed sections, ending with (and including) the next open one.
        val run = mutableListOf(section)
        var next = index + 1
        while (next < sections.size) {
            run += sections[next]
            if (sections[next].open) break
            next++
        }
        val last = run.last()
        item(key = section.key) {
            SettingsGroup(isTablet = isTablet, modifier = settingsItemMotion().animateContentSize()) {
                run.forEachIndexed { position, member ->
                    if (position > 0) SettingsGroupDivider(isTablet = isTablet)
                    member.header()
                }
            }
        }
        if (last.open) animatedSettingsItems(last.key, last.content)
        index += run.size
    }
}
