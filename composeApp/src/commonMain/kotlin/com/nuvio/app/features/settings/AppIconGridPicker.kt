package com.nuvio.app.features.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nuvio.app.core.ui.NuvioLoadingIndicator
import com.nuvio.app.core.ui.NuvioTokens
import com.nuvio.app.core.ui.APPEARANCE_PRESETS
import com.nuvio.app.core.ui.ThemeColors
import com.nuvio.app.core.ui.nuvio
import com.nuvio.app.features.profiles.CopperInitialsPalette
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.cd_selected
import nuvio.composeapp.generated.resources.morivia_theme_show_fewer
import nuvio.composeapp.generated.resources.morivia_theme_show_more
import nuvio.composeapp.generated.resources.settings_appearance_app_icon_change_failed
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * The app icon picker, built exactly like the Appearance theme picker: the same columns (three on
 * a phone, four on a tablet), the same chips with ring, check and name, nine first and the rest
 * behind the arrow, and the icons in the theme picker's order, but with Morivia's app icons.
 */
@Composable
internal fun AppIconGridPicker(
    isTablet: Boolean,
    state: AppIconSettingsState,
    onSelected: (AppIconOption) -> Unit,
) {
    val icons = remember { APPEARANCE_PRESETS.map { AppIconOption.valueOf(it.name) } }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var showCustom by rememberSaveable { mutableStateOf(false) }
    val customColors by ThemeSettingsRepository.customThemeColors.collectAsStateWithLifecycle()
    val alphabetic = icons.map { it to stringResource(it.labelResource).lowercase() }.sortedBy { it.second }.map { it.first }
    val visible = if (expanded) alphabetic else alphabetic.collapsedPresets(state.selected, COLLAPSED_ICON_COUNT)
    val spacing = if (isTablet) NuvioTokens.Space.s16 else NuvioTokens.Space.s12
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = if (isTablet) NuvioTokens.Space.s20 else NuvioTokens.Space.s16,
                vertical = if (isTablet) NuvioTokens.Space.s18 else NuvioTokens.Space.s14,
            ),
    ) {
        val preferredColumns = if (isTablet) 4 else 3
        val minCellWidth = if (isTablet) 92.dp else 78.dp
        val columns = ((maxWidth + spacing) / (minCellWidth + spacing)).toInt().coerceIn(1, preferredColumns)
        Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
            if (state.changeFailed) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.nuvio.shapes.compactCard,
                    color = MaterialTheme.nuvio.colors.danger.copy(alpha = 0.12f),
                ) {
                    Text(
                        text = stringResource(Res.string.settings_appearance_app_icon_change_failed),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.nuvio.colors.danger,
                    )
                }
            }
            visible.chunked(columns).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                ) {
                    row.forEach { icon ->
                        AppIconChip(
                            icon = icon,
                            isSelected = icon == state.selected,
                            isPending = icon == state.pending,
                            enabled = state.pending == null,
                            onClick = { onSelected(icon) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            ThemeActions(
                customAvailable = true,
                customColors = customColors,
                customSelected = false,
                onReset = { if (state.pending == null) onSelected(AppIconOption.ORIGINAL) },
                onCustom = { if (state.pending == null) showCustom = true },
            )
            if (icons.size > COLLAPSED_ICON_COUNT) {
                PresetExpansionArrow(expanded) { expanded = it }
            }
        }
    }
    if (showCustom) {
        CustomThemeEditor(
            initialColors = customColors,
            allowGradient = false,
            onSave = { onSelected(iconForTheme(com.nuvio.app.core.ui.AppTheme.CUSTOM, it)) },
            onDismiss = { showCustom = false },
        )
    }
}

private const val COLLAPSED_ICON_COUNT = 9

/** One app icon, drawn like a theme chip: the icon in the circle, ring and check when chosen. */
@Composable
private fun AppIconChip(
    icon: AppIconOption,
    isSelected: Boolean,
    isPending: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = MaterialTheme.nuvio
    val ring = icon.theme?.let { ThemeColors.getColorPalette(it).focusRing } ?: CopperInitialsPalette.focusRing
    Column(
        modifier = modifier
            .clip(tokens.shapes.compactCard)
            .selectable(selected = isSelected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = NuvioTokens.Space.s4, vertical = NuvioTokens.Space.s8),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(NuvioTokens.Space.s56).then(
                if (isSelected || isPending) Modifier.border(tokens.borders.medium, ring, CircleShape) else Modifier,
            ),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(NuvioTokens.Space.s40 + NuvioTokens.Space.s4)
                    .clip(CircleShape)
                    .background(Color.Black),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(icon.previewResource),
                    contentDescription = null,
                    // The logo fills about half of the icon image; enlarge it to fill the circle.
                    modifier = Modifier.fillMaxSize().graphicsLayer(scaleX = 1.45f, scaleY = 1.45f),
                    contentScale = ContentScale.Crop,
                )
            }
            // A small badge instead of darkening the icon, so the chosen colour stays clear.
            if (isSelected || isPending) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(NuvioTokens.Space.s22)
                        .clip(CircleShape)
                        .background(ring),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isPending) {
                        NuvioLoadingIndicator(modifier = Modifier.size(14.dp), color = Color(0xFF111111))
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(Res.string.cd_selected),
                            tint = Color(0xFF111111),
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(NuvioTokens.Space.s6))
        Text(
            text = stringResource(icon.labelResource),
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
