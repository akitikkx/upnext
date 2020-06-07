package com.theupnextapp.network.models.trakt

import com.theupnextapp.domain.TraktRemoveFromHistory

data class NetworkTraktRemoveFromHistoryResponse(
    val deleted: NetworkTraktRemoveFromHistoryResponseDeleted?,
    val not_found: NetworkTraktRemoveFromHistoryResponseNotFound?
)

data class NetworkTraktRemoveFromHistoryResponseDeleted(
    val episodes: Int?,
    val movies: Int?
)

data class NetworkTraktRemoveFromHistoryResponseNotFound(
    val episodes: List<Any>,
    val ids: List<Int>,
    val movies: List<NetworkTraktRemoveFromHistoryResponseMovy>,
    val seasons: List<Any>,
    val shows: List<Any>
)

data class NetworkTraktRemoveFromHistoryResponseMovy(
    val ids: NetworkTraktRemoveFromHistoryResponseIds?
)

data class NetworkTraktRemoveFromHistoryResponseIds(
    val imdb: String?
)

fun NetworkTraktRemoveFromHistoryResponse.asDomainModel() : TraktRemoveFromHistory {
    return TraktRemoveFromHistory(
        deletedEpisodes = deleted?.episodes,
        notFoundShows = not_found?.shows?.size,
        notFoundEpisodes = not_found?.episodes?.size,
        notFoundSeasons = not_found?.seasons?.size
    )
}