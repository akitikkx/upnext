package com.theupnextapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "simkl_access")
data class DatabaseSimklAccess(
    @PrimaryKey
    val accessToken: String,
    val tokenType: String?,
    val scope: String?
)
