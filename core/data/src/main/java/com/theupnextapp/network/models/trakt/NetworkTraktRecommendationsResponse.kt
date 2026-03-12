package com.theupnextapp.network.models.trakt

import com.google.gson.annotations.SerializedName

class NetworkTraktRecommendationsResponse : ArrayList<NetworkTraktRecommendationsResponseItem>()

data class NetworkTraktRecommendationsResponseItem(
    val show: NetworkTraktRecommendationsResponseItemShow?
)

data class NetworkTraktRecommendationsResponseItemShow(
    val title: String?,
    val year: Int?,
    val ids: NetworkTraktRecommendationsResponseItemIds?
)

data class NetworkTraktRecommendationsResponseItemIds(
    val trakt: Int?,
    val slug: String?,
    val tvdb: Int?,
    val imdb: String?,
    val tmdb: Int?,
    val tvmaze: Int?
)
