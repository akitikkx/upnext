package com.theupnextapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.theupnextapp.domain.TraktWatchlist

@Entity(tableName = "trakt_watchlist")
data class DatabaseTraktWatchlist constructor(
    @PrimaryKey
    val id: Int,
    val listed_at: String?,
    val rank: Int?,
    val title: String?,
    val mediumImageUrl: String?,
    val originalImageUrl: String?,
    val imdbID: String?,
    val slug: String?,
    val tmdbID: Int?,
    val traktID: Int?,
    val tvdbID: Int?,
    val tvrageID: Int?,
    val tvMazeID: Int?
)

fun List<DatabaseTraktWatchlist>.asDomainModel(): List<TraktWatchlist> {
    return map {
        TraktWatchlist(
            id = it.id,
            listed_at = it.listed_at,
            rank = it.rank,
            title = it.title,
            mediumImageUrl = it.mediumImageUrl,
            originalImageUrl = it.originalImageUrl,
            imdbID = it.imdbID,
            slug = it.slug,
            tmdbID = it.tmdbID,
            traktID = it.traktID,
            tvdbID = it.tvdbID,
            tvrageID = it.tvrageID,
            tvMazeID = it.tvMazeID
        )
    }
}