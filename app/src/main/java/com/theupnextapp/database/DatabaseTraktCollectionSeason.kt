package com.theupnextapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trakt_collection_seasons")
data class DatabaseTraktCollectionSeason(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var imdbID: String?,
    val seasonNumber: Int?
)