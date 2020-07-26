package com.theupnextapp.network.models.trakt

import com.theupnextapp.domain.TraktAddToCollection

data class NetworkTraktAddToCollectionResponse(
    val added: NetworkTraktAddToCollectionResponseAdded?,
    val existing: NetworkTraktAddToCollectionResponseExisting?,
    val not_found: NetworkTraktAddToCollectionResponseNotFound?,
    val updated: NetworkTraktAddToCollectionResponseUpdated?
)

data class NetworkTraktAddToCollectionResponseAdded(
    val episodes: Int?,
    val movies: Int?
)

data class NetworkTraktAddToCollectionResponseExisting(
    val episodes: Int?,
    val movies: Int?
)

data class NetworkTraktAddToCollectionResponseNotFound(
    val episodes: List<Any>,
    val movies: List<NetworkTraktAddToCollectionResponseMovy>,
    val seasons: List<Any>,
    val shows: List<Any>
)

data class NetworkTraktAddToCollectionResponseUpdated(
    val episodes: Int?,
    val movies: Int?
)

data class NetworkTraktAddToCollectionResponseMovy(
    val ids: NetworkTraktAddToCollectionResponseIds?
)

data class NetworkTraktAddToCollectionResponseIds(
    val imdb: String?
)

fun NetworkTraktAddToCollectionResponse.asDomainModel() : TraktAddToCollection {
    return TraktAddToCollection(
        addedEpisodes = added?.episodes,
        notFoundEpisodes = not_found?.episodes?.size,
        notFoundShows = not_found?.shows?.size,
        notFoundSeasons = not_found?.seasons?.size,
        updatedEpisodes = not_found?.seasons?.size,
        existingEpisodes = existing?.episodes
    )
}