package com.theupnextapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.theupnextapp.domain.TraktCollection

@Entity(tableName = "trakt_collection")
data class DatabaseTraktCollection(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    val title: String?,
    var mediumImageUrl: String?,
    var originalImageUrl: String?,
    val year: Int?,
    val slug: String?,
    val imdbID: String?,
    val tmdbID: Int?,
    val traktID: Int?,
    val tvdbID: Int?,
    val tvrageID: Int?,
    val tvMazeID: Int?,
    val lastCollectedAt: String?,
    val lastUpdatedAt: String?
)

fun List<DatabaseTraktCollection>.asDomainModel(): List<TraktCollection> {
    return map {
        TraktCollection(
            id = it.id,
            title = it.title,
            mediumImageUrl = it.mediumImageUrl,
            originalImageUrl = it.originalImageUrl,
            year = it.year,
            slug = it.slug,
            imdbID = it.imdbID,
            tmdbID = it.tmdbID,
            traktID = it.traktID,
            tvdbID = it.tvdbID,
            tvrageID = it.tvrageID,
            tvMazeID = it.tvMazeID,
            lastUpdatedAt = it.lastCollectedAt,
            lastCollectedAt = it.lastCollectedAt
        )
    }
}