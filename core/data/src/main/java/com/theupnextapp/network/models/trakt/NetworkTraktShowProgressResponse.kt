package com.theupnextapp.network.models.trakt

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class NetworkTraktShowProgressResponse(
    val aired: Int?,
    val completed: Int?,
    @Json(name = "last_watched_at")
    val lastWatchedAt: String?,
    @Json(name = "next_episode")
    val nextEpisode: NetworkTraktWatchedEpisode?,
    @Json(name = "last_episode")
    val lastEpisode: NetworkTraktWatchedEpisode?,
)
