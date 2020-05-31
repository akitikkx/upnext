package com.theupnextapp.network

import com.theupnextapp.domain.TraktAccessToken

data class NetworkTraktAccessRefreshTokenResponse(
    val access_token: String?,
    val token_type: String?,
    val expires_in: Int?,
    val refresh_token: String?,
    val scope: String?,
    val created_at: Int?
)

fun NetworkTraktAccessRefreshTokenResponse.asDomainModel(): TraktAccessToken {
    return TraktAccessToken(
        access_token = access_token,
        token_type = token_type,
        expires_in = expires_in,
        refresh_token = refresh_token,
        scope = scope,
        created_at = created_at
    )
}