package com.theupnextapp.network.models.trakt

import androidx.annotation.Keep
@Keep
data class NetworkTraktPlaybackResponse(
    val progress: Float?,
    val action: String?,
    val type: String?,
    val show: NetworkTraktWatchedShowInfo?,
    val episode: NetworkTraktWatchedEpisode?,
)
