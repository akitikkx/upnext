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

package com.theupnextapp.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.SaverScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DestinationsNavSaverTest {

    private val dummySaverScope = object : SaverScope {
        override fun canBeSaved(value: Any): Boolean = true
    }

    @Test
    fun `save and restore initial backstack`() {
        val original = mutableStateListOf<Any>(Destinations.EmptyDetail)

        val saved = with(DestinationsNavSaver) { dummySaverScope.save(original) }
        val restored = DestinationsNavSaver.restore(saved!!)

        assertNotNull(restored)
        assertEquals(1, restored!!.size)
        assertEquals(Destinations.EmptyDetail, restored[0])
    }

    @Test
    fun `save and restore complex show navigation backstack across config changes`() {
        val showDetail = Destinations.ShowDetail(
            source = "dashboard",
            showId = "123",
            showTitle = "Saturday Night Live",
            showImageUrl = "http://example.com/poster.jpg",
            showBackgroundUrl = "http://example.com/banner.jpg",
            imdbID = "tt0072562",
            isAuthorizedOnTrakt = true,
            showTraktId = 999,
        )
        val showSeasons = Destinations.ShowSeasons(
            source = "show_detail",
            showId = "123",
            showTitle = "Saturday Night Live",
            showImageUrl = "http://example.com/poster.jpg",
            showBackgroundUrl = "http://example.com/banner.jpg",
            imdbID = "tt0072562",
            isAuthorizedOnTrakt = true,
            showTraktId = 999,
        )
        val episodeDetail = Destinations.EpisodeDetail(
            showTraktId = 999,
            seasonNumber = 50,
            episodeNumber = 1,
            showTitle = "Saturday Night Live",
            showId = 123,
            imdbID = "tt0072562",
            isAuthorizedOnTrakt = true,
            showImageUrl = "http://example.com/poster.jpg",
            showBackgroundUrl = "http://example.com/banner.jpg",
            episodeImageUrl = "http://example.com/ep1.jpg",
            isWatched = true,
        )

        val original = mutableStateListOf<Any>(
            Destinations.EmptyDetail,
            showDetail,
            showSeasons,
            episodeDetail,
        )

        val saved = with(DestinationsNavSaver) { dummySaverScope.save(original) }
        val restored = DestinationsNavSaver.restore(saved!!)

        assertNotNull(restored)
        assertEquals(4, restored!!.size)
        assertEquals(Destinations.EmptyDetail, restored[0])
        assertEquals(showDetail, restored[1])
        assertEquals(showSeasons, restored[2])
        assertEquals(episodeDetail, restored[3])
    }

    @Test
    fun `restore empty list yields default EmptyDetail`() {
        val restored = DestinationsNavSaver.restore(ArrayList())
        assertNotNull(restored)
        assertEquals(1, restored!!.size)
        assertEquals(Destinations.EmptyDetail, restored[0])
    }

    @Test
    fun `restore handles corrupted data gracefully`() {
        val corrupted = ArrayList<String>().apply {
            add("{\"type\":\"invalid.unknown.Type\"}")
        }
        val restored = DestinationsNavSaver.restore(corrupted)
        assertNotNull(restored)
        assertEquals(1, restored!!.size)
        assertEquals(Destinations.EmptyDetail, restored[0])
    }

    @Test
    fun `all destination types can be saved and restored`() {
        val allDestinations = listOf(
            Destinations.Dashboard,
            Destinations.Schedule,
            Destinations.Search,
            Destinations.Explore,
            Destinations.TraktAccount("auth_code_123"),
            Destinations.Settings,
            Destinations.PersonDetail("p1", "Actor Name", "http://example.com/actor.jpg"),
            Destinations.WatchHistory,
            Destinations.EmptyDetail,
        )

        val original = mutableStateListOf<Any>().apply { addAll(allDestinations) }
        val saved = with(DestinationsNavSaver) { dummySaverScope.save(original) }
        val restored = DestinationsNavSaver.restore(saved!!)

        assertNotNull(restored)
        assertEquals(allDestinations.size, restored!!.size)
        for (i in allDestinations.indices) {
            assertEquals(allDestinations[i], restored[i])
        }
    }
}
