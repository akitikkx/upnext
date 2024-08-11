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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.database.TraktDao
import com.theupnextapp.domain.Result
import com.theupnextapp.domain.ShowCast
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.ShowDetailSummary
import com.theupnextapp.domain.ShowNextEpisode
import com.theupnextapp.domain.ShowPreviousEpisode
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.domain.TraktUserListItem
import com.theupnextapp.domain.asDatabaseModel
import com.theupnextapp.domain.emptyShowData
import com.theupnextapp.repository.ShowDetailRepository
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.ui.showDetail.ShowDetailViewModel
import com.theupnextapp.work.AddFavoriteShowWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class ShowDetailVMTest {

    @get:Rule
    val rule = MockitoJUnit.rule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var traktDao: TraktDao

    @Mock
    private lateinit var showDetailRepository: ShowDetailRepository

    @Mock
    private lateinit var workManager: WorkManager

    @Mock
    private lateinit var traktRepository: TraktRepository

    @Mock
    private lateinit var firebaseCrashlytics: FirebaseCrashlytics

    private val dispatcher = StandardTestDispatcher()

    private lateinit var viewModel: ShowDetailViewModel

    private lateinit var testScope: TestScope

    @Before
    fun setup() {
        testScope = TestScope(dispatcher)
        Dispatchers.setMain(dispatcher)

        // Mock traktAccessToken flow
        val accessToken = TraktAccessToken(
            access_token = ACCESS_TOKEN,
            refresh_token = REFRESH_TOKEN,
            expires_in = EXPIRES_IN,
            scope = SCOPE,
            token_type = TOKEN_TYPE,
            created_at = CREATED_AT
        )

        val mockTraktAccessToken = MutableStateFlow<TraktAccessToken?>(accessToken)
        Mockito.`when`(traktRepository.traktAccessToken).thenReturn(mockTraktAccessToken)

        val mockTraktAccessData =
            flow { emit(accessToken.asDatabaseModel()) }
        Mockito.`when`(traktDao.getTraktAccessData()).thenReturn(mockTraktAccessData)

        val mockFavoriteShow =
            MutableStateFlow<TraktUserListItem?>(null)

        Mockito.`when`(traktRepository.favoriteShow).thenReturn(mockFavoriteShow)

        Mockito.`when`(showDetailRepository.isLoading).thenReturn(
            flowOf(false).stateIn(
                scope = testScope,
                started = SharingStarted.Eagerly,
                initialValue = false
            )
        )

        Mockito.`when`(traktRepository.isLoading).thenReturn(
            flowOf(false).stateIn(
                scope = testScope,
                started = SharingStarted.Eagerly,
                initialValue = false
            )
        )

        viewModel = ShowDetailViewModel(
            showDetailRepository,
            workManager,
            traktRepository,
            firebaseCrashlytics
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `selectedShow emits show and triggers getShowSummary`() = testScope.runTest {
        val show = ShowDetailArg(
            showId = "1",
            showTitle = "Test Show",
            showBackgroundUrl = "test_url",
            showImageUrl = "test_image_url"
        )

        viewModel.selectedShow(show)

        val emittedShow = viewModel.show.first()
        assertEquals(show, emittedShow)
    }

    @Test
    fun `getShowSummary success`() = testScope.runTest {
        val showId = 1
        val showSummary = ShowDetailSummary(
            airDays = "Monday, Tuesday",
            averageRating = "8.5",
            id = 1,
            imdbID = "tt12345",
            genres = "Comedy, Drama",
            language = "English",
            mediumImageUrl = "medium_url",
            name = "Test Show",
            originalImageUrl = "original_url",
            summary = "Test summary",
            time = "12:00 PM",
            status = "Running",
            previousEpisodeHref = "prev_episode_href",
            nextEpisodeHref = "next_episode_href",
            nextEpisodeLinkedId = 1,
            previousEpisodeLinkedId = 2
        )

        Mockito.`when`(showDetailRepository.getShowSummary(showId)).thenReturn(
            flow { emit(Result.Success(showSummary)) }
        )

        viewModel.selectedShow(
            ShowDetailArg(
                showId = showId.toString(),
                showTitle = "Test Show",
                showBackgroundUrl = "test_url",
                showImageUrl = "test_image_url"
            )
        )

        advanceUntilIdle()

        assertEquals(showSummary, viewModel.showSummary.value)
    }

    @Test
    fun `getShowSummary handles empty showId`() = testScope.runTest {
        viewModel.selectedShow(
            ShowDetailArg(
                showId = "",
                showTitle = "Test Show",
                showBackgroundUrl = "test_url",
                showImageUrl = "test_image_url"
            )
        )

        advanceUntilIdle()

        assertEquals(emptyShowData(), viewModel.showSummary.value)
    }

    @Test
    fun `getShowSummary handles loading and error states`() = testScope.runTest {
        val showId = 1

        Mockito.`when`(showDetailRepository.getShowSummary(showId)).thenReturn(
            flow {
                emit(Result.Loading(true))
                emit(Result.UnknownError(Exception("Test exception")))
            }
        )

        viewModel.selectedShow(
            ShowDetailArg(
                showId = showId.toString(),
                showTitle = "Test Show",
                showBackgroundUrl = "test_url",
                showImageUrl = "test_image_url"
            )
        )

        advanceUntilIdle()

        // Verify loading state
        assert(viewModel.isLoading.value)

        // Verify error handling and state
        assertNull(viewModel.showSummary.value)
        Mockito.verify(firebaseCrashlytics).recordException(Mockito.any(Exception::class.java))
    }

    @Test
    fun `getShowPreviousEpisode success`() = testScope.runTest {
        val previousEpisode = ShowPreviousEpisode(
            previousEpisodeId = 1,
            previousEpisodeAirdate = "2023-01-01",
            previousEpisodeAirstamp = "2023-01-01T12:00:00Z",
            previousEpisodeAirtime = "12:00 PM",
            previousEpisodeMediumImageUrl = "previous_episode_medium_url",
            previousEpisodeOriginalImageUrl = "previous_episode_original_url",
            previousEpisodeName = "previous_episode_name",
            previousEpisodeNumber = "previous_episode_number",
            previousEpisodeRuntime = "previous_episode_runtime",
            previousEpisodeSeason = "previous_episode_season",
            previousEpisodeSummary = "previous_episode_summary",
            previousEpisodeUrl = "previous_episode_url"
        )

        Mockito.`when`(showDetailRepository.getPreviousEpisode(anyString())).thenAnswer {
            runBlocking { Result.Success(previousEpisode) }
        }

        viewModel._showSummary.value = ShowDetailSummary(
            airDays = "Monday, Tuesday",
            averageRating = "8.5",
            id = 1,
            imdbID = "tt12345",
            genres = "Comedy, Drama",
            language = "English",
            mediumImageUrl = "medium_url",
            name = "Test Show",
            originalImageUrl = "original_url",
            summary = "Test summary",
            time = "12:00 PM",
            status = "Running",
            previousEpisodeHref = "prev_episode_href",
            nextEpisodeHref = "next_episode_href",
            nextEpisodeLinkedId = 1,
            previousEpisodeLinkedId = 2
        )

        advanceUntilIdle()

        assertEquals(previousEpisode, viewModel.showPreviousEpisode.value)
    }

    @Test
    fun `getShowNextEpisode success`() = testScope.runTest {
        val nextEpisode = ShowNextEpisode(
            nextEpisodeId = 1,
            nextEpisodeAirdate = "2023-01-01",
            nextEpisodeAirstamp = "2023-01-01T12:00:00Z",
            nextEpisodeAirtime = "12:00 PM",
            nextEpisodeMediumImageUrl = "next_episode_medium_url",
            nextEpisodeOriginalImageUrl = "next_episode_original_url",
            nextEpisodeName = "next_episode_name",
            nextEpisodeNumber = "next_episode_number",
            nextEpisodeRuntime = "next_episode_runtime",
            nextEpisodeSeason = "next_episode_season",
            nextEpisodeSummary = "next_episode_summary",
            nextEpisodeUrl = "next_episode_url"
        )

        Mockito.`when`(showDetailRepository.getNextEpisode(anyString())).thenAnswer {
            runBlocking { Result.Success(nextEpisode) }
        }

        viewModel._showSummary.value = ShowDetailSummary(
            airDays = "Monday, Tuesday",
            averageRating = "8.5",
            id = 1,
            imdbID = "tt12345",
            genres = "Comedy, Drama",
            language = "English",
            mediumImageUrl = "medium_url",
            name = "Test Show",
            originalImageUrl = "original_url",
            summary = "Test summary",
            time = "12:00 PM",
            status = "Running",
            previousEpisodeHref = "prev_episode_href",
            nextEpisodeHref = "next_episode_href",
            nextEpisodeLinkedId = 1,
            previousEpisodeLinkedId = 2
        )

        advanceUntilIdle()

        assertEquals(nextEpisode, viewModel.showNextEpisode.value)
    }

    @Test
    fun `getShowCast success`() = testScope.runTest {
        val showCast = listOf(
            ShowCast(
                id = 1,
                name = "Test Actor",
                country = "USA",
                birthday = "1990-01-01",
                deathday = null,
                gender = "Male",
                originalImageUrl = "original_image_url",
                mediumImageUrl = "medium_image_url",
                characterId = 1,
                characterUrl = "character_url",
                characterName = "Test Character",
                characterMediumImageUrl = "character_medium_image_url",
                characterOriginalImageUrl = "character_original_image_url",
                self = false,
                voice = false
            )
        )

        Mockito.`when`(showDetailRepository.getShowCast(anyInt())).thenReturn(
            flow { emit(Result.Success(showCast)) }
        )

        viewModel.selectedShow(
            ShowDetailArg(
                showId = "1",
                showTitle = "Test Show",
                showBackgroundUrl = "test_url",
                showImageUrl = "test_image_url"
            )
        )

        advanceUntilIdle()

        assertEquals(showCast, viewModel.showCast.value)
    }

    @Test
    fun `onAddRemoveFavoriteClick adds favorite show when not already favorite`() =
        testScope.runTest {
            val accessToken = TraktAccessToken(
                access_token = ACCESS_TOKEN,
                created_at = CREATED_AT,
                expires_in = EXPIRES_IN,
                refresh_token = REFRESH_TOKEN,
                scope = SCOPE,
                token_type = TOKEN_TYPE
            )
            val showSummary = ShowDetailSummary(
                airDays = "Monday, Tuesday",
                averageRating = "8.5",
                id = 1,
                imdbID = "tt12345",
                genres = "Comedy, Drama",
                language = "English",
                mediumImageUrl = "medium_url",
                name = "Test Show",
                originalImageUrl = "original_url",
                summary = "Test summary",
                time = "12:00 PM",
                status = "Running",
                previousEpisodeHref = "prev_episode_href",
                nextEpisodeHref = "next_episode_href",
                nextEpisodeLinkedId = 1,
                previousEpisodeLinkedId = 2
            )

            Mockito.`when`(traktRepository.favoriteShow).thenReturn(MutableStateFlow(null))
            viewModel._showSummary.value = showSummary
            val mockTraktAccessToken = MutableStateFlow<TraktAccessToken?>(accessToken)
            Mockito.`when`(traktRepository.traktAccessToken).thenReturn(mockTraktAccessToken)

            viewModel.onAddRemoveFavoriteClick()

            advanceUntilIdle()

            Mockito.verify(workManager).enqueue(
                listOf(
                    Mockito.argThat { workRequest ->
                        workRequest is OneTimeWorkRequest &&
                                workRequest.workSpec.input.keyValueMap[AddFavoriteShowWorker.ARG_IMDB_ID] == "tt12345" &&
                                workRequest.workSpec.input.keyValueMap[AddFavoriteShowWorker.ARG_TOKEN] == accessToken.access_token
                    }
                )
            )
        }

    companion object {
        const val EXPIRES_IN = 3600L
        const val ACCESS_TOKEN = "test_access_token"
        const val CREATED_AT = 1623456789L
        const val REFRESH_TOKEN =  "test_refresh_token"
        const val SCOPE =  "test_scope"
        const val TOKEN_TYPE =  "test_token_type"
    }
}