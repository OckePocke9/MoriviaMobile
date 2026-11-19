package com.nuvio.app.features.profiles

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
internal fun AvatarPicker(
    avatars: List<AvatarCatalogItem>,
    selectedAvatarId: String?,
    onAvatarSelected: (AvatarCatalogItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (avatars.isEmpty()) return
    val spacing = 10.dp
    val minAvatarSize = 58.dp
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val columns = ((maxWidth + spacing) / (minAvatarSize + spacing)).toInt().coerceAtLeast(1)
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            val groups = avatars.groupBy { avatarGroup(it) }
            val order = listOf("Animals", "Linear", "Originals", "Portraits", "Sketches")
            (order + groups.keys).distinct().forEach { group ->
                val items = groups[group] ?: return@forEach
                Text(group, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                items.chunked(columns).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing),
                    ) {
                        row.forEach { avatar ->
                            AvatarChoiceItem(
                                avatar = avatar,
                                modifier = Modifier.weight(1f),
                                isSelected = avatar.id == selectedAvatarId,
                                onClick = { onAvatarSelected(avatar) },
                            )
                        }
                        repeat(columns - row.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

private val referenceAvatarNames = mapOf(
    "Animals" to "Bram Clover Pip Otto Milo Miso Elio Finn Poppy Bao",
    "Linear" to "Lin Max Ava Theo Zara Kai Nova",
    "Originals" to "Nova Bolt Marina Shadow Rook Riff Clue Cedar Ruby Sage",
    "Portraits" to "Quinn Iris Ari Hugo Skye Maya Drew Leo Zia Elle",
    "Sketches" to "Rowan Silas Nico Violet Moss",
).mapValues { it.value.lowercase().split(" ").toSet() }

private fun avatarGroup(avatar: AvatarCatalogItem): String {
    val source = (avatar.category + "/" + avatar.storagePath).lowercase()
    return referenceAvatarNames.keys.firstOrNull { it.lowercase().removeSuffix("s") in source }
        ?: referenceAvatarNames.entries.firstOrNull { avatar.displayName.lowercase() in it.value }?.key
        ?: avatar.category.replaceFirstChar { it.uppercase() }
}

/** One avatar with its name underneath. */
@Composable
private fun AvatarChoiceItem(
    avatar: AvatarCatalogItem,
    modifier: Modifier,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier.clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(CircleShape)
                .background(
                    avatar.bgColor?.let(::parseHexColor)
                        ?: MaterialTheme.colorScheme.surfaceVariant,
                )
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape,
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = avatarImageUrl(avatar),
                contentDescription = avatar.displayName,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            modifier = Modifier.clickable(onClick = onClick),
            text = avatar.displayName,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,

        )
    }
}
