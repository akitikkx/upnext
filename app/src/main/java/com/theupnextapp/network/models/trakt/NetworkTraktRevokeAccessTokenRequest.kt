package com.theupnextapp.network.models.trakt

data class NetworkTraktRevokeAccessTokenRequest(
    val client_id: String,
    val client_secret: String,
    val token: String
)