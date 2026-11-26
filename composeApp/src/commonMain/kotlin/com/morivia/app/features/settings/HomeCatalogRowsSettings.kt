package com.morivia.app.features.settings

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.morivia.app.core.ui.Chip
import com.morivia.app.core.ui.MoriviaActionLabel
import com.morivia.app.core.ui.nuvio
import com.morivia.app.features.home.HomeCatalogSettingsItem
import com.morivia.app.features.home.HomeCatalogSettingsRepository
import com.morivia.app.features.home.findHomeCatalog
import morivia.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState

internal class HomeCatalogRowsState(
    val settings: MoriviaLayoutSettings,
    val catalogs: Map<HomeCatalogRow, HomeCatalogSettingsItem?>,
    val reorderState: ReorderableLazyListState,
    val expandedKey: MutableState<String?>,
)

@Composable
internal fun rememberHomeCatalogRowsState(listState: LazyListState): HomeCatalogRowsState {
    val settings by remember {
        MoriviaLayoutSettingsRepository.ensureLoaded()
        MoriviaLayoutSettingsRepository.settings
    }.collectAsStateWithLifecycle()
    val homeSettings by HomeCatalogSettingsRepository.uiState.collectAsStateWithLifecycle()
    val catalogs = remember(homeSettings.items) {
        HomeCatalogRow.entries.associateWith { homeSettings.items.findHomeCatalog(it) }
    }
    val reorderState = rememberReorderableLazyListState(listState) { from, to ->
        MoriviaLayoutSettingsRepository.update { current ->
            val order = current.orderedHomeCatalogRows().toMutableList()
            val fromIndex = order.indexOfFirst { it.settingsKey == from.key }
            val toIndex = order.indexOfFirst { it.settingsKey == to.key }
            if (fromIndex == -1 || toIndex == -1) return@update current

            order.add(toIndex, order.removeAt(fromIndex))
            current.copy(homeCatalogOrder = order)
        }
    }
    val expandedKey = rememberSaveable { mutableStateOf<String?>(null) }
    return HomeCatalogRowsState(settings, catalogs, reorderState, expandedKey)
}

private val HomeCatalogRow.settingsKey: String
    get() = "home_catalog_${name}"

internal fun LazyListScope.homeCatalogRowsSettingsContent(
    isTablet: Boolean,
    state: HomeCatalogRowsState,
) {
    val rows = state.settings.orderedHomeCatalogRows()
    item {
        SettingsSection(
            title = stringResource(Res.string.morivia_catalogs_title),
            isTablet = isTablet,
            actions = {
                MoriviaActionLabel(text = stringResource(Res.string.action_reset), onClick = {
                    state.expandedKey.value = null
                    HomeCatalogSettingsRepository.resetCatalogRows(state.catalogs.values.mapNotNull { it?.key })
                    MoriviaLayoutSettingsRepository.update {
                        it.copy(homeCatalogOrder = HomeCatalogRow.entries.toList(), homeCatalogLandscape = emptyMap())
                    }
                })
            },
        ) {}
    }
    itemsIndexed(rows, key = { _, row -> row.settingsKey }) { index, row ->
        val catalog = state.catalogs[row]
        ReorderableItem(state.reorderState, key = row.settingsKey) { isDragging ->
            val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)
            Surface(
                color = MaterialTheme.nuvio.colors.surface,
                shape = MaterialTheme.nuvio.shapes.card,
                shadowElevation = elevation,
            ) {
                Column {
                    if (catalog != null) {
                        HomescreenCatalogRow(
                            item = catalog,
                            isTablet = isTablet,
                            expanded = state.expandedKey.value == row.settingsKey,
                            onExpandedChange = { expanded -> state.expandedKey.value = if (expanded) row.settingsKey else null },
                            onTitleChange = { HomeCatalogSettingsRepository.setCustomTitle(catalog.key, it) },
                            onEnabledChange = { HomeCatalogSettingsRepository.setEnabled(catalog.key, it) },
                            dragHandleScope = this@ReorderableItem,
                            useCatalogCardStyle = true,
                        )
                    } else {
                        Text(
                            text = stringResource(when (row) {
                                HomeCatalogRow.POPULAR_MOVIES -> Res.string.morivia_catalog_popular_movies
                                HomeCatalogRow.POPULAR_SERIES -> Res.string.morivia_catalog_popular_series
                                HomeCatalogRow.FEATURED_MOVIES -> Res.string.morivia_catalog_featured_movies
                                HomeCatalogRow.FEATURED_SERIES -> Res.string.morivia_catalog_featured_series
                            }),
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.nuvio.colors.textMuted,
                        )
                    }

                    val isLandscape = state.settings.usesLandscapeCards(row)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(false, true).forEach { landscape ->
                            Chip(
                                label = stringResource(if (landscape) Res.string.morivia_catalog_landscape else Res.string.morivia_catalog_poster),
                                selected = isLandscape == landscape,
                                onClick = {
                                    MoriviaLayoutSettingsRepository.update {
                                        it.copy(homeCatalogLandscape = it.homeCatalogLandscape + (row to landscape))
                                    }
                                },
                            )
                        }
                    }
                    if (index < rows.lastIndex) SettingsGroupDivider(isTablet = isTablet)
                }
            }
        }
    }
}
