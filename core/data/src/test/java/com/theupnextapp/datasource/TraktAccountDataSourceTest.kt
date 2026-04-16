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
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

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
                    ids = NetworkTraktWatchlistResponseItemShowIds(trakt = 1, slug = "test", imdb = "tt123", tmdb = 1, tvdb = 2, tvMazeID = null)
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
}
