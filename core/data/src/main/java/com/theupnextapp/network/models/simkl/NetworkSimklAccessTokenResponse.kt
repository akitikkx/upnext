package com.theupnextapp.network.models.simkl

import com.google.gson.annotations.SerializedName
import com.theupnextapp.database.DatabaseSimklAccess
import com.theupnextapp.domain.SimklAccessToken

data class NetworkSimklAccessTokenResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("token_type")
    val tokenType: String?,
    @SerializedName("scope")
    val scope: String?
)

fun NetworkSimklAccessTokenResponse.asDatabaseModel(): DatabaseSimklAccess {
    return DatabaseSimklAccess(
        accessToken = accessToken,
        tokenType = tokenType,
        scope = scope
    )
}

fun NetworkSimklAccessTokenResponse.asDomainModel(): SimklAccessToken {
    return SimklAccessToken(
        accessToken = accessToken,
        tokenType = tokenType,
        scope = scope
    )
}
