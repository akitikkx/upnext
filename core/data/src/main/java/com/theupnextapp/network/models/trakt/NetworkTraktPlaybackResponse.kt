package com.theupnextapp.network.models.trakt

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class NetworkTraktPlaybackResponse(
    val progress: Float?,
    val action: String?,
    val type: String?,
    val show: NetworkTraktWatchedShowInfo?,
    val episode: NetworkTraktWatchedEpisode?,
)
