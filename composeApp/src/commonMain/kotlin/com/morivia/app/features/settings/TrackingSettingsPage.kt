package com.morivia.app.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.morivia.app.core.ui.MoriviaLoadingIndicator
import com.morivia.app.core.ui.nuvio
import com.morivia.app.features.library.LibrarySourceMode
import com.morivia.app.features.mdblist.MdbListTracker
import com.morivia.app.features.profiles.ProfileRepository
import com.morivia.app.features.simkl.SimklAnimeIdPreference
import com.morivia.app.features.simkl.SimklAuthUiState
import com.morivia.app.features.simkl.SimklConnectionMode
import com.morivia.app.features.tracking.TrackingProviderId
import com.morivia.app.features.tracking.TrackingRatings
import com.morivia.app.features.tracking.TrackingSettingsRepository
import com.morivia.app.features.tracking.TrackingSettingsUiState
import com.morivia.app.features.tracking.WatchProgressSource
import com.morivia.app.features.tracking.effectiveLibrarySourceMode
import com.morivia.app.features.tracking.effectiveWatchProgressSource
import com.morivia.app.features.trakt.MoreLikeThisSourcePreference
import com.morivia.app.features.trakt.TRAKT_CONTINUE_WATCHING_DAYS_CAP_ALL
import com.morivia.app.features.trakt.TraktAuthUiState
import com.morivia.app.features.trakt.TraktConnectionMode
import com.morivia.app.features.trakt.TraktContinueWatchingDaysOptions
import com.morivia.app.features.trakt.normalizeTraktContinueWatchingDaysCap
import com.morivia.app.features.watchprogress.WatchProgressSourceCoordinator
import kotlinx.coroutines.launch
import morivia.composeapp.generated.resources.Res
import morivia.composeapp.generated.resources.tracking_source_mdblist
import morivia.composeapp.generated.resources.settings_mdblist_library_description
import morivia.composeapp.generated.resources.settings_mdblist_progress_description
import morivia.composeapp.generated.resources.action_retry
import morivia.composeapp.generated.resources.settings_tracking_connect_first
import morivia.composeapp.generated.resources.settings_tracking_data_sources
import morivia.composeapp.generated.resources.settings_tracking_morivia_library_description
import morivia.composeapp.generated.resources.settings_tracking_morivia_progress_description
import morivia.composeapp.generated.resources.settings_tracking_progress_refresh_failed
import morivia.composeapp.generated.resources.settings_tracking_services
import morivia.composeapp.generated.resources.settings_tracking_simkl_library_description
import morivia.composeapp.generated.resources.settings_tracking_simkl_progress_description
import morivia.composeapp.generated.resources.settings_tracking_simkl_recommendations_description
import morivia.composeapp.generated.resources.settings_tracking_source_fallback
import morivia.composeapp.generated.resources.settings_tracking_tmdb_recommendations_description
import morivia.composeapp.generated.resources.settings_tracking_trakt_library_description
import morivia.composeapp.generated.resources.settings_tracking_trakt_progress_description
import morivia.composeapp.generated.resources.settings_tracking_trakt_progress_required
import morivia.composeapp.generated.resources.settings_tracking_trakt_recommendations_description
import morivia.composeapp.generated.resources.settings_tracking_viewing_discovery
import morivia.composeapp.generated.resources.settings_tracking_anime_id_dialog_subtitle
import morivia.composeapp.generated.resources.settings_tracking_anime_id_dialog_title
import morivia.composeapp.generated.resources.settings_tracking_anime_id_imdb
import morivia.composeapp.generated.resources.settings_tracking_anime_id_imdb_description
import morivia.composeapp.generated.resources.settings_tracking_anime_id_kitsu
import morivia.composeapp.generated.resources.settings_tracking_anime_id_kitsu_description
import morivia.composeapp.generated.resources.settings_tracking_anime_id_tvdb
import morivia.composeapp.generated.resources.settings_tracking_anime_id_tvdb_description
import morivia.composeapp.generated.resources.settings_tracking_anime_id_mal
import morivia.composeapp.generated.resources.settings_tracking_anime_id_mal_description
import morivia.composeapp.generated.resources.settings_tracking_anime_id_subtitle
import morivia.composeapp.generated.resources.settings_tracking_anime_id_title
import morivia.composeapp.generated.resources.settings_tracking_anime_section
import morivia.composeapp.generated.resources.settings_tracking_mdblist_section
import morivia.composeapp.generated.resources.compose_action_off
import morivia.composeapp.generated.resources.settings_tracking_ratings
import morivia.composeapp.generated.resources.settings_tracking_ratings_description
import morivia.composeapp.generated.resources.settings_tracking_ratings_mdblist_description
import morivia.composeapp.generated.resources.settings_tracking_ratings_off_description
import morivia.composeapp.generated.resources.settings_tracking_ratings_picker_subtitle
import morivia.composeapp.generated.resources.settings_tracking_ratings_simkl_description
import morivia.composeapp.generated.resources.settings_mdblist_library_lists
import morivia.composeapp.generated.resources.settings_mdblist_library_lists_description
import morivia.composeapp.generated.resources.settings_mdblist_library_lists_summary
import morivia.composeapp.generated.resources.settings_trakt_comments
import morivia.composeapp.generated.resources.settings_trakt_comments_description
import morivia.composeapp.generated.resources.tracking_source_simkl
import morivia.composeapp.generated.resources.tracking_watch_progress_dialog_subtitle
import morivia.composeapp.generated.resources.trakt_all_history
import morivia.composeapp.generated.resources.trakt_continue_watching_subtitle
import morivia.composeapp.generated.resources.trakt_continue_watching_window
import morivia.composeapp.generated.resources.trakt_cw_window_subtitle
import morivia.composeapp.generated.resources.trakt_cw_window_title
import morivia.composeapp.generated.resources.trakt_days_format
import morivia.composeapp.generated.resources.trakt_library_source_dialog_subtitle
import morivia.composeapp.generated.resources.trakt_library_source_dialog_title
import morivia.composeapp.generated.resources.trakt_library_source_morivia
import morivia.composeapp.generated.resources.trakt_library_source_subtitle
import morivia.composeapp.generated.resources.trakt_library_source_title
import morivia.composeapp.generated.resources.trakt_library_source_trakt
import morivia.composeapp.generated.resources.trakt_more_like_this_source_dialog_subtitle
import morivia.composeapp.generated.resources.trakt_more_like_this_source_dialog_title
import morivia.composeapp.generated.resources.trakt_more_like_this_source_subtitle
import morivia.composeapp.generated.resources.trakt_more_like_this_source_title
import morivia.composeapp.generated.resources.trakt_more_like_this_source_tmdb
import morivia.composeapp.generated.resources.trakt_more_like_this_source_trakt
import morivia.composeapp.generated.resources.trakt_more_like_this_source_simkl
import morivia.composeapp.generated.resources.trakt_watch_progress_dialog_title
import morivia.composeapp.generated.resources.trakt_watch_progress_source_morivia
import morivia.composeapp.generated.resources.trakt_watch_progress_source_trakt
import morivia.composeapp.generated.resources.trakt_watch_progress_subtitle
import morivia.composeapp.generated.resources.trakt_watch_progress_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

internal fun LazyListScope.trackingSettingsContent(
    isTablet: Boolean,
    traktUiState: TraktAuthUiState,
    simklUiState: SimklAuthUiState,
    settingsUiState: TrackingSettingsUiState,
    commentsEnabled: Boolean,
    onCommentsEnabledChange: (Boolean) -> Unit,
    mdbListConnected: Boolean,
    accountsSectionTitle: StringResource = Res.string.settings_tracking_services,
) {
    item {
        SettingsSection(
            title = stringResource(accountsSectionTitle).uppercase(),
            isTablet = isTablet,
        ) {
            TrackingProviderCards(
                isTablet = isTablet,
                traktUiState = traktUiState,
                simklUiState = simklUiState,
            )
        }
    }

    item {
        SettingsSection(
            title = stringResource(Res.string.settings_tracking_data_sources).uppercase(),
            isTablet = isTablet,
        ) {
            TrackingDataSources(
                isTablet = isTablet,
                settingsUiState = settingsUiState,
                traktConnected = traktUiState.mode == TraktConnectionMode.CONNECTED,
                simklConnected = simklUiState.mode == SimklConnectionMode.CONNECTED,
                commentsEnabled = commentsEnabled,
                onCommentsEnabledChange = onCommentsEnabledChange,
            )
        }
    }
}

@Composable
private fun MdbListLibraryListsRow(isTablet: Boolean) {
    val tokens = MaterialTheme.nuvio
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val options by remember { MdbListTracker.library.listOptions }.collectAsStateWithLifecycle(emptyList())
    val isRefreshing by remember { MdbListTracker.library.isRefreshing }.collectAsStateWithLifecycle(false)

    SettingsNavigationRow(
        title = stringResource(Res.string.settings_mdblist_library_lists),
        description = stringResource(Res.string.settings_mdblist_library_lists_description),
        isTablet = isTablet,
        trailingContent = {
            if (options.isNotEmpty()) {
                Text(
                    text = stringResource(
                        Res.string.settings_mdblist_library_lists_summary,
                        options.count { it.visible },
                        options.size,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.colors.accent,
                    fontWeight = FontWeight.Medium,
                )
            }
        },
        onClick = {
            MdbListTracker.library.prepare()
            showPicker = true
        },
    )

    if (showPicker) {
        MdbListLibraryListsPicker(
            isTablet = isTablet,
            options = options,
            isLoading = isRefreshing,
            onToggle = { option, onResult ->
                MdbListTracker.library.setListVisibleAsync(option.key, !option.visible, onResult)
            },
            onDismiss = { showPicker = false },
        )
    }
}

@Composable
private fun TrackingDataSources(
    isTablet: Boolean,
    settingsUiState: TrackingSettingsUiState,
    traktConnected: Boolean,
    simklConnected: Boolean,
    commentsEnabled: Boolean,
    onCommentsEnabledChange: (Boolean) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val transitionState by remember {
        WatchProgressSourceCoordinator.ensureStarted()
        WatchProgressSourceCoordinator.uiState
    }.collectAsStateWithLifecycle()
    val mdblistConnected by remember { MdbListTracker.ensureLoaded(); MdbListTracker.isAuthenticated }.collectAsStateWithLifecycle()
    val connectedProviders = buildSet {
        if (traktConnected) add(TrackingProviderId.TRAKT)
        if (simklConnected) add(TrackingProviderId.SIMKL)
        if (mdblistConnected) add(TrackingProviderId.MDBLIST)
    }
    val effectiveLibrarySource = effectiveLibrarySourceMode(settingsUiState.librarySourceMode) { provider ->
        provider in connectedProviders
    }
    val effectiveProgressSource = effectiveWatchProgressSource(settingsUiState.watchProgressSource) { provider ->
        provider in connectedProviders
    }
    val libraryFallback = if (effectiveLibrarySource != settingsUiState.librarySourceMode) {
        stringResource(
            Res.string.settings_tracking_source_fallback,
            librarySourceModeLabel(settingsUiState.librarySourceMode),
            librarySourceModeLabel(effectiveLibrarySource),
        )
    } else {
        null
    }
    val progressFallback = if (effectiveProgressSource != settingsUiState.watchProgressSource) {
        stringResource(
            Res.string.settings_tracking_source_fallback,
            watchProgressSourceLabel(settingsUiState.watchProgressSource),
            watchProgressSourceLabel(effectiveProgressSource),
        )
    } else {
        null
    }

    SettingsGroup(isTablet = isTablet) {
        TrackingChoiceRow(
            title = stringResource(Res.string.trakt_library_source_title),
            options = librarySourceOptions(traktConnected, simklConnected, mdblistConnected),
            selected = effectiveLibrarySource,
            onSelected = TrackingSettingsRepository::setLibrarySourceMode,
            supportingMessage = libraryFallback,
            isTablet = isTablet,
        )
        SettingsGroupDivider(isTablet = isTablet)
        TrackingChoiceRow(
            title = stringResource(Res.string.trakt_watch_progress_title),
            options = watchProgressSourceOptions(traktConnected, simklConnected, mdblistConnected),
            selected = effectiveProgressSource,
            onSelected = { source ->
                scope.launch {
                    WatchProgressSourceCoordinator.selectSource(
                        profileId = ProfileRepository.activeProfileId,
                        source = source,
                    )
                }
            },
            supportingMessage = progressFallback,
            isLoading = transitionState.isRefreshing,
            isTablet = isTablet,
        )
        if (transitionState.lastRefreshSucceeded == false && !transitionState.isRefreshing) {
            SettingsGroupDivider(isTablet = isTablet)
            TrackingInlineErrorRow(
                isTablet = isTablet,
                message = stringResource(Res.string.settings_tracking_progress_refresh_failed),
                onRetry = {
                    scope.launch {
                        WatchProgressSourceCoordinator.refreshActiveSource(ProfileRepository.activeProfileId)
                    }
                },
            )
        }
        if (traktConnected || simklConnected) {
            val effectiveRecommendationsSource = effectiveTrackingRecommendationsSource(
                source = settingsUiState.moreLikeThisSource,
                traktConnected = traktConnected,
            )
            val recommendationsFallback = if (effectiveRecommendationsSource != settingsUiState.moreLikeThisSource) {
                stringResource(
                    Res.string.settings_tracking_source_fallback,
                    moreLikeThisSourceLabel(settingsUiState.moreLikeThisSource),
                    moreLikeThisSourceLabel(effectiveRecommendationsSource),
                )
            } else {
                null
            }
            SettingsGroupDivider(isTablet = isTablet)
            TrackingChoiceRow(
                title = stringResource(Res.string.trakt_more_like_this_source_title),
                options = recommendationsSourceOptions(traktConnected),
                selected = effectiveRecommendationsSource,
                onSelected = TrackingSettingsRepository::setMoreLikeThisSource,
                supportingMessage = recommendationsFallback,
                isTablet = isTablet,
            )
        }
        if (simklConnected || mdblistConnected) {
            val effectiveRatingSource = settingsUiState.ratingSource?.takeIf { it in connectedProviders }
            val ratingsFallback = settingsUiState.ratingSource?.takeIf { effectiveRatingSource == null }?.let { source ->
                stringResource(
                    Res.string.settings_tracking_source_fallback,
                    ratingSourceLabel(source),
                    ratingSourceLabel(null),
                )
            }
            SettingsGroupDivider(isTablet = isTablet)
            TrackingChoiceRow(
                title = stringResource(Res.string.settings_tracking_ratings),
                options = ratingSourceOptions(simklConnected, mdblistConnected),
                selected = effectiveRatingSource,
                onSelected = TrackingRatings::setSource,
                supportingMessage = ratingsFallback,
                isTablet = isTablet,
            )
        }
        // Per-service options live in Sources too (owner's request, 2026-11-22): the page has
        // only the Tracking cards and this Sources group.
        if (traktConnected && settingsUiState.watchProgressSource == WatchProgressSource.TRAKT) {
            SettingsGroupDivider(isTablet = isTablet)
            TrackingChoiceRow(
                title = stringResource(Res.string.trakt_continue_watching_window),
                description = stringResource(Res.string.trakt_continue_watching_subtitle),
                options = continueWatchingOptions(),
                selected = normalizeTraktContinueWatchingDaysCap(settingsUiState.continueWatchingDaysCap),
                onSelected = TrackingSettingsRepository::setContinueWatchingDaysCap,
                isTablet = isTablet,
            )
        }
        if (traktConnected) {
            SettingsGroupDivider(isTablet = isTablet)
            SettingsSwitchRow(
                title = stringResource(Res.string.settings_trakt_comments),
                description = stringResource(Res.string.settings_trakt_comments_description),
                checked = commentsEnabled,
                isTablet = isTablet,
                onCheckedChange = onCommentsEnabledChange,
            )
        }
        if (simklConnected) {
            SettingsGroupDivider(isTablet = isTablet)
            TrackingChoiceRow(
                title = stringResource(Res.string.settings_tracking_anime_id_title),
                description = stringResource(Res.string.settings_tracking_anime_id_subtitle),
                options = animeIdPreferenceOptions(),
                selected = settingsUiState.simklAnimeIdPreference,
                onSelected = TrackingSettingsRepository::setSimklAnimeIdPreference,
                isTablet = isTablet,
            )
        }
        if (mdblistConnected) {
            SettingsGroupDivider(isTablet = isTablet)
            MdbListLibraryListsRow(isTablet = isTablet)
        }
    }
}

@Composable
private fun TrackingInlineErrorRow(
    isTablet: Boolean,
    message: String,
    onRetry: () -> Unit,
) {
    val tokens = MaterialTheme.nuvio
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = if (isTablet) 20.dp else 16.dp,
                vertical = if (isTablet) 14.dp else 12.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = message,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = tokens.colors.danger,
        )
        TextButton(onClick = onRetry) {
            Text(stringResource(Res.string.action_retry))
        }
    }
}

@Composable
private fun librarySourceOptions(
    traktConnected: Boolean,
    simklConnected: Boolean,
    mdblistConnected: Boolean,
): List<TrackingPickerOption<LibrarySourceMode>> {
    val traktAvailable = isTrackingBrandAvailable(TrackingBrand.TRAKT, traktConnected, simklConnected)
    val simklAvailable = isTrackingBrandAvailable(TrackingBrand.SIMKL, traktConnected, simklConnected)
    return listOf(
        TrackingPickerOption(
            value = LibrarySourceMode.LOCAL,
            title = stringResource(Res.string.trakt_library_source_morivia),
            description = stringResource(Res.string.settings_tracking_morivia_library_description),
        ),
        TrackingPickerOption(
            value = LibrarySourceMode.TRAKT,
            title = stringResource(Res.string.trakt_library_source_trakt),
            description = stringResource(Res.string.settings_tracking_trakt_library_description),
            enabled = traktAvailable,
            unavailableReason = trackingUnavailableReason(TrackingBrand.TRAKT, traktAvailable),
        ),
        TrackingPickerOption(
            value = LibrarySourceMode.SIMKL,
            title = stringResource(Res.string.tracking_source_simkl),
            description = stringResource(Res.string.settings_tracking_simkl_library_description),
            enabled = simklAvailable,
            unavailableReason = trackingUnavailableReason(TrackingBrand.SIMKL, simklAvailable),
        ),
        TrackingPickerOption(
            value = LibrarySourceMode.MDBLIST,
            title = stringResource(Res.string.tracking_source_mdblist),
            description = stringResource(Res.string.settings_mdblist_library_description),
            enabled = mdblistConnected,
            unavailableReason = trackingUnavailableReason(TrackingBrand.MDBLIST, mdblistConnected),
        ),
    )
}

@Composable
private fun watchProgressSourceOptions(
    traktConnected: Boolean,
    simklConnected: Boolean,
    mdblistConnected: Boolean,
): List<TrackingPickerOption<WatchProgressSource>> {
    val traktAvailable = isTrackingBrandAvailable(TrackingBrand.TRAKT, traktConnected, simklConnected)
    val simklAvailable = isTrackingBrandAvailable(TrackingBrand.SIMKL, traktConnected, simklConnected)
    return listOf(
        TrackingPickerOption(
            value = WatchProgressSource.NUVIO_SYNC,
            title = stringResource(Res.string.trakt_watch_progress_source_morivia),
            description = stringResource(Res.string.settings_tracking_morivia_progress_description),
        ),
        TrackingPickerOption(
            value = WatchProgressSource.TRAKT,
            title = stringResource(Res.string.trakt_watch_progress_source_trakt),
            description = stringResource(Res.string.settings_tracking_trakt_progress_description),
            enabled = traktAvailable,
            unavailableReason = trackingUnavailableReason(TrackingBrand.TRAKT, traktAvailable),
        ),
        TrackingPickerOption(
            value = WatchProgressSource.SIMKL,
            title = stringResource(Res.string.tracking_source_simkl),
            description = stringResource(Res.string.settings_tracking_simkl_progress_description),
            enabled = simklAvailable,
            unavailableReason = trackingUnavailableReason(TrackingBrand.SIMKL, simklAvailable),
        ),
        TrackingPickerOption(
            value = WatchProgressSource.MDBLIST,
            title = stringResource(Res.string.tracking_source_mdblist),
            description = stringResource(Res.string.settings_mdblist_progress_description),
            enabled = mdblistConnected,
            unavailableReason = trackingUnavailableReason(TrackingBrand.MDBLIST, mdblistConnected),
        ),
    )
}

@Composable
private fun continueWatchingOptions(): List<TrackingPickerOption<Int>> =
    TraktContinueWatchingDaysOptions.map { days ->
        val normalizedDays = normalizeTraktContinueWatchingDaysCap(days)
        TrackingPickerOption(
            value = normalizedDays,
            title = continueWatchingDaysCapLabel(normalizedDays),
        )
    }

@Composable
private fun recommendationsSourceOptions(
    traktConnected: Boolean,
): List<TrackingPickerOption<MoreLikeThisSourcePreference>> {
    val traktAvailable = isTrackingBrandAvailable(TrackingBrand.TRAKT, traktConnected, simklConnected = false)
    val simklConnected = com.morivia.app.features.simkl.SimklAuthRepository.let {
        it.ensureLoaded()
        it.isAuthenticated.value
    }
    return listOf(
        TrackingPickerOption(
            value = MoreLikeThisSourcePreference.TMDB,
            title = stringResource(Res.string.trakt_more_like_this_source_tmdb),
            description = stringResource(Res.string.settings_tracking_tmdb_recommendations_description),
        ),
        TrackingPickerOption(
            value = MoreLikeThisSourcePreference.TRAKT,
            title = stringResource(Res.string.trakt_more_like_this_source_trakt),
            description = stringResource(Res.string.settings_tracking_trakt_recommendations_description),
            enabled = traktAvailable,
            unavailableReason = trackingUnavailableReason(TrackingBrand.TRAKT, traktAvailable),
        ),
        TrackingPickerOption(
            value = MoreLikeThisSourcePreference.SIMKL,
            title = stringResource(Res.string.trakt_more_like_this_source_simkl),
            description = stringResource(Res.string.settings_tracking_simkl_recommendations_description),
            enabled = simklConnected,
            unavailableReason = if (!simklConnected) stringResource(Res.string.settings_tracking_connect_first, "Simkl") else null,
        ),
    )
}

@Composable
private fun ratingSourceOptions(
    simklConnected: Boolean,
    mdblistConnected: Boolean,
): List<TrackingPickerOption<TrackingProviderId?>> = listOf(
    TrackingPickerOption(
        value = null,
        title = ratingSourceLabel(null),
        description = stringResource(Res.string.settings_tracking_ratings_off_description),
    ),
    TrackingPickerOption(
        value = TrackingProviderId.SIMKL,
        title = ratingSourceLabel(TrackingProviderId.SIMKL),
        description = stringResource(Res.string.settings_tracking_ratings_simkl_description),
        enabled = simklConnected,
        unavailableReason = trackingUnavailableReason(TrackingBrand.SIMKL, simklConnected),
    ),
    TrackingPickerOption(
        value = TrackingProviderId.MDBLIST,
        title = ratingSourceLabel(TrackingProviderId.MDBLIST),
        description = stringResource(Res.string.settings_tracking_ratings_mdblist_description),
        enabled = mdblistConnected,
        unavailableReason = trackingUnavailableReason(TrackingBrand.MDBLIST, mdblistConnected),
    ),
)

@Composable
private fun ratingSourceLabel(source: TrackingProviderId?): String =
    source?.displayName ?: stringResource(Res.string.compose_action_off)

@Composable
private fun trackingUnavailableReason(
    brand: TrackingBrand,
    connected: Boolean,
): String? = if (connected) {
    null
} else {
    stringResource(Res.string.settings_tracking_connect_first, brand.displayName)
}

@Composable
private fun librarySourceModeLabel(source: LibrarySourceMode): String = when (source) {
    LibrarySourceMode.TRAKT -> stringResource(Res.string.trakt_library_source_trakt)
    LibrarySourceMode.LOCAL -> stringResource(Res.string.trakt_library_source_morivia)
    LibrarySourceMode.SIMKL -> stringResource(Res.string.tracking_source_simkl)
    LibrarySourceMode.MDBLIST -> stringResource(Res.string.tracking_source_mdblist)
}

@Composable
private fun watchProgressSourceLabel(source: WatchProgressSource): String = when (source) {
    WatchProgressSource.TRAKT -> stringResource(Res.string.trakt_watch_progress_source_trakt)
    WatchProgressSource.NUVIO_SYNC -> stringResource(Res.string.trakt_watch_progress_source_morivia)
    WatchProgressSource.SIMKL -> stringResource(Res.string.tracking_source_simkl)
    WatchProgressSource.MDBLIST -> stringResource(Res.string.tracking_source_mdblist)
}

@Composable
private fun moreLikeThisSourceLabel(source: MoreLikeThisSourcePreference): String = when (source) {
    MoreLikeThisSourcePreference.TRAKT -> stringResource(Res.string.trakt_more_like_this_source_trakt)
    MoreLikeThisSourcePreference.TMDB -> stringResource(Res.string.trakt_more_like_this_source_tmdb)
    MoreLikeThisSourcePreference.SIMKL -> stringResource(Res.string.trakt_more_like_this_source_simkl)
}

@Composable
private fun continueWatchingDaysCapLabel(daysCap: Int): String {
    val normalized = normalizeTraktContinueWatchingDaysCap(daysCap)
    return if (normalized == TRAKT_CONTINUE_WATCHING_DAYS_CAP_ALL) {
        stringResource(Res.string.trakt_all_history)
    } else {
        stringResource(Res.string.trakt_days_format, normalized)
    }
}

internal fun effectiveTrackingRecommendationsSource(
    source: MoreLikeThisSourcePreference,
    traktConnected: Boolean,
): MoreLikeThisSourcePreference {
    if (source == MoreLikeThisSourcePreference.TRAKT && !traktConnected) {
        return MoreLikeThisSourcePreference.TMDB
    }
    if (source == MoreLikeThisSourcePreference.SIMKL) {
        com.morivia.app.features.simkl.SimklAuthRepository.ensureLoaded()
        if (!com.morivia.app.features.simkl.SimklAuthRepository.isAuthenticated.value) {
            return MoreLikeThisSourcePreference.TMDB
        }
    }
    return source
}

@Composable
private fun animeIdPreferenceOptions(): List<TrackingPickerOption<SimklAnimeIdPreference>> = listOf(
    TrackingPickerOption(
        value = SimklAnimeIdPreference.IMDB,
        title = stringResource(Res.string.settings_tracking_anime_id_imdb),
        description = stringResource(Res.string.settings_tracking_anime_id_imdb_description),
    ),
    TrackingPickerOption(
        value = SimklAnimeIdPreference.MAL,
        title = stringResource(Res.string.settings_tracking_anime_id_mal),
        description = stringResource(Res.string.settings_tracking_anime_id_mal_description),
    ),
    TrackingPickerOption(
        value = SimklAnimeIdPreference.KITSU,
        title = stringResource(Res.string.settings_tracking_anime_id_kitsu),
        description = stringResource(Res.string.settings_tracking_anime_id_kitsu_description),
    ),
    TrackingPickerOption(
        value = SimklAnimeIdPreference.TVDB,
        title = stringResource(Res.string.settings_tracking_anime_id_tvdb),
        description = stringResource(Res.string.settings_tracking_anime_id_tvdb_description),
    ),
)

@Composable
private fun animeIdPreferenceLabel(preference: SimklAnimeIdPreference): String = when (preference) {
    SimklAnimeIdPreference.IMDB -> stringResource(Res.string.settings_tracking_anime_id_imdb)
    SimklAnimeIdPreference.MAL -> stringResource(Res.string.settings_tracking_anime_id_mal)
    SimklAnimeIdPreference.KITSU -> stringResource(Res.string.settings_tracking_anime_id_kitsu)
    SimklAnimeIdPreference.TVDB -> stringResource(Res.string.settings_tracking_anime_id_tvdb)
}
