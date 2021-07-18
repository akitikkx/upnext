package com.theupnextapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.theupnextapp.domain.TraktUserListItem

@Entity(tableName = "favorite_shows")
data class DatabaseFavoriteShows(
    @PrimaryKey
    val id: Int?,
    val title: String?,
    val year: String?,
    val mediumImageUrl: String?,
    val originalImageUrl: String?,
    val imdbID: String?,
    val slug: String?,
    val tmdbID: Int?,
    val traktID: Int?,
    val tvdbID: Int?,
    val tvMazeID: Int?
)

fun List<DatabaseFavoriteShows>.asDomainModel(): List<TraktUserListItem> {
    return map {
        TraktUserListItem(
            id = it.id,
            title = it.title,
            slug = it.slug,
            year = it.year,
            mediumImageUrl = it.mediumImageUrl,
            originalImageUrl = it.originalImageUrl,
            imdbID = it.imdbID,
            tmdbID = it.tmdbID,
            traktID = it.traktID,
            tvdbID = it.tvdbID,
            tvMazeID = it.tvMazeID
        )
    }
}

fun DatabaseFavoriteShows.asDomainModel(): TraktUserListItem {
    return TraktUserListItem(
        id = id,
        title = title,
        slug = slug,
        year = year,
        mediumImageUrl = mediumImageUrl,
        originalImageUrl = originalImageUrl,
        imdbID = imdbID,
        tmdbID = tmdbID,
        traktID = traktID,
        tvdbID = tvdbID,
        tvMazeID = tvMazeID
    )
}