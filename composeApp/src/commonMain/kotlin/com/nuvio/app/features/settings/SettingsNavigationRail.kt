package com.nuvio.app.features.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nuvio.app.AppScreenTab
import com.nuvio.app.core.ui.nuvio
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.compose_nav_home
import nuvio.composeapp.generated.resources.compose_nav_library
import nuvio.composeapp.generated.resources.compose_nav_profile
import nuvio.composeapp.generated.resources.compose_nav_search
import nuvio.composeapp.generated.resources.sidebar_library
import nuvio.composeapp.generated.resources.sidebar_search
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Space on each side of the landscape navigation bar, so it sits as far from the edge as from the list. */
internal val SettingsNavigationRailSideSpace = 16.dp

/**
 * Left navigation bar for Settings in landscape (owner's request, 2026-11-22): the bottom bar's
 * Home, Search, Library and Profile, standing upright, with the same space on both sides before
 * the Settings list starts. Profile is selected while Settings is open.
 */
@Composable
internal fun SettingsNavigationRail(
    onTabSelected: (AppScreenTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = MaterialTheme.nuvio
    Surface(
        modifier = modifier.padding(horizontal = SettingsNavigationRailSideSpace),
        shape = RoundedCornerShape(36.dp),
        color = Color(0xFF121113),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RailItem(
                label = stringResource(Res.string.compose_nav_home),
                painter = rememberVectorPainter(Icons.Filled.Home),
                selected = false,
                onClick = { onTabSelected(AppScreenTab.Home) },
            )
            RailItem(
                label = stringResource(Res.string.compose_nav_search),
                painter = painterResource(Res.drawable.sidebar_search),
                selected = false,
                onClick = { onTabSelected(AppScreenTab.Search) },
            )
            RailItem(
                label = stringResource(Res.string.compose_nav_library),
                painter = painterResource(Res.drawable.sidebar_library),
                selected = false,
                onClick = { onTabSelected(AppScreenTab.Library) },
            )
            RailItem(
                label = stringResource(Res.string.compose_nav_profile),
                painter = rememberVectorPainter(Icons.Rounded.AccountCircle),
                selected = true,
                onClick = { onTabSelected(AppScreenTab.Settings) },
            )
        }
    }
}

@Composable
private fun RailItem(
    label: String,
    painter: Painter,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val tokens = MaterialTheme.nuvio
    val tint = if (selected) tokens.colors.accent else Color.White.copy(alpha = 0.72f)
    Column(
        modifier = Modifier
            .width(76.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(if (selected) Color.White.copy(alpha = 0.08f) else Color.Transparent)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painter = painter, contentDescription = null, tint = tint, modifier = Modifier.size(28.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = tint,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
        )
    }
}
