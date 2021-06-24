package com.theupnextapp.network.models.tvmaze

data class NetworkTvMazeShowLookupNetwork(
    val country: NetworkTvMazeShowLookupCountry,
    val id: Int,
    val name: String
)