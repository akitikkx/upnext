package com.theupnextapp.domain

data class TraktWatchProviders(
    val link: String?,
    val flatrate: List<TraktWatchProvider>?,
    val rent: List<TraktWatchProvider>?,
    val buy: List<TraktWatchProvider>?,
    val free: List<TraktWatchProvider>?
)

data class TraktWatchProvider(
    val name: String?,
    val id: Int?,
    val logoPath: String?
)
