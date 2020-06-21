package com.theupnextapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trakt_collection_episodes")
data class DatabaseTraktCollectionEpisode(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var imdbID: String?,
    val seasonNumber: Int?,
    val episodeNumber: Int?,
    val collectedAt: String?
)