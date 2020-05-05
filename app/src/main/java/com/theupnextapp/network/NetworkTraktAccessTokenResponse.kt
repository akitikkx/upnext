package com.theupnextapp.network

import com.theupnextapp.domain.TraktAccessToken

data class TraktAccessTokenResponse(
    val access_token: String?,
    val created_at: Int?,
    val expires_in: Int?,
    val refresh_token: String?,
    val scope: String?,
    val token_type: String?
)

fun TraktAccessTokenResponse.asDomainModel(): TraktAccessToken {
    return TraktAccessToken(
        access_token = access_token,
        created_at = created_at,
        expires_in = expires_in,
        refresh_token = refresh_token,
        scope = scope,
        token_type = token_type
    )
}