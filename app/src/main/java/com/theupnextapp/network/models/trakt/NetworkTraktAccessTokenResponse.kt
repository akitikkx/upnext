package com.theupnextapp.network.models.trakt

import com.theupnextapp.database.DatabaseTraktAccess

data class NetworkTraktAccessTokenResponse(
    val access_token: String?,
    val created_at: Long?,
    val expires_in: Long?,
    val refresh_token: String?,
    val scope: String?,
    val token_type: String?
)

fun NetworkTraktAccessTokenResponse.asDatabaseModel(): DatabaseTraktAccess {
    return DatabaseTraktAccess(
        id = 1,
        access_token = access_token,
        created_at = created_at,
        expires_in = expires_in,
        refresh_token = refresh_token,
        scope = scope,
        token_type = token_type
    )
}
