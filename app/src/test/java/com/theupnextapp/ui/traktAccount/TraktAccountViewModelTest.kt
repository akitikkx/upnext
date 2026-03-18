package com.theupnextapp.ui.traktAccount

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.WorkManager
import com.theupnextapp.common.utils.TraktAuthManager
import com.theupnextapp.repository.TraktRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.launch
import com.theupnextapp.domain.TraktUserListItem
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class TraktAccountViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var traktRepository: TraktRepository

    @Mock
    private lateinit var workManager: WorkManager

    @Mock
    private lateinit var traktAuthManager: TraktAuthManager

    private lateinit var viewModel: TraktAccountViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        whenever(traktRepository.isLoading).thenReturn(MutableStateFlow(false))
        whenever(traktRepository.isLoadingFavoriteShows).thenReturn(MutableStateFlow(false))
        whenever(traktRepository.traktFavoriteShows).thenReturn(MutableStateFlow(emptyList()))
        whenever(traktRepository.favoriteShowsError).thenReturn(MutableStateFlow(null))
        whenever(traktRepository.traktAccessToken).thenReturn(MutableStateFlow(null))

        viewModel =
            TraktAccountViewModel(
                traktRepository,
                workManager,
                traktAuthManager,
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `favoriteShows initialization observes from repository mapped states`() {
        val state = viewModel.favoriteShows.value
        assert(state.isEmpty())
        assert(viewModel.favoriteShowsEmpty.value)
    }

    @Test
    fun `favoriteShows emits filtered list when search query changes`() = runTest {
        val mockShows = listOf(
            TraktUserListItem(id = 1, traktID = 1, title = "The Boys", originalImageUrl = "", mediumImageUrl = "", imdbID = "", slug = "", tmdbID = 1, tvdbID = 1, tvMazeID = 1, year = "", network = null, status = null, rating = null),
            TraktUserListItem(id = 2, traktID = 2, title = "Kaos", originalImageUrl = "", mediumImageUrl = "", imdbID = "", slug = "", tmdbID = 2, tvdbID = 2, tvMazeID = 2, year = "", network = null, status = null, rating = null)
        )
        whenever(traktRepository.traktFavoriteShows).thenReturn(MutableStateFlow(mockShows))
        
        viewModel = TraktAccountViewModel(traktRepository, workManager, traktAuthManager)
        
        val job = launch { viewModel.favoriteShows.collect {} }
        advanceUntilIdle() // let stateIn initialize
        
        viewModel.onSearchQueryChange("Kaos")
        advanceUntilIdle()
        
        assert(viewModel.favoriteShows.value.size == 1)
        assert(viewModel.favoriteShows.value.first().title == "Kaos")
        
        job.cancel()
    }
    
    @Test
    fun `favoriteShows emits sorted list when sort option changes`() = runTest {
        val mockShows = listOf(
            TraktUserListItem(id = 1, traktID = 1, title = "Zebra", year = "2020", originalImageUrl = "", mediumImageUrl = "", imdbID = "", slug = "", tmdbID = 1, tvdbID = 1, tvMazeID = 1, network = null, status = null, rating = null),
            TraktUserListItem(id = 2, traktID = 2, title = "Apple", year = "2024", originalImageUrl = "", mediumImageUrl = "", imdbID = "", slug = "", tmdbID = 2, tvdbID = 2, tvMazeID = 2, network = null, status = null, rating = null)
        )
        whenever(traktRepository.traktFavoriteShows).thenReturn(MutableStateFlow(mockShows))
        
        viewModel = TraktAccountViewModel(traktRepository, workManager, traktAuthManager)
        
        val job = launch { viewModel.favoriteShows.collect {} }
        advanceUntilIdle()
        
        viewModel.onSortOptionChange(WatchlistSortOption.TITLE)
        advanceUntilIdle()
        
        assert(viewModel.favoriteShows.value.size == 2)
        assert(viewModel.favoriteShows.value[0].title == "Apple")
        assert(viewModel.favoriteShows.value[1].title == "Zebra")
        
        viewModel.onSortOptionChange(WatchlistSortOption.RELEASE_YEAR)
        advanceUntilIdle()
        
        assert(viewModel.favoriteShows.value[0].year == "2024")
        assert(viewModel.favoriteShows.value[1].year == "2020")
        
        job.cancel()
    }
}
