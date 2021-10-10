package com.theupnextapp.domain

data class TraktCheckInStatus(
    val season: Int? = null,
    val episode: Int? = null,
    val checkInTime: String? = null,
    val message: String? = null
)