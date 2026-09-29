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
class TraktDaoListsTest {

    private lateinit var database: UpnextDatabase
    private lateinit var traktDao: TraktDao

    private val customList1 = DatabaseCustomList(
        traktId = 101,
        slug = "best-sci-fi",
        name = "Best Sci-Fi",
        description = "Favorite sci-fi television shows",
        itemCount = 2,
        updatedAt = "2026-09-20T10:00:00.000Z",
        likes = 15,
    )

    private val customList2 = DatabaseCustomList(
        traktId = 102,
        slug = "comedy-gold",
        name = "Comedy Gold",
        description = "Laugh out loud shows",
        itemCount = 1,
        updatedAt = "2026-09-25T14:30:00.000Z",
        likes = 8,
    )

    private val item1 = DatabaseCustomListItem(
        listTraktId = 101,
        traktID = 1001,
        id = 1,
        title = "Severance",
        year = "2022",
        mediumImageUrl = null,
        originalImageUrl = null,
        imdbID = "tt11280740",
        slug = "severance",
        tmdbID = 95396,
        tvdbID = 371980,
        tvMazeID = 44458,
        network = "Apple TV+",
        status = "returning series",
        rating = 8.7,
        rank = 1,
        listedAt = "2026-09-20T10:05:00.000Z",
    )

    private val item2 = DatabaseCustomListItem(
        listTraktId = 101,
        traktID = 1002,
        id = 2,
        title = "Dark",
        year = "2017",
        mediumImageUrl = null,
        originalImageUrl = null,
        imdbID = "tt5753856",
        slug = "dark",
        tmdbID = 70523,
        tvdbID = 334824,
        tvMazeID = 17825,
        network = "Netflix",
        status = "ended",
        rating = 8.9,
        rank = 2,
        listedAt = "2026-09-20T10:10:00.000Z",
    )

    @Before
    fun setup() {
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
    fun insertAndQueryCustomListsFlow() = runTest {
        traktDao.insertCustomLists(listOf(customList1, customList2))

        val lists = traktDao.getCustomListsFlow().first()
        assertEquals(2, lists.size)
        // Ordered by name ASC: "Best Sci-Fi" then "Comedy Gold"
        assertEquals("Best Sci-Fi", lists[0].name)
        assertEquals("Comedy Gold", lists[1].name)
        assertEquals(15, lists[0].likes)
    }

    @Test
    fun queryCustomListByTraktId() = runTest {
        traktDao.insertCustomLists(listOf(customList1))

        val retrieved = traktDao.getCustomListByTraktId(101)
        assertNotNull(retrieved)
        assertEquals("best-sci-fi", retrieved?.slug)

        val nonExistent = traktDao.getCustomListByTraktId(999)
        assertNull(nonExistent)
    }

    @Test
    fun deleteMissingCustomLists() = runTest {
        traktDao.insertCustomLists(listOf(customList1, customList2))

        // Retain only customList1
        traktDao.deleteMissingCustomLists(listOf(101))

        val remaining = traktDao.getCustomListsRaw()
        assertEquals(1, remaining.size)
        assertEquals(101, remaining[0].traktId)
    }

    @Test
    fun clearCustomLists() = runTest {
        traktDao.insertCustomLists(listOf(customList1, customList2))
        traktDao.clearCustomLists()

        val lists = traktDao.getCustomListsRaw()
        assertTrue(lists.isEmpty())
    }

    @Test
    fun insertAndQueryCustomListItemsFlow() = runTest {
        traktDao.insertCustomListItems(listOf(item1, item2))

        val items = traktDao.getCustomListItemsFlow(101).first()
        assertEquals(2, items.size)
        // Ordered by rank ASC: item1 (rank 1), item2 (rank 2)
        assertEquals("Severance", items[0].title)
        assertEquals("Dark", items[1].title)
    }

    @Test
    fun updateCustomListItemImages() = runTest {
        traktDao.insertCustomListItems(listOf(item1))

        traktDao.updateCustomListItemImages(
            showTraktId = 1001,
            posterUrl = "https://example.com/poster.jpg",
            heroImageUrl = "https://example.com/hero.jpg",
            tvMazeId = 44458,
        )

        val updated = traktDao.getCustomListItemsRaw(101).first { it.traktID == 1001 }
        assertEquals("https://example.com/poster.jpg", updated.originalImageUrl)
        assertEquals("https://example.com/hero.jpg", updated.mediumImageUrl)
        assertEquals(44458, updated.tvMazeID)
    }

    @Test
    fun deleteMissingCustomListItems() = runTest {
        traktDao.insertCustomListItems(listOf(item1, item2))

        // Only retain item2 (traktID = 1002) for list 101
        traktDao.deleteMissingCustomListItems(101, listOf(1002))

        val remaining = traktDao.getCustomListItemsRaw(101)
        assertEquals(1, remaining.size)
        assertEquals(1002, remaining[0].traktID)
    }

    @Test
    fun clearCustomListItems() = runTest {
        traktDao.insertCustomListItems(listOf(item1, item2))
        traktDao.clearCustomListItems(101)

        val items = traktDao.getCustomListItemsRaw(101)
        assertTrue(items.isEmpty())
    }
}
