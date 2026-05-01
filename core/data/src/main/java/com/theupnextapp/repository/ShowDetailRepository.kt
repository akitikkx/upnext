package com.theupnextapp.repository

import com.theupnextapp.domain.EpisodeDetail
import com.theupnextapp.domain.EpisodePeople
import com.theupnextapp.domain.Result
import com.theupnextapp.domain.ShowCast
import com.theupnextapp.domain.ShowDetailSummary
import com.theupnextapp.domain.ShowNextEpisode
import com.theupnextapp.domain.ShowPreviousEpisode
import com.theupnextapp.domain.ShowSeason
import com.theupnextapp.domain.ShowSeasonEpisode
import com.theupnextapp.domain.TmdbWatchProviders
import com.theupnextapp.network.models.tmdb.NetworkTmdbPersonImagesResponse
import com.theupnextapp.network.models.tmdb.NetworkTmdbPersonTvCreditsResponse
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupResponse
import kotlinx.coroutines.flow.Flow

interface ShowDetailRepository {
    fun getShowSummary(showId: Int): Flow<Result<ShowDetailSummary>>
    fun getShowLookup(imdbId: String): Flow<Result<NetworkTvMazeShowLookupResponse>>
    fun getPreviousEpisode(episodeRef: String?): Flow<Result<ShowPreviousEpisode>>
    fun getNextEpisode(episodeRef: String?): Flow<Result<ShowNextEpisode>>
    fun getShowCast(showId: Int): Flow<Result<List<ShowCast>>>
    fun getShowSeasons(showId: Int): Flow<Result<List<ShowSeason>>>
    fun getShowSeasonEpisodes(showId: Int, seasonNumber: Int): Flow<Result<List<ShowSeasonEpisode>>>
    fun getShowWatchProviders(imdbID: String?, countryCode: String = java.util.Locale.getDefault().country): Flow<Result<TmdbWatchProviders>>
    fun getEpisodeDetails(traktId: Int, seasonNumber: Int, episodeNumber: Int): Flow<Result<EpisodeDetail>>
    fun getEpisodePeople(traktId: Int, seasonNumber: Int, episodeNumber: Int): Flow<Result<EpisodePeople>>
    fun getPersonTvCredits(personId: Int): Flow<Result<NetworkTmdbPersonTvCreditsResponse>>
    fun getPersonImages(personId: Int): Flow<Result<NetworkTmdbPersonImagesResponse>>
}
