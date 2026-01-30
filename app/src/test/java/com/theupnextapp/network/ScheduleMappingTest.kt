package com.theupnextapp.network

import com.theupnextapp.network.models.tvmaze.NetworkScheduleImage
import com.theupnextapp.network.models.tvmaze.NetworkScheduleShow
import com.theupnextapp.network.models.tvmaze.NetworkShowEpisodeLinks
import com.theupnextapp.network.models.tvmaze.NetworkShowNextEpisodeSelf
import com.theupnextapp.network.models.tvmaze.NetworkYesterdayScheduleResponse
import com.theupnextapp.network.models.tvmaze.asDatabaseModel
import org.junit.Assert.assertEquals
import org.junit.Test

class ScheduleMappingTest {
    @Test
    fun `verify yesterday schedule maps show name correctly`() {
        val episodeName = "Episode Title"
        val showName = "Show Title"

        val networkResponse =
            NetworkYesterdayScheduleResponse(
                _links = NetworkShowEpisodeLinks(NetworkShowNextEpisodeSelf("")),
                airdate = "2023-01-01",
                airstamp = "",
                airtime = "",
                id = 1,
                image = null,
                name = episodeName, // Episode Name
                number = 1,
                runtime = 60,
                season = 1,
                show =
                    NetworkScheduleShow(
                        _links = null,
                        externals = null,
                        genres = null,
                        id = 100,
                        image = NetworkScheduleImage("", ""),
                        language = "English",
                        name = showName, // Show Name
                        network = null,
                        officialSite = "",
                        premiered = "",
                        rating = null,
                        runtime = 60,
                        schedule = null,
                        status = "",
                        summary = "",
                        type = "",
                        updated = 123,
                        url = "",
                        webChannel = null,
                        weight = 100,
                    ),
                summary = "",
                url = "",
            )

        val databaseModel = networkResponse.asDatabaseModel()

        assertEquals("Mapping should use Show Name, not Episode Name", showName, databaseModel.name)
    }
}
