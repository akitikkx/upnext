package com.theupnextapp.network.models.trakt

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class NetworkTraktShowProgressResponse(
    val aired: Int?,
    val completed: Int?,
    @SerializedName("last_watched_at")
    val lastWatchedAt: String?,
    @SerializedName("next_episode")
    val nextEpisode: NetworkTraktWatchedEpisode?,
    @SerializedName("last_episode")
    val lastEpisode: NetworkTraktWatchedEpisode?,
)
