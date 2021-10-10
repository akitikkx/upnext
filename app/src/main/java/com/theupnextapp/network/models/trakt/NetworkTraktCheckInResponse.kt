package com.theupnextapp.network.models.trakt

import com.theupnextapp.common.utils.DateUtils
import com.theupnextapp.domain.TraktCheckInStatus

data class NetworkTraktCheckInResponse(
    val episode: NetworkTraktCheckInResponseEpisode?,
    val id: Long?,
    val sharing: NetworkTraktCheckInResponseSharing?,
    val show: NetworkTraktCheckInResponseShow?,
    val watched_at: String?
)

data class NetworkTraktCheckInResponseEpisode(
    val ids: NetworkTraktCheckInResponseEpisodeIds?,
    val number: Int?,
    val season: Int?,
    val title: String?
)

data class NetworkTraktCheckInResponseEpisodeIds(
    val imdb: Any?,
    val tmdb: Any?,
    val trakt: Int?,
    val tvdb: Int?
)

data class NetworkTraktCheckInResponseShow(
    val ids: NetworkTraktCheckInResponseShowIds?,
    val title: String?,
    val year: Int?
)

data class NetworkTraktCheckInResponseShowIds(
    val imdb: String?,
    val slug: String?,
    val tmdb: Int?,
    val trakt: Int?,
    val tvdb: Int?
)

data class NetworkTraktCheckInResponseSharing(
    val tumblr: Boolean?,
    val twitter: Boolean?
)

fun NetworkTraktCheckInResponse.asDomainModel(): TraktCheckInStatus {
    return TraktCheckInStatus(
        season = episode?.season,
        episode = episode?.number,
        checkInTime = watched_at?.let { DateUtils.getDisplayDateFromDateStamp(it).toString() },
        message = null
    )
}