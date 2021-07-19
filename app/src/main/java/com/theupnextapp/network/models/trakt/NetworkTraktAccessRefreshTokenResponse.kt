package com.theupnextapp.network.models.trakt

import com.theupnextapp.database.DatabaseTraktAccess

data class NetworkTraktAccessRefreshTokenResponse(
    val access_token: String?,
    val token_type: String?,
    val expires_in: Long?,
    val refresh_token: String?,
    val scope: String?,
    val created_at: Long?
)

fun NetworkTraktAccessRefreshTokenResponse.asDatabaseModel(): DatabaseTraktAccess {
    return DatabaseTraktAccess(
        id = 1,
        access_token = access_token,
        token_type = token_type,
        expires_in = expires_in,
        refresh_token = refresh_token,
        scope = scope,
        created_at = created_at
    )
}
