package com.morivia.app.features.profiles

import com.morivia.app.core.ui.MoriviaToastController
import morivia.composeapp.generated.resources.Res
import morivia.composeapp.generated.resources.profile_already_active
import org.jetbrains.compose.resources.getString

internal fun routeProfileSelection(
    profile: MoriviaProfile,
    isEditMode: Boolean,
    activeProfileIndex: Int? = null,
    onEditProfile: (MoriviaProfile) -> Unit,
    onActiveProfileSelected: (MoriviaProfile) -> Unit,
    onPinRequired: (MoriviaProfile) -> Unit,
    onProfileSelected: (MoriviaProfile) -> Unit,
) {
    when {
        isEditMode -> onEditProfile(profile)
        profile.profileIndex == activeProfileIndex -> onActiveProfileSelected(profile)
        profile.pinEnabled -> onPinRequired(profile)
        else -> onProfileSelected(profile)
    }
}

internal suspend fun showAlreadyActiveProfileToast(profile: MoriviaProfile) {
    MoriviaToastController.show(getString(Res.string.profile_already_active, profile.name))
}
