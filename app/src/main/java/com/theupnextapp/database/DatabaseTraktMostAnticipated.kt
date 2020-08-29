package com.theupnextapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.theupnextapp.domain.TraktMostAnticipated

@Entity(tableName = "trakt_most_anticipated")
data class DatabaseTraktMostAnticipated(
    @PrimaryKey
    val id: Int?,
    val title: String?,
    val year: String?,
    val medium_image_url: String?,
    val original_image_url: String?,
    val imdbID: String?,
    val slug: String?,
    val tmdbID: Int?,
    val traktID: Int?,
    val tvdbID: Int?,
    val tvMazeID: Int?,
    val list_count: Int?
)

fun List<DatabaseTraktMostAnticipated>.asDomainModel() : List<TraktMostAnticipated> {
    return map {
        TraktMostAnticipated(
            id = it.traktID,
            title = it.title,
            year = it.year,
            mediumImageUrl = it.medium_image_url,
            originalImageUrl = it.original_image_url,
            imdbID = it.imdbID,
            slug = it.slug,
            tmdbID = it.tmdbID,
            traktID = it.traktID,
            tvdbID = it.tvdbID,
            tvMazeID = it.tvMazeID,
            listCount = it.list_count
        )
    }
}