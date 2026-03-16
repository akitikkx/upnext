package com.theupnextapp.ui.episodeDetail

import com.theupnextapp.navigation.Destinations
import org.junit.Assert.assertEquals
import org.junit.Test

class NavTest {
    @Test
    fun testReflection() {
        val routeObj = Destinations.EpisodeDetail(1234, 1, 5)
        assertEquals(1234, routeObj.showTraktId)
        assertEquals(1, routeObj.seasonNumber)
        assertEquals(5, routeObj.episodeNumber)
    }
}
