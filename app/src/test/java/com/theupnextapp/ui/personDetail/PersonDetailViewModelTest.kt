package com.theupnextapp.ui.personDetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.theupnextapp.CoroutineTestRule
import com.theupnextapp.domain.Result
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupResponse
import com.theupnextapp.repository.fakes.FakeShowDetailRepository
import com.theupnextapp.repository.fakes.FakeTraktRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import java.io.IOException

@ExperimentalCoroutinesApi
class PersonDetailViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var viewModel: PersonDetailViewModel
    private lateinit var fakeShowDetailRepository: FakeShowDetailRepository
    private lateinit var fakeTraktRepository: FakeTraktRepository

    @Before
    fun setUp() {
        fakeShowDetailRepository = FakeShowDetailRepository()
        fakeTraktRepository = FakeTraktRepository()
        viewModel = PersonDetailViewModel(fakeTraktRepository, fakeShowDetailRepository)
    }

    @Test
    fun `onCreditClicked sets navigateToShowDetail on successful lookup`() =
        runTest {
            val testImdbId = "tt123456"
            val testTitle = "Fake Show"
            val testTraktId = 1234

            // Arrange: Fake an id resolution
            val mockResponse =
                mock<NetworkTvMazeShowLookupResponse> {
                    on { id }.thenReturn(9999)
                }
            fakeShowDetailRepository.showLookupResult = Result.Success(mockResponse)

            // Act
            viewModel.onCreditClicked(
                imdbId = testImdbId,
                title = testTitle,
                traktId = testTraktId,
            )

            // Assert
            val arg = viewModel.navigateToShowDetail.first { it != null }
            assertNotNull(arg)
            assertEquals("9999", arg?.showId)
            assertEquals(testImdbId, arg?.imdbID)
            assertEquals(testTitle, arg?.showTitle)
            assertEquals(testTraktId, arg?.showTraktId)
        }

    @Test
    fun `onCreditClicked sets errorMessage on lookup failure`() =
        runTest {
            // Arrange: Fake a TVMaze 404
            fakeShowDetailRepository.showLookupResult = Result.NetworkError(IOException())

            // Act
            viewModel.onCreditClicked(
                imdbId = "tt999999",
                title = "Missing Show",
                traktId = null,
            )

            // Assert
            val uiState = viewModel.uiState.first { it.errorMessage != null }
            assertEquals("The show details are not available at present", uiState.errorMessage)
            assertNull(viewModel.navigateToShowDetail.value)
        }

    @Test
    fun `clearErrorMessage successfully unsets the active error state`() =
        runTest {
            // Arrange: Setup a fake error state
            fakeShowDetailRepository.showLookupResult = Result.NetworkError(IOException())
            viewModel.onCreditClicked(imdbId = "tt999999", title = "Missing", traktId = null)

            val errorState = viewModel.uiState.first { it.errorMessage != null }
            assertEquals("The show details are not available at present", errorState.errorMessage)

            // Act
            viewModel.clearErrorMessage()

            // Assert
            val clearedState = viewModel.uiState.first { it.errorMessage == null }
            assertNull(clearedState.errorMessage)
        }
}
