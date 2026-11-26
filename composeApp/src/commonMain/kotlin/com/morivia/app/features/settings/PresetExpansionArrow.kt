package com.morivia.app.features.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.morivia.app.core.ui.nuvio
import kotlinx.coroutines.launch
import morivia.composeapp.generated.resources.Res
import morivia.composeapp.generated.resources.morivia_theme_show_fewer
import morivia.composeapp.generated.resources.morivia_theme_show_more
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PresetExpansionArrow(expanded: Boolean, onExpandedChange: (Boolean) -> Unit) {
    val anchor = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    val label = stringResource(if (expanded) Res.string.morivia_theme_show_fewer else Res.string.morivia_theme_show_more)
    Box(
        modifier = Modifier.fillMaxWidth()
            .bringIntoViewRequester(anchor)
            .clip(MaterialTheme.nuvio.shapes.compactCard)
            .clickable(onClickLabel = label) {
                onExpandedChange(!expanded)
                if (expanded) scope.launch {
                    // Wait for the collapsed grid to be laid out before scrolling to its arrow.
                    withFrameNanos { }
                    withFrameNanos { }
                    anchor.bringIntoView()
                }
            }
            .padding(vertical = 4.dp),
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

/** Keep a selected preset visible while preserving the sorted order of the source list. */
internal fun <T> List<T>.collapsedPresets(selected: T, limit: Int): List<T> {
    val first = take(limit)
    if (selected in first || selected !in this) return first
    return filterIndexed { index, preset -> index < limit - 1 || preset == selected }
}

/**
 * Sort key for colour names: case-insensitive, with å, ä and ö after z as in the Swedish and
 * Finnish alphabets (owner, 2026-11-23).
 */
internal fun alphabeticalKey(label: String): String =
    label.lowercase().map { char ->
        when (char) {
            'å' -> "z\u0001"
            'ä', 'æ' -> "z\u0002"
            'ö', 'ø' -> "z\u0003"
            else -> char.toString()
        }
    }.joinToString("")
