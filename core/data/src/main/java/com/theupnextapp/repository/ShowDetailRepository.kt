/*
 * MIT License
 *
 * Copyright (c) 2022 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.repository

import com.theupnextapp.common.CrashlyticsHelper
import com.theupnextapp.database.UpnextDao
import com.theupnextapp.domain.EpisodeDetail
import com.theupnextapp.domain.Result
import com.theupnextapp.domain.ShowCast
import com.theupnextapp.domain.ShowDetailSummary
import com.theupnextapp.domain.ShowNextEpisode
import com.theupnextapp.domain.ShowPreviousEpisode
import com.theupnextapp.domain.ShowSeason
import com.theupnextapp.domain.ShowSeasonEpisode
import com.theupnextapp.domain.safeApiCall
import com.theupnextapp.domain.TmdbWatchProvider
import com.theupnextapp.domain.TmdbWatchProviders
import com.theupnextapp.network.TmdbService
import com.theupnextapp.network.TvMazeService
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupResponse
import com.theupnextapp.network.models.trakt.asDomainModel
import com.theupnextapp.network.models.tvmaze.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class ShowDetailRepository(
    upnextDao: UpnextDao,
    tvMazeService: TvMazeService,
    private val traktService: com.theupnextapp.network.TraktService,
    private val tmdbService: TmdbService,
    private val crashlytics: CrashlyticsHelper,
) : BaseRepository(upnextDao = upnextDao, tvMazeService = tvMazeService) {
    fun getShowSummary(showId: Int): Flow<Result<ShowDetailSummary>> {
        return flow {
            emit(Result.Loading(true))
            val response =
                safeApiCall(Dispatchers.IO) {
                    tvMazeService.getShowSummaryAsync(showId.toString()).await().asDomainModel()
                }

            when (response) {
                is Result.NetworkError -> crashlytics.recordException(response.exception)
                is Result.GenericError -> crashlytics.recordException(response.exception)
                is Result.Error -> response.exception?.let { crashlytics.recordException(it) }
                else -> { /* No action for Success or Loading */ }
            }

            emit(Result.Loading(false))
            emit(response)
        }
            .catch {
                crashlytics.recordException(it)
                emit(Result.Loading(false))
                emit(Result.Error(it, "An unexpected error occurred in the repository flow."))
            }
            .flowOn(Dispatchers.IO)
    }

    fun getShowLookup(imdbId: String): Flow<Result<NetworkTvMazeShowLookupResponse>> {
        return flow {
            emit(Result.Loading(true))
            val response =
                safeApiCall(Dispatchers.IO) {
                    tvMazeService.getShowLookupAsync(imdbId).await()
                }

            when (response) {
                is Result.NetworkError -> crashlytics.recordException(response.exception)
                is Result.GenericError -> crashlytics.recordException(response.exception)
                is Result.Error -> response.exception?.let { crashlytics.recordException(it) }
                else -> { /* No action for Success or Loading */ }
            }

            emit(Result.Loading(false))
            emit(response)
        }
            .catch {
                crashlytics.recordException(it)
                emit(Result.Loading(false))
                emit(Result.Error(it, "An unexpected error occurred in the repository flow."))
            }
            .flowOn(Dispatchers.IO)
    }

    fun getPreviousEpisode(episodeRef: String?): Flow<Result<ShowPreviousEpisode>> {
        return flow {
            emit(Result.Loading(true))
            val previousEpisodeLink =
                episodeRef?.substring(
                    episodeRef.lastIndexOf("/") + 1,
                    episodeRef.length,
                )?.replace("/", "")

            if (!previousEpisodeLink.isNullOrEmpty()) {
                val response =
                    safeApiCall(Dispatchers.IO) {
                        tvMazeService.getPreviousEpisodeAsync(
                            previousEpisodeLink.replace("/", ""),
                        ).await().asDomainModel()
                    }

                when (response) {
                    is Result.NetworkError -> crashlytics.recordException(response.exception)
                    is Result.GenericError -> crashlytics.recordException(response.exception)
                    is Result.Error -> response.exception?.let { crashlytics.recordException(it) }
                    else -> { /* No action for Success or Loading */ }
                }

                emit(Result.Loading(false))
                emit(response)
            } else {
                emit(Result.Loading(false))
            }
        }
            .catch {
                crashlytics.recordException(it)
                emit(Result.Loading(false))
                emit(Result.Error(it, "An unexpected error occurred in the repository flow."))
            }
            .flowOn(Dispatchers.IO)
    }

    fun getNextEpisode(episodeRef: String?): Flow<Result<ShowNextEpisode>> {
        return flow {
            emit(Result.Loading(true))
            val nextEpisodeLink =
                episodeRef?.substring(
                    episodeRef.lastIndexOf("/") + 1,
                    episodeRef.length,
                )?.replace("/", "")

            if (!nextEpisodeLink.isNullOrEmpty()) {
                val response =
                    safeApiCall(Dispatchers.IO) {
                        tvMazeService.getNextEpisodeAsync(
                            nextEpisodeLink.replace("/", ""),
                        ).await().asDomainModel()
                    }

                when (response) {
                    is Result.NetworkError -> crashlytics.recordException(response.exception)
                    is Result.GenericError -> crashlytics.recordException(response.exception)
                    is Result.Error -> response.exception?.let { crashlytics.recordException(it) }
                    else -> { /* No action for Success or Loading */ }
                }

                emit(Result.Loading(false))
                emit(response)
            } else {
                emit(Result.Loading(false))
            }
        }
            .catch {
                crashlytics.recordException(it)
                emit(Result.Loading(false))
                emit(Result.Error(it, "An unexpected error occurred in the repository flow."))
            }
            .flowOn(Dispatchers.IO)
    }

    fun getShowCast(showId: Int): Flow<Result<List<ShowCast>>> {
        return flow {
            emit(Result.Loading(true))
            val response =
                safeApiCall(Dispatchers.IO) {
                    tvMazeService.getShowCastAsync(showId.toString()).await().asDomainModel()
                }

            when (response) {
                is Result.NetworkError -> crashlytics.recordException(response.exception)
                is Result.GenericError -> crashlytics.recordException(response.exception)
                is Result.Error -> response.exception?.let { crashlytics.recordException(it) }
                else -> { /* No action for Success or Loading */ }
            }

            emit(Result.Loading(false))
            emit(response)
        }
            .catch {
                crashlytics.recordException(it)
                emit(Result.Loading(false))
                emit(Result.Error(it, "An unexpected error occurred in the repository flow."))
            }
            .flowOn(Dispatchers.IO)
    }

    fun getShowSeasons(showId: Int): Flow<Result<List<ShowSeason>>> {
        return flow {
            emit(Result.Loading(true))
            val response =
                safeApiCall(Dispatchers.IO) {
                    tvMazeService.getShowSeasonsAsync(showId.toString()).await().asDomainModel()
                }

            when (response) {
                is Result.NetworkError -> crashlytics.recordException(response.exception)
                is Result.GenericError -> crashlytics.recordException(response.exception)
                is Result.Error -> response.exception?.let { crashlytics.recordException(it) }
                else -> { /* No action for Success or Loading */ }
            }

            emit(Result.Loading(false))
            emit(response)
        }
            .catch {
                crashlytics.recordException(it)
                emit(Result.Loading(false))
                emit(Result.Error(it, "An unexpected error occurred in the repository flow."))
            }
            .flowOn(Dispatchers.IO)
    }

    fun getShowSeasonEpisodes(
        showId: Int,
        seasonNumber: Int,
    ): Flow<Result<List<ShowSeasonEpisode>>> {
        return flow {
            emit(Result.Loading(true))
            val response =
                safeApiCall(Dispatchers.IO) {
                    tvMazeService.getSeasonEpisodesAsync(showId.toString()).await().asDomainModel()
                        .filter { it.season == seasonNumber }
                }

            when (response) {
                is Result.NetworkError -> crashlytics.recordException(response.exception)
                is Result.GenericError -> crashlytics.recordException(response.exception)
                is Result.Error -> response.exception?.let { crashlytics.recordException(it) }
                else -> { /* No action for Success or Loading */ }
            }

            emit(Result.Loading(false))
            emit(response)
        }
            .catch {
                crashlytics.recordException(it)
                emit(Result.Loading(false))
                emit(Result.Error(it, "An unexpected error occurred in the repository flow."))
            }
            .flowOn(Dispatchers.IO)
    }

    fun getShowWatchProviders(
        imdbID: String?,
        countryCode: String = "US"
    ): Flow<Result<TmdbWatchProviders>> {
        return flow {
            emit(Result.Loading(true))
            if (imdbID.isNullOrBlank()) {
                emit(Result.Loading(false))
                emit(Result.Success(TmdbWatchProviders(id = null, providers = emptyList())))
                return@flow
            }

            // Extract TMDb ID using Trakt idLookup
            val idLookupResponse = safeApiCall(Dispatchers.IO) {
                traktService.idLookupAsync(idType = "imdb", id = imdbID).await()
            }
            var tmdbId: Int? = null
            if (idLookupResponse is Result.Success) {
                tmdbId = idLookupResponse.data?.firstOrNull()?.show?.ids?.tmdb
            }
            
            // Fallback to getShowInfoAsync if lookup failed
            if (tmdbId == null) {
                 val fallbackResponse = safeApiCall(Dispatchers.IO) {
                    traktService.getShowInfoAsync(imdbID).await()
                 }
                 if (fallbackResponse is Result.Success) {
                     tmdbId = fallbackResponse.data?.ids?.tmdb
                 }
            }

            if (tmdbId == null) {
                emit(Result.Loading(false))
                emit(Result.Success(TmdbWatchProviders(id = null, providers = emptyList())))
                return@flow
            }

            // With valid TMDb ID retrieved, fetch the providers
            val tmdbProvidersResponse = safeApiCall(Dispatchers.IO) {
                tmdbService.getShowWatchProvidersAsync(tmdbId).await()
            }

            when (tmdbProvidersResponse) {
                is Result.NetworkError -> crashlytics.recordException(tmdbProvidersResponse.exception)
                is Result.GenericError -> crashlytics.recordException(tmdbProvidersResponse.exception)
                is Result.Error -> tmdbProvidersResponse.exception?.let { crashlytics.recordException(it) }

                is Result.Success -> {
                    val results = tmdbProvidersResponse.data?.results ?: emptyMap()
                    
                    val key = results.keys.firstOrNull { it.equals(countryCode, ignoreCase = true) }
                    val countryNode = if (key != null) results[key] else null
                    
                    val parsedProvidersList = mutableListOf<TmdbWatchProvider>()
                    
                    countryNode?.flatrate?.forEach {
                        if (it.provider_id != null && it.provider_name != null && it.logo_path != null) {
                            parsedProvidersList.add(
                                TmdbWatchProvider(
                                    id = it.provider_id,
                                    name = it.provider_name,
                                    logoUrl = it.logo_path,
                                    tier = "Flatrate"
                                )
                            )
                        }
                    }
                    
                    emit(Result.Loading(false))
                    emit(Result.Success(TmdbWatchProviders(
                        id = tmdbId,
                        providers = parsedProvidersList.distinctBy { it.id }
                    )))
                    return@flow
                }
                else -> {}
            }

            emit(Result.Loading(false))
            emit(Result.Success(TmdbWatchProviders(id = tmdbId, providers = emptyList())))
        }
        .catch {
            crashlytics.recordException(it)
            emit(Result.Loading(false))
            emit(Result.Error(it, "An unexpected error occurred in the watch providers repository flow."))
        }
        .flowOn(Dispatchers.IO)
    }

    fun getEpisodeDetails(
        traktId: Int,
        seasonNumber: Int,
        episodeNumber: Int,
    ): Flow<Result<EpisodeDetail>> {
        return flow {
            emit(Result.Loading(true))
            val response =
                safeApiCall(Dispatchers.IO) {
                    traktService.getEpisodeAsync(
                        id = traktId.toString(),
                        season = seasonNumber,
                        episode = episodeNumber,
                    ).await().asDomainModel()
                }

            when (response) {
                is Result.NetworkError -> crashlytics.recordException(response.exception)
                is Result.GenericError -> crashlytics.recordException(response.exception)
                is Result.Error -> response.exception?.let { crashlytics.recordException(it) }
                else -> { /* No action for Success or Loading */ }
            }

            emit(Result.Loading(false))
            emit(response)
        }.catch {
            crashlytics.recordException(it)
            emit(Result.Loading(false))
            emit(Result.Error(it, "An unexpected error occurred in the repository flow."))
        }.flowOn(Dispatchers.IO)
    }

    fun getEpisodePeople(
        traktId: Int,
        seasonNumber: Int,
        episodeNumber: Int,
    ): Flow<Result<com.theupnextapp.domain.EpisodePeople>> {
        return flow {
            emit(Result.Loading(true))
            val response =
                safeApiCall(Dispatchers.IO) {
                    traktService.getEpisodePeopleAsync(
                        id = traktId.toString(),
                        season = seasonNumber,
                        episode = episodeNumber,
                    ).await().asDomainModel()
                }

            when (response) {
                is Result.NetworkError -> crashlytics.recordException(response.exception)
                is Result.GenericError -> crashlytics.recordException(response.exception)
                is Result.Error -> response.exception?.let { crashlytics.recordException(it) }
                else -> { /* No action for Success or Loading */ }
            }

            emit(Result.Loading(false))
            emit(response)
        }.catch {
            crashlytics.recordException(it)
            emit(Result.Loading(false))
            emit(Result.Error(it, "An unexpected error occurred in the repository flow."))
        }.flowOn(Dispatchers.IO)
    }
}
