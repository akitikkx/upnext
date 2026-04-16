package com.theupnextapp.repository.fakes

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
import com.theupnextapp.repository.ShowDetailRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.mockito.kotlin.mock

class FakeShowDetailRepository : ShowDetailRepository {
    var showSummaryResult: Result<ShowDetailSummary> =
        Result.Success(
            ShowDetailSummary(airDays = "", averageRating = "", id = 1, imdbID = "tt123456", genres = "", language = "English", mediumImageUrl = "", name = "Fake Show", originalImageUrl = "", summary = "Fake Summary", time = "", status = "Running", previousEpisodeHref = "", nextEpisodeHref = "", nextEpisodeLinkedId = 1, previousEpisodeLinkedId = 1, tmdbID = 1, network = "HBO", premiered = "2024-01-01"),
        )

    var showLookupResult: Result<NetworkTvMazeShowLookupResponse> = Result.Success(mock())

    var previousEpisodeResult: Result<ShowPreviousEpisode> =
        Result.Success(
            ShowPreviousEpisode(previousEpisodeId = 1, previousEpisodeAirdate = "", previousEpisodeAirstamp = "", previousEpisodeAirtime = "", previousEpisodeMediumImageUrl = "", previousEpisodeOriginalImageUrl = "", previousEpisodeName = "", previousEpisodeNumber = "1", previousEpisodeRuntime = "1", previousEpisodeSeason = "1", previousEpisodeSummary = "", previousEpisodeUrl = ""),
        )

    var nextEpisodeResult: Result<ShowNextEpisode> =
        Result.Success(
            ShowNextEpisode(nextEpisodeId = 2, nextEpisodeAirdate = "", nextEpisodeAirstamp = "", nextEpisodeAirtime = "", nextEpisodeMediumImageUrl = "", nextEpisodeOriginalImageUrl = "", nextEpisodeName = "", nextEpisodeNumber = "2", nextEpisodeRuntime = "2", nextEpisodeSeason = "1", nextEpisodeSummary = "", nextEpisodeUrl = ""),
        )

    var showCastResult: Result<List<ShowCast>> = Result.Success(emptyList())

    var showSeasonsResult: Result<List<ShowSeason>> = Result.Success(emptyList())

    var showSeasonEpisodesResult: Result<List<ShowSeasonEpisode>> = Result.Success(emptyList())

    var showWatchProvidersResult: Result<TmdbWatchProviders> =
        Result.Success(
            TmdbWatchProviders(id = 1, providers = emptyList()),
        )

    var episodeDetailsResult: Result<EpisodeDetail> =
        Result.Success(
            EpisodeDetail(title = "Fake Episode Details", overview = "Fake overview", season = 1, number = 1, firstAired = "", runtime = 1, rating = 1.0, tvdbId = 1, imdbId = "", tmdbId = 1, votes = 1),
        )

    var episodePeopleResult: Result<EpisodePeople> =
        Result.Success(
            EpisodePeople(cast = emptyList(), guestStars = emptyList(), crew = emptyList()),
        )

    override fun getShowSummary(showId: Int): Flow<Result<ShowDetailSummary>> = flowOf(showSummaryResult)

    override fun getShowLookup(imdbId: String): Flow<Result<NetworkTvMazeShowLookupResponse>> = flowOf(showLookupResult)

    override fun getPreviousEpisode(episodeRef: String?): Flow<Result<ShowPreviousEpisode>> = flowOf(previousEpisodeResult)

    override fun getNextEpisode(episodeRef: String?): Flow<Result<ShowNextEpisode>> = flowOf(nextEpisodeResult)

    override fun getShowCast(showId: Int): Flow<Result<List<ShowCast>>> = flowOf(showCastResult)

    override fun getShowSeasons(showId: Int): Flow<Result<List<ShowSeason>>> = flowOf(showSeasonsResult)

    override fun getShowSeasonEpisodes(
        showId: Int,
        seasonNumber: Int,
    ): Flow<Result<List<ShowSeasonEpisode>>> =
        flowOf(
            showSeasonEpisodesResult,
        )

    override fun getShowWatchProviders(
        imdbID: String?,
        countryCode: String,
    ): Flow<Result<TmdbWatchProviders>> =
        flowOf(
            showWatchProvidersResult,
        )

    override fun getEpisodeDetails(
        traktId: Int,
        seasonNumber: Int,
        episodeNumber: Int,
    ): Flow<Result<EpisodeDetail>> =
        flowOf(
            episodeDetailsResult,
        )

    override fun getEpisodePeople(
        traktId: Int,
        seasonNumber: Int,
        episodeNumber: Int,
    ): Flow<Result<EpisodePeople>> =
        flowOf(
            episodePeopleResult,
        )

    var personTvCreditsResult: Result<NetworkTmdbPersonTvCreditsResponse> = Result.Success(mock())

    override fun getPersonTvCredits(personId: Int): Flow<Result<NetworkTmdbPersonTvCreditsResponse>> =
        flowOf(
            personTvCreditsResult,
        )

    var personImagesResult: Result<NetworkTmdbPersonImagesResponse> = Result.Success(mock())

    override fun getPersonImages(personId: Int): Flow<Result<NetworkTmdbPersonImagesResponse>> =
        flowOf(
            personImagesResult,
        )
}
