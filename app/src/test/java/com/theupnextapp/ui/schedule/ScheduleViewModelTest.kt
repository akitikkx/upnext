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

package com.theupnextapp.ui.schedule

import androidx.work.WorkManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.theupnextapp.CoroutineTestRule
import com.theupnextapp.domain.ScheduleShow
import com.theupnextapp.repository.fakes.FakeDashboardRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

@ExperimentalCoroutinesApi
class ScheduleViewModelTest {

    @get:Rule
    var coroutineTestRule = CoroutineTestRule()

    private lateinit var workManager: WorkManager
    private val firebaseAnalytics: FirebaseAnalytics = Mockito.mock(FirebaseAnalytics::class.java)

    private lateinit var viewModel: ScheduleViewModel
    private lateinit var fakeRepository: FakeDashboardRepository

    @Before
    fun setup() {
        workManager = Mockito.mock(WorkManager::class.java)
        fakeRepository = FakeDashboardRepository()
        viewModel = ScheduleViewModel(fakeRepository, workManager, firebaseAnalytics)
    }

    @Test
    fun `isLoading is true when only yesterday is loading`() = runTest {
        // Given
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.isLoading.collect {}
        }
        fakeRepository.setLoadingYesterday(true)
        fakeRepository.setLoadingToday(false)
        fakeRepository.setLoadingTomorrow(false)

        // Then
        assertTrue(viewModel.isLoading.value)
        collectJob.cancel()
    }

    @Test
    fun `isLoading is true when only today is loading`() = runTest {
        // Given
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.isLoading.collect {}
        }
        fakeRepository.setLoadingYesterday(false)
        fakeRepository.setLoadingToday(true)
        fakeRepository.setLoadingTomorrow(false)

        // Then
        assertTrue(viewModel.isLoading.value)
        collectJob.cancel()
    }

    @Test
    fun `isLoading is true when only tomorrow is loading`() = runTest {
        // Given
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.isLoading.collect {}
        }
        fakeRepository.setLoadingYesterday(false)
        fakeRepository.setLoadingToday(false)
        fakeRepository.setLoadingTomorrow(true)

        // Then
        assertTrue(viewModel.isLoading.value)
        collectJob.cancel()
    }

    @Test
    fun `isLoading is false when all of the loading states are false`() = runTest {
        // Given
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.isLoading.collect {}
        }
        fakeRepository.setLoadingYesterday(false)
        fakeRepository.setLoadingToday(false)
        fakeRepository.setLoadingTomorrow(false)

        // Then
        assertFalse(viewModel.isLoading.value)
        collectJob.cancel()
    }

    @Test
    fun `yesterdayShowsList returns the correct data`() = runTest {
        // Given
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.yesterdayShowsList.collect {}
        }
        val shows =
            listOf(
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
                    url = "url",
                ),
            )
        fakeRepository.setYesterdayShows(shows)

        // Then
        assertEquals(shows, viewModel.yesterdayShowsList.value)
        collectJob.cancel()
    }

    @Test
    fun `todayShowsList returns the correct data`() = runTest {
        // Given
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.todayShowsList.collect {}
        }
        val shows =
            listOf(
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
                    url = "url",
                ),
            )
        fakeRepository.setTodayShows(shows)

        // Then
        assertEquals(shows, viewModel.todayShowsList.value)
        collectJob.cancel()
    }

    @Test
    fun `tomorrowShowsList returns the correct data`() = runTest {
        // Given
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.tomorrowShowsList.collect {}
        }
        val shows =
            listOf(
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
                    url = "url",
                ),
            )
        fakeRepository.setTomorrowShows(shows)

        // Then
        assertEquals(shows, viewModel.tomorrowShowsList.value)
        collectJob.cancel()
    }
}
