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

package com.theupnextapp.ui.search

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.theupnextapp.CoroutineTestRule
import com.theupnextapp.repository.SearchRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class SearchViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private val application: Application = mock()
    private val searchRepository: SearchRepository = mock()

    private lateinit var viewModel: SearchViewModel

    @Before
    fun setup() {
        whenever(searchRepository.getRecentSearches()).thenReturn(flowOf(emptyList()))

        // Mock suspending functions by returning unit synchronously where possible
        // or using runBlockingTest internally if strictly required by mockito.
        // However, since we're using mockito-kotlin's `whenever` for suspend functions,
        // it's generally better to wrap the test execution instead of the setup block.
        // We'll leave the suspend mocks to the actual test blocks or use a runTest wrapper here if necessary,
        // but for now, we'll try basic synchronous mocks if they don't suspend, or wrap them.
        viewModel = SearchViewModel(application, searchRepository)
    }

    @Test
    fun `onQuerySaved calls repository saveSearchQuery`() =
        runTest {
            val query = "Test Query"
            whenever(searchRepository.saveSearchQuery(org.mockito.kotlin.any())).thenReturn(Unit)
            viewModel.onQuerySaved(query)
            verify(searchRepository).saveSearchQuery(query)
        }

    @Test
    fun `onClearRecentSearches calls repository clearRecentSearches`() =
        runTest {
            whenever(searchRepository.clearRecentSearches()).thenReturn(Unit)
            viewModel.onClearRecentSearches()
            verify(searchRepository).clearRecentSearches()
        }
}
