package com.morivia.app.features.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import com.morivia.app.features.simkl.SimklBrandAsset
import com.morivia.app.features.simkl.simklBrandPainter
import morivia.composeapp.generated.resources.Res
import morivia.composeapp.generated.resources.introdb_favicon
import morivia.composeapp.generated.resources.mdblist_logo
import morivia.composeapp.generated.resources.rating_tmdb
import morivia.composeapp.generated.resources.trakt_tv_favicon
import org.jetbrains.compose.resources.painterResource

@Composable
internal actual fun integrationLogoPainter(logo: IntegrationLogo): Painter =
    when (logo) {
        IntegrationLogo.Tmdb -> painterResource(Res.drawable.rating_tmdb)
        IntegrationLogo.Trakt -> painterResource(Res.drawable.trakt_tv_favicon)
        IntegrationLogo.Simkl -> simklBrandPainter(SimklBrandAsset.Glyph)
        IntegrationLogo.MdbList -> painterResource(Res.drawable.mdblist_logo)
        IntegrationLogo.IntroDb -> painterResource(Res.drawable.introdb_favicon)
    }
