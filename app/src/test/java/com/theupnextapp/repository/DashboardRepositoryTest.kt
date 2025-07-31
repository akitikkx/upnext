package com.theupnextapp.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.theupnextapp.CoroutineTestRule
import com.theupnextapp.database.DatabaseTableUpdate
// import com.theupnextapp.database.DatabaseYesterdaySchedule // Already imported by fakes.FakeTvMazeDao if needed by test, or directly
import com.theupnextapp.repository.fakes.FakeCrashlytics // Corrected import
import com.theupnextapp.repository.fakes.FakeTvMazeService
import com.theupnextapp.repository.fakes.FakeUpnextDao
import com.theupnextapp.network.models.tvmaze.NetworkScheduleExternals
import com.theupnextapp.network.models.tvmaze.NetworkScheduleImage
import com.theupnextapp.network.models.tvmaze.NetworkScheduleShow
import com.theupnextapp.network.models.tvmaze.NetworkShowEpisodeLinks
import com.theupnextapp.network.models.tvmaze.NetworkShowNextEpisodeSelf
import com.theupnextapp.network.models.tvmaze.NetworkYesterdayScheduleResponse
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowImageResponse // Added import
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowImageResponseItem // Added import
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowImageResolutions // Added import
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowImageOriginal // Added import
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowImageMedium // Added import
import com.theupnextapp.repository.fakes.FakeTvMazeDao
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class DashboardRepositoryTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutineTestRule = CoroutineTestRule()

    private lateinit var fakeUpnextDao: FakeUpnextDao
    private lateinit var fakeTvMazeDao: FakeTvMazeDao
    private lateinit var fakeTvMazeService: FakeTvMazeService
    private lateinit var fakeCrashlytics: FakeCrashlytics // Changed type

    private lateinit var repository: DashboardRepository

    @Before
    fun setup() {
        fakeUpnextDao = FakeUpnextDao()
        fakeTvMazeDao = FakeTvMazeDao()
        fakeTvMazeService = FakeTvMazeService()
        fakeCrashlytics = FakeCrashlytics() // Changed instantiation

        // Clear fakes before each test for isolation
        fakeUpnextDao.clearAll()
        fakeTvMazeDao.clearAllData()
        fakeTvMazeService.reset()
        fakeCrashlytics.clear()

        repository = DashboardRepository(
            upnextDao = fakeUpnextDao,
            tvMazeDao = fakeTvMazeDao,
            tvMazeService = fakeTvMazeService,
            firebaseCrashlytics = fakeCrashlytics // Corrected parameter name
        )
    }

    @Test
    fun `refreshYesterdayShows - network call and db insert happen when update is needed`() = runTest {
        // GIVEN: An update is needed (no recent log in fakeUpnextDao by default)
        val mockResponse = listOf(createMockYesterdayScheduleResponse(1, "imdb1", "image.png"))
        fakeTvMazeService.mockYesterdayScheduleResponse = mockResponse
        fakeTvMazeService.mockShowImagesResponse = NetworkTvMazeShowImageResponse().apply {
            add(NetworkTvMazeShowImageResponseItem(
                id = 101, type = "poster", main = true,
                resolutions = NetworkTvMazeShowImageResolutions(
                    original = NetworkTvMazeShowImageOriginal(url = "final_original.png"),
                    medium = NetworkTvMazeShowImageMedium(url = "final_medium.png")
                )))
        }
        val testTableName = "schedule_yesterday"

        // WHEN: refreshYesterdayShows is called
        repository.refreshYesterdayShows("US", "2023-01-01")

        // THEN: The network is called, and the db is updated
        assertEquals(1, fakeTvMazeDao.yesterdayShowsList.size)
        assertEquals(1, fakeTvMazeDao.yesterdayShowsList.first().id)
        assertEquals("final_original.png", fakeTvMazeDao.yesterdayShowsList.first().image)
        assertEquals("final_medium.png", fakeTvMazeDao.yesterdayShowsList.first().mediumImage)

        val tableUpdateLog = fakeUpnextDao.getTableLastUpdateTime(testTableName)
        assertNotNull(tableUpdateLog)
        assertEquals(testTableName, tableUpdateLog?.table_name)
    }

    @Test
    fun `refreshYesterdayShows - network call and db insert do NOT happen when not needed`() = runTest {
        // GIVEN: An update is NOT needed (a recent log exists)
        val tableName = "schedule_yesterday"
        val recentUpdateTimestamp = System.currentTimeMillis()
        val initialLog = DatabaseTableUpdate(table_name = tableName, last_updated = recentUpdateTimestamp)
        fakeUpnextDao.insertTableUpdateLog(initialLog)


        // WHEN: refreshYesterdayShows is called
        repository.refreshYesterdayShows("US", "2023-01-01")

        // THEN: No network/db operations occur for yesterday's schedule
        assertTrue(fakeTvMazeDao.yesterdayShowsList.isEmpty()) // No new shows added

        val finalLog = fakeUpnextDao.getTableLastUpdateTime(tableName)
        assertEquals(initialLog, finalLog) // Log for this table should not have changed

        assertTrue(fakeCrashlytics.getRecordedExceptions().isEmpty())
    }

    @Test
    fun `refreshYesterdayShows - handles network exception gracefully`() = runTest {
        // GIVEN: An update is needed, but the network call will fail
        fakeTvMazeService.yesterdayScheduleError = IOException("Network failed")

        // WHEN: refreshYesterdayShows is called
        repository.refreshYesterdayShows("US", "2023-01-01")

        // THEN: The exception is logged and no data is inserted
        assertEquals(1, fakeCrashlytics.getRecordedExceptions().size)
        assertTrue(fakeCrashlytics.getRecordedExceptions().first() is IOException)
        assertEquals("Network failed", fakeCrashlytics.getRecordedExceptions().first().message)
        assertTrue(fakeTvMazeDao.yesterdayShowsList.isEmpty())
    }

    @Test
    fun `refreshYesterdayShows - filters out shows with no image or imdb id from initial fetch`() = runTest {
        // GIVEN: An update is needed
        val validShowNetwork = createMockYesterdayScheduleResponse(id = 1, imdb = "imdb1", image = "image.png", showId = 10)
        val showWithoutImageNetwork = createMockYesterdayScheduleResponse(id = 2, imdb = "imdb2", image = null, showId = 20, showImageOriginal = null, showImageMedium = null)
        val showWithoutImdbNetwork = createMockYesterdayScheduleResponse(id = 3, imdb = null, image = "image.png", showId = 30)

        fakeTvMazeService.mockYesterdayScheduleResponse = listOf(validShowNetwork, showWithoutImageNetwork, showWithoutImdbNetwork)

        fakeTvMazeService.mockShowImagesResponse = NetworkTvMazeShowImageResponse().apply {
            add(NetworkTvMazeShowImageResponseItem(
                id = 101, type = "poster", main = true,
                resolutions = NetworkTvMazeShowImageResolutions(
                    original = NetworkTvMazeShowImageOriginal(url = "final_original_for_show_10.png"),
                    medium = NetworkTvMazeShowImageMedium(url = "final_medium_for_show_10.png")
                )))
        }

        // WHEN: refreshYesterdayShows is called
        repository.refreshYesterdayShows("US", "2023-01-01")

        // THEN: Only the valid show is inserted into the database
        assertEquals(1, fakeTvMazeDao.yesterdayShowsList.size)
        val insertedShow = fakeTvMazeDao.yesterdayShowsList.first()
        assertEquals(1, insertedShow.id) // Episode ID
        assertEquals(10, insertedShow.showId) // Show ID
        assertEquals("final_original_for_show_10.png", insertedShow.image)
        assertEquals("final_medium_for_show_10.png", insertedShow.mediumImage)
    }

    private fun createMockYesterdayScheduleResponse(
        id: Int,
        imdb: String?,
        image: String?, // This is for the episode's direct image (original URL)
        showId: Int = id * 10,
        showImageOriginal: String? = "show_original_default.png", // For nested Show's image
        showImageMedium: String? = "show_medium_default.png"   // For nested Show's image
    ): NetworkYesterdayScheduleResponse {
        val mockLinks = NetworkShowEpisodeLinks(self = NetworkShowNextEpisodeSelf(href = "href_episode_$id"))

        val episodeImageObject: NetworkScheduleImage? = image?.let {
            NetworkScheduleImage(original = it, medium = it) // Use same for medium if only one is provided
        }

        val showImageObject: NetworkScheduleImage? = if (showImageOriginal != null || showImageMedium != null) {
            NetworkScheduleImage(original = showImageOriginal, medium = showImageMedium)
        } else {
            null
        }

        return NetworkYesterdayScheduleResponse(
            id = id,
            name = "Episode Name $id",
            airdate = "2023-01-01",
            airstamp = "sometime",
            airtime = "10:00",
            runtime = 30,
            season = 1,
            number = id,
            image = episodeImageObject, // Assign the NetworkScheduleImage? object for the episode
            summary = "summary for episode $id",
            url = "url_episode_$id",
            _links = mockLinks,
            show = NetworkScheduleShow(
                id = showId,
                name = "Test Show $showId",
                externals = imdb?.let { NetworkScheduleExternals(imdb = it, thetvdb = 1, tvrage = 1) }, // Corrected line
                image = showImageObject, // Assign the NetworkScheduleImage? object for the show
                genres = emptyList(),
                language = "English",
                network = null,
                officialSite = null,
                premiered = null,
                rating = null,
                runtime = 30,
                schedule = null,
                status = "Running",
                summary = "summary for show $showId",
                type = "Scripted",
                updated = 123,
                url = "url_show_$showId",
                webChannel = null,
                weight = 1,
                _links = null
            )
        )
    }
}
