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

package com.theupnextapp.repository

import com.theupnextapp.database.DatabaseTableUpdate
import com.theupnextapp.network.TvMazeService
import com.theupnextapp.repository.fakes.FakeUpnextDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class BaseRepositoryTest {

    private lateinit var fakeUpnextDao: FakeUpnextDao

    @Mock
    private lateinit var mockTvMazeService: TvMazeService

    private lateinit var repository: ConcreteTestRepository

    @Before
    fun setUp() {
        fakeUpnextDao = FakeUpnextDao()
        repository = ConcreteTestRepository(fakeUpnextDao, mockTvMazeService)
    }

    private val testTableName = "test_shows"
    private val shortIntervalMinutes = 30L

    @Test
    fun `canProceedWithUpdate should return true when no last update time exists`() {
        // Act
        val canProceed =
            repository.testCanProceedWithUpdate(testTableName, shortIntervalMinutes)

        assertTrue(
            "Should proceed if no last update time is recorded.", canProceed
        )
    }

    @Test
    fun `canProceedWithUpdate should return true when update interval has passed`() {
        // Arrange
        val currentTime = System.currentTimeMillis()
        // Make sure the interval has more than passed
        val lastUpdateTimeMillis =
            currentTime - ((shortIntervalMinutes + 5) * 60 * 1000) // Added a bit more buffer
        fakeUpnextDao.addTableUpdate(
            DatabaseTableUpdate(
                table_name = testTableName,
                last_updated = lastUpdateTimeMillis
            )
        )

        // Act
        val canProceed =
            repository.testCanProceedWithUpdate(testTableName, shortIntervalMinutes)

        // Assert
        assertTrue("Should proceed as the interval has passed.", canProceed)
    }

    @Test
    fun `canProceedWithUpdate should return false when update interval has not passed`() {
        // Arrange
        val currentTime = System.currentTimeMillis()
        // Last update was very recent, less than the interval
        val lastUpdateTimeMillis = currentTime - ((shortIntervalMinutes - 5) * 60 * 1000)
        fakeUpnextDao.addTableUpdate(
            DatabaseTableUpdate(
                table_name = testTableName,
                last_updated = lastUpdateTimeMillis
            )
        )

        // Act
        val canProceed =
            repository.testCanProceedWithUpdate(testTableName, shortIntervalMinutes)

        // Assert
        org.junit.Assert.assertFalse(
            "Should not proceed as the interval has not passed.",
            canProceed
        )
    }

}