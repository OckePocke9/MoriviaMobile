package com.nuvio.app.features.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nuvio.app.core.build.AppVersionConfig
import com.nuvio.app.core.ui.AppTheme
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** The app's name (owner, 2026-11-23). */
internal const val NoriviaName = "Norivia"

/** The Norivia logo in the colours of the selected theme (Original for themes without one). */
internal fun AppTheme.noriviaLogoResource(): DrawableResource = when (this) {
    AppTheme.CHERRY -> Res.drawable.norivia_logo_cherry
    AppTheme.CORAL -> Res.drawable.norivia_logo_coral
    AppTheme.MANGO -> Res.drawable.norivia_logo_mango
    AppTheme.AMBER -> Res.drawable.norivia_logo_amber
    AppTheme.LEMON -> Res.drawable.norivia_logo_lemon
    AppTheme.LIME -> Res.drawable.norivia_logo_lime
    AppTheme.EMERALD -> Res.drawable.norivia_logo_emerald
    AppTheme.MINT -> Res.drawable.norivia_logo_mint
    AppTheme.TEAL -> Res.drawable.norivia_logo_teal
    AppTheme.CERULEAN -> Res.drawable.norivia_logo_cerulean
    AppTheme.AZURE -> Res.drawable.norivia_logo_azure
    AppTheme.COBALT -> Res.drawable.norivia_logo_cobalt
    AppTheme.INDIGO -> Res.drawable.norivia_logo_indigo
    AppTheme.GRAPE -> Res.drawable.norivia_logo_grape
    AppTheme.ORCHID -> Res.drawable.norivia_logo_orchid
    AppTheme.BERRY -> Res.drawable.norivia_logo_berry
    AppTheme.COPPER -> Res.drawable.norivia_logo_copper
    AppTheme.WHITE -> Res.drawable.norivia_logo_white
    else -> Res.drawable.norivia_logo_original
}

/**
 * Logo and "Norivia" in the profile picker's lettering: a 28 dp logo, 8 dp gap and the name in
 * white Plus Jakarta Sans ExtraBold at 25 sp, all scaled to [height].
 */
@Composable
internal fun NoriviaWordmark(height: Dp, modifier: Modifier = Modifier) {
    val theme by remember {
        ThemeSettingsRepository.ensureLoaded()
        ThemeSettingsRepository.selectedTheme
    }.collectAsStateWithLifecycle()
    NoriviaNameRow(height = height, modifier = modifier) {
        Image(
            painter = painterResource(theme.noriviaLogoResource()),
            contentDescription = null,
            modifier = Modifier.height(height),
            contentScale = ContentScale.Fit,
        )
    }
}

/** A logo followed by "Norivia" in the profile picker's lettering, scaled to the logo's [height]. */
@Composable
internal fun NoriviaNameRow(
    height: Dp,
    modifier: Modifier = Modifier,
    fontRatio: Float = 25f / 28f,
    gapRatio: Float = 8f / 28f,
    logo: @Composable () -> Unit,
) {
    val scale = height.value / 28f
    Row(
        modifier = modifier.height(height),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        logo()
        Spacer(Modifier.width(height * gapRatio))
        Text(
            text = NoriviaName,
            style = TextStyle(
                color = Color.White,
                fontSize = (height.value * fontRatio).sp,
                letterSpacing = (0.25f * scale).sp,
                fontWeight = FontWeight(800),
                fontFamily = noriviaWordmarkFont(),
            ),
            maxLines = 1,
        )
    }
}

/** Norivia's logo, name and version, at the bottom of the System page. */
internal fun LazyListScope.noriviaBrandFooter(isTablet: Boolean) {
    item(key = "norivia_brand_footer") {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = if (isTablet) 24.dp else 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (isTablet) 10.dp else 8.dp),
        ) {
            NoriviaWordmark(height = if (isTablet) 32.dp else 28.dp)
            Text(
                text = stringResource(
                    Res.string.compose_about_version_format,
                    AppVersionConfig.VERSION_NAME,
                    AppVersionConfig.VERSION_CODE,
                ),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
@Composable
private fun noriviaWordmarkFont(): androidx.compose.ui.text.font.FontFamily =
    androidx.compose.ui.text.font.FontFamily(
        org.jetbrains.compose.resources.Font(
            resource = Res.font.plus_jakarta_sans,
            weight = FontWeight(800),
            variationSettings = androidx.compose.ui.text.font.FontVariation.Settings(
                androidx.compose.ui.text.font.FontVariation.weight(800),
            ),
        ),
    )
