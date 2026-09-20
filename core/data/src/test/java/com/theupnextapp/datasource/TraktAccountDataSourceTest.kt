package com.theupnextapp.datasource

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.theupnextapp.database.TraktDao
import com.theupnextapp.database.UpnextDao
import com.theupnextapp.network.TraktService
import com.theupnextapp.network.TvMazeService
import com.theupnextapp.network.models.trakt.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import com.theupnextapp.database.DatabaseWatchlistShows

class TraktAccountDataSourceTest {
    private val traktDao: TraktDao = mock()
    private val upnextDao: UpnextDao = mock()
    private val tvMazeService: TvMazeService = mock()
    private val firebaseCrashlytics: FirebaseCrashlytics = mock()
    private val traktService: TraktService = mock()
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private lateinit var dataSource: TraktAccountDataSource

    @Before
    fun setup() {
        dataSource = TraktAccountDataSource(
            traktDao,
            traktService,
            moshi,
            upnextDao,
            tvMazeService,
            firebaseCrashlytics
        )
    }

    @Test
    fun `getWatchlist returns success`() = runBlocking {
        val mockResponse = NetworkTraktWatchlistResponse()
        mockResponse.add(
            NetworkTraktWatchlistResponseItem(
                id = 1L,
                rank = 1,
                listedAt = "today",
                notes = null,
                type = "show",
                show = NetworkTraktWatchlistResponseItemShow(
                    title = "Test",
                    year = 2024,
                    network = null,
                    status = null,
                    rating = null,
                    ids = NetworkTraktWatchlistResponseItemShowIds(trakt = 1, slug = "test", imdb = "tt123", tmdb = 1, tvdb = 2, tvRage = null)
                )
            )
        )

        whenever(traktService.getWatchlistAsync(any(), any(), any(), any())).thenReturn(
            CompletableDeferred(mockResponse)
        )

        val result = dataSource.getWatchlist("valid_token")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("Test", result.getOrNull()?.first()?.show?.title)
    }

    @Test
    fun `addToWatchlist returns success`() = runBlocking {
        val mockResponse = NetworkTraktAddShowToListResponse(
            added = NetworkTraktAddShowToListResponseAdded(shows = 1),
            existing = NetworkTraktAddShowToListResponseExisting(shows = 0),
            not_found = NetworkTraktAddShowToListResponseNotFound(shows = emptyList())
        )

        whenever(traktService.addToWatchlistAsync(eq("Bearer valid_token"), any())).thenReturn(
            CompletableDeferred(mockResponse)
        )

        val result = dataSource.addToWatchlist(12345, "valid_token")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `removeFromWatchlist returns success`() = runBlocking {
        val mockResponse = NetworkTraktRemoveShowFromListResponse(
            deleted = NetworkTraktRemoveShowFromListResponseDeleted(shows = 1, seasons = 0, episodes = 0),
            not_found = NetworkTraktRemoveShowFromListResponseNotFound(shows = emptyList())
        )

        whenever(traktService.removeFromWatchlistAsync(eq("Bearer valid_token"), any())).thenReturn(
            CompletableDeferred(mockResponse)
        )

        val result = dataSource.removeFromWatchlist(12345, "valid_token")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `refreshWatchlistShows retains cached images when tvmaze fetch fails`() = runBlocking {
        val mockResponse = NetworkTraktWatchlistResponse()
        mockResponse.add(
            NetworkTraktWatchlistResponseItem(
                id = 1L,
                rank = 1,
                listedAt = "today",
                notes = null,
                type = "show",
                show = NetworkTraktWatchlistResponseItemShow(
                    title = "Test",
                    year = 2024,
                    network = null,
                    status = null,
                    rating = null,
                    ids = NetworkTraktWatchlistResponseItemShowIds(trakt = 1, slug = "test", imdb = "tt123", tmdb = 1, tvdb = 2, tvRage = null)
                )
            )
        )

        whenever(traktService.getWatchlistAsync(any(), any(), any(), any())).thenReturn(
            CompletableDeferred(mockResponse)
        )

        whenever(traktDao.getWatchlistShowByTraktId(eq(1))).thenReturn(
            com.theupnextapp.database.DatabaseWatchlistShows(
                id = 1,
                title = "Test",
                slug = "test",
                year = "2024",
                imdbID = "tt123",
                tvdbID = 2,
                tmdbID = 1,
                traktID = 1,
                tvMazeID = null, // Forces fallback fetch evaluation
                originalImageUrl = "old_poster_url",
                mediumImageUrl = "old_hero_url",
                rating = null,
                network = null,
                status = null
            )
        )

        whenever(traktDao.getAllWatchlistShowTraktIds()).thenReturn(emptyList())

        // Simulating TVMaze API failing
        whenever(tvMazeService.getShowLookupAsync(any())).thenThrow(RuntimeException("API Error"))

        val result = dataSource.refreshWatchlistShows("valid_token")

        assertTrue(result.isSuccess)

        val captor = argumentCaptor<Array<DatabaseWatchlistShows>>()
        verify(traktDao).insertAllWatchlistShows(*captor.capture())
        val capturedShows = captor.firstValue
        assertTrue(capturedShows.isNotEmpty())
        assertEquals("old_poster_url", capturedShows[0].originalImageUrl)
        assertEquals("old_hero_url", capturedShows[0].mediumImageUrl)
        assertEquals(null, capturedShows[0].tvMazeID)
    }

    @Test
    fun `getTraktRecentHistory passes pagination parameters`() {
        runBlocking {
            val mockHistory = listOf(
                NetworkTraktHistoryResponse(
                    id = 101L,
                    watchedAt = "2026-09-15T20:00:00.000Z",
                    action = "watch",
                    type = "episode",
                    show = NetworkTraktWatchedShowInfo(
                        title = "Severance",
                        year = 2022,
                        ids = NetworkTraktWatchedShowIds(trakt = 100, slug = "severance", tvdb = 1, imdb = "tt100", tmdb = 1)
                    ),
                    episode = NetworkTraktWatchedEpisode(
                        season = 1,
                        number = 1,
                        title = "Good News About Hell",
                        plays = 1,
                        lastWatchedAt = "2026-09-15T20:00:00.000Z"
                    )
                )
            )

            whenever(traktService.getRecentHistoryAsync(token = "Bearer test_token", page = 2, limit = 15, extended = "full"))
                .thenReturn(CompletableDeferred(mockHistory))

            val result = dataSource.getTraktRecentHistory("test_token", page = 2, limit = 15)

            assertTrue(result.isSuccess)
            assertEquals(1, result.getOrNull()?.size)
            assertEquals("Severance", result.getOrNull()?.first()?.show?.title)
            verify(traktService).getRecentHistoryAsync(token = "Bearer test_token", page = 2, limit = 15, extended = "full")
        }
    }

    @Test
    fun `getTraktPlaybackProgress calculates progress and includes next episode`() {
        runBlocking {
        val paused = listOf(
            NetworkTraktPlaybackResponse(
                progress = 42.5f,
                action = "pause",
                type = "episode",
                show = NetworkTraktWatchedShowInfo(
                    title = "Slow Horses",
                    year = 2022,
                    ids = NetworkTraktWatchedShowIds(trakt = 200, slug = "slow-horses", tvdb = 2, imdb = "tt200", tmdb = 2)
                ),
                episode = NetworkTraktWatchedEpisode(
                    season = 2,
                    number = 3,
                    title = "Drinking Games",
                    plays = 1,
                    lastWatchedAt = "2026-09-10T19:00:00.000Z"
                )
            )
        )

        val watchedShows = listOf(
            NetworkTraktWatchedShowsResponse(
                plays = 5,
                lastWatchedAt = "2026-09-14T21:00:00.000Z",
                lastUpdatedAt = "2026-09-14T21:00:00.000Z",
                resetAt = null,
                show = NetworkTraktWatchedShowInfo(
                    title = "Severance",
                    year = 2022,
                    ids = NetworkTraktWatchedShowIds(trakt = 100, slug = "severance", tvdb = 1, imdb = "tt100", tmdb = 1)
                ),
                seasons = emptyList()
            )
        )

        val showProgress = NetworkTraktShowProgressResponse(
            aired = 10,
            completed = 4,
            lastWatchedAt = "2026-09-14T21:00:00.000Z",
            lastEpisode = null,
            nextEpisode = NetworkTraktWatchedEpisode(
                season = 1,
                number = 5,
                title = "The Grim Barbarity of Optics and Design",
                plays = 0,
                lastWatchedAt = null
            )
        )

        whenever(traktService.getPlaybackProgressAsync("Bearer test_token")).thenReturn(CompletableDeferred(paused))
        whenever(traktService.getWatchedShowsAsync("Bearer test_token")).thenReturn(CompletableDeferred(watchedShows))
        whenever(traktService.getShowProgressAsync(token = "Bearer test_token", id = "100")).thenReturn(CompletableDeferred(showProgress))

        val result = dataSource.getTraktPlaybackProgress("test_token")

        assertTrue(result.isSuccess)
        val items = result.getOrNull()
        assertEquals(2, items?.size)
        // First is the paused item
        assertEquals("Slow Horses", items?.get(0)?.show?.title)
        assertEquals(42.5f, items?.get(0)?.progress)
        // Second is the next unwatched episode
        assertEquals("Severance", items?.get(1)?.show?.title)
        assertEquals(1, items?.get(1)?.episode?.season)
        assertEquals(5, items?.get(1)?.episode?.number)
        assertEquals(40.0f, items?.get(1)?.progress) // 4 completed out of 10 aired = 40%
        }
    }
}
