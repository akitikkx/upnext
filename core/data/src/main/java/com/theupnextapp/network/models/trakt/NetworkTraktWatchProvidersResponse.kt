package com.theupnextapp.network.models.trakt

data class NetworkTraktWatchProvidersResponse(
    val link: String?,
    val flatrate: List<NetworkTraktWatchProvider>?,
    val rent: List<NetworkTraktWatchProvider>?,
    val buy: List<NetworkTraktWatchProvider>?,
    val free: List<NetworkTraktWatchProvider>?
)

data class NetworkTraktWatchProvider(
    val name: String?,
    val id: Int?,
    val logo_path: String?
)
