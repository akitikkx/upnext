package com.theupnextapp.network.models.trakt

data class NetworkTraktAccessRefreshTokenRequest(
    val refresh_token: String?,
    val client_id: String?,
    val client_secret: String?,
    val redirect_uri: String?,
    val grant_type: String?
)