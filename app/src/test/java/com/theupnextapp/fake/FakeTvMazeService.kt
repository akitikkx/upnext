/*
 * MIT License
 *
 * Copyright (c) 2022 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.fake

import com.theupnextapp.network.TvMazeService
import com.theupnextapp.network.models.tvmaze.NetworkShowCastResponse
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoResponse
import com.theupnextapp.network.models.tvmaze.NetworkShowNextEpisodeResponse
import com.theupnextapp.network.models.tvmaze.NetworkShowPreviousEpisodeResponse
import com.theupnextapp.network.models.tvmaze.NetworkShowSearchResponse
import com.theupnextapp.network.models.tvmaze.NetworkShowSeasonsResponse
import com.theupnextapp.network.models.tvmaze.NetworkTodayScheduleResponse
import com.theupnextapp.network.models.tvmaze.NetworkTomorrowScheduleResponse
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeEpisodesResponse
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowImageResponse
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupResponse
import com.theupnextapp.network.models.tvmaze.NetworkYesterdayScheduleResponse

// Imports for NetworkShowSearchResponse construction
import com.theupnextapp.network.models.tvmaze.NetworkShowSearchResponseShow
import com.theupnextapp.network.models.tvmaze.NetworkShowSearchResponseImage
import com.theupnextapp.network.models.tvmaze.Rating
import com.theupnextapp.network.models.tvmaze.Schedule
import com.theupnextapp.network.models.tvmaze.NetworkShowSeasonsResponseNetwork
import com.theupnextapp.network.models.tvmaze.NetworkShowSeasonsResponseCountry
import com.theupnextapp.network.models.tvmaze.NetworkShowSearchReponseLinks
import com.theupnextapp.network.models.tvmaze.NetworkShowSearchResponsePreviousepisode
import com.theupnextapp.network.models.tvmaze.NetworkShowSearchResponseSelf
import com.theupnextapp.network.models.tvmaze.Externals
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

class FakeTvMazeService : TvMazeService {

    var mockShowInfoResponse: NetworkShowInfoResponse? = null
    var shouldThrowGetShowSummaryError: Boolean = false // Legacy, for generic IOException
    var showSummaryError: Throwable? = null // For specific throwables

    var mockShowLookupResponse: NetworkTvMazeShowLookupResponse? = null
    var showLookupError: Throwable? = null

    var mockNextEpisodeResponse: NetworkShowNextEpisodeResponse? = null
    var nextEpisodeError: Throwable? = null

    var mockPreviousEpisodeResponse: NetworkShowPreviousEpisodeResponse? = null
    var shouldThrowGetPreviousEpisodeError: Boolean = false // Legacy, for generic IOException
    var previousEpisodeError: Throwable? = null // For specific throwables

    var mockShowCastResponse: NetworkShowCastResponse? = null
    var showCastError: Throwable? = null

    var mockShowSeasonsResponse: NetworkShowSeasonsResponse? = null
    var showSeasonsError: Throwable? = null

    var mockTvMazeEpisodesResponse: NetworkTvMazeEpisodesResponse? = null
    var seasonEpisodesError: Throwable? = null


    override fun getYesterdayScheduleAsync(
        countryCode: String,
        date: String?
    ): Deferred<List<NetworkYesterdayScheduleResponse>> {
        throw NotImplementedError("Fake method not implemented")
    }

    override fun getTodayScheduleAsync(
        countryCode: String,
        date: String?
    ): Deferred<List<NetworkTodayScheduleResponse>> {
        throw NotImplementedError("Fake method not implemented")
    }

    override fun getTomorrowScheduleAsync(
        countryCode: String,
        date: String?
    ): Deferred<List<NetworkTomorrowScheduleResponse>> {
        throw NotImplementedError("Fake method not implemented")
    }

    override fun getShowLookupAsync(imdbId: String): Deferred<NetworkTvMazeShowLookupResponse> {
        showLookupError?.let { throw it }
        mockShowLookupResponse?.let { return CompletableDeferred(it) }
        throw NotImplementedError("mockShowLookupResponse not set for this test, or showLookupError not specified.")
    }

    override fun getShowImagesAsync(id: String): Deferred<NetworkTvMazeShowImageResponse> {
        throw NotImplementedError("Fake method not implemented for getShowImagesAsync. Consider adding mock/error properties if needed.")
    }

    override fun getSuggestionListAsync(name: String): Deferred<List<NetworkShowSearchResponse>> {
        // Keeping existing minimal mock for now, can be enhanced with error/mock properties if needed by tests
        val showData = NetworkShowSearchResponseShow(
            id = 1,
            name = "Queen of the South",
            genres = arrayListOf("Drama", "Action", "Crime"),
            status = "Ended",
            premiered = "2021-04-07",
            rating = Rating(average = 8.8),
            image = NetworkShowSearchResponseImage(
                original = "https://static.tvmaze.com/uploads/images/original_untouched/324/811968.jpg",
                medium = "https://static.tvmaze.com/uploads/images/medium_portrait/324/811968.jpg"
            ),
            summary = "Teresa flees Mexico after her drug-runner boyfriend is murdered. Settling in Dallas, she looks to become the country's reigning drug smuggler and to avenge her lover's murder.",
            updated = 1620422,
            network = NetworkShowSeasonsResponseNetwork(
                country = NetworkShowSeasonsResponseCountry(
                    name = "United States",
                    code = "US",
                    timezone = "America/New_York"
                ),
                id = 2,
                name = "USA Network"
            ),
            schedule = Schedule(
                time = "22:00",
                days = arrayListOf("Wednesday")
            ),
            url = "https://www.tvmaze.com/shows/13158/queen-of-the-south",
            _links = NetworkShowSearchReponseLinks(
                previousepisode = NetworkShowSearchResponsePreviousepisode(href = "http://api.tvmaze.com/episodes/2059593"),
                self = NetworkShowSearchResponseSelf(href = "http://api.tvmaze.com/shows/1")
            ),
            externals = Externals(imdb = "tt4352842", thetvdb = 300998, tvrage = null),
            language = "English",
            officialSite = "http://www.usanetwork.com/queen-of-the-south",
            runtime = 60,
            type = "Scripted",
            webChannel = Any(),
            weight = 99
        )
        val searchResult = NetworkShowSearchResponse(score = 10.0, show = showData)
        return CompletableDeferred(listOf(searchResult))
    }

    override fun getShowSummaryAsync(id: String?): Deferred<NetworkShowInfoResponse> {
        showSummaryError?.let { throw it }
        if (shouldThrowGetShowSummaryError) { // Legacy generic error
            throw IOException("Fake network error for getShowSummary")
        }
        mockShowInfoResponse?.let { return CompletableDeferred(it) }
        throw NotImplementedError("mockShowInfoResponse not set for this test, or showSummaryError/shouldThrowGetShowSummaryError not specified.")
    }

    override fun getNextEpisodeAsync(name: String?): Deferred<NetworkShowNextEpisodeResponse> {
        nextEpisodeError?.let { throw it }
        mockNextEpisodeResponse?.let { return CompletableDeferred(it) }
        throw NotImplementedError("mockNextEpisodeResponse not set for this test, or nextEpisodeError not specified.")
    }

    override fun getPreviousEpisodeAsync(name: String?): Deferred<NetworkShowPreviousEpisodeResponse> {
        previousEpisodeError?.let { throw it }
        if (shouldThrowGetPreviousEpisodeError) { // Legacy generic error
            throw IOException("Fake network error for getPreviousEpisode")
        }
        mockPreviousEpisodeResponse?.let { return CompletableDeferred(it) }
        throw NotImplementedError("mockPreviousEpisodeResponse not set for this test, or previousEpisodeError/shouldThrowGetPreviousEpisodeError not specified.")
    }

    override fun getShowCastAsync(id: String?): Deferred<NetworkShowCastResponse> {
        showCastError?.let { throw it }
        mockShowCastResponse?.let { return CompletableDeferred(it) }
        throw NotImplementedError("mockShowCastResponse not set for this test, or showCastError not specified.")
    }

    override fun getShowSeasonsAsync(id: String?): Deferred<NetworkShowSeasonsResponse> {
        showSeasonsError?.let { throw it }
        mockShowSeasonsResponse?.let { return CompletableDeferred(it) }
        throw NotImplementedError("mockShowSeasonsResponse not set for this test, or showSeasonsError not specified.")
    }

    override fun getSeasonEpisodesAsync(id: String?): Deferred<NetworkTvMazeEpisodesResponse> {
        seasonEpisodesError?.let { throw it }
        mockTvMazeEpisodesResponse?.let { return CompletableDeferred(it) }
        throw NotImplementedError("mockTvMazeEpisodesResponse not set for this test, or seasonEpisodesError not specified.")
    }
}
