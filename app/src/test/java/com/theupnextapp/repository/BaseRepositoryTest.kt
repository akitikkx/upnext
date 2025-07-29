package com.theupnextapp.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.theupnextapp.common.utils.DateUtils
import com.theupnextapp.database.DatabaseTableUpdate
import com.theupnextapp.database.UpnextDao
import com.theupnextapp.network.TvMazeService
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoLinks
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoNextEpisode
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoResponse
import com.theupnextapp.network.models.tvmaze.NetworkShowNextEpisodeLinks
import com.theupnextapp.network.models.tvmaze.NetworkShowNextEpisodeResponse
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupExternals
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupImage
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupLinks
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupNetwork
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupPreviousepisode
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupRating
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupResponse
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupSchedule
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupSelf
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupWebChannel
import com.theupnextapp.repository.fakes.FakeUpnextDao
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.HttpException
import retrofit2.Response
import java.util.concurrent.TimeUnit

// Concrete repository for testing protected methods
class ConcreteTestRepository(
    upnextDao: UpnextDao, // Use UpnextDao interface
    tvMazeService: TvMazeService
) : BaseRepository(upnextDao, tvMazeService) {

    fun testCanProceedWithUpdate(tableName: String, intervalMinutes: Long): Boolean {
        // This method is deprecated in BaseRepository, consider removing or updating if logic changes
        return super.canProceedWithUpdate(tableName, intervalMinutes)
    }

    suspend fun testIsUpdateNeededByDay(tableName: String): Boolean {
        return super.isUpdateNeededByDay(tableName)
    }

    suspend fun testLogTableUpdateTimestamp(tableName: String) {
        super.logTableUpdateTimestamp(tableName)
    }
}

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class BaseRepositoryTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var fakeUpnextDao: FakeUpnextDao

    @Mock
    private lateinit var mockTvMazeService: TvMazeService

    private lateinit var repository: ConcreteTestRepository

    private val dummyLinksSelfLink: NetworkTvMazeShowLookupSelf = mock()
    private val dummyPreviousEpisodeLink: NetworkTvMazeShowLookupPreviousepisode = mock()
    private val dummyExternals: NetworkTvMazeShowLookupExternals = mock()
    private val dummyNetwork: NetworkTvMazeShowLookupNetwork = mock()
    private val dummyRating: NetworkTvMazeShowLookupRating = mock()
    private val dummySchedule: NetworkTvMazeShowLookupSchedule = mock()
    private val dummyWebChannel: NetworkTvMazeShowLookupWebChannel = mock()


    @Before
    fun setUp() {
        fakeUpnextDao = FakeUpnextDao() 
        repository = ConcreteTestRepository(fakeUpnextDao, mockTvMazeService)
    }

    private val testTableName = "test_shows"
    private val shortIntervalMinutes = 30L

    @Test
    fun `canProceedWithUpdate should return true when no last update time exists`() {
        val canProceed =
            repository.testCanProceedWithUpdate(testTableName, shortIntervalMinutes)
        assertTrue("Should proceed if no last update time is recorded.", canProceed)
    }

    @Test
    fun `canProceedWithUpdate should return true when update interval has passed`() {
        val currentTime = System.currentTimeMillis()
        val lastUpdateTimeMillis =
            currentTime - ((shortIntervalMinutes + 5) * 60 * 1000)
        fakeUpnextDao.addTableUpdate(
            DatabaseTableUpdate(
                table_name = testTableName,
                last_updated = lastUpdateTimeMillis
            )
        )
        val canProceed =
            repository.testCanProceedWithUpdate(testTableName, shortIntervalMinutes)
        assertTrue("Should proceed as the interval has passed.", canProceed)
    }

    @Test
    fun `canProceedWithUpdate should return false when update interval has not passed`() {
        val currentTime = System.currentTimeMillis()
        val lastUpdateTimeMillis = currentTime - ((shortIntervalMinutes - 5) * 60 * 1000)
        fakeUpnextDao.addTableUpdate(
            DatabaseTableUpdate(
                table_name = testTableName,
                last_updated = lastUpdateTimeMillis
            )
        )
        val canProceed =
            repository.testCanProceedWithUpdate(testTableName, shortIntervalMinutes)
        assertFalse("Should not proceed as the interval has not passed.", canProceed)
    }

    private fun createMockShowResponse(
        id: Int,
        name: String,
        mediumImageUrl: String,
        originalImageUrl: String,
        linksParam: NetworkTvMazeShowLookupLinks = NetworkTvMazeShowLookupLinks(self = dummyLinksSelfLink, previousepisode = dummyPreviousEpisodeLink),
        averageRuntime: Int? = 0, 
        dvdCountry: Any? = null, 
        externals: NetworkTvMazeShowLookupExternals = dummyExternals, 
        genres: List<String>? = emptyList(), 
        language: String = "English", 
        network: NetworkTvMazeShowLookupNetwork = dummyNetwork,
        officialSite: String = "",
        premiered: String = "",
        rating: NetworkTvMazeShowLookupRating = dummyRating,
        runtime: Int = 0,
        schedule: NetworkTvMazeShowLookupSchedule = dummySchedule,
        status: String = "",
        summary: String = "",
        type: String = "",
        updated: Int = 0,
        url: String = "", 
        webChannel: NetworkTvMazeShowLookupWebChannel = dummyWebChannel,
        weight: Int = 0
    ): NetworkTvMazeShowLookupResponse {
        return NetworkTvMazeShowLookupResponse(
            _links = linksParam,
            averageRuntime = averageRuntime,
            dvdCountry = dvdCountry,
            externals = externals,
            genres = genres,
            id = id,
            image = NetworkTvMazeShowLookupImage(
                medium = mediumImageUrl,
                original = originalImageUrl
            ),
            language = language,
            name = name,
            network = network,
            officialSite = officialSite,
            premiered = premiered,
            rating = rating,
            runtime = runtime,
            schedule = schedule,
            status = status,
            summary = summary,
            type = type,
            updated = updated,
            url = url,
            webChannel = webChannel,
            weight = weight
        )
    }

    @Test
    fun `getImages with valid imdbId returns image data`() = runTest {
        val imdbId = "tt1234567"
        val expectedTvMazeId = 123
        val expectedOriginalUrl = "http://example.com/original.jpg"
        val expectedMediumUrl = "http://example.com/medium.jpg"
        val mockResponse = createMockShowResponse(
            id = expectedTvMazeId,
            name = "Test Show",
            mediumImageUrl = expectedMediumUrl,
            originalImageUrl = expectedOriginalUrl
        )
        whenever(mockTvMazeService.getShowLookupAsync(imdbId))
            .thenReturn(CompletableDeferred(mockResponse))

        val result = repository.getImages(imdbId)

        assertEquals(expectedTvMazeId, result.first)
        assertEquals(expectedOriginalUrl, result.second)
        assertEquals(expectedMediumUrl, result.third)
    }

    @Test
    fun `getImages with null imdbId returns null triple`() = runTest {
        val result = repository.getImages(null)
        assertNull(result.first)
        assertNull(result.second)
        assertNull(result.third)
    }

    @Test
    fun `getImages with blank imdbId returns null triple`() = runTest {
        val result = repository.getImages("   ")
        assertNull(result.first)
        assertNull(result.second)
        assertNull(result.third)
    }

    @Test
    fun `getImages when service throws 404 HttpException returns null triple`() = runTest {
        val imdbId = "ttNotFound"
        val responseBody = "".toResponseBody("application/json".toMediaTypeOrNull())
        val mockHttpException = HttpException(Response.error<Any>(404, responseBody))
        whenever(mockTvMazeService.getShowLookupAsync(any())).doSuspendableAnswer { throw mockHttpException }
        val result = repository.getImages(imdbId)
        assertNull(result.first)
        assertNull(result.second)
        assertNull(result.third)
    }

    @Test
    fun `getImages when service throws other HttpException returns null triple`() = runTest {
        val imdbId = "ttError"
        val responseBody = "".toResponseBody("application/json".toMediaTypeOrNull())
        val mockHttpException = HttpException(Response.error<Any>(500, responseBody))
        whenever(mockTvMazeService.getShowLookupAsync(any())).doSuspendableAnswer { throw mockHttpException }
        val result = repository.getImages(imdbId)
        assertNull(result.first)
        assertNull(result.second)
        assertNull(result.third)
    }

    @Test
    fun `getImages when service throws generic Exception returns null triple`() = runTest {
        val imdbId = "ttGenericError"
        val genericException = RuntimeException("Network issue")
        whenever(mockTvMazeService.getShowLookupAsync(any())).doSuspendableAnswer { throw genericException }
        val result = repository.getImages(imdbId)
        assertNull(result.first)
        assertNull(result.second)
        assertNull(result.third)
    }

    // Tests for isUpdateNeededByDay
    @Test
    fun `isUpdateNeededByDay returns true when no previous update timestamp`() = runTest {
        assertTrue(repository.testIsUpdateNeededByDay(testTableName))
    }

    @Test
    fun `isUpdateNeededByDay returns true when last update was on a different day`() = runTest {
        val yesterdayMillis = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
        fakeUpnextDao.addTableUpdate(DatabaseTableUpdate(table_name = testTableName, last_updated = yesterdayMillis))
        assertTrue(repository.testIsUpdateNeededByDay(testTableName))
    }

    @Test
    fun `isUpdateNeededByDay returns false when last update was on the same day`() = runTest {
        val todayMillis = System.currentTimeMillis()
        fakeUpnextDao.clearAll()
        fakeUpnextDao.addTableUpdate(DatabaseTableUpdate(table_name = testTableName, last_updated = todayMillis))
        assertFalse(repository.testIsUpdateNeededByDay(testTableName))
    }

    @Test
    fun `isUpdateNeededByDay returns true when last update timestamp is zero`() = runTest {
        fakeUpnextDao.addTableUpdate(DatabaseTableUpdate(table_name = testTableName, last_updated = 0L))
        assertTrue(repository.testIsUpdateNeededByDay(testTableName))
    }

    // Helper for getNextEpisode tests
    private fun createMockShowInfoResponseForNextEpisode(nextEpisodeHref: String?): NetworkShowInfoResponse {
        val nextEpisodeLink = if (nextEpisodeHref != null) NetworkShowInfoNextEpisode(href = nextEpisodeHref) else null
        val links = NetworkShowInfoLinks(nextepisode = nextEpisodeLink, previousepisode = null, self = null)
        return NetworkShowInfoResponse(
            id = 1,
            name = "Test Show For Next Ep",
            image = null, 
            externals = mock(), 
            genres = emptyList(),
            language = "English",
            _links = links,
            network = null, 
            officialSite = null, 
            premiered = null, 
            rating = null, 
            runtime = 0, 
            schedule = null, 
            status = "Running",
            summary = "Summary",
            type = "Scripted",
            updated = 0,
            url = "http://example.com/show/1",
            webChannel = dummyWebChannel, 
            weight = 0
        )
    }

    private fun createMockNextEpisodeInternalResponse(id: Int, name: String = "The Next One"): NetworkShowNextEpisodeResponse {
        val mockEpLinks = mock<NetworkShowNextEpisodeLinks>()
        return NetworkShowNextEpisodeResponse(
            id = id,
            name = name,
            season = 1,
            number = 1,
            airdate = "2023-01-01",
            airstamp = "2023-01-01T20:00:00Z",
            airtime = "20:00",
            runtime = 30,
            summary = "Episode summary",
            _links = mockEpLinks, 
            image = null,
            mediumShowImageUrl = "http://example.com/medium_show.jpg",
            originalShowImageUrl = "http://example.com/original_show.jpg",
            tvMazeID = 123,
            imdb = "tt1234567",
            url = "http://example.com/mock_url"
        )
    }

    @Test
    fun `getNextEpisode successfully retrieves next episode`() = runTest {
        val tvMazeID = 1
        val nextEpisodeId = "101"
        val nextEpisodeHref = "http://api.tvmaze.com/episodes/$nextEpisodeId"

        val mockShowInfoResponse = createMockShowInfoResponseForNextEpisode(nextEpisodeHref)
        whenever(mockTvMazeService.getShowSummaryAsync(tvMazeID.toString()))
            .thenReturn(CompletableDeferred(mockShowInfoResponse))

        val mockNextEpisodeResponse = createMockNextEpisodeInternalResponse(nextEpisodeId.toInt())
        whenever(mockTvMazeService.getNextEpisodeAsync(nextEpisodeId))
            .thenReturn(CompletableDeferred(mockNextEpisodeResponse))

        val result = repository.getNextEpisode(tvMazeID)

        assertNotNull(result)
        assertEquals(nextEpisodeId.toInt(), result?.id)
        assertEquals("The Next One", result?.name)
    }

    @Test
    fun `getNextEpisode returns null when show summary has no nextepisode link`() = runTest {
        val tvMazeID = 2
        val mockShowInfoResponse = createMockShowInfoResponseForNextEpisode(null) // No href
        whenever(mockTvMazeService.getShowSummaryAsync(tvMazeID.toString()))
            .thenReturn(CompletableDeferred(mockShowInfoResponse))

        val result = repository.getNextEpisode(tvMazeID)
        assertNull(result)
    }

    @Test
    fun `getNextEpisode returns null when nextepisode link is blank`() = runTest {
        val tvMazeID = 3
        val mockShowInfoResponse = createMockShowInfoResponseForNextEpisode("  ") // Blank href
        whenever(mockTvMazeService.getShowSummaryAsync(tvMazeID.toString()))
            .thenReturn(CompletableDeferred(mockShowInfoResponse))

        val result = repository.getNextEpisode(tvMazeID)
        assertNull(result)
    }

    @Test
    fun `getNextEpisode returns null when nextepisode ID cannot be extracted`() = runTest {
        val tvMazeID = 4
        // Href that doesn't end with /ID
        val mockShowInfoResponse = createMockShowInfoResponseForNextEpisode("http://api.tvmaze.com/episodes/showInvalid")
        whenever(mockTvMazeService.getShowSummaryAsync(tvMazeID.toString()))
            .thenReturn(CompletableDeferred(mockShowInfoResponse))

        val result = repository.getNextEpisode(tvMazeID)
        assertNull(result)
    }

    @Test
    fun `getNextEpisode returns null when getShowSummaryAsync fails`() = runTest {
        val tvMazeID = 5
        whenever(mockTvMazeService.getShowSummaryAsync(tvMazeID.toString()))
            .thenThrow(RuntimeException("Network error"))

        val result = repository.getNextEpisode(tvMazeID)
        assertNull(result)
    }

    @Test
    fun `getNextEpisode returns null when getNextEpisodeAsync fails`() = runTest {
        val tvMazeID = 6
        val nextEpisodeId = "102"
        val nextEpisodeHref = "http://api.tvmaze.com/episodes/$nextEpisodeId"

        val mockShowInfoResponse = createMockShowInfoResponseForNextEpisode(nextEpisodeHref)
        whenever(mockTvMazeService.getShowSummaryAsync(tvMazeID.toString()))
            .thenReturn(CompletableDeferred(mockShowInfoResponse))

        whenever(mockTvMazeService.getNextEpisodeAsync(nextEpisodeId))
            .thenThrow(RuntimeException("Network error for next episode"))

        val result = repository.getNextEpisode(tvMazeID)
        assertNull(result)
    }

    @Test
    fun `logTableUpdateTimestamp inserts correct table name and a recent timestamp`() = runTest {
        val tableName = "shows_table"
        val startTime = System.currentTimeMillis()

        repository.testLogTableUpdateTimestamp(tableName)

        val endTime = System.currentTimeMillis()
        val updatedEntry = fakeUpnextDao.getTableLastUpdateTime(tableName)

        assertNotNull("Updated entry should not be null", updatedEntry)
        assertEquals("Table name should match", tableName, updatedEntry?.table_name)
        assertTrue(
            "Timestamp should be within the test execution time range",
            updatedEntry!!.last_updated >= startTime && updatedEntry.last_updated <= endTime
        )
        // Check if it's "today" according to DateUtils, which matches the logic in isUpdateNeededByDay
        val todayString = DateUtils.formatTimestampToString(System.currentTimeMillis(), "yyyy-MM-dd")
        val entryDateString = DateUtils.formatTimestampToString(updatedEntry.last_updated, "yyyy-MM-dd")
        assertEquals("Entry date should be today", todayString, entryDateString)
    }
}
