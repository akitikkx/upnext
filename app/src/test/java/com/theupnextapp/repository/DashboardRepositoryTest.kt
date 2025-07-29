package com.theupnextapp.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.CoroutineTestRule
import com.theupnextapp.database.DatabaseTableUpdate
import com.theupnextapp.database.DatabaseYesterdaySchedule
import com.theupnextapp.database.TvMazeDao
import com.theupnextapp.database.UpnextDao
import com.theupnextapp.network.TvMazeService
import com.theupnextapp.network.models.tvmaze.NetworkScheduleExternals
import com.theupnextapp.network.models.tvmaze.NetworkScheduleImage
import com.theupnextapp.network.models.tvmaze.NetworkScheduleShow
import com.theupnextapp.network.models.tvmaze.NetworkYesterdayScheduleResponse
import com.theupnextapp.network.models.tvmaze.NetworkShowEpisodeLinks
import com.theupnextapp.network.models.tvmaze.NetworkShowNextEpisodeSelf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class DashboardRepositoryTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutineTestRule = CoroutineTestRule()

    private val upnextDao: UpnextDao = mock()
    private val tvMazeDao: TvMazeDao = mock()
    private val tvMazeService: TvMazeService = mock()
    private val firebaseCrashlytics: FirebaseCrashlytics = mock()

    private lateinit var repository: DashboardRepository

    @Before
    fun setup() {
        runTest {
            // Default behavior: an update is needed. Tests that need a different behavior will override this.
            whenever(upnextDao.getTableLastUpdateTime(any())).thenReturn(null)
        }

        repository = DashboardRepository(
            upnextDao = upnextDao,
            tvMazeDao = tvMazeDao,
            tvMazeService = tvMazeService,
            firebaseCrashlytics = firebaseCrashlytics
        )
    }

    @After
    fun tearDown() {
        Mockito.framework().clearInlineMocks()
    }

    @Test
    fun `refreshYesterdayShows - network call and db insert happen when update is needed`() = runTest {
        // GIVEN: An update is needed (the default setup ensures this)
        val deferredResponse = CompletableDeferred<List<NetworkYesterdayScheduleResponse>>()
        whenever(tvMazeService.getYesterdayScheduleAsync(any(), any())).thenReturn(deferredResponse)
        deferredResponse.complete(listOf(createMockYesterdayScheduleResponse(1, "imdb1", "image.png")))

        // WHEN: refreshYesterdayShows is called
        repository.refreshYesterdayShows("US", "2023-01-01")

        // THEN: The network is called, and the db is updated
        verify(tvMazeDao).deleteAllYesterdayShows()

        val showsCaptor = argumentCaptor<DatabaseYesterdaySchedule>()
        verify(tvMazeDao).insertAllYesterdayShows(showsCaptor.capture())
        assertEquals(1, showsCaptor.firstValue.id)

        val tableUpdateCaptor = argumentCaptor<DatabaseTableUpdate>()
        verify(upnextDao).insertTableUpdateLog(tableUpdateCaptor.capture())
        assertEquals("schedule_yesterday", tableUpdateCaptor.firstValue.table_name)
    }

    @Test
    fun `refreshYesterdayShows - network call and db insert do NOT happen when not needed`() = runTest {
        // GIVEN: An update is NOT needed (a recent log exists)
        val recentUpdate = System.currentTimeMillis()
        val tableName = "schedule_yesterday"
        // Override the default setup to simulate a recent update
        whenever(upnextDao.getTableLastUpdateTime(tableName)).thenReturn(DatabaseTableUpdate(table_name = tableName, last_updated = recentUpdate))

        // WHEN: refreshYesterdayShows is called
        repository.refreshYesterdayShows("US", "2023-01-01")

        // THEN: No network/db operations occur
        verify(tvMazeService, never()).getYesterdayScheduleAsync(any(), any())
        verify(tvMazeDao, never()).deleteAllYesterdayShows()
        verify(tvMazeDao, never()).insertAllYesterdayShows(any())
    }

    @Test
    fun `refreshYesterdayShows - handles network exception gracefully`() = runTest {
        // GIVEN: An update is needed, but the network call will fail
        val deferredResponse = CompletableDeferred<List<NetworkYesterdayScheduleResponse>>()
        whenever(tvMazeService.getYesterdayScheduleAsync(any(), any())).thenReturn(deferredResponse)
        deferredResponse.completeExceptionally(RuntimeException("Network failed"))

        // WHEN: refreshYesterdayShows is called
        repository.refreshYesterdayShows("US", "2023-01-01")

        // THEN: The exception is logged and no data is inserted
        verify(firebaseCrashlytics).recordException(any())
        verify(tvMazeDao, never()).insertAllYesterdayShows(any())
    }

    @Test
    fun `refreshYesterdayShows - filters out shows with no image or imdb id`() = runTest {
        // GIVEN: An update is needed
        val validShow = createMockYesterdayScheduleResponse(id = 1, imdb = "imdb1", image = "image.png")
        val showWithoutImage = createMockYesterdayScheduleResponse(id = 2, imdb = "imdb2", image = null)
        val showWithoutImdb = createMockYesterdayScheduleResponse(id = 3, imdb = null, image = "image.png")

        val deferredResponse = CompletableDeferred<List<NetworkYesterdayScheduleResponse>>()
        whenever(tvMazeService.getYesterdayScheduleAsync(any(), any())).thenReturn(deferredResponse)
        deferredResponse.complete(listOf(validShow, showWithoutImage, showWithoutImdb))

        // WHEN: refreshYesterdayShows is called
        repository.refreshYesterdayShows("US", "2023-01-01")

        // THEN: Only the valid show is inserted into the database
        val showsCaptor = argumentCaptor<DatabaseYesterdaySchedule>()
        verify(tvMazeDao).insertAllYesterdayShows(showsCaptor.capture())
        assertEquals(1, showsCaptor.firstValue.id)
    }

    private fun createMockYesterdayScheduleResponse(
        id: Int,
        imdb: String?,
        image: String?
    ): NetworkYesterdayScheduleResponse {
        val mockLinks = NetworkShowEpisodeLinks(self = NetworkShowNextEpisodeSelf(href = "href"))
        return NetworkYesterdayScheduleResponse(
            id = id,
            name = "Test Show",
            airdate = "2023-01-01",
            airstamp = "sometime",
            airtime = "10:00",
            runtime = 30,
            season = 1,
            number = 1,
            image = "",
            summary = "summary",
            url = "url",
            _links = mockLinks,
            show = NetworkScheduleShow(
                id = id,
                name = "Test Show",
                externals = if (imdb != null) NetworkScheduleExternals(imdb = imdb, thetvdb = 1, tvrage = 1) else null,
                image = if (image != null) NetworkScheduleImage(original = image, medium = image) else null,
                genres = emptyList(),
                language = "English",
                network = null,
                officialSite = null,
                premiered = null,
                rating = null,
                runtime = 30,
                schedule = null,
                status = "Running",
                summary = "summary",
                type = "Scripted",
                updated = 123,
                url = "url",
                webChannel = null,
                weight = 1,
                _links = null
            )
        )
    }
}