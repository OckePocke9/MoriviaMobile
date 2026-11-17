package com.nuvio.app.features.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nuvio.app.core.network.NetworkStatusRepository
import com.nuvio.app.core.ui.nuvio
import com.nuvio.app.core.ui.accentBrush
import com.nuvio.app.core.ui.themePalette
import com.nuvio.app.features.addons.AddonRepository
import com.nuvio.app.features.addons.enabledAddons
import com.nuvio.app.features.servers.ServerRepository
import com.nuvio.app.features.settings.AppIconRepository
import com.nuvio.app.features.settings.symbolResource
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.home_empty_no_sources_message
import nuvio.composeapp.generated.resources.home_empty_no_sources_title
import nuvio.composeapp.generated.resources.morivia_add_addon
import nuvio.composeapp.generated.resources.morivia_refresh
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Morivia: Continue Watching, Search and Library show content only when there is a catalog source:
 * an enabled addon with catalogs (for example Cinemeta) or a media server library.
 */
internal enum class CatalogSourceState {
    /** Just opened and no source seen yet; the screens stay blank instead of flashing a state. */
    Starting,
    Loading,
    None,
    Available,
}

/**
 * The addon list is briefly empty at start-up, after a profile switch and while it is pulled from
 * Nuvio. "None" is therefore shown only after it has lasted [NoSourcesDelayMillis], so people with
 * Cinemeta never see "No content sources" flash by. Other states apply at once.
 */
@Composable
internal fun rememberCatalogSourceState(): CatalogSourceState {
    val current = rememberCurrentCatalogSourceState()
    var shown by remember { mutableStateOf(if (current == CatalogSourceState.None) CatalogSourceState.Starting else current) }
    LaunchedEffect(current) {
        if (current == CatalogSourceState.None) {
            delay(NoSourcesDelayMillis)
        }
        shown = current
    }
    return shown
}

internal const val NoSourcesDelayMillis = 1_500L

@Composable
private fun rememberCurrentCatalogSourceState(): CatalogSourceState {
    LaunchedEffect(Unit) { AddonRepository.initialize() }
    val addonState by AddonRepository.uiState.collectAsStateWithLifecycle()
    val serverState by remember {
        ServerRepository.ensureLoaded()
        ServerRepository.uiState
    }.collectAsStateWithLifecycle()
    val addons = addonState.addons.enabledAddons()
    val hasCatalogAddon = addons.any { addon -> addon.manifest?.catalogs?.isNotEmpty() == true }
    val hasServerLibrary = serverState.enabledConnections.any { it.selectedLibraries.isNotEmpty() }
    return when {
        hasCatalogAddon || hasServerLibrary -> CatalogSourceState.Available
        // An addon that is still loading or failed to load may have catalogs: keep the normal screen,
        // which shows loading, offline and retry states.
        addons.any { it.manifest == null } -> CatalogSourceState.Loading
        else -> CatalogSourceState.None
    }
}

/** Reloads addons and the network state, used by the refresh button. */
internal fun refreshCatalogSources() {
    NetworkStatusRepository.requestRefresh(force = true)
    AddonRepository.refreshAll()
}

/**
 * The Home top bar without catalogs: the Morivia icon and name on the left and a refresh button on
 * the right, both as far from the screen edge as the status bar clock.
 */
@Composable
internal fun MoriviaHomeTopBar(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Directly under the clock. The logo starts where the clock starts, and the refresh icon ends
    // where the battery ends (the 48 dp IconButton keeps its icon 12 dp inside its right edge).
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(start = StatusBarEdgePadding, end = StatusBarEdgePadding - 12.dp)
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // The Morivia wordmark (logo with the name on its right), as Nuvio shows its own.
        com.nuvio.app.features.settings.MemberBrandWordmark(height = 26.dp)
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onRefresh) {
            Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = stringResource(Res.string.morivia_refresh),
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

/** Matches the inset of the status bar clock (left) and battery (right) on Android phones. */
internal val StatusBarEdgePadding = 12.dp

/**
 * "No content sources" from the earlier Morivia app: an icon, the title, the message and a round
 * Add Addon button in the theme colour.
 */
@Composable
internal fun NoCatalogSourcesContent(
    onAddAddon: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = MaterialTheme.nuvio
    Column(
        modifier = modifier.padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
    ) {
        // A clapperboard in the theme colour on a soft theme-coloured circle.
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(tokens.colors.accent.copy(alpha = 0.28f), tokens.colors.accent.copy(alpha = 0.06f)),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Movie,
                contentDescription = null,
                tint = tokens.colors.accent,
                modifier = Modifier.size(48.dp),
            )
        }
        // Nuvio's text styles (as in its "No content sources" card).
        Text(
            text = stringResource(Res.string.home_empty_no_sources_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.home_empty_no_sources_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onAddAddon,
            shape = CircleShape,
            modifier = Modifier.background(MaterialTheme.themePalette.accentBrush(), CircleShape),
            colors = ButtonDefaults.buttonColors(
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                contentColor = tokens.colors.onAccent,
            ),
            contentPadding = PaddingValues(horizontal = 30.dp, vertical = 14.dp),
        ) {
            // Same accent as the selected tab in the navigation bar: Original by default, then the
            // theme the user picks.
            Text(
                text = stringResource(Res.string.morivia_add_addon),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

/** A whole screen (Search, Library) with nothing but the "No content sources" message. */
@Composable
internal fun NoCatalogSourcesScreen(
    onAddAddon: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(WindowInsets.statusBars.asPaddingValues()),
        contentAlignment = Alignment.Center,
    ) {
        NoCatalogSourcesContent(onAddAddon = onAddAddon)
    }
}
