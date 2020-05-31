package com.theupnextapp.network.models.trakt

import com.theupnextapp.domain.TraktRemoveFromWatchlist

data class NetworkTraktRemoveFromWatchlistResponse(
    val deleted: NetworkTraktRemoveFromWatchlistDeleted,
    val not_found: NetworkTraktRemoveFromWatchlistNotFound
)

data class NetworkTraktRemoveFromWatchlistDeleted(
    val episodes: Int,
    val movies: Int,
    val seasons: Int,
    val shows: Int
)

data class NetworkTraktRemoveFromWatchlistNotFound(
    val episodes: List<Any>,
    val movies: List<NetworkTraktRemoveFromWatchlistMovy>,
    val seasons: List<Any>,
    val shows: List<Any>
)

data class NetworkTraktRemoveFromWatchlistMovy(
    val ids: Ids
)

data class NetworkTraktRemoveFromWatchlistIds(
    val imdb: String
)

fun NetworkTraktRemoveFromWatchlistResponse.asDomainModel() : TraktRemoveFromWatchlist {
    return TraktRemoveFromWatchlist(
        deletedShows = deleted?.shows,
        deletedEpisodes = deleted?.episodes,
        deletedSeasons = deleted?.seasons,
        notFoundShows = not_found?.shows?.size,
        notFoundEpisodes = not_found?.episodes?.size,
        notFoundSeasons = not_found?.seasons?.size
    )
}