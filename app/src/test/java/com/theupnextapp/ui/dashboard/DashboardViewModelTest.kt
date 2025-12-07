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

package com.theupnextapp.ui.dashboard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.WorkManager
import com.theupnextapp.CoroutineTestRule
import com.theupnextapp.domain.ScheduleShow
import com.theupnextapp.getOrAwaitValue
import com.theupnextapp.repository.fakes.FakeDashboardRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class DashboardViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutineTestRule = CoroutineTestRule()

    private lateinit var workManager: WorkManager

    private lateinit var viewModel: DashboardViewModel
    private lateinit var fakeRepository: FakeDashboardRepository

    @Before
    fun setup() {
        workManager = org.mockito.Mockito.mock(WorkManager::class.java)
        fakeRepository = FakeDashboardRepository()
        viewModel = DashboardViewModel(fakeRepository, workManager)
    }

    @Test
    fun `isLoading is true when any of the loading states are true`() {
        // Given
        fakeRepository.setLoadingYesterday(true)
        fakeRepository.setLoadingToday(false)
        fakeRepository.setLoadingTomorrow(false)

        // When
        val isLoading = viewModel.isLoading.getOrAwaitValue()

        // Then
        assertTrue(isLoading)
    }

    @Test
    fun `isLoading is false when all of the loading states are false`() {
        // Given
        fakeRepository.setLoadingYesterday(false)
        fakeRepository.setLoadingToday(false)
        fakeRepository.setLoadingTomorrow(false)

        // When
        val isLoading = viewModel.isLoading.getOrAwaitValue()

        // Then
        assertFalse(isLoading)
    }

    @Test
    fun `yesterdayShowsList returns the correct data`() {
        // Given
        val shows = listOf(
            ScheduleShow(
                id = 1,
                showId = 1,
                name = "Show 1",
                summary = "Summary 1",
                originalImage = "url",
                mediumImage = "url",
                language = "English",
                officialSite = "url",
                premiered = "yesterday",
                runtime = "60",
                status = "running",
                type = "scripted",
                updated = "yesterday",
                url = "url"
            )
        )
        fakeRepository.setYesterdayShows(shows)

        // When
        val yesterdayShows = viewModel.yesterdayShowsList.getOrAwaitValue()

        // Then
        assertEquals(shows, yesterdayShows)
    }

    @Test
    fun `todayShowsList returns the correct data`() {
        // Given
        val shows = listOf(
            ScheduleShow(
                id = 2,
                showId = 2,
                name = "Show 2",
                summary = "Summary 2",
                originalImage = "url",
                mediumImage = "url",
                language = "English",
                officialSite = "url",
                premiered = "today",
                runtime = "60",
                status = "running",
                type = "scripted",
                updated = "today",
                url = "url"
            )
        )
        fakeRepository.setTodayShows(shows)

        // When
        val todayShows = viewModel.todayShowsList.getOrAwaitValue()

        // Then
        assertEquals(shows, todayShows)
    }

    @Test
    fun `tomorrowShowsList returns the correct data`() {
        // Given
        val shows = listOf(
            ScheduleShow(
                id = 3,
                showId = 3,
                name = "Show 3",
                summary = "Summary 3",
                originalImage = "url",
                mediumImage = "url",
                language = "English",
                officialSite = "url",
                premiered = "tomorrow",
                runtime = "60",
                status = "running",
                type = "scripted",
                updated = "tomorrow",
                url = "url"
            )
        )
        fakeRepository.setTomorrowShows(shows)

        // When
        val tomorrowShows = viewModel.tomorrowShowsList.getOrAwaitValue()

        // Then
        assertEquals(shows, tomorrowShows)
    }
}
