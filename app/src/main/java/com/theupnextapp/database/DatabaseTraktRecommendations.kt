package com.theupnextapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.theupnextapp.domain.TraktRecommendations

@Entity(tableName = "trakt_recommendations")
data class DatabaseTraktRecommendations(
    @PrimaryKey
    val id: Int?,
    val title: String?,
    val year: Int?,
    val mediumImageUrl: String?,
    val originalImageUrl: String?,
    val imdbID: String?,
    val slug: String?,
    val tmdbID: Int?,
    val traktID: Int?,
    val tvdbID: Int?,
    val tvMazeID: Int?
)

fun List<DatabaseTraktRecommendations>.asDomainModel(): List<TraktRecommendations> {
    return map {
        TraktRecommendations(
            id = it.id,
            title = it.title,
            year = it.year,
            mediumImageUrl = it.mediumImageUrl,
            originalImageUrl = it.originalImageUrl,
            imdbID = it.imdbID,
            slug = it.slug,
            tmdbID = it.tmdbID,
            traktID = it.traktID,
            tvdbID = it.tvdbID,
            tvMazeID = it.tvMazeID
        )
    }
}