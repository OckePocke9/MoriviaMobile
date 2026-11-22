package com.nuvio.app.features.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nuvio.app.core.ui.NuvioLoadingIndicator
import com.nuvio.app.core.ui.nuvio

/**
 * Redesigned tracking settings (owner's request, 2026-11-22): every source and option is
 * picked with one tap on the page instead of opening a dialog. Only options that can be used
 * right now (connected services) are offered, plus whatever is currently saved.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun <T> TrackingChoiceRow(
    title: String,
    options: List<TrackingPickerOption<T>>,
    selected: T,
    onSelected: (T) -> Unit,
    isTablet: Boolean,
    modifier: Modifier = Modifier,
    description: String? = null,
    supportingMessage: String? = null,
    isLoading: Boolean = false,
    onBrandCard: Boolean = false,
) {
    val tokens = MaterialTheme.nuvio
    val horizontal = if (onBrandCard) 0.dp else if (isTablet) 20.dp else 16.dp
    val vertical = if (onBrandCard) 4.dp else if (isTablet) 16.dp else 14.dp
    val visibleOptions = options.filter { it.enabled || it.value == selected }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontal, vertical = vertical),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = if (onBrandCard) Color.White else tokens.colors.textPrimary,
            )
            if (isLoading) {
                NuvioLoadingIndicator(color = if (onBrandCard) Color.White else tokens.colors.accent, modifier = Modifier.size(16.dp))
            }
        }
        description?.takeIf(String::isNotBlank)?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = if (onBrandCard) Color.White.copy(alpha = 0.72f) else tokens.colors.textSecondary,
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            visibleOptions.forEach { option ->
                TrackingChoiceChip(
                    label = option.title,
                    selected = option.value == selected,
                    onBrandCard = onBrandCard,
                    onClick = { if (option.value != selected) onSelected(option.value) },
                )
            }
        }
        supportingMessage?.takeIf(String::isNotBlank)?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = if (onBrandCard) Color.White.copy(alpha = 0.72f) else tokens.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun TrackingChoiceChip(
    label: String,
    selected: Boolean,
    onBrandCard: Boolean,
    onClick: () -> Unit,
) {
    val tokens = MaterialTheme.nuvio
    val shape = RoundedCornerShape(50)
    val fill = when {
        !selected -> Color.Transparent
        onBrandCard -> Color.White
        else -> tokens.colors.accent
    }
    val textColor = when {
        selected && onBrandCard -> Color.Black
        selected -> Color.White
        onBrandCard -> Color.White.copy(alpha = 0.86f)
        else -> tokens.colors.textPrimary.copy(alpha = 0.86f)
    }
    val borderColor = when {
        selected -> Color.Transparent
        onBrandCard -> Color.White.copy(alpha = 0.4f)
        else -> tokens.colors.textPrimary.copy(alpha = 0.22f)
    }
    Text(
        text = label,
        modifier = Modifier
            .clip(shape)
            .background(fill, shape)
            .border(BorderStroke(1.dp, borderColor), shape)
            .semantics { this.selected = selected }
            .clickable(role = Role.RadioButton, onClick = onClick)
            .heightIn(min = 36.dp)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        color = textColor,
    )
}

