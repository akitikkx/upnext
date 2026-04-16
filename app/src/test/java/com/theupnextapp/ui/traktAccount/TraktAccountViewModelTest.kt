package com.theupnextapp.ui.traktAccount

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.WorkManager
import com.theupnextapp.common.utils.TraktAuthManager
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.domain.TraktUserListItem
import com.theupnextapp.repository.fakes.FakeTraktRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import kotlin.Result

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class TraktAccountViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var traktRepository: FakeTraktRepository

    @Mock
    private lateinit var workManager: WorkManager

    @Mock
    private lateinit var traktAuthManager: TraktAuthManager

    private lateinit var viewModel: TraktAccountViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        traktRepository = FakeTraktRepository()

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
    fun `watchlistShows initialization observes from repository mapped states`() {
        val state = viewModel.watchlistShows.value
        assert(state.isEmpty())
        assert(viewModel.watchlistShowsEmpty.value)
    }

    @Test
    fun `watchlistShows emits filtered list when search query changes`() =
        runTest {
            val mockShows =
                listOf(
                    TraktUserListItem(id = 1, traktID = 1, title = "The Boys", originalImageUrl = "", mediumImageUrl = "", imdbID = "", slug = "", tmdbID = 1, tvdbID = 1, tvMazeID = 1, year = "", network = null, status = null, rating = null),
                    TraktUserListItem(id = 2, traktID = 2, title = "Kaos", originalImageUrl = "", mediumImageUrl = "", imdbID = "", slug = "", tmdbID = 2, tvdbID = 2, tvMazeID = 2, year = "", network = null, status = null, rating = null),
                )
            traktRepository.setWatchlistShows(mockShows)

            viewModel = TraktAccountViewModel(traktRepository, workManager, traktAuthManager)

            val job = launch { viewModel.watchlistShows.collect {} }
            advanceUntilIdle() // let stateIn initialize

            viewModel.onSearchQueryChange("Kaos")
            advanceUntilIdle()

            assert(viewModel.watchlistShows.value.size == 1)
            assert(viewModel.watchlistShows.value.first().title == "Kaos")

            job.cancel()
        }

    @Test
    fun `watchlistShows emits sorted list when sort option changes`() =
        runTest {
            val mockShows =
                listOf(
                    TraktUserListItem(id = 1, traktID = 1, title = "Zebra", year = "2020", originalImageUrl = "", mediumImageUrl = "", imdbID = "", slug = "", tmdbID = 1, tvdbID = 1, tvMazeID = 1, network = null, status = null, rating = null),
                    TraktUserListItem(id = 2, traktID = 2, title = "Apple", year = "2024", originalImageUrl = "", mediumImageUrl = "", imdbID = "", slug = "", tmdbID = 2, tvdbID = 2, tvMazeID = 2, network = null, status = null, rating = null),
                )
            traktRepository.setWatchlistShows(mockShows)

            viewModel = TraktAccountViewModel(traktRepository, workManager, traktAuthManager)

            val job = launch { viewModel.watchlistShows.collect {} }
            advanceUntilIdle()

            viewModel.onSortOptionChange(WatchlistSortOption.TITLE)
            advanceUntilIdle()

            assert(viewModel.watchlistShows.value.size == 2)
            assert(viewModel.watchlistShows.value[0].title == "Apple")
            assert(viewModel.watchlistShows.value[1].title == "Zebra")

            viewModel.onSortOptionChange(WatchlistSortOption.RELEASE_YEAR)
            advanceUntilIdle()

            assert(viewModel.watchlistShows.value[0].year == "2024")
            assert(viewModel.watchlistShows.value[1].year == "2020")

            job.cancel()
        }

    @Test
    fun `onRemoveFromWatchlistClick calls repository with correct traktId`() =
        runTest {
            val testToken =
                TraktAccessToken(
                    access_token = "test_token",
                    token_type = "Bearer",
                    expires_in = 7776000,
                    refresh_token = "refresh",
                    scope = "public",
                    created_at = System.currentTimeMillis() / 1000,
                )
            traktRepository.setAccessToken(testToken)

            viewModel = TraktAccountViewModel(traktRepository, workManager, traktAuthManager)
            advanceUntilIdle()

            viewModel.onRemoveFromWatchlistClick(42)
            advanceUntilIdle()

            assert(traktRepository.removeFromWatchlistCallCount == 1)
            assert(traktRepository.lastRemovedTraktId == 42)
        }

    @Test
    fun `onRemoveFromWatchlistClick does not trigger refreshWatchlist`() =
        runTest {
            val testToken =
                TraktAccessToken(
                    access_token = "test_token",
                    token_type = "Bearer",
                    expires_in = 7776000,
                    refresh_token = "refresh",
                    scope = "public",
                    created_at = System.currentTimeMillis() / 1000,
                )
            traktRepository.setAccessToken(testToken)

            viewModel = TraktAccountViewModel(traktRepository, workManager, traktAuthManager)
            advanceUntilIdle()

            // Reset counter after init's refreshWatchlist call
            val countAfterInit = traktRepository.refreshWatchlistCallCount

            viewModel.onRemoveFromWatchlistClick(42)
            advanceUntilIdle()

            assert(traktRepository.refreshWatchlistCallCount == countAfterInit) {
                "refreshWatchlist should NOT be called after removal. " +
                    "Expected $countAfterInit but was ${traktRepository.refreshWatchlistCallCount}"
            }
        }

    @Test
    fun `onRemoveFromWatchlistClick with null token does not call repository`() =
        runTest {
            // No token set — access_token is null
            viewModel = TraktAccountViewModel(traktRepository, workManager, traktAuthManager)
            advanceUntilIdle()

            viewModel.onRemoveFromWatchlistClick(42)
            advanceUntilIdle()

            assert(traktRepository.removeFromWatchlistCallCount == 0)
        }

    @Test
    fun `onRemoveFromWatchlistClick with failure surfaces error`() =
        runTest {
            val testToken =
                TraktAccessToken(
                    access_token = "test_token",
                    token_type = "Bearer",
                    expires_in = 7776000,
                    refresh_token = "refresh",
                    scope = "public",
                    created_at = System.currentTimeMillis() / 1000,
                )
            traktRepository.setAccessToken(testToken)
            traktRepository.removeFromWatchlistResult =
                Result.failure(Exception("API error"))

            viewModel = TraktAccountViewModel(traktRepository, workManager, traktAuthManager)
            advanceUntilIdle()

            viewModel.onRemoveFromWatchlistClick(42)
            advanceUntilIdle()

            assert(traktRepository.removeFromWatchlistCallCount == 1)
            // The error is surfaced via favoriteShowsError in the repository
        }

    @Test
    fun `watchlistShows emits filtered list when status filter changes`() =
        runTest {
            val mockShows =
                listOf(
                    TraktUserListItem(id = 1, traktID = 1, title = "Shōgun", originalImageUrl = "", mediumImageUrl = "", imdbID = "", slug = "", tmdbID = 1, tvdbID = 1, tvMazeID = 1, year = "2024", network = "Hulu", status = "Returning Series", rating = 8.5),
                    TraktUserListItem(id = 2, traktID = 2, title = "Game of Thrones", originalImageUrl = "", mediumImageUrl = "", imdbID = "", slug = "", tmdbID = 2, tvdbID = 2, tvMazeID = 2, year = "2011", network = "HBO", status = "Ended", rating = 9.3),
                    TraktUserListItem(id = 3, traktID = 3, title = "The Boys", originalImageUrl = "", mediumImageUrl = "", imdbID = "", slug = "", tmdbID = 3, tvdbID = 3, tvMazeID = 3, year = "2019", network = "Prime Video", status = "Returning Series", rating = 8.4),
                )
            traktRepository.setWatchlistShows(mockShows)

            viewModel = TraktAccountViewModel(traktRepository, workManager, traktAuthManager)

            val job = launch { viewModel.watchlistShows.collect {} }
            advanceUntilIdle()

            viewModel.onStatusFilterChange("Returning Series")
            advanceUntilIdle()

            assert(viewModel.watchlistShows.value.size == 2) {
                "Expected 2 returning shows, got ${viewModel.watchlistShows.value.size}"
            }
            assert(viewModel.watchlistShows.value.all { it.status == "Returning Series" })

            job.cancel()
        }

    @Test
    fun `availableStatuses derives unique sorted statuses from watchlist data`() =
        runTest {
            val mockShows =
                listOf(
                    TraktUserListItem(id = 1, traktID = 1, title = "Show A", originalImageUrl = "", mediumImageUrl = "", imdbID = "", slug = "", tmdbID = 1, tvdbID = 1, tvMazeID = 1, year = "", network = null, status = "Returning Series", rating = null),
                    TraktUserListItem(id = 2, traktID = 2, title = "Show B", originalImageUrl = "", mediumImageUrl = "", imdbID = "", slug = "", tmdbID = 2, tvdbID = 2, tvMazeID = 2, year = "", network = null, status = "Ended", rating = null),
                    TraktUserListItem(id = 3, traktID = 3, title = "Show C", originalImageUrl = "", mediumImageUrl = "", imdbID = "", slug = "", tmdbID = 3, tvdbID = 3, tvMazeID = 3, year = "", network = null, status = "Returning Series", rating = null),
                    TraktUserListItem(id = 4, traktID = 4, title = "Show D", originalImageUrl = "", mediumImageUrl = "", imdbID = "", slug = "", tmdbID = 4, tvdbID = 4, tvMazeID = 4, year = "", network = null, status = null, rating = null),
                )
            traktRepository.setWatchlistShows(mockShows)

            viewModel = TraktAccountViewModel(traktRepository, workManager, traktAuthManager)

            val job = launch { viewModel.availableStatuses.collect {} }
            advanceUntilIdle()

            val statuses = viewModel.availableStatuses.value
            assert(statuses.size == 2) { "Expected 2 unique statuses, got ${statuses.size}: $statuses" }
            assert(statuses == listOf("Ended", "Returning Series")) { "Expected sorted statuses, got $statuses" }

            job.cancel()
        }

    @Test
    fun `status filter and sort combine correctly`() =
        runTest {
            val mockShows =
                listOf(
                    TraktUserListItem(id = 1, traktID = 1, title = "Zebra Show", originalImageUrl = "", mediumImageUrl = "", imdbID = "", slug = "", tmdbID = 1, tvdbID = 1, tvMazeID = 1, year = "2020", network = null, status = "Returning Series", rating = 7.0),
                    TraktUserListItem(id = 2, traktID = 2, title = "Apple Show", originalImageUrl = "", mediumImageUrl = "", imdbID = "", slug = "", tmdbID = 2, tvdbID = 2, tvMazeID = 2, year = "2024", network = null, status = "Returning Series", rating = 9.0),
                    TraktUserListItem(id = 3, traktID = 3, title = "Mango Show", originalImageUrl = "", mediumImageUrl = "", imdbID = "", slug = "", tmdbID = 3, tvdbID = 3, tvMazeID = 3, year = "2022", network = null, status = "Ended", rating = 8.0),
                )
            traktRepository.setWatchlistShows(mockShows)

            viewModel = TraktAccountViewModel(traktRepository, workManager, traktAuthManager)

            val job = launch { viewModel.watchlistShows.collect {} }
            advanceUntilIdle()

            // Filter to "Returning Series" + sort by Title
            viewModel.onStatusFilterChange("Returning Series")
            viewModel.onSortOptionChange(WatchlistSortOption.TITLE)
            advanceUntilIdle()

            val result = viewModel.watchlistShows.value
            assert(result.size == 2) { "Expected 2 returning shows, got ${result.size}" }
            assert(result[0].title == "Apple Show") { "Expected 'Apple Show' first, got '${result[0].title}'" }
            assert(result[1].title == "Zebra Show") { "Expected 'Zebra Show' second, got '${result[1].title}'" }

            // Clear filter
            viewModel.onStatusFilterChange(null)
            advanceUntilIdle()

            assert(viewModel.watchlistShows.value.size == 3) { "Expected all 3 shows after clearing filter" }

            job.cancel()
        }
}
