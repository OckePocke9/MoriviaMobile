package com.morivia.app.features.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material.icons.rounded.InvertColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.morivia.app.core.ui.AppTheme
import com.morivia.app.core.ui.MoriviaBottomSheetActionRow
import com.morivia.app.core.ui.MoriviaModalBottomSheet
import com.morivia.app.core.ui.MoriviaStatusModal
import com.morivia.app.core.ui.dismissMoriviaBottomSheet
import com.morivia.app.core.ui.labelRes
import com.morivia.app.isIos
import kotlinx.coroutines.launch
import morivia.composeapp.generated.resources.Res
import morivia.composeapp.generated.resources.cd_selected
import morivia.composeapp.generated.resources.settings_appearance_amoled_black
import morivia.composeapp.generated.resources.settings_appearance_amoled_description
import morivia.composeapp.generated.resources.settings_appearance_app_icon
import morivia.composeapp.generated.resources.settings_appearance_app_language
import morivia.composeapp.generated.resources.settings_appearance_app_language_restart_message
import morivia.composeapp.generated.resources.settings_appearance_app_language_restart_title
import morivia.composeapp.generated.resources.settings_appearance_app_language_sheet_title
import morivia.composeapp.generated.resources.settings_appearance_section_display
import morivia.composeapp.generated.resources.settings_appearance_section_theme
import morivia.composeapp.generated.resources.settings_theme_card_title
import morivia.composeapp.generated.resources.morivia_appearance_preview_button
import morivia.composeapp.generated.resources.morivia_appearance_preview_icon
import morivia.composeapp.generated.resources.morivia_appearance_preview_text
import morivia.composeapp.generated.resources.morivia_appearance_preview_title
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.morivia.app.core.ui.nuvio
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Which Appearance cards are open; kept while the app runs. */
internal object AppearanceSections {
    var themeOpen by mutableStateOf(false)
    var appIconOpen by mutableStateOf(false)
}

internal fun LazyListScope.appearanceSettingsContent(
    isTablet: Boolean,
    selectedTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    amoledEnabled: Boolean,
    onAmoledToggle: (Boolean) -> Unit,
    appIconState: AppIconSettingsState,
    onAppIconSelected: (AppIconOption) -> Unit,
    onAppIconFailureDismissed: () -> Unit,
    selectedAppLanguage: AppLanguage,
    onAppLanguageSelected: (AppLanguage) -> Unit,
) {
    // Theme and App icon are their own cards; tapping one opens its picker right here (owner, 2026-11-23).
    val themeOpen = AppearanceSections.themeOpen
    val iconOpen = AppearanceSections.appIconOpen
    expandableSettingsSections(
        isTablet = isTablet,
        sections = listOf(
            ExpandableSettingsSection(
                key = "appearance_theme",
                open = themeOpen,
                header = {
                    ConnectionsSectionHeader(
                        title = stringResource(Res.string.settings_theme_card_title),
                        description = stringResource(selectedTheme.labelRes),
                        icon = Icons.Rounded.InvertColors,
                        expanded = themeOpen,
                        isTablet = isTablet,
                        onToggle = { AppearanceSections.themeOpen = !AppearanceSections.themeOpen },
                    )
                },
                content = {
                    item(key = "appearance_theme_picker") {
                        SettingsGroup(isTablet = isTablet) {
                            AppearanceThemePicker(
                                isTablet = isTablet,
                                selectedTheme = selectedTheme,
                                onThemeSelected = onThemeSelected,
                            )
                        }
                    }
                },
            ),
            ExpandableSettingsSection(
                key = "appearance_app_icon",
                open = iconOpen,
                header = {
                    ConnectionsSectionHeader(
                        title = stringResource(Res.string.settings_appearance_app_icon),
                        description = stringResource(appIconState.selected.labelResource),
                        icon = Icons.Rounded.Widgets,
                        expanded = iconOpen,
                        isTablet = isTablet,
                        onToggle = { AppearanceSections.appIconOpen = !AppearanceSections.appIconOpen },
                    )
                },
                content = {
                    item(key = "appearance_app_icon_picker") {
                        SettingsGroup(isTablet = isTablet) {
                            AppIconGridPicker(
                                isTablet = isTablet,
                                state = appIconState,
                                onSelected = { icon ->
                                    onAppIconFailureDismissed()
                                    onAppIconSelected(icon)
                                },
                            )
                        }
                    }
                },
            ),
        ),
    )
    item {
        var showLanguageSheet by remember { mutableStateOf(false) }
        var showLanguageRestartDialog by remember { mutableStateOf(false) }
        val layoutDirection = LocalLayoutDirection.current
        SettingsSection(
            title = stringResource(Res.string.settings_appearance_section_display),
            isTablet = isTablet,
        ) {
            SettingsGroup(isTablet = isTablet) {
                SettingsSwitchRow(
                    title = stringResource(Res.string.settings_appearance_amoled_black),
                    description = stringResource(Res.string.settings_appearance_amoled_description),
                    checked = amoledEnabled,
                    isTablet = isTablet,
                    onCheckedChange = onAmoledToggle,
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsNavigationRow(
                    title = stringResource(Res.string.settings_appearance_app_language),
                    description = stringResource(selectedAppLanguage.labelRes),
                    isTablet = isTablet,
                    onClick = { showLanguageSheet = true },
                )
            }
        }

        if (showLanguageSheet) {
            AppearanceLanguageBottomSheet(
                selectedLanguage = selectedAppLanguage,
                onLanguageSelected = {
                    onAppLanguageSelected(it)
                    showLanguageSheet = false
                    val newLayoutDirection = if (it.isRightToLeft()) LayoutDirection.Rtl else LayoutDirection.Ltr
                    if (isIos && newLayoutDirection != layoutDirection) showLanguageRestartDialog = true
                },
                onDismiss = { showLanguageSheet = false },
            )
        }

        MoriviaStatusModal(
            title = stringResource(Res.string.settings_appearance_app_language_restart_title),
            message = stringResource(Res.string.settings_appearance_app_language_restart_message),
            isVisible = showLanguageRestartDialog,
            onConfirm = { showLanguageRestartDialog = false },
        )
    }
    item {
        SettingsSection(
            title = stringResource(Res.string.morivia_appearance_preview_title),
            isTablet = isTablet,
        ) {
            SettingsGroup(isTablet = isTablet) {
                AppearanceThemePreview(isTablet = isTablet, icon = appIconState.selected)
            }
        }
    }
}

/** The earlier Morivia app's "Theme preview": the app icon, text on the background and a button. */
@Composable
private fun AppearanceThemePreview(isTablet: Boolean, icon: AppIconOption) {
    val tokens = MaterialTheme.nuvio
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isTablet) 20.dp else 16.dp, vertical = 16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(14.dp),
    ) {
        androidx.compose.foundation.layout.Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
        ) {
            AppIconThumbnail(icon = icon, modifier = Modifier.size(44.dp), cornerRadius = 11.dp)
            androidx.compose.foundation.layout.Column {
                Text(
                    text = "Morivia",
                    style = MaterialTheme.typography.titleMedium,
                    color = tokens.colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(Res.string.morivia_appearance_preview_icon, stringResource(icon.labelResource)),
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.colors.textSecondary,
                )
            }
        }
        Text(
            text = stringResource(Res.string.morivia_appearance_preview_text),
            style = MaterialTheme.typography.bodyMedium,
            color = tokens.colors.textSecondary,
        )
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
                .background(rememberThemeAccent().accent)
                .padding(horizontal = 22.dp, vertical = 10.dp),
        ) {
            Text(
                text = stringResource(Res.string.morivia_appearance_preview_button),
                style = MaterialTheme.typography.labelLarge,
                color = rememberThemeAccent().onAccent,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private data class AppLanguageSheetOption(
    val language: AppLanguage,
    val labelRes: StringResource,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppearanceLanguageBottomSheet(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val options = remember {
        AppLanguage.entries.map { language ->
            AppLanguageSheetOption(
                language = language,
                labelRes = language.labelRes,
            )
        }
    }

    MoriviaModalBottomSheet(
        onDismissRequest = {
            coroutineScope.launch {
                dismissMoriviaBottomSheet(sheetState = sheetState, onDismiss = onDismiss)
            }
        },
        sheetState = sheetState,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
        ) {
            item {
                Text(
                    text = stringResource(Res.string.settings_appearance_app_language_sheet_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                )
            }

            items(options) { option ->
                MoriviaBottomSheetActionRow(
                    title = stringResource(option.labelRes),
                    selected = option.language == selectedLanguage,
                    onClick = {
                        onLanguageSelected(option.language)
                        coroutineScope.launch {
                            dismissMoriviaBottomSheet(sheetState = sheetState, onDismiss = onDismiss)
                        }
                    },
                    trailingContent = {
                        if (option.language == selectedLanguage) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(Res.string.cd_selected),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    },
                )
            }
        }
    }
}
