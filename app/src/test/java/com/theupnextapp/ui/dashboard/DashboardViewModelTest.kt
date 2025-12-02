package com.theupnextapp.ui.dashboard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.WorkManager
import com.theupnextapp.CoroutineTestRule
import com.theupnextapp.domain.ScheduleShow
import com.theupnextapp.repository.fakes.FakeDashboardRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class DashboardViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var fakeDashboardRepository: FakeDashboardRepository

    @Mock
    private lateinit var workManager: WorkManager

    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        fakeDashboardRepository = FakeDashboardRepository()
        viewModel = DashboardViewModel(fakeDashboardRepository, workManager)
    }

    @Test
    fun `isLoading aggregates repository loading states`() {
        // Observe the MediatorLiveData to trigger updates
        viewModel.isLoading.observeForever {}

        // Test case 1: All false -> false
        fakeDashboardRepository.setLoadingYesterday(false)
        fakeDashboardRepository.setLoadingToday(false)
        fakeDashboardRepository.setLoadingTomorrow(false)
        assertEquals(false, viewModel.isLoading.value)

        // Test case 2: One true -> true
        fakeDashboardRepository.setLoadingYesterday(true)
        assertEquals(true, viewModel.isLoading.value)

        // Test case 3: Multiple true -> true
        fakeDashboardRepository.setLoadingToday(true)
        assertEquals(true, viewModel.isLoading.value)

        // Test case 4: All false again -> false
        fakeDashboardRepository.setLoadingYesterday(false)
        fakeDashboardRepository.setLoadingToday(false)
        fakeDashboardRepository.setLoadingTomorrow(false)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `shows lists are correctly exposed`() = runTest {
        val yesterdayShows = listOf(ScheduleShow(id = 1, showId = 10, name = "Yesterday Show", summary = "Summary", mediumImage = "medium", originalImage = "original", runtime = "60", language = "English", officialSite = "site", status = "Ended", url = "url", updated = "1234567890", premiered = "2023-01-01", type = "Scripted"))
        val todayShows = listOf(ScheduleShow(id = 2, showId = 20, name = "Today Show", summary = "Summary", mediumImage = "medium", originalImage = "original", runtime = "60", language = "English", officialSite = "site", status = "Ended", url = "url", updated = "1234567890", premiered = "2023-01-01", type = "Scripted"))
        val tomorrowShows = listOf(ScheduleShow(id = 3, showId = 30, name = "Tomorrow Show", summary = "Summary", mediumImage = "medium", originalImage = "original", runtime = "60", language = "English", officialSite = "site", status = "Ended", url = "url", updated = "1234567890", premiered = "2023-01-01", type = "Scripted"))

        fakeDashboardRepository.setYesterdayShows(yesterdayShows)
        fakeDashboardRepository.setTodayShows(todayShows)
        fakeDashboardRepository.setTomorrowShows(tomorrowShows)

        viewModel.yesterdayShowsList.observeForever {
            assertEquals(yesterdayShows, it)
        }
        viewModel.todayShowsList.observeForever {
            assertEquals(todayShows, it)
        }
        viewModel.tomorrowShowsList.observeForever {
            assertEquals(tomorrowShows, it)
        }
    }
}
