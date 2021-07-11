package com.theupnextapp.domain

import com.theupnextapp.repository.datastore.UpnextDataStoreManager

data class TraktAccessToken(
    val access_token: String?,
    val created_at: Long?,
    val expires_in: Long?,
    val refresh_token: String?,
    val scope: String?,
    val token_type: String?
)

fun TraktAccessToken.areVariablesEmpty(): Boolean {
    return (access_token.isNullOrEmpty() || created_at == UpnextDataStoreManager.NOT_FOUND || expires_in == UpnextDataStoreManager.NOT_FOUND || refresh_token.isNullOrEmpty() || scope.isNullOrEmpty() || token_type.isNullOrEmpty())
}