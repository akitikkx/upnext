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
import java.io.IOException // Added for error simulation
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

class FakeTvMazeService : TvMazeService {

    var mockShowInfoResponse: NetworkShowInfoResponse? = null
    // Flag for getShowSummaryAsync error simulation - already present
    var shouldThrowGetShowSummaryError: Boolean = false


    var mockNextEpisodeResponse: NetworkShowNextEpisodeResponse? = null
    var mockPreviousEpisodeResponse: NetworkShowPreviousEpisodeResponse? = null
    var mockShowCastResponse: NetworkShowCastResponse? = null
    var mockShowSeasonsResponse: NetworkShowSeasonsResponse? = null
    var mockTvMazeEpisodesResponse: NetworkTvMazeEpisodesResponse? = null

    // Added: Flag for getPreviousEpisodeAsync error simulation
    var shouldThrowGetPreviousEpisodeError: Boolean = false

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
        throw NotImplementedError("Fake method not implemented")
    }

    override fun getShowImagesAsync(id: String): Deferred<NetworkTvMazeShowImageResponse> {
        throw NotImplementedError("Fake method not implemented")
    }

    override fun getSuggestionListAsync(name: String): Deferred<List<NetworkShowSearchResponse>> {
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
            webChannel = Any(), // Consider replacing Any with a more specific mock or null if appropriate
            weight = 99
        )

        val searchResult = NetworkShowSearchResponse(score = 10.0, show = showData)
        val searchResults = mutableListOf(searchResult)

        return CompletableDeferred(searchResults)
    }

    override fun getShowSummaryAsync(id: String?): Deferred<NetworkShowInfoResponse> {
        if (shouldThrowGetShowSummaryError) {
            throw IOException("Fake network error for getShowSummary")
        }
        mockShowInfoResponse?.let {
            return CompletableDeferred(it)
        } ?: throw NotImplementedError("mockShowInfoResponse not set for this test")
    }

    override fun getNextEpisodeAsync(name: String?): Deferred<NetworkShowNextEpisodeResponse> {
        mockNextEpisodeResponse?.let {
            return CompletableDeferred(it)
        } ?: throw NotImplementedError("mockNextEpisodeResponse not set for this test")
    }

    override fun getPreviousEpisodeAsync(name: String?): Deferred<NetworkShowPreviousEpisodeResponse> {
        if (shouldThrowGetPreviousEpisodeError) {
            throw IOException("Fake network error for getPreviousEpisode")
        }
        mockPreviousEpisodeResponse?.let {
            return CompletableDeferred(it)
        } ?: throw NotImplementedError("mockPreviousEpisodeResponse not set for this test")
    }

    override fun getShowCastAsync(id: String?): Deferred<NetworkShowCastResponse> {
        mockShowCastResponse?.let {
            return CompletableDeferred(it)
        } ?: throw NotImplementedError("mockShowCastResponse not set for this test")
    }

    override fun getShowSeasonsAsync(id: String?): Deferred<NetworkShowSeasonsResponse> {
        mockShowSeasonsResponse?.let {
            return CompletableDeferred(it)
        } ?: throw NotImplementedError("mockShowSeasonsResponse not set for this test")
    }

    override fun getSeasonEpisodesAsync(id: String?): Deferred<NetworkTvMazeEpisodesResponse> {
        mockTvMazeEpisodesResponse?.let {
            return CompletableDeferred(it)
        } ?: throw NotImplementedError("mockTvMazeEpisodesResponse not set for this test")
    }
}
