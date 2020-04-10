package com.theupnextapp.network

data class NetworkTraktAccessTokenRequest(
    val code: String?,
    val client_id: String?,
    val client_secret: String?,
    val redirect_uri: String?,
    val grant_type: String?
)