package com.theupnextapp.domain

import java.util.concurrent.TimeUnit

data class TraktAccessToken(
    val access_token: String?,
    val created_at: Long?,
    val expires_in: Long?,
    val refresh_token: String?,
    val scope: String?,
    val token_type: String?
)

fun TraktAccessToken.areVariablesEmpty(): Boolean {
    return (access_token.isNullOrEmpty() || created_at == null || expires_in == null || refresh_token.isNullOrEmpty() || scope.isNullOrEmpty() || token_type.isNullOrEmpty())
}

/**
 * Check if the Trakt access token is still valid
 */
fun TraktAccessToken.isTraktAccessTokenValid(): Boolean {
    var isValid = false
    val currentDateEpoch = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
    val expiryDateEpoch =
        expires_in?.let { created_at?.plus(it) }

    if (expiryDateEpoch != null) {
        if (expiryDateEpoch > currentDateEpoch) {
            isValid = true
        }
    }
    return isValid
}
