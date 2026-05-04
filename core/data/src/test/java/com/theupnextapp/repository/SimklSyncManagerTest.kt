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

import com.theupnextapp.network.SimklService
import com.theupnextapp.network.models.simkl.NetworkSimklActivityResponse
import com.theupnextapp.network.models.simkl.NetworkSimklActivityTimestamps
import com.theupnextapp.network.models.simkl.NetworkSimklAllItemsResponse
import com.theupnextapp.network.models.simkl.NetworkSimklLibraryResponse
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response

class SimklSyncManagerTest {

    private lateinit var simklService: SimklService
    private lateinit var providerManager: ProviderManager
    private lateinit var simklRepository: SimklRepository
    private lateinit var simklSyncManager: SimklSyncManager

    private val validToken = "valid_token"
    private val formattedToken = "Bearer \$validToken"

    @Before
    fun setup() {
        simklService = mock()
        providerManager = mock {
            on { simklLastActivityHash } doReturn flowOf(null)
            on { simklLastSyncDate } doReturn flowOf(null)
        }
        simklRepository = mock()

        simklSyncManager = SimklSyncManager(
            simklService = simklService,
            providerManager = providerManager,
            simklRepository = simklRepository
        )
    }

    @Test
    fun `sync short circuits if activities hash matches`() = runTest {
        // Given
        val hash = "latest_hash"
        whenever(providerManager.simklLastActivityHash).thenReturn(flowOf(hash))
        
        val activityResponse = NetworkSimklActivityResponse(
            tvShows = NetworkSimklActivityTimestamps(updatedAt = hash, removedAt = null),
            all = null
        )
        whenever(simklService.getActivities(any())).thenReturn(Response.success(activityResponse))

        // When
        val result = simklSyncManager.sync(validToken)

        // Then
        assertTrue(result.isSuccess)
        verify(simklService, never()).getSyncShows(any())
        verify(simklService, never()).getAllItems(any(), any())
    }

    @Test
    fun `sync executes Phase 1 when no previous sync date exists`() = runTest {
        // Given
        whenever(providerManager.simklLastActivityHash).thenReturn(flowOf("old_hash"))
        whenever(providerManager.simklLastSyncDate).thenReturn(flowOf(null))

        val activityResponse = NetworkSimklActivityResponse(
            tvShows = NetworkSimklActivityTimestamps(updatedAt = "new_hash", removedAt = null),
            all = null
        )
        whenever(simklService.getActivities(any())).thenReturn(Response.success(activityResponse))
        
        val showsResponse = listOf<NetworkSimklLibraryResponse>()
        whenever(simklService.getSyncShows(any())).thenReturn(Response.success(showsResponse))

        // When
        val result = simklSyncManager.sync(validToken)

        // Then
        assertTrue(result.isSuccess)
        verify(simklService).getSyncShows(any())
        verify(simklService, never()).getAllItems(any(), any())
        verify(providerManager).setSimklLastActivityHash("new_hash")
        verify(providerManager).setSimklLastSyncDate(any())
    }

    @Test
    fun `sync executes Phase 2 when previous sync date exists`() = runTest {
        // Given
        val lastSyncDate = "2024-01-01T00:00:00Z"
        whenever(providerManager.simklLastActivityHash).thenReturn(flowOf("old_hash"))
        whenever(providerManager.simklLastSyncDate).thenReturn(flowOf(lastSyncDate))

        val activityResponse = NetworkSimklActivityResponse(
            tvShows = NetworkSimklActivityTimestamps(updatedAt = "new_hash", removedAt = null),
            all = null
        )
        whenever(simklService.getActivities(any())).thenReturn(Response.success(activityResponse))
        
        val allItemsResponse = NetworkSimklAllItemsResponse(tvShows = emptyList())
        whenever(simklService.getAllItems(any(), any())).thenReturn(Response.success(allItemsResponse))

        // When
        val result = simklSyncManager.sync(validToken)

        // Then
        assertTrue(result.isSuccess)
        verify(simklService, never()).getSyncShows(any())
        verify(simklService).getAllItems(any(), org.mockito.kotlin.eq(lastSyncDate))
        verify(providerManager).setSimklLastActivityHash("new_hash")
        verify(providerManager).setSimklLastSyncDate(any())
        verify(simklRepository).pushPendingWatchedHistory(validToken)
        verify(simklRepository).refreshWatchedHistory(validToken)
    }

    @Test
    fun `sync triggers watch history refresh in Phase 1`() = runTest {
        // Given
        whenever(providerManager.simklLastActivityHash).thenReturn(flowOf("old_hash"))
        whenever(providerManager.simklLastSyncDate).thenReturn(flowOf(null))

        val activityResponse = NetworkSimklActivityResponse(
            tvShows = NetworkSimklActivityTimestamps(updatedAt = "new_hash", removedAt = null),
            all = null
        )
        whenever(simklService.getActivities(any())).thenReturn(Response.success(activityResponse))
        
        val showsResponse = listOf<NetworkSimklLibraryResponse>()
        whenever(simklService.getSyncShows(any())).thenReturn(Response.success(showsResponse))

        // When
        val result = simklSyncManager.sync(validToken)

        // Then
        assertTrue(result.isSuccess)
        verify(simklRepository).pushPendingWatchedHistory(validToken)
        verify(simklRepository).refreshWatchedHistory(validToken)
    }
}
