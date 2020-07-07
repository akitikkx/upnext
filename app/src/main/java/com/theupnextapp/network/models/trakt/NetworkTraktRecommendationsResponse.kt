package com.theupnextapp.network.models.trakt

import com.theupnextapp.database.DatabaseTraktRecommendations

class NetworkTraktRecommendationsResponse : ArrayList<NetworkTraktRecommendationsItem>()

data class NetworkTraktRecommendationsItem(
    val ids: NetworkTraktRecommendationsItemIds?,
    val title: String?,
    val year: Int?,
    var mediumImageUrl: String?,
    var originalImageUrl: String?
)

data class NetworkTraktRecommendationsItemIds(
    val imdb: String?,
    val slug: String?,
    val tmdb: Int?,
    val trakt: Int?,
    val tvdb: Int?,
    var tvMazeID: Int?
)

fun NetworkTraktRecommendationsItem.asDatabaseModel() : DatabaseTraktRecommendations {
    return DatabaseTraktRecommendations(
        title = title,
        year = year,
        imdbID = ids?.imdb,
        slug = ids?.slug,
        tmdbID = ids?.tmdb,
        traktID = ids?.trakt,
        tvdbID = ids?.tvMazeID,
        tvMazeID = ids?.tvMazeID,
        mediumImageUrl = mediumImageUrl,
        originalImageUrl = originalImageUrl
    )
}