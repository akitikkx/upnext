/*
 * MIT License
 *
 * Copyright (c) 2024 Ahmed Tikiwa
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

import com.theupnextapp.database.TraktDao
import com.theupnextapp.database.UpnextDao
import com.theupnextapp.datasource.TraktAccountDataSource
import com.theupnextapp.datasource.TraktAuthDataSource
import com.theupnextapp.datasource.TraktRecommendationsDataSource
import com.theupnextapp.network.TvMazeService
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class TraktRepositoryImplTest {
    private val upnextDao: UpnextDao = mock()
    private val tvMazeService: TvMazeService = mock()
    private val traktDao: TraktDao = mock()
    private val traktAuthDataSource: TraktAuthDataSource = mock()
    private val traktRecommendationsDataSource: TraktRecommendationsDataSource = mock()
    private val traktAccountDataSource: TraktAccountDataSource = mock()

    private lateinit var repository: TraktRepositoryImpl

    @Before
    fun setup() {
        whenever(traktDao.getTraktAccessData()).thenReturn(flowOf(null))
        whenever(traktDao.getTraktTrending()).thenReturn(flowOf(emptyList()))
        whenever(traktDao.getTraktPopular()).thenReturn(flowOf(emptyList()))
        whenever(traktDao.getTraktMostAnticipated()).thenReturn(flowOf(emptyList()))
        whenever(traktDao.getFavoriteShows()).thenReturn(flowOf(emptyList()))

        repository =
            TraktRepositoryImpl(
                upnextDao,
                tvMazeService,
                traktDao,
                traktAuthDataSource,
                traktRecommendationsDataSource,
                traktAccountDataSource,
            )
    }

    @Test
    fun getTraktAccessToken_delegatesToAuthDataSource() {
        runBlocking {
            val code = "test_code"
            repository.getTraktAccessToken(code)
            verify(traktAuthDataSource).getAccessToken(code)
        }
    }

    @Test
    fun refreshTraktTrendingShows_delegatesToRecommendationsDataSource() {
        runBlocking {
            repository.refreshTraktTrendingShows(true)
            verify(traktRecommendationsDataSource).refreshTraktTrendingShows(true)
        }
    }

    @Test
    fun getTraktShowRating_delegatesToRecommendationsDataSource() {
        runBlocking {
            val showId = "slug"
            repository.getTraktShowRating(showId)
            verify(traktRecommendationsDataSource).getTraktShowRating(showId)
        }
    }

    @Test
    fun getTraktShowStats_delegatesToRecommendationsDataSource() {
        runBlocking {
            val showId = "slug"
            repository.getTraktShowStats(showId)
            verify(traktRecommendationsDataSource).getTraktShowStats(showId)
        }
    }

    @Test
    fun getTraktIdLookup_delegatesToRecommendationsDataSource() {
        runBlocking {
            val imdbId = "tt123"
            repository.getTraktIdLookup(imdbId)
            verify(traktRecommendationsDataSource).getTraktIdFromImdbId(imdbId)
        }
    }

    @Test
    fun refreshWatchlist_clearsAndSavesToDb() {
        runBlocking {
            val token = "test_token"
            val showIds =
                com.theupnextapp.network.models.trakt.NetworkTraktWatchlistResponseItemShowIds(
                    imdb = "tt123",
                    slug = "slug",
                    tmdb = 303,
                    trakt = 101,
                    tvdb = 202,
                    tvMazeID = 404,
                )
            val show =
                com.theupnextapp.network.models.trakt.NetworkTraktWatchlistResponseItemShow(
                    ids = showIds,
                    title = "Test Show",
                    year = 2024,
                    network = null,
                    status = null,
                    rating = null,
                )
            val item =
                com.theupnextapp.network.models.trakt.NetworkTraktWatchlistResponseItem(
                    id = 1L,
                    listedAt = "2024-01-01T00:00:00Z",
                    notes = "notes",
                    rank = 1,
                    show = show,
                    type = "show",
                )
            val responseList = com.theupnextapp.network.models.trakt.NetworkTraktWatchlistResponse()
            responseList.add(item)

            whenever(traktAccountDataSource.getWatchlist(token)).thenReturn(Result.success(responseList))
            whenever(traktAccountDataSource.getImages("tt123")).thenReturn(Triple(12345, "https://poster.jpg", "https://hero.jpg"))

            val result = repository.refreshWatchlist(token)

            assert(result.isSuccess)
            verify(traktDao).deleteAllFavoriteShows()

            val expectedShow =
                com.theupnextapp.database.DatabaseFavoriteShows(
                    id = 101,
                    title = "Test Show",
                    year = "2024",
                    mediumImageUrl = "https://hero.jpg",
                    originalImageUrl = "https://poster.jpg",
                    imdbID = "tt123",
                    slug = "slug",
                    tmdbID = 303,
                    traktID = 101,
                    tvdbID = 202,
                    tvMazeID = 12345,
                    network = null,
                    status = null,
                    rating = null,
                )
            verify(traktDao).insertAllFavoriteShows(expectedShow)
        }
    }

    @Test
    fun addToWatchlist_delegatesToAccountDataSource() {
        runBlocking {
            val token = "test_token"
            val traktId = 101
            whenever(traktAccountDataSource.addToWatchlist(traktId, token)).thenReturn(Result.success(Unit))

            repository.addToWatchlist(traktId, token)
            verify(traktAccountDataSource).addToWatchlist(traktId, token)
        }
    }

    @Test
    fun removeFromWatchlist_delegatesToAccountDataSource() {
        runBlocking {
            val token = "test_token"
            val traktId = 101
            whenever(traktAccountDataSource.removeFromWatchlist(traktId, token)).thenReturn(Result.success(Unit))

            repository.removeFromWatchlist(traktId, token)
            verify(traktAccountDataSource).removeFromWatchlist(traktId, token)
        }
    }
}
