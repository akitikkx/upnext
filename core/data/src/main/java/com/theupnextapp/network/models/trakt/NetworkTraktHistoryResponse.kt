package com.theupnextapp.network.models.trakt

import com.squareup.moshi.Json

data class NetworkTraktHistoryResponse(
    val id: Long?,
    @Json(name = "watched_at")
    val watchedAt: String?,
    val action: String?,
    val type: String?,
    val show: NetworkTraktWatchedShowInfo?,
    val episode: NetworkTraktWatchedEpisode?,
)
