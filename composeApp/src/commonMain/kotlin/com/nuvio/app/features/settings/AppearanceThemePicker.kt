package com.nuvio.app.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nuvio.app.core.ui.AppTheme
import com.nuvio.app.core.ui.CustomThemeColors
import com.nuvio.app.core.ui.NuvioTokens
import com.nuvio.app.core.ui.ThemeColors
import com.nuvio.app.core.ui.accentBrush
import com.nuvio.app.core.ui.labelRes
import com.nuvio.app.core.ui.nuvio
import com.nuvio.app.features.membership.MemberAccessRepository
import com.nuvio.app.features.membership.availableAppThemes
import com.nuvio.app.features.profiles.ProfileRepository
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.cd_selected
import nuvio.composeapp.generated.resources.custom_theme_edit
import nuvio.composeapp.generated.resources.morivia_theme_custom
import nuvio.composeapp.generated.resources.morivia_theme_reset_original
import nuvio.composeapp.generated.resources.morivia_theme_show_fewer
import nuvio.composeapp.generated.resources.morivia_theme_show_more
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AppearanceThemePicker(
    isTablet: Boolean,
    selectedTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
) {
    val memberAccess by remember {
        MemberAccessRepository.ensureStarted()
        MemberAccessRepository.access
    }.collectAsStateWithLifecycle()
    val customColors by ThemeSettingsRepository.customThemeColors.collectAsStateWithLifecycle()
    val profileState by ProfileRepository.state.collectAsStateWithLifecycle()
    val profileId = profileState.activeProfile?.profileIndex
    val gradientEnabled = memberAccess.tier != null
    var showEditor by rememberSaveable(profileId, gradientEnabled) { mutableStateOf(false) }
    val allThemes = availableAppThemes(memberAccess.entitlements)
    val customAvailable = AppTheme.CUSTOM in allThemes
    // Like the earlier Morivia app: nine themes first, the rest behind the arrow; Custom has its own button.
    var expanded by rememberSaveable { mutableStateOf(false) }
    val gridThemes = allThemes.filter { it != AppTheme.CUSTOM }
    val themes = if (expanded || gridThemes.size <= COLLAPSED_THEME_COUNT) {
        gridThemes
    } else {
        gridThemes.take(COLLAPSED_THEME_COUNT).let { first ->
            // Keep the selected theme visible when it is one of the hidden ones.
            if (selectedTheme in gridThemes && selectedTheme !in first) first.dropLast(1) + selectedTheme else first
        }
    }
    val themeSpacing = if (isTablet) NuvioTokens.Space.s16 else NuvioTokens.Space.s12
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = if (isTablet) NuvioTokens.Space.s20 else NuvioTokens.Space.s16,
                vertical = if (isTablet) NuvioTokens.Space.s18 else NuvioTokens.Space.s14,
            ),
    ) {
        val preferredColumns = if (isTablet) 4 else 3
        val minThemeCellWidth = if (isTablet) 92.dp else 78.dp
        val themeColumns = ((maxWidth + themeSpacing) / (minThemeCellWidth + themeSpacing))
            .toInt().coerceIn(1, preferredColumns)
        Column(verticalArrangement = Arrangement.spacedBy(themeSpacing)) {
            themes.chunked(themeColumns).forEach { rowThemes ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(themeSpacing),
                ) {
                    rowThemes.forEach { theme ->
                        ThemeChip(
                            theme = theme,
                            customColors = customColors,
                            isSelected = theme == selectedTheme,
                            onClick = {
                                if (theme == AppTheme.CUSTOM) showEditor = true else onThemeSelected(theme)
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(themeColumns - rowThemes.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            ThemeActions(
                customAvailable = customAvailable,
                customColors = customColors,
                customSelected = selectedTheme == AppTheme.CUSTOM,
                onReset = { onThemeSelected(AppTheme.ORIGINAL) },
                onCustom = { showEditor = true },
            )
            if (gridThemes.size > COLLAPSED_THEME_COUNT) {
                val label = stringResource(if (expanded) Res.string.morivia_theme_show_fewer else Res.string.morivia_theme_show_more)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.nuvio.shapes.compactCard)
                        .clickable(onClickLabel = label) { expanded = !expanded }
                        .padding(vertical = NuvioTokens.Space.s4),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = label,
                        tint = MaterialTheme.nuvio.colors.textSecondary,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }
    }
    if (showEditor) {
        CustomThemeEditor(
            initialColors = customColors,
            allowGradient = gradientEnabled,
            onSave = ThemeSettingsRepository::setCustomTheme,
            onDismiss = { showEditor = false },
        )
    }
}

private const val COLLAPSED_THEME_COUNT = 9

/** "Reset to Original" and "Custom theme", as in the earlier Morivia app. */
@Composable
private fun ThemeActions(
    customAvailable: Boolean,
    customColors: CustomThemeColors,
    customSelected: Boolean,
    onReset: () -> Unit,
    onCustom: () -> Unit,
) {
    val tokens = MaterialTheme.nuvio
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(50)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(NuvioTokens.Space.s12),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .clip(shape)
                .border(1.dp, tokens.colors.textSecondary.copy(alpha = 0.35f), shape)
                .clickable(onClick = onReset),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(Res.string.morivia_theme_reset_original),
                style = MaterialTheme.typography.labelLarge,
                color = tokens.colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (customAvailable) {
            val customPalette = remember(customColors) { ThemeColors.getColorPalette(AppTheme.CUSTOM, customColors) }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(shape)
                    .border(
                        if (customSelected) 2.dp else 1.dp,
                        if (customSelected) customPalette.focusRing else tokens.colors.textSecondary.copy(alpha = 0.35f),
                        shape,
                    )
                    .clickable(onClick = onCustom)
                    .padding(horizontal = NuvioTokens.Space.s12),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(customPalette.accentBrush()),
                )
                Spacer(Modifier.size(NuvioTokens.Space.s8))
                Text(
                    text = stringResource(Res.string.morivia_theme_custom),
                    style = MaterialTheme.typography.labelLarge,
                    color = tokens.colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ThemeChip(
    theme: AppTheme,
    customColors: CustomThemeColors,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = MaterialTheme.nuvio
    val palette = remember(theme, customColors) { ThemeColors.getColorPalette(theme, customColors) }
    Column(
        modifier = modifier
            .clip(tokens.shapes.compactCard)
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = NuvioTokens.Space.s4, vertical = NuvioTokens.Space.s8),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(NuvioTokens.Space.s56).then(
                if (isSelected) Modifier.border(tokens.borders.medium, palette.focusRing, CircleShape)
                else Modifier,
            ),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(NuvioTokens.Space.s40 + NuvioTokens.Space.s4)
                    .clip(CircleShape)
                    .background(palette.accentBrush()),
                contentAlignment = Alignment.Center,
            ) {
                if (isSelected || theme == AppTheme.CUSTOM) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Edit,
                        contentDescription = stringResource(
                            if (isSelected) Res.string.cd_selected else Res.string.custom_theme_edit,
                        ),
                        tint = palette.onSecondary,
                        modifier = Modifier.size(NuvioTokens.Space.s22),
                    )
                }
            }
        }
        Spacer(Modifier.height(NuvioTokens.Space.s6))
        Text(
            text = stringResource(theme.labelRes),
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) tokens.colors.textPrimary else tokens.colors.textSecondary,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
