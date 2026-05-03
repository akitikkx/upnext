package com.theupnextapp.network.models.simkl

import com.google.gson.annotations.SerializedName

data class NetworkSimklAccessTokenRequest(
    @SerializedName("code")
    val code: String,
    @SerializedName("client_id")
    val clientId: String,
    @SerializedName("client_secret")
    val clientSecret: String,
    @SerializedName("redirect_uri")
    val redirectUri: String,
    @SerializedName("grant_type")
    val grantType: String
)
