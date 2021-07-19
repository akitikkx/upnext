package com.theupnextapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.theupnextapp.domain.TraktAccessToken

@Entity(tableName = "trakt_access")
data class DatabaseTraktAccess(
    @PrimaryKey
    val id: Int,
    val access_token: String?,
    val created_at: Long?,
    val expires_in: Long?,
    val refresh_token: String?,
    val scope: String?,
    val token_type: String?
)

fun DatabaseTraktAccess.asDomainModel(): TraktAccessToken {
    return TraktAccessToken(
        access_token = access_token,
        token_type = token_type,
        expires_in = expires_in,
        refresh_token = refresh_token,
        scope = scope,
        created_at = created_at
    )
}