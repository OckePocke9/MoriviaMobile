package com.nuvio.app.features.auth

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuvio.app.core.auth.DeviceLinkAuthFailure
import com.nuvio.app.core.auth.DeviceLinkAuthState
import com.nuvio.app.core.ui.NuvioLoadingIndicator
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.compose_auth_link_cancel
import nuvio.composeapp.generated.resources.compose_auth_link_code_expired
import nuvio.composeapp.generated.resources.compose_auth_link_creating_code
import nuvio.composeapp.generated.resources.compose_auth_link_failed
import nuvio.composeapp.generated.resources.compose_auth_link_open
import nuvio.composeapp.generated.resources.compose_auth_link_open_failed
import nuvio.composeapp.generated.resources.compose_auth_link_sign_in
import nuvio.composeapp.generated.resources.compose_auth_link_signing_in
import nuvio.composeapp.generated.resources.compose_auth_link_try_again
import nuvio.composeapp.generated.resources.compose_auth_link_waiting
import org.jetbrains.compose.resources.stringResource

/**
 * Nuvio link sign-in as a QR code, under the Nuvio text on tablets and iPad (owner, 2026-11-24).
 * It opens when "Sign In with Link" is tapped; scanning the code with a phone opens nuvio.tv/link with
 * the code filled in, and this device signs in once it is approved there.
 */
@Composable
internal fun AuthQrPanel(
    state: DeviceLinkAuthState,
    scale: Float,
    onStart: () -> Unit,
    onCancel: () -> Unit = {},
    qrSize: androidx.compose.ui.unit.Dp = 148.dp * scale,
) {
    // Closed until "Sign In with Link" is tapped, as in the phone app (owner, 2026-11-24).
    // Always shown on iPad and tablets (owner, 2026-11-24): a code is made as soon as the screen
    // shows, and a new one when it expires.
    LaunchedEffect(state) {
        val expired = state is DeviceLinkAuthState.Failed && state.reason == DeviceLinkAuthFailure.Expired
        if (state is DeviceLinkAuthState.Idle || expired) onStart()
    }
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp * scale),
    ) {
        Box(
            modifier = Modifier
                .size(qrSize)
                .clip(RoundedCornerShape(18.dp * scale))
                .background(androidx.compose.ui.graphics.Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Crossfade(targetState = (state as? DeviceLinkAuthState.Waiting)?.verificationUrl, animationSpec = tween(300), label = "authQr") { url ->
                if (url != null) {
                    QrCodeImage(text = url, modifier = Modifier.size(qrSize))
                } else if (state !is DeviceLinkAuthState.Failed) {
                    NuvioLoadingIndicator(modifier = Modifier.size(28.dp * scale), color = androidx.compose.ui.graphics.Color(0xFF111111))
                }
            }
        }
        Column(
            modifier = Modifier.widthIn(max = 260.dp * scale),
            verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        ) {
            Text(
                text = stringResource(Res.string.compose_auth_link_sign_in),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = AuthTextPrimary,
                    fontSize = (18f * scale).sp,
                    lineHeight = (23f * scale).sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            when (state) {
                is DeviceLinkAuthState.Waiting -> {
                    Text(
                        text = state.code,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = AuthTextPrimary,
                            fontSize = (22f * scale).sp,
                            lineHeight = (26f * scale).sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (2f * scale).sp,
                        ),
                    )
                    Text(
                        text = stringResource(
                            if (state.isCompleting) Res.string.compose_auth_link_signing_in
                            else Res.string.compose_auth_link_waiting,
                        ),
                        style = qrCaptionStyle(scale),
                    )
                }
                is DeviceLinkAuthState.Failed -> {
                    Text(
                        text = stringResource(
                            if (state.reason == DeviceLinkAuthFailure.Expired) Res.string.compose_auth_link_code_expired
                            else Res.string.compose_auth_link_failed,
                        ),
                        style = qrCaptionStyle(scale),
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .border(1.dp, AuthFieldBorder, RoundedCornerShape(50))
                            .background(AuthFieldBackground)
                            .clickable(onClick = onStart)
                            .padding(horizontal = 18.dp * scale, vertical = 9.dp * scale),
                    ) {
                        Text(
                            text = stringResource(Res.string.compose_auth_link_try_again),
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = AuthTextPrimary,
                                fontSize = (14f * scale).sp,
                                fontWeight = FontWeight.SemiBold,
                            ),
                        )
                    }
                }
                else -> Text(
                    text = stringResource(Res.string.compose_auth_link_creating_code),
                    style = qrCaptionStyle(scale),
                )
            }
        }
    }
    }
}

@Composable
private fun qrCaptionStyle(scale: Float) = MaterialTheme.typography.bodyMedium.copy(
    color = AuthTextSecondary,
    fontSize = (14f * scale).sp,
    lineHeight = (19f * scale).sp,
)
