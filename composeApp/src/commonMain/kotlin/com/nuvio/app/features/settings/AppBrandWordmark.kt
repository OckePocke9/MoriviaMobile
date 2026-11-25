package com.nuvio.app.features.settings

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import com.nuvio.app.core.ui.appTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ResourceEnvironment
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.getDrawableResourceBytes
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.rememberResourceEnvironment

@Composable
internal fun AppBrandWordmark(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    icon: AppIconOption? = null,
) {
    val resource = rememberAppBrandWordmarkResource(icon)
    val environment = rememberResourceEnvironment()
    val bitmap by produceState(AppBrandWordmarkCache[resource], resource, environment) {
        if (value == null) value = AppBrandWordmarkCache.load(resource, environment)
    }
    val loaded = bitmap
    val painter = if (loaded != null) {
        remember(loaded) { BitmapPainter(loaded) }
    } else {
        painterResource(resource)
    }
    // The logo part of the wordmark picture, then the name written as "Morivia" (owner, 2026-11-23).
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = modifier) {
        val height = if (maxHeight.value.isFinite()) maxHeight else 40.dp
        // The new Morivia logo in the theme colour (owner, 2026-11-23).
        val logoTheme by ThemeSettingsRepository.selectedTheme.collectAsStateWithLifecycle()
        MoriviaNameRow(height = height, fontRatio = 0.43f, gapRatio = 0.21f) {
            Image(
                painter = painterResource(logoTheme.noriviaLogoResource()),
                contentDescription = contentDescription,
                modifier = Modifier.height(height),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
internal fun PreloadAppBrandWordmark() {
    val resource = rememberAppBrandWordmarkResource(icon = null)
    val environment = rememberResourceEnvironment()
    LaunchedEffect(resource, environment) {
        AppBrandWordmarkCache.load(resource, environment)
    }
}

@Composable
private fun rememberAppBrandWordmarkResource(icon: AppIconOption?): DrawableResource {
    val state by remember {
        AppIconRepository.ensureLoaded()
        AppIconRepository.state
    }.collectAsStateWithLifecycle()
    return icon?.wordmarkResource ?: MaterialTheme.appTheme.wordmarkResource(state.selected)
}

private object AppBrandWordmarkCache {
    private val bitmaps = MutableStateFlow<Map<DrawableResource, ImageBitmap>>(emptyMap())

    operator fun get(resource: DrawableResource): ImageBitmap? = bitmaps.value[resource]

    suspend fun load(resource: DrawableResource, environment: ResourceEnvironment): ImageBitmap {
        get(resource)?.let { return it }
        val bitmap = withContext(Dispatchers.Default) {
            getDrawableResourceBytes(environment, resource).decodeToImageBitmap()
        }
        bitmaps.update { cached -> cached + (resource to bitmap) }
        return bitmap
    }
}
