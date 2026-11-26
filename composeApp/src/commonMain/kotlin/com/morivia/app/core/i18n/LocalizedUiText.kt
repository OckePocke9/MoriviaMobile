package com.morivia.app.core.i18n

import kotlinx.coroutines.runBlocking
import morivia.composeapp.generated.resources.player_picture_in_picture
import morivia.composeapp.generated.resources.Res
import morivia.composeapp.generated.resources.action_play
import morivia.composeapp.generated.resources.action_play_episode
import morivia.composeapp.generated.resources.action_resume
import morivia.composeapp.generated.resources.action_resume_episode
import morivia.composeapp.generated.resources.compose_player_episode_code_episode_only
import morivia.composeapp.generated.resources.compose_player_episode_code_full
import morivia.composeapp.generated.resources.streams_episode_fallback_title
import morivia.composeapp.generated.resources.compose_player_no_subtitle_lines_found
import morivia.composeapp.generated.resources.compose_player_subtitle_lines_load_error
import morivia.composeapp.generated.resources.continue_watching_up_next
import morivia.composeapp.generated.resources.continue_watching_up_next_episode
import morivia.composeapp.generated.resources.date_month_april
import morivia.composeapp.generated.resources.date_month_august
import morivia.composeapp.generated.resources.date_month_december
import morivia.composeapp.generated.resources.date_month_february
import morivia.composeapp.generated.resources.date_month_january
import morivia.composeapp.generated.resources.date_month_july
import morivia.composeapp.generated.resources.date_month_june
import morivia.composeapp.generated.resources.date_month_march
import morivia.composeapp.generated.resources.date_month_may
import morivia.composeapp.generated.resources.date_month_november
import morivia.composeapp.generated.resources.date_month_october
import morivia.composeapp.generated.resources.date_month_september
import morivia.composeapp.generated.resources.date_month_short_apr
import morivia.composeapp.generated.resources.date_month_short_aug
import morivia.composeapp.generated.resources.date_month_short_dec
import morivia.composeapp.generated.resources.date_month_short_feb
import morivia.composeapp.generated.resources.date_month_short_jan
import morivia.composeapp.generated.resources.date_month_short_jul
import morivia.composeapp.generated.resources.date_month_short_jun
import morivia.composeapp.generated.resources.date_month_short_mar
import morivia.composeapp.generated.resources.date_month_short_may
import morivia.composeapp.generated.resources.date_month_short_nov
import morivia.composeapp.generated.resources.date_month_short_oct
import morivia.composeapp.generated.resources.date_month_short_sep
import morivia.composeapp.generated.resources.media_anime
import morivia.composeapp.generated.resources.media_channels
import morivia.composeapp.generated.resources.media_movie
import morivia.composeapp.generated.resources.media_movies
import morivia.composeapp.generated.resources.media_series
import morivia.composeapp.generated.resources.media_tv
import morivia.composeapp.generated.resources.p2p_error_unknown
import morivia.composeapp.generated.resources.settings_stream_badge_enter_url
import morivia.composeapp.generated.resources.settings_stream_badge_import_failed
import morivia.composeapp.generated.resources.settings_stream_badge_import_limit
import morivia.composeapp.generated.resources.settings_stream_badge_url_scheme_invalid
import morivia.composeapp.generated.resources.unit_bytes_b
import morivia.composeapp.generated.resources.unit_bytes_gb
import morivia.composeapp.generated.resources.unit_bytes_kb
import morivia.composeapp.generated.resources.unit_bytes_mb
import org.jetbrains.compose.resources.getString

fun localizedMediaTypeLabel(type: String): String {
    val fallback = type.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    return when (type.trim().lowercase()) {
        "movie" -> resourceString("Movies") { getString(Res.string.media_movies) }
        "series" -> resourceString("Series") { getString(Res.string.media_series) }
        "anime" -> resourceString("Anime") { getString(Res.string.media_anime) }
        "channel" -> resourceString("Channels") { getString(Res.string.media_channels) }
        "tv" -> resourceString("TV") { getString(Res.string.media_tv) }
        else -> fallback
    }
}

fun localizedMovieTypeLabel(): String = resourceString("Movie") { getString(Res.string.media_movie) }


private val genericEpisodeTitleRegex = Regex("""(?i)^Episode\s+(\d+)$""")

/** Same idea as NuvioTV localizeEpisodeTitle: "Episode 12" -> localized word + number. */
fun localizedEpisodeTitle(episodeTitle: String?, episodeNumber: Int? = null): String? {
    val title = episodeTitle?.trim()?.takeIf { it.isNotBlank() } ?: return null
    val match = genericEpisodeTitleRegex.matchEntire(title) ?: return title
    val n = match.groupValues[1].toIntOrNull() ?: episodeNumber ?: return title
    return resourceString("Episode $n") {
        "${getString(Res.string.streams_episode_fallback_title)} $n"
    }
}

fun localizedSeasonEpisodeCode(seasonNumber: Int?, episodeNumber: Int?): String? =
    when {
        seasonNumber != null && episodeNumber != null ->
            resourceString("S${seasonNumber} E${episodeNumber}") {
                getString(Res.string.compose_player_episode_code_full, seasonNumber, episodeNumber)
            }
        episodeNumber != null ->
            resourceString("E${episodeNumber}") {
                getString(Res.string.compose_player_episode_code_episode_only, episodeNumber)
            }
        else -> null
    }

fun localizedPlayLabel(seasonNumber: Int?, episodeNumber: Int?): String {
    val episodeCode = localizedSeasonEpisodeCode(seasonNumber, episodeNumber)
    return if (episodeCode != null) {
        resourceString("Play $episodeCode") { getString(Res.string.action_play_episode, episodeCode) }
    } else {
        resourceString("Play") { getString(Res.string.action_play) }
    }
}

fun localizedResumeLabel(seasonNumber: Int?, episodeNumber: Int?): String {
    val episodeCode = localizedSeasonEpisodeCode(seasonNumber, episodeNumber)
    return if (episodeCode != null) {
        resourceString("Resume $episodeCode") { getString(Res.string.action_resume_episode, episodeCode) }
    } else {
        resourceString("Resume") { getString(Res.string.action_resume) }
    }
}

fun localizedUpNextLabel(seasonNumber: Int?, episodeNumber: Int?): String =
    if (seasonNumber != null && episodeNumber != null) {
        resourceString("Next Up • S${seasonNumber} E${episodeNumber}") {
            getString(Res.string.continue_watching_up_next_episode, seasonNumber, episodeNumber)
        }
    } else {
        resourceString("Next Up") { getString(Res.string.continue_watching_up_next) }
    }

fun localizedMonthName(month: Int): String =
    when (month) {
        1 -> resourceString("January") { getString(Res.string.date_month_january) }
        2 -> resourceString("February") { getString(Res.string.date_month_february) }
        3 -> resourceString("March") { getString(Res.string.date_month_march) }
        4 -> resourceString("April") { getString(Res.string.date_month_april) }
        5 -> resourceString("May") { getString(Res.string.date_month_may) }
        6 -> resourceString("June") { getString(Res.string.date_month_june) }
        7 -> resourceString("July") { getString(Res.string.date_month_july) }
        8 -> resourceString("August") { getString(Res.string.date_month_august) }
        9 -> resourceString("September") { getString(Res.string.date_month_september) }
        10 -> resourceString("October") { getString(Res.string.date_month_october) }
        11 -> resourceString("November") { getString(Res.string.date_month_november) }
        12 -> resourceString("December") { getString(Res.string.date_month_december) }
        else -> month.toString()
    }

fun localizedShortMonthName(month: Int): String =
    when (month) {
        1 -> resourceString("Jan") { getString(Res.string.date_month_short_jan) }
        2 -> resourceString("Feb") { getString(Res.string.date_month_short_feb) }
        3 -> resourceString("Mar") { getString(Res.string.date_month_short_mar) }
        4 -> resourceString("Apr") { getString(Res.string.date_month_short_apr) }
        5 -> resourceString("May") { getString(Res.string.date_month_short_may) }
        6 -> resourceString("Jun") { getString(Res.string.date_month_short_jun) }
        7 -> resourceString("Jul") { getString(Res.string.date_month_short_jul) }
        8 -> resourceString("Aug") { getString(Res.string.date_month_short_aug) }
        9 -> resourceString("Sep") { getString(Res.string.date_month_short_sep) }
        10 -> resourceString("Oct") { getString(Res.string.date_month_short_oct) }
        11 -> resourceString("Nov") { getString(Res.string.date_month_short_nov) }
        12 -> resourceString("Dec") { getString(Res.string.date_month_short_dec) }
        else -> month.toString()
    }

fun localizedNoSubtitleLinesFound(): String =
    resourceString("No subtitle lines found") { getString(Res.string.compose_player_no_subtitle_lines_found) }

fun localizedSubtitleLinesLoadError(): String =
    resourceString("Unable to load subtitle lines") { getString(Res.string.compose_player_subtitle_lines_load_error) }

fun localizedBadgeImportFailed(): String =
    resourceString("Badge import failed.") { getString(Res.string.settings_stream_badge_import_failed) }

fun localizedBadgeEnterUrl(): String =
    resourceString("Enter a badge JSON URL.") { getString(Res.string.settings_stream_badge_enter_url) }

fun localizedBadgeUrlSchemeInvalid(): String =
    resourceString("Badge URL must start with http:// or https://.") { getString(Res.string.settings_stream_badge_url_scheme_invalid) }

fun localizedBadgeImportLimit(limit: Int): String =
    resourceString("You can import up to $limit badge URLs.") { getString(Res.string.settings_stream_badge_import_limit, limit) }

fun localizedP2pUnknownTorrentError(): String =
    resourceString("Unknown torrent error") { getString(Res.string.p2p_error_unknown) }

fun localizedByteUnit(unit: String): String =
    when (unit) {
        "GB" -> resourceString("GB") { getString(Res.string.unit_bytes_gb) }
        "MB" -> resourceString("MB") { getString(Res.string.unit_bytes_mb) }
        "KB" -> resourceString("KB") { getString(Res.string.unit_bytes_kb) }
        else -> resourceString("B") { getString(Res.string.unit_bytes_b) }
    }

fun localizedPlayerPictureInPicture(): String =
    resourceString("Morivia is playing in Picture in Picture mode") { getString(Res.string.player_picture_in_picture) }

private fun resourceString(
    fallback: String,
    provider: suspend () -> String,
): String = runCatching {
    runBlocking { provider() }
}.getOrDefault(fallback)
