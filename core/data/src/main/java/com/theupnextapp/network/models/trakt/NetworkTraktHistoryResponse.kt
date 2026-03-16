package com.theupnextapp.network.models.trakt

import com.google.gson.annotations.SerializedName

data class NetworkTraktHistoryResponse(
    val id: Long?,
    @SerializedName("watched_at")
    val watchedAt: String?,
    val action: String?,
    val type: String?,
    val show: NetworkTraktWatchedShowInfo?,
    val episode: NetworkTraktWatchedEpisode?,
)
