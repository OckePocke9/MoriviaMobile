package com.nuvio.app.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The page pill of the earlier Morivia app ("Search", "Library"): a small dark pill at the top
 * left with the page icon in a circle of the theme colour.
 */
@Composable
fun MoriviaPagePill(
    label: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onClick: (() -> Unit)? = null,
    // The icon after the text, for a pill in the right corner such as Show All (owner, 2026-11-25).
    iconAtEnd: Boolean = false,
) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .then(if (compact) Modifier.defaultMinSize(minHeight = 30.dp) else Modifier.height(44.dp))
            .clip(shape)
            .background(Color.Black.copy(alpha = 0.35f), shape)
            .border(1.dp, Color.White.copy(alpha = 0.08f), shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(
                start = when {
                    icon == null || iconAtEnd -> if (compact) 10.dp else 16.dp
                    compact -> 5.dp
                    else -> 7.dp
                },
                end = when {
                    icon != null && iconAtEnd -> if (compact) 5.dp else 7.dp
                    else -> if (compact) 10.dp else 16.dp
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 10.dp),
    ) {
        val iconCircle: @Composable () -> Unit = {
            if (icon != null) {
            Box(
                modifier = Modifier
                    .size(if (compact) 20.dp else 30.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(if (compact) 12.dp else 17.dp),
                )
            }
        }
    }
        if (!iconAtEnd) iconCircle()
        Text(
            text = label,
            style = if (compact) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
        if (iconAtEnd) iconCircle()
    }
}
