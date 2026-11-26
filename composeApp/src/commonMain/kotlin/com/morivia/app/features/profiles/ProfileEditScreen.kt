package com.morivia.app.features.profiles

import com.morivia.app.features.settings.collapsedPresets
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.graphics.toArgb
import androidx.compose.material.icons.rounded.Wallpaper
import com.morivia.app.features.settings.CustomThemeEditor
import com.morivia.app.features.settings.PresetExpansionArrow
import com.morivia.app.features.settings.ThemeActions
import com.morivia.app.core.ui.APPEARANCE_PRESETS
import com.morivia.app.core.ui.labelRes
import com.morivia.app.core.ui.ThemeColors
import com.morivia.app.core.ui.CustomThemeColors
import com.morivia.app.core.ui.formatHexColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.morivia.app.core.ui.MoriviaInputField
import com.morivia.app.core.ui.MoriviaPrimaryButton
import com.morivia.app.core.ui.MoriviaScreen
import com.morivia.app.core.ui.MoriviaScreenHeader
import com.morivia.app.core.ui.MoriviaStatusModal
import com.morivia.app.core.ui.MoriviaSurfaceCard
import com.morivia.app.core.ui.MoriviaTheme
import com.morivia.app.features.settings.ThemeSettingsRepository
import com.morivia.app.core.ui.themePalette
import com.morivia.app.core.ui.nuvio
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.morivia.app.features.membership.CosmeticEntitlement
import com.morivia.app.features.membership.MemberAccessRepository
import com.morivia.app.features.membership.ProfileBackgroundRepository
import kotlinx.coroutines.launch
import morivia.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Edit Profile and Add Profile draw the theme background behind cards that follow AMOLED Black:
 * black cards with it on, the original card colour with it off.
 */
@Composable
fun ProfileEditScreen(
    profile: MoriviaProfile? = null,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val appTheme by ThemeSettingsRepository.selectedTheme.collectAsStateWithLifecycle()
    val customThemeColors by ThemeSettingsRepository.customThemeColors.collectAsStateWithLifecycle()
    val amoled by ThemeSettingsRepository.amoledEnabled.collectAsStateWithLifecycle()
    // Like Settings (owner, 2026-11-23): grey cards on a black background (pure black with AMOLED
    // Black), and the theme colour on switches, rings, PIN and buttons.
    com.morivia.app.features.settings.SettingsBackground(modifier = Modifier.fillMaxSize()) {
        com.morivia.app.features.settings.ThemedAccent {
            // In landscape (and on wide screens) the page is a centred column instead of stretching
            // across the screen (owner, 2026-11-24).
            androidx.compose.foundation.layout.BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter,
            ) {
                val wide = maxWidth > 720.dp
                ProfileEditContent(
                    profile = profile,
                    onBack = onBack,
                    onSaved = onSaved,
                    modifier = if (wide) modifier.widthIn(max = 640.dp) else modifier,
                )
            }
        }
    }
}

@Composable
private fun ProfileEditContent(
    profile: MoriviaProfile?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier,
) {
    val isNew = profile == null
    val scope = rememberCoroutineScope()
    val profileState by ProfileRepository.state.collectAsStateWithLifecycle()
    val currentProfile = remember(profile?.profileIndex, profileState.profiles, profile) {
        profile?.let { snapshot ->
            profileState.profiles.find { it.profileIndex == snapshot.profileIndex } ?: snapshot
        }
    }
    // The fallback colour behind the initials, as on nuvio.tv ("Use initials").
    // The initials colour comes from the Appearance theme (owner, 2026-11-23).
    // A new profile starts on Original, so Add Profile shows Original's colours (owner, 2026-11-23).
    val themeInitials = rememberThemeInitialsColour()
    val initialsColour = if (isNew) com.morivia.app.core.ui.ThemeColors.Original.secondary else themeInitials
    val fallbackColorHex = com.morivia.app.core.ui.formatHexColor(initialsColour.toArgb() and 0xFFFFFF)

    var name by rememberSaveable { mutableStateOf(currentProfile?.name ?: "") }
    var selectedAvatarId by rememberSaveable { mutableStateOf(currentProfile?.avatarId) }
    var avatarUrl by rememberSaveable { mutableStateOf(currentProfile?.avatarUrl.orEmpty()) }
    var useInitials by rememberSaveable {
        mutableStateOf(!isNew && currentProfile?.avatarId == null && currentProfile?.avatarUrl.isNullOrBlank())
    }
    var selectedBackgroundId by rememberSaveable { mutableStateOf(currentProfile?.profileBackgroundId) }
    var selectedBackgroundUrl by rememberSaveable { mutableStateOf(currentProfile?.profileBackgroundUrl) }
    var usesPrimaryAddons by rememberSaveable { mutableStateOf(currentProfile?.usesPrimaryAddons ?: false) }
    var isSaving by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    // A profile with a PIN must be unlocked before it can be deleted (owner, 2026-11-23).
    var showDeletePin by remember { mutableStateOf(false) }
    var showPinSetup by remember { mutableStateOf(false) }
    var showPinClear by remember { mutableStateOf(false) }
    // Add Profile: the PIN is kept here and set on the new profile right after it is created.
    var pendingPin by remember { mutableStateOf<String?>(null) }
    val authState by com.morivia.app.core.auth.AuthRepository.state.collectAsStateWithLifecycle()
    val hasMoriviaAccount = (authState as? com.morivia.app.core.auth.AuthState.Authenticated)?.isAnonymous == false
    val memberAccess by remember {
        MemberAccessRepository.ensureStarted()
        MemberAccessRepository.access
    }.collectAsStateWithLifecycle()
    val backgroundCatalog by ProfileBackgroundRepository.catalog.collectAsStateWithLifecycle()
    val canChooseBackground = !isNew && memberAccess.entitlements.includes(CosmeticEntitlement.PROFILE_BACKGROUNDS)

    val avatars by AvatarRepository.avatars.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        AvatarRepository.refreshAvatars()
    }
    LaunchedEffect(canChooseBackground) {
        if (canChooseBackground) ProfileBackgroundRepository.preloadLandscapeImages()
    }
    LaunchedEffect(isNew, avatars, selectedAvatarId, avatarUrl, useInitials) {
        if (isNew && !useInitials && avatarUrl.isBlank() && selectedAvatarId == null && avatars.isNotEmpty()) {
            selectedAvatarId = avatars.first().id
        }
    }

    val customAvatarUrl = remember(avatarUrl) { normalizedAvatarUrl(avatarUrl) }
    val avatarUrlIsInvalid = avatarUrl.isNotBlank() && customAvatarUrl == null
    val selectedAvatarItem = remember(selectedAvatarId, avatars) {
        selectedAvatarId?.let { id -> avatars.find { it.id == id } }
    }
    val visibleAvatarItem = if (customAvatarUrl == null && !useInitials) selectedAvatarItem else null
    val previewAccent = remember(visibleAvatarItem, fallbackColorHex) {
        parseHexColor(visibleAvatarItem?.bgColor ?: fallbackColorHex)
    }

    // Switches, PIN lock and Save changes in the Appearance theme colour; Original on Add Profile
    // (owner, 2026-11-23).
    com.morivia.app.features.settings.ThemedAccent(
        accentOverride = if (isNew) androidx.compose.ui.graphics.Color(0xFF7A2BFF) else null,
    ) {
    MoriviaScreen(modifier = modifier) {
        stickyHeader {
            MoriviaScreenHeader(
                title = if (isNew) {
                    stringResource(Res.string.profile_edit_add_title)
                } else {
                    stringResource(Res.string.profile_edit_edit_title)
                },
                onBack = onBack,
            )
        }

        item {
            ProfileIdentityCard(
                name = name,
                isNew = isNew,
                profileIndex = currentProfile?.profileIndex,
                usesPrimaryAddons = usesPrimaryAddons,
                onNameChange = { name = it },
                onUsesPrimaryAddonsChange = { usesPrimaryAddons = it },
                selectedAvatar = visibleAvatarItem,
                customAvatarUrl = customAvatarUrl,
                accentColor = previewAccent,
                hasAvatarChoices = avatars.isNotEmpty(),
            )
        }

        item {
            MoriviaSurfaceCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = stringResource(Res.string.profile_custom_avatar_url),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(Res.string.profile_custom_avatar_url_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    MoriviaInputField(
                        value = avatarUrl,
                        onValueChange = { value ->
                            avatarUrl = value
                            if (value.isNotBlank()) {
                                selectedAvatarId = null
                                useInitials = false
                            }
                        },
                        placeholder = stringResource(Res.string.profile_custom_avatar_url_placeholder),
                    )
                    if (avatarUrlIsInvalid) {
                        Text(
                            text = stringResource(Res.string.profile_avatar_url_invalid),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }

        if (canChooseBackground) {
            item {
                MoriviaSurfaceCard {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = stringResource(Res.string.profile_choose_background),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(Res.string.profile_background_member_note),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        ProfileBackgroundPicker(
                            backgrounds = backgroundCatalog,
                            selectedBackgroundId = selectedBackgroundId,
                            selectedBackgroundUrl = selectedBackgroundUrl,
                            customBackgroundUrl = currentProfile?.profileBackgroundUrl,
                            standardBackgroundColor = previewAccent,
                            onSelectionChange = { id, url ->
                                selectedBackgroundId = id
                                selectedBackgroundUrl = url
                            },
                        )
                    }
                }
            }
        }

        item {
            MoriviaSurfaceCard {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = stringResource(Res.string.profile_choose_avatar),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    SelectedAvatarSummary(
                        avatar = visibleAvatarItem,
                        initials = if (useInitials && customAvatarUrl == null) profileInitial(name) else null,
                        customAvatarUrl = customAvatarUrl,
                        accentColor = previewAccent,
                        fallbackText = if (avatars.isEmpty()) {
                            stringResource(Res.string.profile_loading_avatars)
                        } else {
                            stringResource(Res.string.profile_select_avatar)
                        },
                    )

                    // No colour cards: the Appearance theme gives the initials their colour (owner,
                    // 2026-11-23).
                    // An "Initials" heading, like "Animals" above the avatars (owner, 2026-11-23).
                    Text("Initials", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    UseInitialsOption(
                        initial = profileInitial(name),
                        colour = parseHexColor(fallbackColorHex),
                        selected = useInitials && customAvatarUrl == null,
                        onClick = {
                            useInitials = true
                            avatarUrl = ""
                            selectedAvatarId = null
                        },
                    )
                    // A light line between Use initials and the pictures, like the lines in Settings.
                    androidx.compose.material3.HorizontalDivider(
                        thickness = MaterialTheme.nuvio.borders.hairline,
                        color = MaterialTheme.nuvio.colors.borderSubtle,
                    )
                    AvatarPicker(
                        avatars = avatars,
                        selectedAvatarId = selectedAvatarId.takeIf { customAvatarUrl == null && !useInitials },
                        onAvatarSelected = { avatar ->
                            avatarUrl = ""
                            useInitials = false
                            selectedAvatarId = avatar.id
                        },
                    )
                }
            }
        }

        // A PIN is stored on the Nuvio account, so guests cannot set one.
        if (hasMoriviaAccount) item {
            val pinEnabled = if (isNew) pendingPin != null else currentProfile?.pinEnabled == true
            MoriviaSurfaceCard {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = stringResource(Res.string.profile_security),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (pinEnabled) {
                            stringResource(Res.string.profile_security_pin_enabled)
                        } else {
                            stringResource(Res.string.profile_security_pin_disabled)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (pinEnabled) {
                        MoriviaPrimaryButton(
                            text = stringResource(Res.string.profile_remove_pin_lock),
                            onClick = {
                                if (isNew) pendingPin = null else showPinClear = true
                            },
                        )
                    } else {
                        MoriviaPrimaryButton(
                            text = stringResource(Res.string.profile_set_pin_lock),
                            onClick = { showPinSetup = true },
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            MoriviaPrimaryButton(
                text = if (isSaving) {
                    stringResource(Res.string.profile_saving)
                } else if (isNew) {
                    stringResource(Res.string.profile_create_profile)
                } else {
                    stringResource(Res.string.collections_editor_save_changes)
                },
                enabled = name.isNotBlank() && !avatarUrlIsInvalid && !isSaving,
                onClick = {
                    isSaving = true
                    scope.launch {
                        val avatarColorHex = visibleAvatarItem?.bgColor ?: fallbackColorHex
                        val chosenAvatarId = if (customAvatarUrl == null && !useInitials) selectedAvatarId else null
                        val saved = if (isNew) {
                            val newProfileIndex = ProfileRepository.createProfile(
                                name = name,
                                avatarColorHex = avatarColorHex,
                                avatarId = chosenAvatarId,
                                avatarUrl = customAvatarUrl,
                                usesPrimaryAddons = usesPrimaryAddons,
                            )
                            // Every new profile starts with the Original theme and app icon (owner, 2026-11-23).
                            newProfileIndex?.let(com.morivia.app.features.settings.ThemeSettingsStorage::resetProfileAppearance)
                            val pin = pendingPin
                            if (newProfileIndex != null && pin != null && hasMoriviaAccount) {
                                val result = ProfileRepository.setPin(profileIndex = newProfileIndex, pin = pin)
                                if (!result.unlocked) {
                                    // The profile exists; tell the user the PIN lock was not set.
                                    com.morivia.app.core.ui.MoriviaToastController.show(
                                        result.message ?: org.jetbrains.compose.resources.getString(Res.string.profile_pin_set_failed),
                                    )
                                }
                            }
                            newProfileIndex != null
                        } else {
                            ProfileRepository.updateProfile(
                                profileIndex = currentProfile!!.profileIndex,
                                name = name,
                                avatarColorHex = avatarColorHex,
                                avatarId = chosenAvatarId,
                                avatarUrl = customAvatarUrl,
                                profileBackgroundId = selectedBackgroundId,
                                profileBackgroundUrl = selectedBackgroundUrl,
                                usesPrimaryAddons = usesPrimaryAddons,
                            )
                        }
                        isSaving = false
                        if (saved) {
                            onSaved()
                        } else {
                            // Stay here so nothing is lost; the profile was not saved on Nuvio.
                            com.morivia.app.core.ui.MoriviaToastController.show(
                                org.jetbrains.compose.resources.getString(Res.string.profile_save_failed),
                            )
                        }
                    }
                },
            )
        }

        if (!isNew && (currentProfile?.profileIndex ?: 0) > 1) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text(
                        text = stringResource(Res.string.profile_delete_title),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }

    MoriviaStatusModal(
        title = stringResource(Res.string.profile_delete_title),
        message = stringResource(
            Res.string.profile_delete_confirm_message,
            currentProfile?.name.orEmpty(),
        ),
        isVisible = showDeleteConfirm,
        destructive = true,
        confirmText = stringResource(Res.string.action_delete),
        dismissText = stringResource(Res.string.action_cancel),
        onConfirm = {
            showDeleteConfirm = false
            if (currentProfile?.pinEnabled == true) {
                showDeletePin = true
            } else {
                scope.launch {
                    currentProfile?.let { ProfileRepository.deleteProfile(it.profileIndex) }
                    onBack()
                }
            }
        },
        onDismiss = { showDeleteConfirm = false },
    )

    if (showDeletePin && currentProfile != null) {
        PinEntryDialog(
            profileName = currentProfile.name,
            onVerify = { pin -> ProfileRepository.verifyPin(currentProfile.profileIndex, pin) },
            onVerified = {
                showDeletePin = false
                scope.launch {
                    ProfileRepository.deleteProfile(currentProfile.profileIndex)
                    onBack()
                }
            },
            onDismiss = { showDeletePin = false },
        )
    }

    if (showPinSetup && isNew) {
        NewPinWithConfirmDialog(
            onConfirmed = { pin ->
                pendingPin = pin
                PinVerifyResult(unlocked = true)
            },
            onDone = { showPinSetup = false },
            onDismiss = { showPinSetup = false },
        )
    }

    if (showPinSetup && currentProfile != null) {
        PinSetupDialog(
            profileIndex = currentProfile.profileIndex,
            hasExistingPin = currentProfile.pinEnabled,
            onDone = {
                showPinSetup = false
            },
            onDismiss = { showPinSetup = false },
        )
    }

    if (showPinClear && currentProfile != null) {
        PinEntryDialog(
            profileName = stringResource(Res.string.profile_remove_pin_for, currentProfile.name),
            onVerify = { pin -> ProfileRepository.clearPin(currentProfile.profileIndex, pin) },
            onVerified = {
                showPinClear = false
            },
            onDismiss = {
                showPinClear = false
            },
        )
    }
    }
}

@Composable
private fun ProfileIdentityCard(
    name: String,
    isNew: Boolean,
    profileIndex: Int?,
    usesPrimaryAddons: Boolean,
    onNameChange: (String) -> Unit,
    onUsesPrimaryAddonsChange: (Boolean) -> Unit,
    selectedAvatar: AvatarCatalogItem?,
    customAvatarUrl: String?,
    accentColor: Color,
    hasAvatarChoices: Boolean,
) {
    MoriviaSurfaceCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(
                            if (selectedAvatar != null || customAvatarUrl != null) androidx.compose.ui.graphics.SolidColor(accentColor)
                            else initialsBrush(accentColor),
                        )
                        .border(
                            width = 2.dp,
                            color = if (selectedAvatar == null && customAvatarUrl == null) {
                                accentColor.copy(alpha = 0.35f)
                            } else {
                                Color.Transparent
                            },
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (customAvatarUrl != null) {
                        AsyncImage(
                            model = customAvatarUrl,
                            contentDescription = name,
                            modifier = Modifier.size(88.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                    } else if (selectedAvatar != null) {
                        AsyncImage(
                            model = avatarImageUrl(selectedAvatar),
                            contentDescription = selectedAvatar.displayName,
                            modifier = Modifier.size(88.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                    } else if (name.isNotBlank()) {
                        Text(
                            text = name.take(1).uppercase(),
                            style = MaterialTheme.typography.displayLarge,
                            color = initialsTextColor(accentColor),
                            fontWeight = FontWeight.Bold,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(40.dp),
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = name.ifBlank {
                            if (isNew) stringResource(Res.string.profile_new)
                            else stringResource(Res.string.profile_unnamed)
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = listOf(
                            if (isNew) {
                                stringResource(Res.string.profile_new)
                            } else {
                                profileIndex?.let { stringResource(Res.string.profile_label_number, it) }
                                    ?: stringResource(Res.string.profile_unnamed)
                            },
                            if (usesPrimaryAddons) {
                                stringResource(Res.string.profile_primary_addons_on)
                            } else {
                                stringResource(Res.string.profile_primary_addons_off)
                            },
                        ).joinToString("  |  "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = when {
                            customAvatarUrl != null -> stringResource(Res.string.profile_custom_avatar_selected)
                            selectedAvatar != null -> stringResource(
                                Res.string.profile_avatar_selected,
                                selectedAvatar.displayName,
                            )
                            hasAvatarChoices -> stringResource(Res.string.profile_choose_avatar_below)
                            else -> stringResource(Res.string.profile_avatar_options_pending)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            MoriviaInputField(
                value = name,
                onValueChange = onNameChange,
                placeholder = stringResource(Res.string.profile_name_placeholder),
            )

            ProfileOptionRow(
                title = stringResource(Res.string.profile_use_primary_addons),
                description = stringResource(Res.string.profile_use_primary_addons_description),
                checked = usesPrimaryAddons,
                onCheckedChange = onUsesPrimaryAddonsChange,
            )
        }
    }
}


/** The chosen avatar and its name, shown under the "Choose an avatar" title. */
@Composable
private fun SelectedAvatarSummary(
    avatar: AvatarCatalogItem?,
    initials: String?,
    customAvatarUrl: String?,
    accentColor: Color,
    fallbackText: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val imageUrl = customAvatarUrl ?: avatar?.let(::avatarImageUrl)
        if (initials != null) {
            InitialCircle(initial = initials, colour = accentColor, size = 48.dp)
            Text(
                text = stringResource(Res.string.profile_use_initials),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            return@Row
        }
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(avatar?.bgColor?.let(::parseHexColor) ?: accentColor),
                contentScale = ContentScale.Crop,
            )
        }
        Text(
            text = when {
                customAvatarUrl != null -> stringResource(Res.string.profile_custom_avatar_selected)
                avatar != null -> avatar.displayName
                else -> fallbackText
            },
            style = MaterialTheme.typography.titleMedium,
            color = if (imageUrl != null) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

/** Show the initial as soon as the profile name contains a letter. */
private fun profileInitial(name: String): String =
    name.firstOrNull { it.isLetter() }?.uppercase().orEmpty()

@Composable
private fun InitialCircle(initial: String, colour: Color, size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(initialsBrush(colour)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.titleMedium,
            color = initialsTextColor(colour),
            fontWeight = FontWeight.SemiBold,
        )
    }
}

/**
 * The colour behind the initials (nuvio.tv calls it the fallback colour). The same chips as the
 * Appearance theme picker: the theme names and gradients, with the profile initial, nine first
 * and the rest behind the arrow.
 */
@Composable
private fun FallbackColourPicker(
    initial: String,
    selectedHex: String,
    onSelected: (String) -> Unit,
) {
    val known = INITIALS_COLOR_OPTIONS.firstOrNull { it.hex.equals(selectedHex, ignoreCase = true) }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var showCustom by rememberSaveable { mutableStateOf(false) }
    val alphabetic = APPEARANCE_PRESETS.map { theme ->
        val palette = ThemeColors.getColorPalette(theme)
        InitialsColorOption(palette.nativeAccentHex, theme.labelRes, palette)
    }.map { option ->
        option to com.morivia.app.features.settings.alphabeticalKey(stringResource(option.label))
    }.sortedBy { it.second }.map { it.first }
    val selectedPreset = alphabetic.firstOrNull { it.hex.equals(selectedHex, ignoreCase = true) }
    val visible = when {
        expanded -> alphabetic
        else -> alphabetic.take(INITIALS_COLLAPSED_COUNT)
    }
    val spacing = 12.dp
    androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columns = ((maxWidth + spacing) / (78.dp + spacing)).toInt()
            .coerceIn(1, if (maxWidth >= 560.dp) 4 else 3)
        Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
            visible.chunked(columns).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                ) {
                    row.forEach { option ->
                        val hex = option.hex
                        InitialsColourChip(
                            colour = parseHexColor(hex),
                            initial = initial,
                            label = stringResource(option.label),
                            selected = hex.equals(selectedHex, ignoreCase = true),
                            onClick = { onSelected(hex) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            val custom = CustomThemeColors.solid(com.morivia.app.core.ui.parseHexColor(selectedHex) ?: CustomThemeColors.Default.second)
            ThemeActions(
                customAvailable = true,
                customColors = custom,
                customSelected = known == null,
                onReset = { onSelected(PROFILE_COLORS.first()) },
                onCustom = { showCustom = true },
            )
            if (alphabetic.size > INITIALS_COLLAPSED_COUNT) {
                PresetExpansionArrow(expanded) { expanded = it }
            }
        }
    }
    if (showCustom) {
        CustomThemeEditor(
            initialColors = CustomThemeColors.solid(com.morivia.app.core.ui.parseHexColor(selectedHex) ?: CustomThemeColors.Default.second),
            allowGradient = false,
            onSave = { onSelected(formatHexColor(it.second)) },
            onDismiss = { showCustom = false },
        )
    }
}

private const val INITIALS_COLLAPSED_COUNT = 9

/** One colour, drawn like a theme chip in Appearance. */
@Composable
private fun InitialsColourChip(
    colour: Color,
    initial: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = MaterialTheme.nuvio
    val ring = initialsPalette(colour)?.focusRing ?: colour
    Column(
        modifier = modifier
            .clip(tokens.shapes.compactCard)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(56.dp).then(
                if (selected) Modifier.border(tokens.borders.medium, ring, CircleShape) else Modifier,
            ),
            contentAlignment = Alignment.Center,
        ) {
            InitialCircle(initial = initial, colour = colour, size = 44.dp)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) tokens.colors.textPrimary else tokens.colors.textSecondary,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** "Use initials": the profile shows its first letter on the fallback colour, as on nuvio.tv. */
@Composable
private fun UseInitialsOption(
    initial: String,
    colour: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        InitialCircle(initial = initial, colour = colour, size = 40.dp)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(Res.string.profile_use_initials),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(Res.string.profile_use_initials_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ProfileOptionRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant,
            ),
        )
    }
}

@Composable
fun PinSetupDialog(
    profileIndex: Int,
    hasExistingPin: Boolean,
    onDone: () -> Unit,
    onDismiss: () -> Unit,
) {
    var step by remember { mutableStateOf(if (hasExistingPin) "current" else "new") }
    var currentPin by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    when (step) {
        "current" -> PinEntryDialog(
            profileName = stringResource(Res.string.profile_enter_current_pin),
            onVerify = { pin -> ProfileRepository.verifyPin(profileIndex, pin) },
            onVerified = { pin ->
                currentPin = pin
                step = "new"
            },
            onDismiss = onDismiss,
        )

        "new" -> NewPinWithConfirmDialog(
            onConfirmed = { pin ->
                ProfileRepository.setPin(
                    profileIndex = profileIndex,
                    pin = pin,
                    currentPin = currentPin.ifEmpty { null },
                )
            },
            onDone = onDone,
            onDismiss = onDismiss,
        )
    }
}

/**
 * Asks for a new PIN and then for the same PIN again. [onConfirmed] runs only when both match;
 * a different second PIN asks for the new PIN again from the start.
 */
@Composable
private fun NewPinWithConfirmDialog(
    onConfirmed: suspend (String) -> PinVerifyResult,
    onDone: () -> Unit,
    onDismiss: () -> Unit,
) {
    var firstPin by remember { mutableStateOf<String?>(null) }
    var attempt by remember { mutableStateOf(0) }
    val mismatch = stringResource(Res.string.profile_pin_mismatch)
    val pin = firstPin
    PinEntryDialog(
        entryStep = attempt to (pin == null),
        profileName = when {
            pin != null -> stringResource(Res.string.profile_confirm_pin)
            attempt > 0 -> mismatch
            else -> stringResource(Res.string.profile_enter_new_pin)
        },
        onVerify = { entered ->
            when {
                pin == null -> PinVerifyResult(unlocked = true)
                entered == pin -> onConfirmed(entered)
                else -> {
                    firstPin = null
                    attempt++
                    PinVerifyResult(unlocked = false, message = mismatch)
                }
            }
        },
        onVerified = { entered ->
            if (pin == null) firstPin = entered else onDone()
        },
        onDismiss = onDismiss,
    )
}

/** The name of the initial's colour: its theme name, or Custom. */
@Composable
private fun initialsColourLabel(hex: String): String {
    val theme = APPEARANCE_PRESETS.firstOrNull {
        ThemeColors.getColorPalette(it).nativeAccentHex.equals(hex, ignoreCase = true)
    }
    return stringResource(theme?.labelRes ?: com.morivia.app.core.ui.AppTheme.CUSTOM.labelRes)
}

/** A card like Theme in Appearance: tapping it opens [content] right under it. */
@Composable
private fun ExpandableColourCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    open: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column {
        com.morivia.app.features.settings.SettingsGroup(isTablet = false) {
            com.morivia.app.features.settings.ConnectionsSectionHeader(
                title = title,
                description = description,
                icon = icon,
                expanded = open,
                isTablet = false,
                onToggle = onToggle,
            )
        }
        androidx.compose.animation.AnimatedVisibility(visible = open) {
            Box(Modifier.padding(top = 14.dp)) { content() }
        }
    }
}
