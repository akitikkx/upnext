package com.theupnextapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_next_episodes")
data class DatabaseFavoriteNextEpisode(
    @PrimaryKey
    val tvMazeID: Int?,
    val number: Int?,
    val season: Int?,
    val title: String?,
    val airStamp: String?,
    val mediumImageUrl: String?,
    val originalImageUrl: String?,
    val imdb: String?
)