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
import com.theupnextapp.CoroutineTestRule
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.repository.DashboardRepository
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.repository.WatchProgressRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.junit.Assert.assertNotNull

@ExperimentalCoroutinesApi
class DashboardViewModelTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutineTestRule = CoroutineTestRule()

    private lateinit var traktRepository: TraktRepository
    private lateinit var dashboardRepository: DashboardRepository
    private lateinit var watchProgressRepository: WatchProgressRepository

    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setup() {
        traktRepository = mock(TraktRepository::class.java)
        dashboardRepository = mock(DashboardRepository::class.java)
        watchProgressRepository = mock(WatchProgressRepository::class.java)

        `when`(traktRepository.traktAccessToken).thenReturn(flowOf(null))
        `when`(traktRepository.traktMostAnticipatedShows).thenReturn(flowOf(emptyList()))
        `when`(dashboardRepository.todayShows).thenReturn(flowOf(emptyList()))

        viewModel = DashboardViewModel(
            traktRepository = traktRepository,
            dashboardRepository = dashboardRepository,
            watchProgressRepository = watchProgressRepository
        )
    }

    @Test
    fun `viewModel initializes correctly`() {
        assertNotNull(viewModel)
    }
}
