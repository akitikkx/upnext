/*
 * MIT License
 *
 * Copyright (c) 2024 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.ui.dashboard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.WorkManager
import com.theupnextapp.CoroutineTestRule
import com.theupnextapp.repository.DashboardRepository
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.repository.WatchProgressRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@ExperimentalCoroutinesApi
class DashboardViewModelTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutineTestRule = CoroutineTestRule()

    private lateinit var traktRepository: TraktRepository
    private lateinit var dashboardRepository: DashboardRepository
    private lateinit var watchProgressRepository: WatchProgressRepository
    private lateinit var localWorkManager: WorkManager

    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setup() {
        traktRepository = mock(TraktRepository::class.java)
        dashboardRepository = mock(DashboardRepository::class.java)
        watchProgressRepository = mock(WatchProgressRepository::class.java)
        localWorkManager = mock(WorkManager::class.java)

        `when`(traktRepository.traktAccessToken).thenReturn(flowOf(null))
        `when`(traktRepository.traktMostAnticipatedShows).thenReturn(flowOf(emptyList()))
        `when`(dashboardRepository.todayShows).thenReturn(flowOf(emptyList()))
        `when`(watchProgressRepository.isSyncing).thenReturn(MutableStateFlow(false))

        viewModel =
            DashboardViewModel(
                traktRepository = traktRepository,
                dashboardRepository = dashboardRepository,
                watchProgressRepository = watchProgressRepository,
                localWorkManager = localWorkManager,
            )
    }

    @Test
    fun `viewModel initializes correctly`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `when trakt access token is null, dependent flows handle gracefully`() =
        runTest {
            // Ensure the mock returns null explicitly for access token
            `when`(traktRepository.traktAccessToken).thenReturn(flowOf(null))

            // Create viewModel fresh inside the scope to trigger the init block collection
            val testViewModel =
                DashboardViewModel(
                    traktRepository = traktRepository,
                    dashboardRepository = dashboardRepository,
                    watchProgressRepository = watchProgressRepository,
                    localWorkManager = localWorkManager,
                )

            // Give flows time to emit initial states
            val accessTokenValue = testViewModel.traktAccessToken.value
            val airingSoonValue = testViewModel.airingSoonShows.value
            val recentHistoryValue = testViewModel.recentHistory.value

            // Validate that null access token safely maps to null/empty dependent structures, not crashes
            assertNull("Access token should be null", accessTokenValue)
            assertTrue("Airing soon shows should be empty when unauthorized", airingSoonValue.isNullOrEmpty())
            assertTrue("Recent history should be empty when unauthorized", recentHistoryValue.isNullOrEmpty())
        }
}
