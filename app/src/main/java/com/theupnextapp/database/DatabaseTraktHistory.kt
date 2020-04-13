package com.theupnextapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.theupnextapp.domain.TraktHistory

@Entity(tableName = "trakt_history")
data class DatabaseTraktHistory(
    @PrimaryKey
    val id: Int?,
    val showTitle: String?,
    val showYear: Int?,
    val episodeTitle: String?,
    val episodeSeasonNumber: Int?,
    val episodeNumber: Int?,
    val historyType: String?,
    val watchedAt: String?,
    val historyAction: String?,
    val mediumImageUrl: String?,
    val originalImageUrl: String?,
    val imdbID: String?,
    val slug: String?,
    val tmdbID: Int?,
    val traktID: Int?,
    val tvdbID: Int?,
    val tvMazeID: Int?
)

fun List<DatabaseTraktHistory>.asDomainModel(): List<TraktHistory> {
    return map {
        TraktHistory(
            id = it.id,
            showTitle = it.showTitle,
            showYear = it.showYear,
            episodeTitle = it.episodeTitle,
            episodeNumber = it.episodeNumber,
            episodeSeasonNumber = it.episodeSeasonNumber,
            historyType = it.historyType,
            historyAction = it.historyAction,
            watchedAt = it.watchedAt,
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

fun DatabaseTraktHistory.asDomainModel() : TraktHistory {
    return TraktHistory(
        id = id,
        showTitle = showTitle,
        showYear = showYear,
        episodeTitle = episodeTitle,
        episodeNumber = episodeNumber,
        episodeSeasonNumber = episodeSeasonNumber,
        historyType = historyType,
        historyAction = historyAction,
        watchedAt = watchedAt,
        mediumImageUrl = mediumImageUrl,
        originalImageUrl = originalImageUrl,
        imdbID = imdbID,
        slug = slug,
        tmdbID = tmdbID,
        traktID = traktID,
        tvdbID = tvdbID,
        tvMazeID = tvMazeID
    )
}