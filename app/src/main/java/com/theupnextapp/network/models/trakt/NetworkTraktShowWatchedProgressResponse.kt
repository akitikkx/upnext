package com.theupnextapp.network.models.trakt

import com.theupnextapp.domain.TraktShowWatchedProgress
import com.theupnextapp.domain.TraktWatchedShowProgressLastEpisode
import com.theupnextapp.domain.TraktWatchedShowProgressNextEpisode
import com.theupnextapp.domain.TraktWatchedShowProgressSeason

data class NetworkTraktShowWatchedProgressResponse(
    val aired: Int?,
    val completed: Int?,
    val hidden_seasons: List<TraktWatchedShowProgressHiddenSeason>?,
    val last_episode: TraktWatchedShowProgressLastEpisode?,
    val last_watched_at: String?,
    val next_episode: TraktWatchedShowProgressNextEpisode?,
    val reset_at: Any?,
    val seasons: List<TraktWatchedShowProgressSeason>?
)

data class TraktWatchedShowProgressHiddenSeason(
    val ids: NetworkTraktWatchedShowProgressIds?,
    val number: Int?
)

data class NetworkTraktWatchedShowProgressSeason(
    val aired: Int?,
    val completed: Int?,
    val episodes: List<NetworkTraktWatchedShowProgressSeasonEpisode?>,
    val number: Int?
)

data class NetworkTraktWatchedShowProgressIds(
    val tmdb: Int?,
    val trakt: Int?,
    val tvdb: Int?
)

data class NetworkTraktWatchedShowProgressSeasonEpisode(
    val completed: Boolean?,
    val last_watched_at: String?,
    val number: Int?
)

fun NetworkTraktShowWatchedProgressResponse.asDomainModel() : TraktShowWatchedProgress {
    return TraktShowWatchedProgress(
        episodesAired = aired,
        episodesWatched = completed,
        lastWatchedAt = last_watched_at,
        nextEpisodeToWatch = next_episode,
        previousEpisodeWatched = last_episode,
        seasons = seasons
    )
}