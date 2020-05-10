package com.theupnextapp.network

import com.theupnextapp.domain.TraktAddToWatchlist

data class NetworkTraktAddToWatchlistResponse(
    val added: NetworkTraktAddToWatchlistResponseAdded?,
    val existing: NetworkTraktAddToWatchlistResponseExisting?,
    val not_found: NetworkTraktAddToWatchlistResponseNotFound?
)

data class NetworkTraktAddToWatchlistResponseAdded(
    val episodes: Int,
    val movies: Int,
    val seasons: Int,
    val shows: Int
)

data class NetworkTraktAddToWatchlistResponseExisting(
    val episodes: Int,
    val movies: Int,
    val seasons: Int,
    val shows: Int
)

data class NetworkTraktAddToWatchlistResponseNotFound(
    val episodes: List<Any>,
    val movies: List<NetworkTraktAddToWatchlistRequestMovy>,
    val seasons: List<Any>,
    val shows: List<Any>
)

data class NetworkTraktAddToWatchlistMovy(
    val ids: NetworkTraktAddToWatchlistResponseIds
)

data class NetworkTraktAddToWatchlistResponseIds(
    val imdb: String
)

fun NetworkTraktAddToWatchlistResponse.asDomainModel() : TraktAddToWatchlist {
    return TraktAddToWatchlist(
        addedShows = added?.shows,
        addedEpisodes = added?.episodes,
        addedSeasons = added?.seasons,
        existingShows = existing?.shows,
        existingEpisodes = existing?.episodes,
        existingSeasons = existing?.seasons,
        notFoundShows = not_found?.shows?.size,
        notFoundEpisodes = not_found?.episodes?.size,
        notFoundSeasons = not_found?.seasons?.size
    )
}