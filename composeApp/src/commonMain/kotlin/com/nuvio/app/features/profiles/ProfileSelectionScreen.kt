package com.nuvio.app.features.profiles

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
import com.nuvio.app.core.ui.NuvioBackButton
import com.nuvio.app.core.ui.NuvioToastHost
import com.nuvio.app.core.ui.LocalVerticalBarPadding
import com.nuvio.app.features.membership.CosmeticEntitlement
import com.nuvio.app.features.settings.MemberBrandWordmark
import com.nuvio.app.features.settings.brandSymbolResource
import com.nuvio.app.features.settings.iconForTheme
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileSelectionScreen(
    onProfileSelected: (NuvioProfile) -> Unit,
    onEditProfile: (NuvioProfile) -> Unit,
    onAddProfile: () -> Unit,
    onBack: (() -> Unit)? = null,
    interactionEnabled: Boolean = true,
    activeProfileIndex: Int? = null,
    contentVisible: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val profileState by ProfileRepository.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var pinDialogProfile by remember { mutableStateOf<NuvioProfile?>(null) }
    // A locked profile asks for its PIN before Edit Profile opens.
    var pinForEdit by remember { mutableStateOf(false) }
    val openEditor: (NuvioProfile) -> Unit = { profile ->
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
    val onProfileClick: (NuvioProfile) -> Unit = { profile ->
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
        manageAlpha.animateTo(1f, tween(500))
    }

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val backgroundProfile = profileState.activeProfile ?: profileState.profiles.firstOrNull()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize(),
    ) {
        val isTabletLayout = maxWidth >= 768.dp
        ProfileBackgroundBackdrop(
            profile = backgroundProfile,
            modifier = Modifier.fillMaxSize(),
        )

        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(180)),
            modifier = Modifier.fillMaxSize(),
        ) {
            // The content sits in the middle of the screen (it scrolls when there are many profiles).
            val viewportHeight = this@BoxWithConstraints.maxHeight - statusBarTop
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = statusBarTop)
                    .then(
                        if (isTabletLayout) {
                            Modifier
                        } else {
                            Modifier
                                .verticalScroll(rememberScrollState())
                                .heightIn(min = viewportHeight)
                        },
                    )
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(if (isTabletLayout) 0.dp else 24.dp))

                // The logo and name from the earlier Morivia app (1.4.130), at its exact size: a 34 x 28
                // symbol, 8 dp gap, "Morivia" at 25 sp, then "Who's watching?" 20 dp below at 30 sp.
                Row(
                    modifier = Modifier.height(34.dp).graphicsLayer {
                        alpha = titleAlpha.value
                        translationY = titleOffset.value
                    },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    androidx.compose.foundation.Image(
                        painter = org.jetbrains.compose.resources.painterResource(rememberThemedBrandSymbol()),
                        contentDescription = null,
                        modifier = Modifier.width(34.dp).height(28.dp),
                    )
                    Text(
                        text = "Morivia",
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 25.sp, letterSpacing = 0.25.sp, fontFamily = earlierMoriviaFont(800)),
                        color = Color.White,
                        fontWeight = FontWeight(800),
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(Res.string.profile_who_is_watching),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 30.sp,
                        lineHeight = 38.sp,
                        letterSpacing = (-0.3).sp,
                        fontFamily = earlierMoriviaFont(800),
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.graphicsLayer {
                        alpha = titleAlpha.value
                        translationY = titleOffset.value
                    },
                )

                Spacer(modifier = Modifier.height(if (isTabletLayout) 28.dp else 48.dp))

                val profiles = profileState.profiles
                // Add Profile shows while Manage Profiles is open, or when there are no profiles yet.
                val showAddTile = (isEditMode || profiles.isEmpty()) && profiles.size < MAX_PROFILES
                val items = profiles.size + if (showAddTile) 1 else 0
                val onProfileLongClick: (NuvioProfile) -> Unit = { profile ->
                    if (interactionEnabled) openEditor(profile)
                }

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
                                        onLongClick = { onProfileLongClick(profile) },
                                        animDelay = currentIndex * 80,
                                        enabled = interactionEnabled,
                                        onClick = {
                                            onProfileClick(profile)
                                        },
                                    )
                                } else {
                                    AddProfileCard(
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
                                                onLongClick = { onProfileLongClick(profile) },
                                                animDelay = currentIndex * 80,
                                                enabled = interactionEnabled,
                                                onClick = {
                                                    onProfileClick(profile)
                                                },
                                            )
                                        } else {
                                            AddProfileCard(
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

                Spacer(modifier = Modifier.height(if (isTabletLayout) 28.dp else 48.dp))

                // Colours and text of the earlier Morivia app's Manage Profiles / Done button. It opens
                // and closes managing (pencils and Add Profile); with no profiles only Add Profile shows.
                if (profiles.isNotEmpty()) Box(
                    modifier = Modifier
                        .graphicsLayer { alpha = manageAlpha.value }
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isEditMode) Color(0xFF262729) else Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = if (isEditMode) Color(0xFF4A4B4F) else Color(0xFF34373E),
                            shape = RoundedCornerShape(24.dp),
                        )
                        .clickable(enabled = interactionEnabled) { isEditMode = !isEditMode }
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                ) {
                    Text(
                        text = if (isEditMode) {
                            stringResource(Res.string.action_done)
                        } else {
                            stringResource(Res.string.profile_manage_profiles)
                        },
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = if (isEditMode) 16.sp else 15.5.sp, fontFamily = earlierMoriviaFont(600)),
                        color = if (isEditMode) Color.White else Color(0xFF9FA3AA),
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Spacer(modifier = Modifier.height(if (isTabletLayout) 0.dp else 32.dp))
            }
        }

        if (onBack != null && interactionEnabled && contentVisible) {
            // A see-through button with a thin light edge, so it shows against the glow.
            NuvioBackButton(
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

        NuvioToastHost(modifier = Modifier.align(Alignment.TopCenter))
    }

    pinDialogProfile?.let { profile ->
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

/** The earlier app's logo in the colours of the selected theme (the logo's details stay the same). */
@Composable
private fun rememberThemedBrandSymbol(): org.jetbrains.compose.resources.DrawableResource {
    val theme = com.nuvio.app.core.ui.LocalAppTheme.current
    val custom by com.nuvio.app.features.settings.ThemeSettingsRepository.customThemeColors.collectAsStateWithLifecycle()
    return remember(theme, custom) { iconForTheme(theme, custom).brandSymbolResource }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ProfileAvatarCard(
    profile: NuvioProfile,
    isEditMode: Boolean,
    isActive: Boolean,
    animDelay: Int,
    enabled: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val avatarColor = remember(profile.avatarColorHex) {
        parseHexColor(profile.avatarColorHex)
    }
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
            .combinedClickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                },
                onClick = {
                    if (isEditMode) {
                        onClick()
                    } else {
                        scope.launch {
                            tapScale.animateTo(1.1f, tween(140, easing = FastOutSlowInEasing))
                            onClick()
                            tapScale.animateTo(1f, tween(180, easing = FastOutSlowInEasing))
                        }
                    }
                },
            )
            .padding(8.dp),
    ) {
        Box(
            modifier = Modifier.size(110.dp),
            contentAlignment = Alignment.Center,
        ) {
            // No ring around the profile picture.
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .clip(CircleShape)
                    .background(avatarItem?.bgColor?.let { parseHexColor(it) } ?: avatarColor),
                contentAlignment = Alignment.Center,
            ) {
                if (avatarImageUrl != null) {
                    AsyncImage(
                        model = avatarImageUrl,
                        contentDescription = avatarItem?.displayName ?: profile.name,
                        modifier = Modifier.size(104.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                } else if (profile.name.isNotBlank()) {
                    // Like "Use initials": a white letter on the profile colour.
                    Text(
                        text = profile.name.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 40.sp, fontFamily = earlierMoriviaFont(700)),
                        color = Color.White,
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

            if (isEditMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .border(2.dp, Color.Black, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
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

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = profile.name.ifBlank {
                stringResource(Res.string.profile_label_number, profile.profileIndex)
            },
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp, lineHeight = 22.sp, fontFamily = earlierMoriviaFont(600)),
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
        Box(
            modifier = Modifier.size(110.dp),
            contentAlignment = Alignment.Center,
        ) {
            // The earlier app's near-black add tile, without a ring like the profiles.
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF171717)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(40.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(Res.string.compose_profile_add_profile),
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp, lineHeight = 22.sp, fontFamily = earlierMoriviaFont(600)),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
