package com.theupnextapp.network.models.trakt

data class NetworkTraktCreateCustomListRequest(
    val allow_comments: Boolean = false,
    val description: String = "Your list of favorites on the Upnext: TV Series Manager app",
    val display_numbers: Boolean = false,
    val name: String,
    val privacy: String = "private",
    val sort_by: String = "rank",
    val sort_how: String = "asc"
)