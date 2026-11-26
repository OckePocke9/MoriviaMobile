package com.morivia.app.features.settings

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.morivia.app.core.ui.Chip
import com.morivia.app.core.ui.PosterCardStyleRepository
import com.morivia.app.features.home.HomeCatalogSettingsRepository
import com.morivia.app.features.watchprogress.ContinueWatchingPreferencesRepository
import com.morivia.app.features.watchprogress.ContinueWatchingSortMode
import morivia.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CollectionArtSettingsRow(isTablet: Boolean) {
    val layout by rememberMoriviaLayout()
    SettingsChipRow(
        title = stringResource(Res.string.morivia_hl_collection_gifs),
        description = stringResource(Res.string.morivia_hl_collection_gifs_sub),
        isTablet = isTablet,
    ) {
        MoriviaCollectionGifs.entries.forEach { mode ->
            Chip(
                label = stringResource(
                    if (mode == MoriviaCollectionGifs.ANIMATED) Res.string.morivia_hl_gifs_animated else Res.string.morivia_hl_gifs_still,
                ),
                selected = layout.collectionGifs == mode,
                onClick = { MoriviaLayoutSettingsRepository.update { it.copy(collectionGifs = mode) } },
            )
        }
    }
}

internal fun LazyListScope.moriviaHeroSettingsContent(isTablet: Boolean) {
    item {
        val layout by rememberMoriviaLayout()
        SettingsSection(title = stringResource(Res.string.morivia_hl_section_hero), isTablet = isTablet) {
            SettingsGroup(isTablet = isTablet) {
                HeroPlaybackRows(layout = layout, isTablet = isTablet)
            }
        }
    }
}

@Composable
private fun HeroPlaybackRows(layout: MoriviaLayoutSettings, isTablet: Boolean) {
    // Play Trailers in Hero and Auto-Rotate Hero work independently of each other.
    SettingsSwitchRow(
        title = stringResource(Res.string.morivia_hl_hero_video),
        description = stringResource(Res.string.morivia_hl_hero_video_sub),
        checked = layout.heroVideo,
        isTablet = isTablet,
        onCheckedChange = { value -> MoriviaLayoutSettingsRepository.update { it.copy(heroVideo = value) } },
    )
    SettingsGroupDivider(isTablet = isTablet)
    SettingsSwitchRow(
        title = stringResource(Res.string.morivia_hl_auto_rotate),
        description = stringResource(Res.string.morivia_hl_auto_rotate_sub),
        checked = layout.heroLayout != MoriviaHeroLayout.PINNED,
        isTablet = isTablet,
        onCheckedChange = { value ->
            MoriviaLayoutSettingsRepository.update {
                it.copy(heroLayout = if (value) MoriviaHeroLayout.ROLLING else MoriviaHeroLayout.PINNED)
            }
        },
    )
}

/**
 * The earlier Morivia app's Home & Layout page: one place for the hero, posters, Continue
 * Watching and rows. Settings that also exist elsewhere are shown here too and change the same values.
 */
internal fun LazyListScope.moriviaHomeLayoutContent(
    isTablet: Boolean,
    onHeroSourceClick: () -> Unit,
    onHomeRowsClick: () -> Unit,
    onCollectionsClick: () -> Unit,
) {
    item {
        val layout by rememberMoriviaLayout()
        val home by HomeCatalogSettingsRepository.uiState.collectAsStateWithLifecycle()
        SettingsSection(title = stringResource(Res.string.morivia_hl_section_hero), isTablet = isTablet) {
            SettingsGroup(isTablet = isTablet) {
                HeroPlaybackRows(layout = layout, isTablet = isTablet)
                SettingsGroupDivider(isTablet = isTablet)
                SettingsNavigationRow(
                    title = stringResource(Res.string.morivia_hl_hero_source),
                    description = stringResource(Res.string.morivia_hl_hero_source_sub),
                    isTablet = isTablet,
                    onClick = onHeroSourceClick,
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.morivia_hl_trailer_sound),
                    description = stringResource(Res.string.morivia_hl_trailer_sound_sub),
                    checked = layout.trailerSound,
                    isTablet = isTablet,
                    onCheckedChange = { value -> MoriviaLayoutSettingsRepository.update { it.copy(trailerSound = value) } },
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.morivia_hl_featured),
                    description = stringResource(Res.string.morivia_hl_featured_sub),
                    checked = home.heroEnabled,
                    isTablet = isTablet,
                    onCheckedChange = HomeCatalogSettingsRepository::setHeroEnabled,
                )
            }
        }
    }
    item {
        val layout by rememberMoriviaLayout()
        val home by HomeCatalogSettingsRepository.uiState.collectAsStateWithLifecycle()
        val posters by PosterCardStyleRepository.uiState.collectAsStateWithLifecycle()
        SettingsSection(title = stringResource(Res.string.morivia_hl_section_posters), isTablet = isTablet) {
            SettingsGroup(isTablet = isTablet) {
                SettingsSwitchRow(
                    title = stringResource(Res.string.morivia_hl_landscape_posters),
                    description = stringResource(Res.string.morivia_hl_landscape_posters_sub),
                    checked = posters.catalogLandscapeModeEnabled,
                    isTablet = isTablet,
                    onCheckedChange = PosterCardStyleRepository::setCatalogLandscapeModeEnabled,
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.morivia_hl_poster_labels),
                    description = stringResource(Res.string.morivia_hl_poster_labels_sub),
                    checked = !posters.hideLabelsEnabled,
                    isTablet = isTablet,
                    onCheckedChange = { show -> PosterCardStyleRepository.setHideLabelsEnabled(!show) },
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.morivia_hl_poster_banners),
                    description = stringResource(Res.string.morivia_hl_poster_banners_sub),
                    checked = layout.posterBanners,
                    isTablet = isTablet,
                    onCheckedChange = { value -> MoriviaLayoutSettingsRepository.update { it.copy(posterBanners = value) } },
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.morivia_hl_hide_unreleased),
                    description = stringResource(Res.string.morivia_hl_hide_unreleased_sub),
                    checked = home.hideUnreleasedContent,
                    isTablet = isTablet,
                    onCheckedChange = HomeCatalogSettingsRepository::setHideUnreleasedContent,
                )
            }
        }
    }
    item {
        val layout by rememberMoriviaLayout()
        val cw by remember {
            ContinueWatchingPreferencesRepository.ensureLoaded()
            ContinueWatchingPreferencesRepository.uiState
        }.collectAsStateWithLifecycle()
        SettingsSection(title = stringResource(Res.string.morivia_hl_section_continue_watching), isTablet = isTablet) {
            SettingsGroup(isTablet = isTablet) {
                SettingsChipRow(
                    title = stringResource(Res.string.morivia_hl_cw_sort),
                    description = stringResource(Res.string.morivia_hl_cw_sort_sub),
                    isTablet = isTablet,
                ) {
                    ContinueWatchingSortMode.entries.forEach { mode ->
                        Chip(
                            label = stringResource(
                                when (mode) {
                                    ContinueWatchingSortMode.DEFAULT -> Res.string.morivia_hl_cw_sort_recent
                                    ContinueWatchingSortMode.STREAMING_STYLE -> Res.string.morivia_hl_cw_sort_streaming
                                    ContinueWatchingSortMode.SPLIT_UPCOMING -> Res.string.settings_continue_watching_sort_mode_split_upcoming
                                },
                            ),
                            selected = cw.sortMode == mode,
                            onClick = { ContinueWatchingPreferencesRepository.setSortMode(mode) },
                        )
                    }
                }
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.morivia_hl_episode_thumbnails),
                    description = stringResource(Res.string.morivia_hl_episode_thumbnails_sub),
                    checked = cw.useEpisodeThumbnails,
                    isTablet = isTablet,
                    onCheckedChange = ContinueWatchingPreferencesRepository::setUseEpisodeThumbnails,
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.morivia_hl_next_up_furthest),
                    description = stringResource(Res.string.morivia_hl_next_up_furthest_sub),
                    checked = cw.upNextFromFurthestEpisode,
                    isTablet = isTablet,
                    onCheckedChange = ContinueWatchingPreferencesRepository::setUpNextFromFurthestEpisode,
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.morivia_hl_unaired_next_up),
                    description = stringResource(Res.string.morivia_hl_unaired_next_up_sub),
                    checked = cw.showUnairedNextUp,
                    isTablet = isTablet,
                    onCheckedChange = ContinueWatchingPreferencesRepository::setShowUnairedNextUp,
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.morivia_hl_hide_spoilers),
                    description = stringResource(Res.string.morivia_hl_hide_spoilers_sub),
                    checked = cw.blurNextUp,
                    isTablet = isTablet,
                    onCheckedChange = ContinueWatchingPreferencesRepository::setBlurNextUp,
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.morivia_hl_countdown),
                    description = stringResource(Res.string.morivia_hl_countdown_sub),
                    checked = layout.upcomingCountdown,
                    isTablet = isTablet,
                    onCheckedChange = { value -> MoriviaLayoutSettingsRepository.update { it.copy(upcomingCountdown = value) } },
                )
            }
        }
    }
    item {
        val layout by rememberMoriviaLayout()
        SettingsSection(title = stringResource(Res.string.morivia_hl_section_personalized), isTablet = isTablet) {
            SettingsGroup(isTablet = isTablet) {
                SettingsSwitchRow(
                    title = stringResource(Res.string.morivia_movies_by_genres),
                    description = stringResource(Res.string.morivia_movies_by_genres_sub),
                    checked = layout.moviesByGenres,
                    isTablet = isTablet,
                    onCheckedChange = { value -> MoriviaLayoutSettingsRepository.update { it.copy(moviesByGenres = value) } },
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.morivia_series_by_genres),
                    description = stringResource(Res.string.morivia_series_by_genres_sub),
                    checked = layout.seriesByGenres,
                    isTablet = isTablet,
                    onCheckedChange = { value -> MoriviaLayoutSettingsRepository.update { it.copy(seriesByGenres = value) } },
                )
            }
        }
    }
    item {
        val layout by rememberMoriviaLayout()
        SettingsSection(title = stringResource(Res.string.morivia_hl_section_rows), isTablet = isTablet) {
            SettingsGroup(isTablet = isTablet) {
                SettingsNavigationRow(
                    title = stringResource(Res.string.morivia_hl_home_rows),
                    description = stringResource(Res.string.morivia_hl_home_rows_sub),
                    isTablet = isTablet,
                    onClick = onHomeRowsClick,
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsNavigationRow(
                    title = stringResource(Res.string.morivia_hl_collections),
                    description = stringResource(Res.string.morivia_hl_collections_sub),
                    isTablet = isTablet,
                    onClick = onCollectionsClick,
                )
                SettingsGroupDivider(isTablet = isTablet)
                CollectionArtSettingsRow(isTablet = isTablet)
                SettingsGroupDivider(isTablet = isTablet)
                SettingsChipRow(
                    title = stringResource(Res.string.morivia_hl_auto_refresh),
                    description = stringResource(Res.string.morivia_hl_auto_refresh_sub),
                    isTablet = isTablet,
                ) {
                    listOf(0, 15, 30, 60).forEach { minutes ->
                        Chip(
                            label = if (minutes == 0) {
                                stringResource(Res.string.morivia_off)
                            } else {
                                stringResource(Res.string.morivia_hl_auto_refresh_minutes, minutes)
                            },
                            selected = layout.autoRefreshMinutes == minutes,
                            onClick = { MoriviaLayoutSettingsRepository.update { it.copy(autoRefreshMinutes = minutes) } },
                        )
                    }
                }
                SettingsGroupDivider(isTablet = isTablet)
                SettingsChipRow(
                    title = stringResource(Res.string.morivia_top_row),
                    description = stringResource(Res.string.morivia_top_row_sub),
                    isTablet = isTablet,
                ) {
                    listOf(10, 20, 30, 40, 50, 0).forEach { count ->
                        Chip(
                            label = if (count == 0) stringResource(Res.string.morivia_top_row_all) else count.toString(),
                            selected = layout.topRowCount == count,
                            onClick = { MoriviaLayoutSettingsRepository.update { it.copy(topRowCount = count) } },
                        )
                    }
                }
            }
        }
    }
}

/** Search settings: which parts of the Search page show before you type. */
internal fun LazyListScope.moriviaSearchSettingsContent(isTablet: Boolean) {
    item {
        val layout by rememberMoriviaLayout()
        SettingsSection(title = stringResource(Res.string.morivia_search_section), isTablet = isTablet) {
            SettingsGroup(isTablet = isTablet) {
                SettingsSwitchRow(
                    title = stringResource(Res.string.morivia_search_trending),
                    description = stringResource(Res.string.morivia_search_trending_sub),
                    checked = layout.searchTrending,
                    isTablet = isTablet,
                    onCheckedChange = { value -> MoriviaLayoutSettingsRepository.update { it.copy(searchTrending = value) } },
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.morivia_search_browse),
                    description = stringResource(Res.string.morivia_search_browse_sub),
                    checked = layout.searchBrowse,
                    isTablet = isTablet,
                    onCheckedChange = { value -> MoriviaLayoutSettingsRepository.update { it.copy(searchBrowse = value) } },
                )
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.morivia_search_history),
                    description = stringResource(Res.string.morivia_search_history_sub),
                    checked = layout.searchHistory,
                    isTablet = isTablet,
                    // The one Recent searches switch: it both saves and shows them (owner, 2026-11-26).
                    onCheckedChange = { value ->
                        MoriviaLayoutSettingsRepository.update { it.copy(searchHistory = value) }
                        com.morivia.app.features.search.SearchHistoryRepository.setEnabled(value)
                    },
                )
            }
        }
    }
}

/** Library settings: the section it opens on, the sort order and the watched marks. */
internal fun LazyListScope.moriviaLibrarySettingsContent(isTablet: Boolean) {
    item {
        val layout by rememberMoriviaLayout()
        SettingsSection(title = stringResource(Res.string.morivia_library_section), isTablet = isTablet) {
            SettingsGroup(isTablet = isTablet) {
                SettingsChipRow(
                    title = stringResource(Res.string.morivia_library_opens_on),
                    description = stringResource(Res.string.morivia_library_opens_on_sub),
                    isTablet = isTablet,
                ) {
                    MoriviaLibrarySection.entries.forEach { section ->
                        Chip(
                            label = stringResource(section.labelRes()),
                            selected = layout.librarySection == section,
                            onClick = { MoriviaLayoutSettingsRepository.update { it.copy(librarySection = section) } },
                        )
                    }
                }
                SettingsGroupDivider(isTablet = isTablet)
                SettingsChipRow(
                    title = stringResource(Res.string.morivia_library_sort),
                    description = stringResource(Res.string.morivia_library_sort_sub),
                    isTablet = isTablet,
                ) {
                    MoriviaLibrarySort.entries.forEach { sort ->
                        Chip(
                            label = stringResource(sort.labelRes()),
                            selected = layout.librarySort == sort,
                            onClick = { MoriviaLayoutSettingsRepository.update { it.copy(librarySort = sort) } },
                        )
                    }
                }
                SettingsGroupDivider(isTablet = isTablet)
                SettingsSwitchRow(
                    title = stringResource(Res.string.morivia_library_watched_marks),
                    description = stringResource(Res.string.morivia_library_watched_marks_sub),
                    checked = layout.libraryWatchedMarks,
                    isTablet = isTablet,
                    onCheckedChange = { value -> MoriviaLayoutSettingsRepository.update { it.copy(libraryWatchedMarks = value) } },
                )
            }
        }
    }
}

internal fun MoriviaLibrarySection.labelRes() = when (this) {
    MoriviaLibrarySection.RECENTLY_ADDED -> Res.string.morivia_library_recently_added
    MoriviaLibrarySection.MOVIES -> Res.string.morivia_library_movies
    MoriviaLibrarySection.SHOWS -> Res.string.morivia_library_shows
    MoriviaLibrarySection.CONTINUE_WATCHING -> Res.string.morivia_library_continue_watching
}

internal fun MoriviaLibrarySort.labelRes() = when (this) {
    MoriviaLibrarySort.ADDED -> Res.string.morivia_library_sort_added
    MoriviaLibrarySort.NAME -> Res.string.morivia_library_sort_name
    MoriviaLibrarySort.RECENTLY_WATCHED -> Res.string.morivia_library_sort_recently_watched
}

@Composable
private fun rememberMoriviaLayout() = remember {
    MoriviaLayoutSettingsRepository.ensureLoaded()
    MoriviaLayoutSettingsRepository.settings
}.collectAsStateWithLifecycle()
