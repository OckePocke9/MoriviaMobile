package com.nuvio.app.features.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.nuvio.app.features.home.MetaPreview
import com.nuvio.app.features.home.isSeriesLikeType
import com.nuvio.app.features.settings.MoriviaLibrarySection
import com.nuvio.app.features.settings.MoriviaLibrarySort
import com.nuvio.app.features.settings.labelRes
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/** One poster in the earlier Morivia app's Library grid. */
internal data class MoriviaLibraryEntry(
    val key: String,
    val id: String,
    val type: String,
    val name: String,
    val poster: String?,
    val savedAtEpochMs: Long,
    val libraryItem: LibraryItem?,
    val preview: MetaPreview,
)

/** The entries of [section], sorted by [sort]. */
internal fun moriviaLibraryEntries(
    items: List<LibraryItem>,
    continueWatching: List<MetaPreview>,
    lastWatchedById: Map<String, Long>,
    section: MoriviaLibrarySection,
    sort: MoriviaLibrarySort,
): List<MoriviaLibraryEntry> {
    val saved = items
        .distinctBy { "${it.type}|${it.id}" }
        .map { item ->
            MoriviaLibraryEntry(
                key = "${item.type}|${item.id}",
                id = item.id,
                type = item.type,
                name = item.name,
                poster = item.poster,
                savedAtEpochMs = item.savedAtEpochMs,
                libraryItem = item,
                preview = item.toMetaPreview(),
            )
        }
    val entries = when (section) {
        MoriviaLibrarySection.RECENTLY_ADDED -> saved
        MoriviaLibrarySection.MOVIES -> saved.filter { !it.type.isSeriesLikeType() }
        MoriviaLibrarySection.SHOWS -> saved.filter { it.type.isSeriesLikeType() }
        MoriviaLibrarySection.CONTINUE_WATCHING -> continueWatching
            .distinctBy { "${it.type}|${it.id}" }
            .map { preview ->
                MoriviaLibraryEntry(
                    key = "${preview.type}|${preview.id}",
                    id = preview.id,
                    type = preview.type,
                    name = preview.name,
                    poster = preview.poster,
                    savedAtEpochMs = lastWatchedById[preview.id] ?: 0L,
                    libraryItem = null,
                    preview = preview,
                )
            }
    }
    return when (sort) {
        MoriviaLibrarySort.ADDED -> entries.sortedByDescending { it.savedAtEpochMs }
        MoriviaLibrarySort.NAME -> entries.sortedBy { it.name.lowercase() }
        MoriviaLibrarySort.RECENTLY_WATCHED -> entries.sortedWith(
            compareByDescending<MoriviaLibraryEntry> { lastWatchedById[it.id] ?: 0L }
                .thenByDescending { it.savedAtEpochMs },
        )
    }
}

internal fun LazyListScope.moriviaLibraryContent(
    entries: List<MoriviaLibraryEntry>,
    section: MoriviaLibrarySection,
    sort: MoriviaLibrarySort,
    columns: Int,
    horizontalPadding: Dp,
    isWatched: (MoriviaLibraryEntry) -> Boolean,
    onSectionChange: (MoriviaLibrarySection) -> Unit,
    onSortChange: (MoriviaLibrarySort) -> Unit,
    onEntryClick: (MoriviaLibraryEntry) -> Unit,
    onEntryLongClick: (MoriviaLibraryEntry) -> Unit,
) {
    item(key = "morivia-library-chips") {
        LazyRow(
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(top = 8.dp, bottom = 18.dp),
        ) {
            MoriviaLibrarySection.entries.forEach { option ->
                item(key = option.name) {
                    MoriviaLibraryChip(
                        label = stringResource(option.labelRes()),
                        icon = option.icon(),
                        selected = option == section,
                        onClick = { onSectionChange(option) },
                    )
                }
            }
        }
    }
    item(key = "morivia-library-heading") {
        val movies = entries.count { !it.type.isSeriesLikeType() }
        val shows = entries.size - movies
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
                .padding(bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(section.labelRes()),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(Res.string.morivia_library_count, movies, shows),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            MoriviaLibrarySortPill(sort = sort, onSortChange = onSortChange)
        }
    }
    if (entries.isEmpty()) {
        item(key = "morivia-library-empty") {
            Text(
                text = stringResource(Res.string.morivia_library_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 24.dp),
            )
        }
        return
    }
    val rows = entries.chunked(columns)
    rows.forEachIndexed { rowIndex, row ->
        item(key = "morivia-library-row-$rowIndex-${row.first().key}") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                row.forEach { entry ->
                    MoriviaLibraryPoster(
                        entry = entry,
                        watched = isWatched(entry),
                        onClick = { onEntryClick(entry) },
                        onLongClick = { onEntryLongClick(entry) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(columns - row.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

private fun MoriviaLibrarySection.icon(): ImageVector = when (this) {
    MoriviaLibrarySection.RECENTLY_ADDED -> Icons.Rounded.Schedule
    MoriviaLibrarySection.MOVIES -> Icons.Rounded.Movie
    MoriviaLibrarySection.SHOWS -> Icons.Rounded.Tv
    MoriviaLibrarySection.CONTINUE_WATCHING -> Icons.Rounded.PlayCircle
}

/** A chip of the earlier app: white when chosen, dark with a theme-coloured icon otherwise. */
@Composable
private fun MoriviaLibraryChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = Modifier
            .height(44.dp)
            .clip(shape)
            .background(if (selected) Color.White else com.nuvio.app.core.ui.amoledCardColor(Color(0xFF1C1C1E)), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) Color.Black else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
            color = if (selected) Color.Black else Color.White,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

/** "Sort: Added ⌄" in the theme colour, with the choices in a menu. */
@Composable
private fun MoriviaLibrarySortPill(
    sort: MoriviaLibrarySort,
    onSortChange: (MoriviaLibrarySort) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        val shape = RoundedCornerShape(50)
        Row(
            modifier = Modifier
                .height(40.dp)
                .clip(shape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.28f), shape)
                .clickable { expanded = true }
                .padding(start = 14.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(Res.string.morivia_library_sort_label, stringResource(sort.labelRes())),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
            )
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            MoriviaLibrarySort.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(option.labelRes())) },
                    onClick = {
                        expanded = false
                        onSortChange(option)
                    },
                    trailingIcon = if (option == sort) {
                        { Icon(Icons.Rounded.Check, contentDescription = null) }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MoriviaLibraryPoster(
    entry: MoriviaLibraryEntry,
    watched: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.675f)
                .clip(RoundedCornerShape(10.dp))
                .background(com.nuvio.app.core.ui.amoledCardColor(Color(0xFF1C1C1E))),
        ) {
            if (!entry.poster.isNullOrBlank()) {
                AsyncImage(
                    model = entry.poster,
                    contentDescription = entry.name,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center).padding(8.dp),
                    maxLines = 3,
                )
            }
            if (watched) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .border(1.5.dp, Color.White.copy(alpha = 0.9f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
        Text(
            text = entry.name,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
