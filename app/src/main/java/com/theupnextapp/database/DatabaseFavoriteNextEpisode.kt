package com.theupnextapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.theupnextapp.domain.FavoriteNextEpisode

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

fun List<DatabaseFavoriteNextEpisode>.asDomainModel(): List<FavoriteNextEpisode> {
    return map {
        FavoriteNextEpisode(
            tvMazeID = it.tvMazeID,
            number = it.number,
            season = it.season,
            title = it.title,
            airStamp = it.airStamp,
            mediumImageUrl = it.mediumImageUrl,
            originalImageUrl = it.originalImageUrl,
            imdb = it.imdb
        )
    }
}