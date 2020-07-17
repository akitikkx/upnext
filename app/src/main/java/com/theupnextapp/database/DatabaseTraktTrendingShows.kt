package com.theupnextapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.theupnextapp.domain.TraktTrendingShows

@Entity(tableName = "trakt_trending")
data class DatabaseTraktTrendingShows(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String?,
    val year: String?,
    val medium_image_url: String?,
    val original_image_url: String?,
    val imdbID: String?,
    val slug: String?,
    val tmdbID: Int?,
    val traktID: Int?,
    val tvdbID: Int?,
    val tvMazeID: Int?
)

fun List<DatabaseTraktTrendingShows>.asDomainModel() : List<TraktTrendingShows> {
    return map {
        TraktTrendingShows(
            title = it.title,
            year = it.year,
            mediumImageUrl = it.medium_image_url,
            originalImageUrl = it.original_image_url,
            imdbID = it.imdbID,
            slug = it.slug,
            tmdbID = it.tmdbID,
            traktID = it.traktID,
            tvdbID = it.tvdbID,
            tvMazeID = it.tvMazeID
        )
    }
}