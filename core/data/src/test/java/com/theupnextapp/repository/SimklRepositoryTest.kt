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

import com.theupnextapp.database.DatabaseSimklWatchedEpisode
import com.theupnextapp.database.SimklDao
import com.theupnextapp.domain.SyncStatus
import com.theupnextapp.network.SimklService
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response

class SimklRepositoryTest {

    private lateinit var simklService: SimklService
    private lateinit var simklDao: SimklDao
    private lateinit var simklRepository: SimklRepository

    private lateinit var traktDao: com.theupnextapp.database.TraktDao

    private val validToken = "valid_token"

    @Before
    fun setup() {
        simklService = mock()
        simklDao = mock()
        traktDao = mock()

        simklRepository = SimklRepository(
            simklService = simklService,
            simklDao = simklDao,
            traktDao = traktDao
        )
    }

    @Test
    fun `markEpisodeWatched inserts PENDING_ADD state to database`() = runTest {
        // When
        val result = simklRepository.markEpisodeWatched(
            simklId = 123,
            imdbID = "tt123",
            seasonNumber = 1,
            episodeNumber = 1,
            token = validToken
        )

        // Then
        assertTrue(result.isSuccess)
        verify(simklDao).insertWatchedEpisode(any())
    }

    @Test
    fun `pushPendingWatchedHistory syncs PENDING_ADD episodes successfully`() = runTest {
        // Given
        val pendingEpisodes = listOf(
            DatabaseSimklWatchedEpisode(
                showSimklId = 123,
                showTvMazeId = null,
                showImdbId = "tt123",
                showTraktId = null,
                seasonNumber = 1,
                episodeNumber = 1,
                watchedAt = 1000L,
                syncStatus = SyncStatus.PENDING_ADD.ordinal,
                lastModified = 1000L
            )
        )
        whenever(simklDao.getPendingSyncEpisodes()).thenReturn(pendingEpisodes)
        whenever(simklService.addHistory(any(), any())).thenReturn(Response.success(Any()))

        // When
        simklRepository.pushPendingWatchedHistory(validToken)

        // Then
        verify(simklService).addHistory(any(), any())
        verify(simklDao).updateSyncStatus(123, 1, 1, SyncStatus.SYNCED.ordinal)
    }

    @Test
    fun `refreshPremieres fetches premieres successfully and updates state`() = runTest {
        val networkShow = com.theupnextapp.network.models.simkl.NetworkSimklTrendingResponse(
            title = "Test Premiere",
            year = 2026,
            ids = com.theupnextapp.network.models.simkl.NetworkSimklIds(simklId = 1, imdbId = "tt1", tmdbId = null, tvdbId = null),
            poster = "poster1"
        )
        whenever(simklService.getPremieres(org.mockito.kotlin.anyOrNull())).thenReturn(Response.success(listOf(networkShow)))

        simklRepository.refreshPremieres()

        val premieres = simklRepository.premieresShows.value
        assertTrue(premieres.size == 1)
        assertTrue(premieres.first().title == "Test Premiere")
        verify(simklService).getPremieres(org.mockito.kotlin.anyOrNull())
    }
}
