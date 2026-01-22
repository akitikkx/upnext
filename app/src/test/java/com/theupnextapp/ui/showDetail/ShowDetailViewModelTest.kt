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

package com.theupnextapp.ui.showDetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.WorkManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.CoroutineTestRule
import com.theupnextapp.domain.ShowCast
import com.theupnextapp.common.utils.TraktAuthManager
import com.theupnextapp.network.models.trakt.NetworkTraktPersonResponse
import com.theupnextapp.network.models.trakt.NetworkTraktPersonShowCreditsResponse
import com.theupnextapp.repository.ShowDetailRepository
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.domain.TraktAuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.junit.Ignore
import org.mockito.Mockito.anyString
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.timeout
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class ShowDetailViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @Mock
    lateinit var showDetailRepository: ShowDetailRepository

    @Mock
    lateinit var workManager: WorkManager

    @Mock
    lateinit var traktRepository: TraktRepository

    @Mock
    lateinit var firebaseCrashlytics: FirebaseCrashlytics

    @Mock
    lateinit var traktAuthManager: TraktAuthManager

    private lateinit var viewModel: ShowDetailViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        `when`(traktAuthManager.traktAuthState).thenReturn(MutableStateFlow(TraktAuthState.LoggedIn))
        viewModel = ShowDetailViewModel(
            showDetailRepository,
            workManager,
            traktRepository,

            firebaseCrashlytics,
            traktAuthManager
        )
    }

    private fun createShowCast(id: Int?, name: String?): ShowCast {
        return ShowCast(
            id = id,
            name = name,
            country = null,
            birthday = null,
            deathday = null,
            gender = null,
            originalImageUrl = null,
            mediumImageUrl = null,
            characterId = null,
            characterUrl = null,
            characterName = "Test Character",
            characterMediumImageUrl = null,
            characterOriginalImageUrl = null,
            self = false,
            voice = false
        )
    }

    @Test
    fun `onShowCastItemClicked uses ID lookup when successful`() = runTest {
        // Given
        val castId = 123
        val traktId = 456
        val showCast = createShowCast(castId, "Test Actor")

        `when`(traktRepository.getTraktPersonIdLookup(castId.toString())).thenReturn(Result.success(traktId))
        `when`(traktRepository.getTraktPersonSummary(traktId.toString())).thenReturn(Result.success(NetworkTraktPersonResponse(name = "Test Actor", ids = null, biography = null, birthday = null, death = null, birthplace = null, homepage = null, gender = null, known_for_department = null, social_ids = null)))
        `when`(traktRepository.getTraktPersonShowCredits(traktId.toString())).thenReturn(Result.success(NetworkTraktPersonShowCreditsResponse(cast = emptyList())))

        // When
        viewModel.onShowCastItemClicked(showCast)

        // Then
        verify(traktRepository, timeout(3000)).getTraktPersonIdLookup(castId.toString())
        verify(traktRepository, never()).getTraktPersonIdSearch(anyString())
    }

    @Test
    fun `onShowCastItemClicked uses Name Search fallback when ID lookup fails`() = runTest {
        // Given
        val castId = 123
        val traktId = 789
        val actorName = "Test Actor"
        val showCast = createShowCast(castId, actorName)

        // ID Lookup Fails
        `when`(traktRepository.getTraktPersonIdLookup(castId.toString())).thenReturn(Result.failure(Exception("Not found")))
        // Name Search Succeeds
        `when`(traktRepository.getTraktPersonIdSearch(actorName)).thenReturn(Result.success(traktId))
        
        `when`(traktRepository.getTraktPersonSummary(traktId.toString())).thenReturn(Result.success(NetworkTraktPersonResponse(name = actorName, ids = null, biography = null, birthday = null, death = null, birthplace = null, homepage = null, gender = null, known_for_department = null, social_ids = null)))
        `when`(traktRepository.getTraktPersonShowCredits(traktId.toString())).thenReturn(Result.success(NetworkTraktPersonShowCreditsResponse(cast = emptyList())))

        // When
        viewModel.onShowCastItemClicked(showCast)

        // Then
        verify(traktRepository, timeout(3000)).getTraktPersonIdLookup(castId.toString())
        verify(traktRepository, timeout(3000)).getTraktPersonIdSearch(actorName)
    }

    @Test
    @Ignore("Concurrency issue with Dispatchers.IO")
    fun `onShowCastItemClicked sets error when both lookups fail`() = runTest {
        // Given
        val castId = 123
        val actorName = "Test Actor"
        val showCast = createShowCast(castId, actorName)

        // ID Lookup Fails
        `when`(traktRepository.getTraktPersonIdLookup(castId.toString())).thenReturn(Result.failure(Exception("Not found")))
        // Name Search Fails
        `when`(traktRepository.getTraktPersonIdSearch(actorName)).thenReturn(Result.failure(Exception("Not found")))

        // When
        viewModel.onShowCastItemClicked(showCast)

        // Then
        verify(traktRepository).getTraktPersonIdLookup(castId.toString())
        verify(traktRepository).getTraktPersonIdSearch(actorName)
        
        // Use verify to ensure no further calls for details are made (implying failure flow)
        verify(traktRepository, never()).getTraktPersonSummary(anyString())
    }
}
