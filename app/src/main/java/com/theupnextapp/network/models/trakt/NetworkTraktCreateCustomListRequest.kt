package com.theupnextapp.network.models.trakt

data class NetworkTraktCreateCustomListRequest(
    val allow_comments: Boolean,
    val description: String,
    val display_numbers: Boolean,
    val name: String,
    val privacy: String,
    val sort_by: String,
    val sort_how: String
)

data class NetworkTraktCreateCustomListRequestIds(
    val slug: String,
    val trakt: Int
)