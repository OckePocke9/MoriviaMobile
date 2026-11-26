package com.morivia.app.features.profiles

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.morivia.app.core.ui.MoriviaBackButton
import com.morivia.app.core.ui.MoriviaToastHost
import com.morivia.app.core.ui.LocalVerticalBarPadding
import com.morivia.app.features.membership.CosmeticEntitlement
import com.morivia.app.features.settings.MemberBrandWordmark
import com.morivia.app.features.settings.noriviaLogoResource
import com.morivia.app.features.settings.brandSymbolResource
import com.morivia.app.features.settings.iconForTheme
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import morivia.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileSelectionScreen(
    onProfileSelected: (MoriviaProfile) -> Unit,
    onEditProfile: (MoriviaProfile) -> Unit,
    onAddProfile: () -> Unit,
    onBack: (() -> Unit)? = null,
    interactionEnabled: Boolean = true,
    activeProfileIndex: Int? = null,
    contentVisible: Boolean = true,
    modifier: Modifier = Modifier,
) {
    // Tablets and iPads show the phone's profile picker, enlarged evenly to fill the screen: the
    // portrait design standing up, the compact landscape design on their side (owner, 2026-11-24).
    PhoneScaledOnTablet(modifier = modifier) {
        ProfileSelectionScreenContent(
            onProfileSelected = onProfileSelected,
            onEditProfile = onEditProfile,
            onAddProfile = onAddProfile,
            onBack = onBack,
            interactionEnabled = interactionEnabled,
            activeProfileIndex = activeProfileIndex,
            contentVisible = contentVisible,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun ProfileSelectionScreenContent(
    onProfileSelected: (MoriviaProfile) -> Unit,
    onEditProfile: (MoriviaProfile) -> Unit,
    onAddProfile: () -> Unit,
    onBack: (() -> Unit)? = null,
    interactionEnabled: Boolean = true,
    activeProfileIndex: Int? = null,
    contentVisible: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val profileState by ProfileRepository.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var pinDialogProfile by remember { mutableStateOf<MoriviaProfile?>(null) }
    // A locked profile asks for its PIN before Edit Profile opens.
    var pinForEdit by remember { mutableStateOf(false) }
    val openEditor: (MoriviaProfile) -> Unit = { profile ->
        if (profile.pinEnabled) {
            pinForEdit = true
            pinDialogProfile = profile
        } else {
            onEditProfile(profile)
        }
    }
    var isEditMode by remember { mutableStateOf(false) }

    val titleAlpha = remember { Animatable(0f) }
    val titleOffset = remember { Animatable(20f) }
    val manageAlpha = remember { Animatable(0f) }
    val onProfileClick: (MoriviaProfile) -> Unit = { profile ->
        if (interactionEnabled) {
            routeProfileSelection(
                profile = profile,
                isEditMode = isEditMode,
                activeProfileIndex = activeProfileIndex,
                onEditProfile = openEditor,
                onActiveProfileSelected = { scope.launch { showAlreadyActiveProfileToast(it) } },
                onPinRequired = {
                    pinForEdit = false
                    pinDialogProfile = it
                },
                onProfileSelected = onProfileSelected,
            )
        }
    }

    LaunchedEffect(Unit) {
        AvatarRepository.refreshAvatars()
    }

    LaunchedEffect(Unit) {
        launch { titleAlpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing)) }
        launch { titleOffset.animateTo(0f, tween(600, easing = FastOutSlowInEasing)) }
        delay(300)
        manageAlpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
    }

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val backgroundProfile = profileState.activeProfile ?: profileState.profiles.firstOrNull()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize(),
    ) {
        val isTabletLayout = maxWidth >= 768.dp
        // A phone on its side is as wide as a tablet but low: tighter gaps so everything fits on one
        // screen, Manage Profiles included (owner, 2026-11-24).
        val isCompactLandscape = isTabletLayout && maxHeight < 700.dp
        ProfileBackgroundBackdrop(
            profile = backgroundProfile,
            modifier = Modifier.fillMaxSize(),
        )

        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(tween(260, easing = androidx.compose.animation.core.LinearOutSlowInEasing)),
            exit = fadeOut(tween(200, easing = androidx.compose.animation.core.FastOutLinearInEasing)),
            modifier = Modifier.fillMaxSize(),
        ) {
            // Nuvio's layout on a phone: the content starts at the top (60 dp below the status bar) and
            // every part sits at the same height as in Nuvio's picker; it scrolls with many profiles.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = statusBarTop)
                    // In landscape the content sits between the status bar and the gesture bar.
                    .then(
                        if (isCompactLandscape) {
                            Modifier.windowInsetsPadding(
                                androidx.compose.foundation.layout.WindowInsets.navigationBars
                                    .only(androidx.compose.foundation.layout.WindowInsetsSides.Bottom),
                            )
                        } else {
                            Modifier
                        },
                    )
                    .then(
                        if (isTabletLayout) {
                            Modifier
                        } else {
                            Modifier.verticalScroll(rememberScrollState())
                        },
                    )
                    .padding(horizontal = 24.dp),
                verticalArrangement = if (isTabletLayout) Arrangement.Center else Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(if (isTabletLayout) 0.dp else 60.dp))

                // The logo and name from the earlier Morivia app (a 34 x 28 symbol, 8 dp gap, "Morivia"
                // at 25 sp) in the 40 dp row where Nuvio shows its wordmark, then "Who's watching?"
                // Keep Morivia's branding, with the reference's compact gap before the heading.
                Row(
                    modifier = Modifier.height(40.dp).graphicsLayer {
                        alpha = titleAlpha.value
                        translationY = titleOffset.value
                    },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // The Morivia logo in the colour of the app icon chosen in Appearance (owner, 2026-11-23).
                    val appIconState by remember {
                        com.morivia.app.features.settings.AppIconRepository.ensureLoaded()
                        com.morivia.app.features.settings.AppIconRepository.state
                    }.collectAsStateWithLifecycle()
                    val previewOriginal by com.morivia.app.features.settings.ThemeSettingsRepository.pickerPreviewOriginal.collectAsStateWithLifecycle()
                    val logoTheme = if (previewOriginal) com.morivia.app.core.ui.AppTheme.ORIGINAL
                        else appIconState.selected.theme ?: com.morivia.app.core.ui.AppTheme.ORIGINAL
                    androidx.compose.animation.Crossfade(
                        targetState = logoTheme,
                        animationSpec = androidx.compose.animation.core.tween(600),
                        label = "profileLogo",
                    ) { theme ->
                        androidx.compose.foundation.Image(
                            painter = org.jetbrains.compose.resources.painterResource(theme.noriviaLogoResource()),
                            contentDescription = null,
                            modifier = Modifier.height(34.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                        )
                    }
                    Text(
                        text = "Morivia",
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 25.sp, letterSpacing = 0.25.sp, fontFamily = earlierMoriviaFont(800)),
                        color = Color.White,
                        fontWeight = FontWeight(800),
                    )
                }

                Spacer(modifier = Modifier.height(if (isCompactLandscape) 6.dp else if (isTabletLayout) 22.dp else 12.dp))

                Text(
                    text = stringResource(Res.string.profile_who_is_watching),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 30.sp,
                        lineHeight = 38.sp,
                        letterSpacing = (-0.3).sp,
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.graphicsLayer {
                        alpha = titleAlpha.value
                        translationY = titleOffset.value
                    },
                )

                // 6 dp less than below the profiles, because each profile tile has its own 8 dp top
                // padding: the gaps above and below the profiles now look the same (owner, 2026-11-23).
                Spacer(modifier = Modifier.height(if (isCompactLandscape) 12.dp else if (isTabletLayout) 22.dp else 42.dp))

                val profiles = profileState.profiles
                // Add Profile shows while Manage Profiles is open, or when there are no profiles yet.
                val showAddTile = (isEditMode || profiles.isEmpty()) && profiles.size < MAX_PROFILES
                val items = profiles.size + if (showAddTile) 1 else 0
                if (isTabletLayout) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                        ) {
                            for (currentIndex in 0 until items) {
                                if (currentIndex < profiles.size) {
                                    val profile = profiles[currentIndex]
                                    ProfileAvatarCard(
                                        profile = profile,
                                        isEditMode = isEditMode,
                                        isActive = profile.profileIndex == activeProfileIndex,
                                        animDelay = currentIndex * 80,
                                        enabled = interactionEnabled,
                                        onClick = {
                                            onProfileClick(profile)
                                        },
                                    )
                                } else {
                                    AddProfileCard(
                                        isEditMode = isEditMode,
                                        animDelay = currentIndex * 80,
                                        enabled = interactionEnabled,
                                        onClick = onAddProfile,
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        var index = 0
                        while (index < items) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                for (col in 0..1) {
                                    if (index < items) {
                                        val currentIndex = index
                                        if (currentIndex < profiles.size) {
                                            val profile = profiles[currentIndex]
                                            ProfileAvatarCard(
                                                profile = profile,
                                                isEditMode = isEditMode,
                                                isActive = profile.profileIndex == activeProfileIndex,
                                                animDelay = currentIndex * 80,
                                                enabled = interactionEnabled,
                                                onClick = {
                                                    onProfileClick(profile)
                                                },
                                            )
                                        } else {
                                            AddProfileCard(
                                                isEditMode = isEditMode,
                                                animDelay = currentIndex * 80,
                                                enabled = interactionEnabled,
                                                onClick = onAddProfile,
                                            )
                                        }
                                        index++
                                    } else {
                                        if (profiles.isNotEmpty()) {
                                            Spacer(modifier = Modifier.width(150.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(if (isCompactLandscape) 12.dp else if (isTabletLayout) 28.dp else 48.dp))

                // Colours and text of the earlier Morivia app's Manage Profiles / Done button. It opens
                // and closes managing (pencils and Add Profile); with no profiles only Add Profile shows.
                if (profiles.isNotEmpty()) Box(
                    modifier = Modifier
                        .graphicsLayer { alpha = manageAlpha.value }
                        .clip(RoundedCornerShape(24.dp))
                        // Always in the Done button's design (owner, 2026-11-23).
                        .background(Color(0xFF262729))
                        .border(
                            width = 1.dp,
                            color = Color(0xFF808080),
                            shape = RoundedCornerShape(24.dp),
                        )
                        .clickable(enabled = interactionEnabled) { isEditMode = !isEditMode }
                        .padding(horizontal = 24.dp, vertical = 9.dp),
                ) {
                    Text(
                        text = if (isEditMode) {
                            stringResource(Res.string.action_done)
                        } else {
                            stringResource(Res.string.profile_manage_profiles)
                        },
                        style = profileActionStyle(),
                        color = profileActionColor(true),
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Spacer(modifier = Modifier.height(if (isCompactLandscape) 12.dp else if (isTabletLayout) 0.dp else 32.dp))
            }
        }

        if (onBack != null && interactionEnabled && contentVisible) {
            // A see-through button with a thin light edge, so it shows against the glow.
            MoriviaBackButton(
                onClick = onBack,
                shape = CircleShape,
                containerColor = Color.Black.copy(alpha = 0.35f),
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(LocalVerticalBarPadding.current)
                    .padding(start = 16.dp, top = statusBarTop + 8.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.22f), CircleShape),
            )
        }

        MoriviaToastHost(modifier = Modifier.align(Alignment.TopCenter))
    }

    pinDialogProfile?.let { profile ->
        // The PIN lock in the Appearance theme colour (owner, 2026-11-23).
        com.morivia.app.features.settings.ThemedAccent {
        PinEntryDialog(
            profileName = profile.name,
            onVerify = { pin -> ProfileRepository.verifyPin(profile.profileIndex, pin) },
            onVerified = {
                pinDialogProfile = null
                if (pinForEdit) {
                    pinForEdit = false
                    if (interactionEnabled) onEditProfile(profile)
                } else if (interactionEnabled && profile.profileIndex != activeProfileIndex) {
                    onProfileSelected(profile)
                }
            },
            onDismiss = {
                pinDialogProfile = null
                pinForEdit = false
            },
        )
        }
    }
}

/** The earlier app's logo in the colours of the selected theme (the logo's details stay the same). */
@Composable
private fun rememberThemedBrandSymbol(): org.jetbrains.compose.resources.DrawableResource {
    val theme = com.morivia.app.core.ui.LocalAppTheme.current
    val custom by com.morivia.app.features.settings.ThemeSettingsRepository.customThemeColors.collectAsStateWithLifecycle()
    return remember(theme, custom) { iconForTheme(theme, custom).brandSymbolResource }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ProfileAvatarCard(
    profile: MoriviaProfile,
    isEditMode: Boolean,
    isActive: Boolean,
    animDelay: Int,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    // Initials take the Appearance theme colour (owner, 2026-11-23).
    val avatarColor = rememberThemeInitialsColour()
    val avatars by AvatarRepository.avatars.collectAsStateWithLifecycle()
    val avatarItem = remember(profile.avatarId, avatars) {
        profile.avatarId?.let { id -> avatars.find { it.id == id } }
    }
    val avatarImageUrl = remember(profile.avatarUrl, avatarItem) {
        profileAvatarImageUrl(profile, avatarItem)
    }

    val animAlpha = remember { Animatable(0f) }
    val animScale = remember { Animatable(0.85f) }
    val animOffset = remember { Animatable(30f) }

    LaunchedEffect(Unit) {
        delay(animDelay.toLong() + 150)
        launch { animAlpha.animateTo(1f, tween(450, easing = FastOutSlowInEasing)) }
        launch { animScale.animateTo(1f, tween(500, easing = FastOutSlowInEasing)) }
        launch { animOffset.animateTo(0f, tween(500, easing = FastOutSlowInEasing)) }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale = if (isPressed) 0.95f else 1f
    // A short grow when a profile is chosen, before the app opens.
    val tapScale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(150.dp)
            .graphicsLayer {
                alpha = animAlpha.value
                scaleX = animScale.value * pressScale * tapScale.value
                scaleY = animScale.value * pressScale * tapScale.value
                translationY = animOffset.value
            }
            .clip(RoundedCornerShape(20.dp))
            .then(if (isEditMode) Modifier else Modifier.clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
            ) {
                scope.launch {
                    tapScale.animateTo(1.1f, tween(140, easing = FastOutSlowInEasing))
                    onClick()
                    tapScale.animateTo(1f, tween(180, easing = FastOutSlowInEasing))
                }
            })
            .padding(8.dp),
    ) {
        Box(
            modifier = Modifier.size(104.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Nuvio's tinted initials circle and subtle outline; photo avatars retain their image.
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(if (avatarImageUrl == null) initialsBrush(avatarColor) else androidx.compose.ui.graphics.SolidColor(avatarItem?.bgColor?.let(::parseHexColor) ?: avatarColor))
                    .border(2.dp, avatarColor.copy(alpha = 0.45f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (avatarImageUrl != null) {
                    AsyncImage(
                        model = avatarImageUrl,
                        contentDescription = avatarItem?.displayName ?: profile.name,
                        modifier = Modifier.size(100.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                } else if (profile.name.isNotBlank()) {
                    // Like "Use initials": a white letter on the profile colour.
                    Text(
                        text = profile.name.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 40.sp),
                        color = initialsTextColor(avatarColor),
                        fontWeight = FontWeight(700),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(46.dp),
                    )
                }
            }

            // The same pencil on every profile, in the Appearance theme colour (owner, 2026-11-23).
            val pencilColour = rememberThemeInitialsColour()
            val pencilBrush = initialsBrush(pencilColour)
            val pencilTint = initialsTextColor(pencilColour)
            if (isEditMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(34.dp)
                        .clip(CircleShape)
                        // The pencil in the profile's initial colour, or the Appearance theme colour when the
                        // profile has a picture (owner, 2026-11-23).
                        .background(pencilBrush)
                        .clickable(enabled = enabled, onClick = onClick)
                        .border(2.dp, Color.Black, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = stringResource(Res.string.profile_manage_profiles) + " " + profile.name,
                        tint = pencilTint,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            // A padlock on profiles with a PIN, so it is clear before tapping that a code is needed.
            if (profile.pinEnabled && !isEditMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xE6101114))
                        .border(1.5.dp, Color.White.copy(alpha = 0.28f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = profile.name.ifBlank {
                stringResource(Res.string.profile_label_number, profile.profileIndex)
            },
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 22.sp),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AddProfileCard(
    isEditMode: Boolean,
    animDelay: Int,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val animAlpha = remember { Animatable(0f) }
    val animScale = remember { Animatable(0.85f) }
    val animOffset = remember { Animatable(30f) }

    LaunchedEffect(Unit) {
        delay(animDelay.toLong() + 150)
        launch { animAlpha.animateTo(1f, tween(450, easing = FastOutSlowInEasing)) }
        launch { animScale.animateTo(1f, tween(500, easing = FastOutSlowInEasing)) }
        launch { animOffset.animateTo(0f, tween(500, easing = FastOutSlowInEasing)) }
    }

    // Add Profile in the Done button's design (owner, 2026-11-23).
    val labelColor = profileActionColor(true)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale = if (isPressed) 0.95f else 1f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(150.dp)
            .graphicsLayer {
                alpha = animAlpha.value
                scaleX = animScale.value * pressScale
                scaleY = animScale.value * pressScale
                translationY = animOffset.value
            }
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(8.dp),
    ) {
        Box(Modifier.size(104.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier.size(100.dp).clip(CircleShape)
                    .background(Color(0xFF262729))
                    .border(2.dp, Color(0xFF808080), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Add, null, tint = labelColor, modifier = Modifier.size(36.dp))
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = stringResource(Res.string.compose_profile_add_profile),
            style = profileActionStyle(),
            color = labelColor,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}

/** The font of the earlier Morivia app (Plus Jakarta Sans, a variable font) at the given weight. */
@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
@Composable
private fun earlierMoriviaFont(weight: Int): androidx.compose.ui.text.font.FontFamily =
    androidx.compose.ui.text.font.FontFamily(
        org.jetbrains.compose.resources.Font(
            resource = Res.font.plus_jakarta_sans,
            weight = FontWeight(weight),
            variationSettings = androidx.compose.ui.text.font.FontVariation.Settings(
                androidx.compose.ui.text.font.FontVariation.weight(weight),
            ),
        ),
    )


private fun profileActionColor(editing: Boolean) = if (editing) Color.White else Color(0xFF9FA3AA)

@Composable
private fun profileActionStyle() = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 20.sp)

/** The profile's initial colour when it shows initials (no picture), otherwise null. */
internal fun MoriviaProfile.initialsAccentOrNull(): Color? =
    if (avatarId == null && normalizedAvatarUrl(avatarUrl) == null) parseHexColor(avatarColorHex) else null

/** The Appearance theme's colour, used for profile initials, pencils and the profile background. */
@Composable
internal fun rememberThemeInitialsColour(): Color {
    val theme by remember {
        com.morivia.app.features.settings.ThemeSettingsRepository.ensureLoaded()
        com.morivia.app.features.settings.ThemeSettingsRepository.selectedTheme
    }.collectAsStateWithLifecycle()
    val custom by com.morivia.app.features.settings.ThemeSettingsRepository.customThemeColors.collectAsStateWithLifecycle()
    val previewOriginal by com.morivia.app.features.settings.ThemeSettingsRepository.pickerPreviewOriginal.collectAsStateWithLifecycle()
    val target = remember(theme, custom, previewOriginal) {
        if (previewOriginal) com.morivia.app.core.ui.ThemeColors.Original.secondary
        else com.morivia.app.core.ui.ThemeColors.getColorPalette(theme, custom).secondary
    }
    val colour by androidx.compose.animation.animateColorAsState(target, androidx.compose.animation.core.tween(600), label = "themeInitials")
    return colour
}

/**
 * Shows [content] at phone size on tablets and iPads (shortest side 600 dp or more), enlarged
 * evenly so it fills the screen: like a phone standing up (411 x 891 dp) or on its side
 * (891 x 411 dp). Phones are left as they are (owner, 2026-11-24).
 */
@Composable
internal fun PhoneScaledOnTablet(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val tablet = minOf(maxWidth, maxHeight) >= 600.dp
        val scale = when {
            !tablet -> 1f
            maxWidth < maxHeight -> minOf(maxWidth.value / 411f, maxHeight.value / 891f)
            else -> minOf(maxWidth.value / 891f, maxHeight.value / 411f)
        }.coerceAtLeast(1f)
        val base = androidx.compose.ui.platform.LocalDensity.current
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(base.density * scale, base.fontScale),
        ) {
            content()
        }
    }
}
