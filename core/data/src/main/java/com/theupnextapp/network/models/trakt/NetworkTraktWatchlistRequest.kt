package com.theupnextapp.network.models.trakt

import com.google.gson.annotations.SerializedName

data class NetworkTraktWatchlistRequest(
    @SerializedName("shows")
    val shows: List<NetworkTraktWatchlistRequestShow>
)

data class NetworkTraktWatchlistRequestShow(
    @SerializedName("ids")
    val ids: NetworkTraktWatchlistRequestShowIds
)

data class NetworkTraktWatchlistRequestShowIds(
    @SerializedName("trakt")
    val trakt: Int
)
