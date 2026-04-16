package com.theupnextapp.network.models.trakt

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NetworkTraktWatchlistResponseTest {

    @Test
    fun `test network models parse correctly without confusing tvMazeID`() {
        val json = """
            {
                "id": 12345,
                "listed_at": "2023-01-01T00:00:00Z",
                "notes": null,
                "rank": 1,
                "type": "show",
                "show": {
                    "title": "Test Show",
                    "year": 2023,
                    "ids": {
                        "trakt": 1,
                        "slug": "test-show",
                        "tvdb": 2,
                        "imdb": "tt123456",
                        "tmdb": 3,
                        "tvrage": 4
                    }
                }
            }
        """.trimIndent()

        val gson = Gson()
        val item = gson.fromJson(json, NetworkTraktWatchlistResponseItem::class.java)

        assertEquals("tt123456", item.show.ids.imdb)
        assertEquals(4, item.show.ids.tvRage)
        
        val dbModel = item.asDatabaseModel()
        assertNull(dbModel.tvMazeID) // Must explicitly remain null, NOT pull from tvRage
        assertEquals("tt123456", dbModel.imdbID)
        assertEquals(1, dbModel.traktID)
    }
}
