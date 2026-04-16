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

package com.theupnextapp.work

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.Data
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.theupnextapp.domain.Result
import kotlin.Result as StdResult
import com.theupnextapp.repository.TraktRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AddToWatchlistWorkerTest {

    private lateinit var context: Context
    private val traktRepository: TraktRepository = mock()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        if (FirebaseApp.getApps(context).isEmpty()) {
            val options = FirebaseOptions.Builder()
                .setApplicationId("1:1234567890:android:abcdef")
                .setApiKey("dummy_api_key")
                .setProjectId("dummy_test_project")
                .build()
            FirebaseApp.initializeApp(context, options)
        }
    }

    @Test
    fun `doWork with missing token returns failure`(): Unit = runBlocking {
        // Given
        val inputData = Data.Builder()
            .putInt(AddToWatchlistWorker.ARG_TRAKT_ID, 123)
            .putString(AddToWatchlistWorker.ARG_IMDB_ID, "tt123")
            .build()
            
        val worker = TestListenableWorkerBuilder<AddToWatchlistWorker>(context)
            .setInputData(inputData)
            .setWorkerFactory(MockWorkerFactory(traktRepository))
            .build()

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.failure(), result)
    }

    @Test
    fun `doWork with valid data extracts params and calls repository`(): Unit = runBlocking {
        // Given
        val inputData = Data.Builder()
            .putString(AddToWatchlistWorker.ARG_TOKEN, "token")
            .putInt(AddToWatchlistWorker.ARG_TRAKT_ID, 123)
            .putString(AddToWatchlistWorker.ARG_IMDB_ID, "tt123")
            .putString(AddToWatchlistWorker.ARG_TITLE, "My Show")
            .putInt(AddToWatchlistWorker.ARG_TVMAZE_ID, 222)
            .putInt(AddToWatchlistWorker.ARG_TMDB_ID, 333)
            .putString(AddToWatchlistWorker.ARG_YEAR, "2024")
            .putString(AddToWatchlistWorker.ARG_NETWORK, "HBO")
            .putString(AddToWatchlistWorker.ARG_STATUS, "Running")
            .putDouble(AddToWatchlistWorker.ARG_RATING, 9.0)
            .build()

        whenever(traktRepository.addToWatchlist(
            any(), any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), 
            anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()
        )).thenReturn(StdResult.success(Unit))

        val worker = TestListenableWorkerBuilder<AddToWatchlistWorker>(context)
            .setInputData(inputData)
            .setWorkerFactory(MockWorkerFactory(traktRepository))
            .build()

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.success(), result)
        
        val traktIdCaptor = argumentCaptor<Int>()
        val tvMazeIdCaptor = argumentCaptor<Int>()
        val networkCaptor = argumentCaptor<String>()
        val yearCaptor = argumentCaptor<String>()

        verify(traktRepository).addToWatchlist(
            traktId = traktIdCaptor.capture(),
            imdbID = any(),
            token = any(),
            title = anyOrNull(),
            originalImageUrl = anyOrNull(),
            mediumImageUrl = anyOrNull(),
            tvMazeID = tvMazeIdCaptor.capture(),
            tmdbID = anyOrNull(),
            year = yearCaptor.capture(),
            network = networkCaptor.capture(),
            status = anyOrNull(),
            rating = anyOrNull()
        )

        assertEquals(123, traktIdCaptor.firstValue)
        assertEquals(222, tvMazeIdCaptor.firstValue)
        assertEquals("HBO", networkCaptor.firstValue)
        assertEquals("2024", yearCaptor.firstValue)
    }

    class MockWorkerFactory(private val repository: TraktRepository) : androidx.work.WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: androidx.work.WorkerParameters
        ): ListenableWorker {
            return AddToWatchlistWorker(appContext, workerParameters, repository)
        }
    }
}
