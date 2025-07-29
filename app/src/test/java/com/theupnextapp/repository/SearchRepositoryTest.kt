package com.theupnextapp.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.theupnextapp.CoroutineTestRule
import com.theupnextapp.domain.Result
import com.theupnextapp.domain.ShowSearch
import com.theupnextapp.network.TvMazeService
import com.theupnextapp.network.models.tvmaze.NetworkShowSearchResponse
import com.theupnextapp.network.models.tvmaze.asDomainModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class SearchRepositoryTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutineTestRule = CoroutineTestRule()

    private val tvMazeService: TvMazeService = mock()

    private lateinit var repository: SearchRepository

    @Before
    fun setup() {
        repository = SearchRepository(tvMazeService)
    }

    @Test
    fun `getShowSearchResults with valid query returns success`() = runTest {
        // GIVEN: a valid search query and a mocked successful response
        val query = "test"
        val mockResponse = listOf(
            NetworkShowSearchResponse(score = 1.0, show = mock()),
            NetworkShowSearchResponse(score = 0.5, show = mock())
        )
        val deferredResponse = CompletableDeferred(mockResponse)
        whenever(tvMazeService.getSuggestionListAsync(query)).thenReturn(deferredResponse)

        // WHEN: getShowSearchResults is called
        val resultFlow = repository.getShowSearchResults(query)
        val emissions = resultFlow.toList()

        // THEN: The flow emits Loading, followed by the success result
        assertEquals(3, emissions.size)
        assertTrue(emissions.get(0) is Result.Loading)
        assertTrue(emissions.get(1) is Result.Loading) // The second loading is for hiding the progress
        assertTrue(emissions.get(2) is Result.Success)

        val successResult = emissions.get(2) as Result.Success<List<ShowSearch>>
        assertEquals(2, successResult.data.size)
        assertEquals(mockResponse.asDomainModel(), successResult.data)
    }

    @Test
    fun `getShowSearchResults with null query returns empty list`() = runTest {
        // WHEN: getShowSearchResults is called with a null query
        val resultFlow = repository.getShowSearchResults(null)
        val emissions = resultFlow.toList()

        // THEN: The flow emits a single Success result with an empty list
        assertEquals(1, emissions.size)
        assertTrue(emissions.get(0) is Result.Success)
        val successResult = emissions.get(0) as Result.Success<List<ShowSearch>>
        assertTrue(successResult.data.isEmpty())

        // Verify that the network service was never called
        verify(tvMazeService, never()).getSuggestionListAsync(any())
    }

    @Test
    fun `getShowSearchResults with empty query returns empty list`() = runTest {
        // WHEN: getShowSearchResults is called with an empty query
        val resultFlow = repository.getShowSearchResults("")
        val emissions = resultFlow.toList()

        // THEN: The flow emits a single Success result with an empty list
        assertEquals(1, emissions.size)
        assertTrue(emissions.get(0) is Result.Success)
        val successResult = emissions.get(0) as Result.Success<List<ShowSearch>>
        assertTrue(successResult.data.isEmpty())

        // Verify that the network service was never called
        verify(tvMazeService, never()).getSuggestionListAsync(any())
    }

    @Test
    fun `getShowSearchResults handles network failure gracefully`() = runTest {
        // GIVEN: a valid search query and a mocked network failure
        val query = "test"
        val exception = RuntimeException("Network failed")
        val deferredResponse = CompletableDeferred<List<NetworkShowSearchResponse>>()
        deferredResponse.completeExceptionally(exception)
        whenever(tvMazeService.getSuggestionListAsync(query)).thenReturn(deferredResponse)

        // WHEN: getShowSearchResults is called
        val resultFlow = repository.getShowSearchResults(query)
        val emissions = resultFlow.toList()

        // THEN: The flow emits Loading, followed by an Error result
        assertEquals(3, emissions.size)
        assertTrue(emissions.get(0) is Result.Loading)
        assertTrue(emissions.get(1) is Result.Loading)
        assertTrue(emissions.get(2) is Result.Error)

        val errorResult = emissions.get(2) as Result.Error
        assertEquals("Network failed", errorResult.exception?.message)
    }
}
