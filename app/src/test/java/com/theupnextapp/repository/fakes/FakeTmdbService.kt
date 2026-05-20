package com.theupnextapp.repository.fakes

import com.theupnextapp.network.TmdbService
import com.theupnextapp.network.models.tmdb.NetworkTmdbPersonImagesResponse
import com.theupnextapp.network.models.tmdb.NetworkTmdbPersonTvCreditsResponse
import com.theupnextapp.network.models.tmdb.NetworkTmdbShowDetailsResponse
import com.theupnextapp.network.models.tmdb.NetworkTmdbWatchProvidersResponse
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

class FakeTmdbService : TmdbService {
    var mockShowDetailsResponse = NetworkTmdbShowDetailsResponse(
        id = 1,
        poster_path = "tmdb_poster.png",
        backdrop_path = "tmdb_backdrop.png"
    )

    override fun getShowDetailsAsync(tmdbId: Int): Deferred<NetworkTmdbShowDetailsResponse> {
        return CompletableDeferred(mockShowDetailsResponse)
    }

    override fun getShowWatchProvidersAsync(tmdbId: Int): Deferred<NetworkTmdbWatchProvidersResponse> {
        return CompletableDeferred(NetworkTmdbWatchProvidersResponse(id = 1, results = emptyMap()))
    }

    override fun getPersonTvCreditsAsync(personId: Int): Deferred<NetworkTmdbPersonTvCreditsResponse> {
        return CompletableDeferred(NetworkTmdbPersonTvCreditsResponse(cast = emptyList(), id = 1))
    }

    override fun getPersonImagesAsync(personId: Int): Deferred<NetworkTmdbPersonImagesResponse> {
        return CompletableDeferred(NetworkTmdbPersonImagesResponse(id = 1, profiles = emptyList()))
    }

    fun reset() {
        mockShowDetailsResponse = NetworkTmdbShowDetailsResponse(
            id = 1,
            poster_path = "tmdb_poster.png",
            backdrop_path = "tmdb_backdrop.png"
        )
    }
}
