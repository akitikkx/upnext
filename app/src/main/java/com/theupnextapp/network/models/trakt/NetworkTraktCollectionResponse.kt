package com.theupnextapp.network.models.trakt

import com.theupnextapp.database.DatabaseTraktCollectionEpisode
import com.theupnextapp.database.DatabaseTraktCollection
import com.theupnextapp.database.DatabaseTraktCollectionSeason

class NetworkTraktCollectionResponse : ArrayList<NetworkTraktCollectionResponseItem>()

data class NetworkTraktCollectionResponseItem(
    val last_collected_at: String?,
    val last_updated_at: String?,
    val seasons: List<NetworkTraktCollectionResponseItemSeason?>,
    val show: NetworkTraktCollectionResponseItemShow?
)

data class NetworkTraktCollectionResponseItemSeason(
    var imdb: String?,
    val episodes: List<NetworkTraktCollectionResponseItemEpisode>?,
    val number: Int?
)

data class NetworkTraktCollectionResponseItemShow(
    val ids: NetworkTraktCollectionResponseItemIds?,
    val title: String?,
    val year: Int?,
    var mediumImageUrl: String?,
    var originalImageUrl: String?
)

data class NetworkTraktCollectionResponseItemEpisode(
    var imdb: String?,
    val collected_at: String?,
    var season_number: Int?,
    val number: Int?
)

data class NetworkTraktCollectionResponseItemIds(
    val imdb: String,
    val slug: String?,
    val tmdb: Int?,
    val trakt: Int?,
    val tvdb: Int?,
    val tvrage: Int?,
    var tvMaze: Int?
)

fun NetworkTraktCollectionResponseItem.asDatabaseModel(): DatabaseTraktCollection {
    return DatabaseTraktCollection(
        title = show?.title,
        mediumImageUrl = show?.mediumImageUrl,
        originalImageUrl = show?.originalImageUrl,
        year = show?.year,
        imdbID = show?.ids?.imdb,
        slug = show?.ids?.slug,
        tmdbID = show?.ids?.tmdb,
        traktID = show?.ids?.trakt,
        tvdbID = show?.ids?.tvdb,
        tvrageID = show?.ids?.tvrage,
        tvMazeID = show?.ids?.tvMaze,
        lastCollectedAt = last_collected_at,
        lastUpdatedAt = last_updated_at
    )
}

fun NetworkTraktCollectionResponseItemSeason.asDatabaseModel(): DatabaseTraktCollectionSeason {
    return DatabaseTraktCollectionSeason(
        imdbID = imdb,
        seasonNumber = number
    )
}

fun NetworkTraktCollectionResponseItemEpisode.asDatabaseModel(): DatabaseTraktCollectionEpisode {
    return DatabaseTraktCollectionEpisode(
        imdbID = imdb,
        seasonNumber = season_number,
        episodeNumber = number,
        collectedAt = collected_at
    )
}
