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

import com.theupnextapp.database.DatabaseTraktMostAnticipated
import com.theupnextapp.database.DatabaseTraktPopularShows
import com.theupnextapp.database.DatabaseTrendingShows
import com.theupnextapp.database.TraktDao
import com.theupnextapp.database.UpnextDao
import com.theupnextapp.datasource.TraktAccountDataSource
import com.theupnextapp.datasource.TraktAuthDataSource
import com.theupnextapp.datasource.TraktRecommendationsDataSource
import com.theupnextapp.network.TvMazeService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
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

    private val trendingShowsFlow = MutableStateFlow<List<DatabaseTrendingShows>>(emptyList())
    private val popularShowsFlow = MutableStateFlow<List<DatabaseTraktPopularShows>>(emptyList())
    private val mostAnticipatedShowsFlow = MutableStateFlow<List<DatabaseTraktMostAnticipated>>(emptyList())

    private lateinit var repository: TraktRepositoryImpl

    @Before
    fun setup() {
        whenever(traktDao.getTraktAccessData()).thenReturn(flowOf(null))
        whenever(traktDao.getTrendingShows("trakt")).thenReturn(trendingShowsFlow)
        whenever(traktDao.getTraktPopular()).thenReturn(popularShowsFlow)
        whenever(traktDao.getTraktMostAnticipated()).thenReturn(mostAnticipatedShowsFlow)
        whenever(traktDao.getWatchlistShows()).thenReturn(flowOf(emptyList()))

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
    fun refreshWatchlist_delegatesToAccountDataSource() {
        runBlocking {
            val token = "test_token"
            whenever(traktAccountDataSource.refreshWatchlistShows(token)).thenReturn(Result.success(Unit))

            val result = repository.refreshWatchlist(token)

            assert(result.isSuccess)
            verify(traktAccountDataSource).refreshWatchlistShows(token)
        }
    }

    @Test
    fun addToWatchlist_delegatesToAccountDataSource() {
        runBlocking {
            val token = "test_token"
            val traktId = 101
            val imdbID = "tt123"
            val testTitle = "Show Title"
            val testOriginalUrl = "original_url"
            val testMediumUrl = "medium_url"
            val testTvMaze = 200
            val testTmdb = 300
            val testYear = "2024"
            val testNetwork = "HBO"
            val testStatus = "Running"
            val testRating = 8.5

            whenever(traktAccountDataSource.addToWatchlist(traktId, token)).thenReturn(Result.success(Unit))

            repository.addToWatchlist(
                traktId = traktId,
                imdbID = imdbID,
                token = token,
                title = testTitle,
                originalImageUrl = testOriginalUrl,
                mediumImageUrl = testMediumUrl,
                tvMazeID = testTvMaze,
                tmdbID = testTmdb,
                year = testYear,
                network = testNetwork,
                status = testStatus,
                rating = testRating,
            )

            verify(traktAccountDataSource).addToWatchlist(traktId, token)

            // Verify optimistic local insert
            val captor = org.mockito.kotlin.argumentCaptor<com.theupnextapp.database.DatabaseWatchlistShows>()
            verify(traktDao).insertWatchlistShow(captor.capture())

            val inserted = captor.firstValue
            assert(inserted.traktID == traktId)
            assert(inserted.title == testTitle)
            assert(inserted.tvMazeID == testTvMaze)
            assert(inserted.tmdbID == testTmdb)
            assert(inserted.year == testYear)
            assert(inserted.network == testNetwork)
            assert(inserted.status == testStatus)
            assert(inserted.rating == testRating)
        }
    }

    @Test
    fun removeFromWatchlist_delegatesAndDeletesLocally() {
        runBlocking {
            val token = "test_token"
            val traktId = 101
            whenever(traktAccountDataSource.removeFromWatchlist(traktId, token)).thenReturn(Result.success(Unit))

            repository.removeFromWatchlist(traktId, token)
            verify(traktAccountDataSource).removeFromWatchlist(traktId, token)
            // Verify optimistic local delete
            verify(traktDao).deleteWatchlistShowByTraktId(traktId)
        }
    }

    @Test
    fun refreshWatchlist_emptyToken_returnsFailure() {
        runBlocking {
            val result = repository.refreshWatchlist("")
            assert(result.isFailure)
            verify(traktAccountDataSource, org.mockito.kotlin.never()).getWatchlist(org.mockito.kotlin.any())
        }
    }

    @Test
    fun refreshWatchlist_delegatesToDataSource() {
        runBlocking {
            val token = "test_token"

            whenever(traktAccountDataSource.refreshWatchlistShows(token)).thenReturn(Result.success(Unit))

            val result = repository.refreshWatchlist(token)

            assert(result.isSuccess)
            verify(traktAccountDataSource).refreshWatchlistShows(token)
        }
    }

    @Test
    fun addToWatchlist_failure_setsError() {
        runBlocking {
            val token = "test_token"
            val traktId = 101
            val imdbID = "tt123"
            whenever(traktAccountDataSource.addToWatchlist(traktId, token))
                .thenReturn(Result.failure(Exception("API error")))

            val result = repository.addToWatchlist(traktId, imdbID, token)

            assert(result.isFailure)
            verify(traktDao, org.mockito.kotlin.never()).insertWatchlistShow(org.mockito.kotlin.any())
        }
    }

    @Test
    fun traktTrendingShows_filtersOutShowsWithoutImages() = runBlocking {
        val showWithImages = com.theupnextapp.database.DatabaseTrendingShows(
            id = 1,
            showId = 101,
            title = "Show with Images",
            year = "2024",
            medium_image_url = "medium_url",
            original_image_url = "original_url",
            imdbID = "tt123",
            slug = "slug",
            tmdbID = 10,
            traktID = 101,
            tvdbID = 1,
            tvMazeID = 100,
            providerId = "trakt"
        )
        val showWithoutImages = com.theupnextapp.database.DatabaseTrendingShows(
            id = 2,
            showId = 102,
            title = "Show without Images",
            year = "2024",
            medium_image_url = null,
            original_image_url = null,
            imdbID = "tt124",
            slug = "slug2",
            tmdbID = 11,
            traktID = 102,
            tvdbID = 2,
            tvMazeID = 101,
            providerId = "trakt"
        )
        trendingShowsFlow.value = listOf(showWithImages, showWithoutImages)

        val collectedTrending = repository.trendingShows.first()
        org.junit.Assert.assertEquals(1, collectedTrending.size)
        org.junit.Assert.assertEquals("Show with Images", collectedTrending.first().title)

        val collectedTraktTrending = repository.traktTrendingShows.first()
        org.junit.Assert.assertEquals(1, collectedTraktTrending.size)
        org.junit.Assert.assertEquals("Show with Images", collectedTraktTrending.first().title)
    }

    @Test
    fun popularShows_filtersOutShowsWithoutImages() = runBlocking {
        val showWithImages = com.theupnextapp.database.DatabaseTraktPopularShows(
            id = 101,
            title = "Show with Images",
            year = "2024",
            medium_image_url = "medium_url",
            original_image_url = "original_url",
            imdbID = "tt123",
            slug = "slug",
            tmdbID = 10,
            traktID = 101,
            tvdbID = 1,
            tvMazeID = 100
        )
        val showWithoutImages = com.theupnextapp.database.DatabaseTraktPopularShows(
            id = 102,
            title = "Show without Images",
            year = "2024",
            medium_image_url = null,
            original_image_url = null,
            imdbID = "tt124",
            slug = "slug2",
            tmdbID = 11,
            traktID = 102,
            tvdbID = 2,
            tvMazeID = 101
        )
        popularShowsFlow.value = listOf(showWithImages, showWithoutImages)

        val collectedPopular = repository.traktPopularShows.first()
        org.junit.Assert.assertEquals(1, collectedPopular.size)
        org.junit.Assert.assertEquals("Show with Images", collectedPopular.first().title)
    }

    @Test
    fun mostAnticipatedShows_filtersOutShowsWithoutImages() = runBlocking {
        val showWithImages = com.theupnextapp.database.DatabaseTraktMostAnticipated(
            id = 101,
            title = "Show with Images",
            year = "2024",
            medium_image_url = "medium_url",
            original_image_url = "original_url",
            imdbID = "tt123",
            slug = "slug",
            tmdbID = 10,
            traktID = 101,
            tvdbID = 1,
            tvMazeID = 100
        )
        val showWithoutImages = com.theupnextapp.database.DatabaseTraktMostAnticipated(
            id = 102,
            title = "Show without Images",
            year = "2024",
            medium_image_url = null,
            original_image_url = null,
            imdbID = "tt124",
            slug = "slug2",
            tmdbID = 11,
            traktID = 102,
            tvdbID = 2,
            tvMazeID = 101
        )
        mostAnticipatedShowsFlow.value = listOf(showWithImages, showWithoutImages)

        val collectedAnticipated = repository.traktMostAnticipatedShows.first()
        org.junit.Assert.assertEquals(1, collectedAnticipated.size)
        org.junit.Assert.assertEquals("Show with Images", collectedAnticipated.first().title)
    }
}
