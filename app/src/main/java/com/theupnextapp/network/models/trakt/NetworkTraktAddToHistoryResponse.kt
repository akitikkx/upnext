package com.theupnextapp.network.models.trakt

import com.theupnextapp.domain.TraktAddToHistory

data class NetworkTraktAddToHistoryResponse(
    val added: NetworkTraktAddToHistoryResponseAdded?,
    val not_found: NetworkTraktAddToHistoryResponseNotFound?
)

data class NetworkTraktAddToHistoryResponseAdded(
    val episodes: Int?,
    val movies: Int?
)

data class NetworkTraktAddToHistoryResponseNotFound(
    val episodes: List<Any>,
    val movies: List<NetworkTraktAddToHistoryResponseMovy>,
    val seasons: List<Any>,
    val shows: List<Any>
)

data class NetworkTraktAddToHistoryResponseMovy(
    val ids: NetworkTraktAddToHistoryResponseIds
)

data class NetworkTraktAddToHistoryResponseIds(
    val imdb: String
)

fun NetworkTraktAddToHistoryResponse.asDomainModel() : TraktAddToHistory {
    return TraktAddToHistory(
        addedEpisodes = added?.episodes,
        notFoundShows = not_found?.shows?.size,
        notFoundEpisodes = not_found?.episodes?.size,
        notFoundSeasons = not_found?.seasons?.size
    )
}