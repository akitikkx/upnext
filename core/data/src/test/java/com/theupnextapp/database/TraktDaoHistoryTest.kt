/*
 * MIT License
 *
 * Copyright (c) 2026 Ahmed Tikiwa
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

package com.theupnextapp.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TraktDaoHistoryTest {

    private lateinit var database: UpnextDatabase
    private lateinit var traktDao: TraktDao

    private val item1 =
        DatabaseWatchHistory(
            historyId = 1L,
            watchedAt = "2026-09-20T20:00:00.000Z",
            watchedAtEpochMillis = 1790000000000L,
            showTraktId = 100,
            episodeTraktId = 1001,
            showTvmazeId = 10,
            showImdbId = "tt100",
            showTitle = "Severance",
            seasonNumber = 1,
            episodeNumber = 1,
            episodeTitle = "Good News About Hell",
            episodeImageUrl = null,
            showPosterUrl = null,
        )

    private val item2 =
        DatabaseWatchHistory(
            historyId = 2L,
            watchedAt = "2026-09-21T21:00:00.000Z",
            watchedAtEpochMillis = 1790090000000L,
            showTraktId = 100,
            episodeTraktId = 1002,
            showTvmazeId = 10,
            showImdbId = "tt100",
            showTitle = "Severance",
            seasonNumber = 1,
            episodeNumber = 2,
            episodeTitle = "Half Loop",
            episodeImageUrl = null,
            showPosterUrl = null,
        )

    private val item3 =
        DatabaseWatchHistory(
            historyId = 3L,
            watchedAt = "2026-08-15T18:00:00.000Z",
            watchedAtEpochMillis = 1787000000000L,
            showTraktId = 200,
            episodeTraktId = 2001,
            showTvmazeId = 20,
            showImdbId = "tt200",
            showTitle = "Slow Horses",
            seasonNumber = 1,
            episodeNumber = 1,
            episodeTitle = "Failure's Contagious",
            episodeImageUrl = "http://slowhorses.jpg",
            showPosterUrl = "http://poster.jpg",
        )

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, UpnextDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        traktDao = database.traktDao
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveWatchHistory_orderedByWatchedAtDescending() = runTest {
        traktDao.insertWatchHistory(listOf(item1, item2, item3))

        val results = traktDao.getWatchHistoryRaw()
        assertEquals(3, results.size)
        // Most recent first: item2, then item1, then item3
        assertEquals(2L, results[0].historyId)
        assertEquals(1L, results[1].historyId)
        assertEquals(3L, results[2].historyId)
    }

    @Test
    fun getWatchHistoryFlow_emitsOrderedList() = runTest {
        traktDao.insertWatchHistory(listOf(item1, item2))

        val flowResult = traktDao.getWatchHistoryFlow().first()
        assertEquals(2, flowResult.size)
        assertEquals(2L, flowResult[0].historyId)
        assertEquals(1L, flowResult[1].historyId)
    }

    @Test
    fun getLatestAndOldestWatchedTimestamp() = runTest {
        traktDao.insertWatchHistory(listOf(item1, item2, item3))

        val latest = traktDao.getLatestWatchedTimestamp()
        val oldest = traktDao.getOldestWatchedTimestamp()

        assertEquals("2026-09-21T21:00:00.000Z", latest)
        assertEquals("2026-08-15T18:00:00.000Z", oldest)
    }

    @Test
    fun updateWatchHistoryImages_updatesFieldsCorrectly() = runTest {
        traktDao.insertWatchHistory(listOf(item1))

        traktDao.updateWatchHistoryImages(
            historyId = 1L,
            episodeImageUrl = "http://severance_ep1.jpg",
            showPosterUrl = "http://severance_poster.jpg",
        )

        val updated = traktDao.getWatchHistoryRaw().first { it.historyId == 1L }
        assertEquals("http://severance_ep1.jpg", updated.episodeImageUrl)
        assertEquals("http://severance_poster.jpg", updated.showPosterUrl)
    }

    @Test
    fun updateShowPosterForShow_updatesNullPostersOnly() = runTest {
        traktDao.insertWatchHistory(listOf(item1, item2))

        traktDao.updateShowPosterForShow(showTraktId = 100, showPosterUrl = "http://severance_poster.jpg")

        val results = traktDao.getWatchHistoryRaw()
        assertEquals("http://severance_poster.jpg", results[0].showPosterUrl)
        assertEquals("http://severance_poster.jpg", results[1].showPosterUrl)
    }

    @Test
    fun getWatchHistoryItemsMissingImages_returnsOnlyMissing() = runTest {
        traktDao.insertWatchHistory(listOf(item1, item2, item3))

        val missing = traktDao.getWatchHistoryItemsMissingImages(10)
        // item1 and item2 are missing images, item3 has both
        assertEquals(2, missing.size)
        assertTrue(missing.any { it.historyId == 1L })
        assertTrue(missing.any { it.historyId == 2L })
    }

    @Test
    fun deleteWatchHistoryItem_removesSpecificItem() = runTest {
        traktDao.insertWatchHistory(listOf(item1, item2))
        assertEquals(2, traktDao.getWatchHistoryCount())

        traktDao.deleteWatchHistoryItem(1L)
        assertEquals(1, traktDao.getWatchHistoryCount())
        assertEquals(2L, traktDao.getWatchHistoryRaw().first().historyId)
    }

    @Test
    fun clearWatchHistory_wipesAllEntries() = runTest {
        traktDao.insertWatchHistory(listOf(item1, item2, item3))
        assertEquals(3, traktDao.getWatchHistoryCount())

        traktDao.clearWatchHistory()
        assertEquals(0, traktDao.getWatchHistoryCount())
        assertTrue(traktDao.getWatchHistoryRaw().isEmpty())
    }
}
