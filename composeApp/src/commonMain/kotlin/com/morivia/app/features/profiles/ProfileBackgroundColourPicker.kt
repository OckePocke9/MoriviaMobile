package com.morivia.app.features.profiles

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.morivia.app.core.ui.APPEARANCE_PRESETS
import com.morivia.app.core.ui.AppTheme
import com.morivia.app.core.ui.CustomThemeColors
import com.morivia.app.core.ui.ThemeColors
import com.morivia.app.core.ui.accentBrush
import com.morivia.app.core.ui.labelRes
import com.morivia.app.core.ui.nuvio
import com.morivia.app.features.settings.PROFILE_BACKGROUND_BLACK
import com.morivia.app.features.settings.PROFILE_BACKGROUND_ORIGINAL
import com.morivia.app.features.settings.PresetExpansionArrow
import com.morivia.app.features.settings.ThemeActions
import com.morivia.app.features.settings.alphabeticalKey
import morivia.composeapp.generated.resources.Res
import morivia.composeapp.generated.resources.cd_selected
import morivia.composeapp.generated.resources.profile_background_black
import org.jetbrains.compose.resources.stringResource

private data class ProfileBackgroundOption(val key: String, val label: String, val brush: Brush, val ring: Color, val dark: Boolean)

/** The name of a profile view background choice ("Black", or a theme name). */
@Composable
internal fun profileBackgroundLabel(key: String): String =
    if (key == PROFILE_BACKGROUND_BLACK) {
        stringResource(Res.string.profile_background_black)
    } else {
        stringResource((AppTheme.entries.firstOrNull { it.name == key } ?: AppTheme.ORIGINAL).labelRes)
    }

/**
 * The profile view background, chosen like a theme in Appearance (owner, 2026-11-23): the same
 * chips with ring and check and the same colours in the same alphabetical order, nine first and
 * the rest behind the arrow.
 */
@Composable
internal fun ProfileBackgroundColourPicker(selected: String, onSelected: (String) -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val themeOptions = APPEARANCE_PRESETS.map { theme ->
        val palette = ThemeColors.getColorPalette(theme)
        ProfileBackgroundOption(theme.name, stringResource(theme.labelRes), palette.accentBrush(), palette.focusRing, palette.secondary.luminance() > 0.6f)
    }
    // The same colours, in the same order, as the Theme picker in Appearance (owner, 2026-11-23).
    val alphabetic = themeOptions.sortedBy { alphabeticalKey(it.label) }
    val visible = if (expanded) alphabetic else alphabetic.take(COLLAPSED_COUNT)
    val spacing = 12.dp
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columns = ((maxWidth + spacing) / (78.dp + spacing)).toInt()
            .coerceIn(1, if (maxWidth >= 560.dp) 4 else 3)
        Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
            visible.chunked(columns).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    row.forEach { option ->
                        BackgroundChip(
                            option = option,
                            selected = option.key == selected,
                            onClick = { onSelected(option.key) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            ThemeActions(
                customAvailable = false,
                customColors = CustomThemeColors.Default,
                customSelected = false,
                onReset = { onSelected(PROFILE_BACKGROUND_ORIGINAL) },
                onCustom = {},
            )
            if (alphabetic.size > COLLAPSED_COUNT) {
                PresetExpansionArrow(expanded) { expanded = it }
            }
        }
    }
}

private const val COLLAPSED_COUNT = 9

@Composable
private fun BackgroundChip(option: ProfileBackgroundOption, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val tokens = MaterialTheme.nuvio
    Column(
        modifier = modifier
            .clip(tokens.shapes.compactCard)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(56.dp).then(
                if (selected) Modifier.border(tokens.borders.medium, option.ring, CircleShape) else Modifier,
            ),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(option.brush)
                    .then(
                        if (option.key == PROFILE_BACKGROUND_BLACK) {
                            Modifier.border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                        } else Modifier,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(Res.string.cd_selected),
                        tint = if (option.dark) Color(0xFF111111) else Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = option.label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) tokens.colors.textPrimary else tokens.colors.textSecondary,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
