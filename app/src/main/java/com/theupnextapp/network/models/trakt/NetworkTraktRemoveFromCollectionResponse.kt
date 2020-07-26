package com.theupnextapp.network.models.trakt

import com.theupnextapp.domain.TraktRemoveFromCollection

data class NetworkTraktRemoveFromCollectionResponse(
    val deleted: NetworkTraktRemoveFromCollectionResponseDeleted?,
    val not_found: NetworkTraktRemoveFromCollectionResponseNotFound?
)

data class NetworkTraktRemoveFromCollectionResponseDeleted(
    val episodes: Int?,
    val movies: Int?
)

data class NetworkTraktRemoveFromCollectionResponseNotFound(
    val episodes: List<Any>,
    val movies: List<NetworkTraktRemoveFromCollectionResponseMovy>,
    val seasons: List<Any>,
    val shows: List<Any>
)

data class NetworkTraktRemoveFromCollectionResponseMovy(
    val ids: NetworkTraktRemoveFromCollectionResponseIds?
)

data class NetworkTraktRemoveFromCollectionResponseIds(
    val imdb: String?
)

fun NetworkTraktRemoveFromCollectionResponse.asDomainModel(): TraktRemoveFromCollection {
    return TraktRemoveFromCollection(
        deletedEpisodes = deleted?.episodes,
        notFoundSeasons = not_found?.seasons?.size,
        notFoundEpisodes = not_found?.episodes?.size,
        notFoundShows = not_found?.shows?.size
    )
}