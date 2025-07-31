package com.theupnextapp.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.theupnextapp.common.utils.DateUtils
import com.theupnextapp.database.DatabaseTableUpdate
import com.theupnextapp.fake.FakeTvMazeService
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
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupCountry // Added for dummyNetwork
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupWebChannel // Added for webChannel
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupCountryX // Added for webChannel
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoExternals // Added for dummyShowInfoExternals
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoImage // Added for dummyShowInfoImage
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoRating // Added for dummyShowInfoRating
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoSchedule // Added for dummyShowInfoSchedule
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoNetwork // Added for dummyShowInfoNetwork
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoCountry // Added for dummyShowInfoCountry
import com.theupnextapp.network.models.tvmaze.NetworkShowNextEpisodeSelf // Added for dummyNextEpisodeSelf
import com.theupnextapp.repository.fakes.FakeUpnextDao
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
import retrofit2.HttpException
import retrofit2.Response
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
class BaseRepositoryTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var fakeUpnextDao: FakeUpnextDao
    private lateinit var fakeTvMazeService: FakeTvMazeService
    private lateinit var repository: ConcreteTestRepository

    // Dummy data for NetworkTvMazeShowLookupResponse
    private val dummyLookupSelfLink = NetworkTvMazeShowLookupSelf(href = "http://api.tvmaze.com/shows/1/self")
    private val dummyLookupPreviousEpisodeLink = NetworkTvMazeShowLookupPreviousepisode(href = "http://api.tvmaze.com/episodes/1")
    private val dummyLookupExternals = NetworkTvMazeShowLookupExternals(tvrage = 123, thetvdb = 456, imdb = "tt1234567")
    private val dummyLookupCountry = NetworkTvMazeShowLookupCountry(name = "US", code = "US", timezone = "America/New_York")
    private val dummyLookupCountryX = NetworkTvMazeShowLookupCountryX(name = "US", code = "US", timezone = "America/New_York")
    private val dummyLookupWebChannel = NetworkTvMazeShowLookupWebChannel(id = 1, name = "Fake Web Channel", country = dummyLookupCountryX)
    private val dummyLookupNetwork = NetworkTvMazeShowLookupNetwork(id = 1, name = "Fake Network", country = dummyLookupCountry)
    private val dummyLookupRating = NetworkTvMazeShowLookupRating(average = 8.5)
    private val dummyLookupSchedule = NetworkTvMazeShowLookupSchedule(time = "22:00", days = listOf("Monday"))

    // Dummy data for NetworkShowInfoResponse
    private val dummyShowInfoCountry = NetworkShowInfoCountry(name = "US", code = "US", timezone = "America/New_York")
    private val dummyShowInfoNetwork = NetworkShowInfoNetwork(id = 1, name = "Fake Network", country = dummyShowInfoCountry)
    private val dummyShowInfoExternals = NetworkShowInfoExternals(tvrage = 123, thetvdb = 456, imdb = "tt1234567")
    private val dummyShowInfoImage = NetworkShowInfoImage(medium = "http://medium.jpg", original = "http://original.jpg")
    private val dummyShowInfoRating = NetworkShowInfoRating(average = 8.0)
    private val dummyShowInfoSchedule = NetworkShowInfoSchedule(time = "20:00", days = listOf("Sunday"))

    // Dummy data for NetworkShowNextEpisodeResponse
    private val dummyNextEpisodeSelf = NetworkShowNextEpisodeSelf(href = "http://api.tvmaze.com/episodes/2/self")


    @Before
    fun setUp() {
        fakeUpnextDao = FakeUpnextDao()
        fakeTvMazeService = FakeTvMazeService()
        repository = ConcreteTestRepository(fakeUpnextDao, fakeTvMazeService)
    }

    private val testTableName = "test_shows"
    private val shortIntervalMinutes = 30L

    @Test
    @Suppress("DEPRECATION")
    fun `canProceedWithUpdate should return true when no last update time exists`() {
        val canProceed =
            repository.testCanProceedWithUpdate(testTableName, shortIntervalMinutes)
        assertTrue("Should proceed if no last update time is recorded.", canProceed)
    }

    @Test
    @Suppress("DEPRECATION")
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
    @Suppress("DEPRECATION")
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

    private fun createMockShowLookupResponse(
        id: Int,
        name: String,
        mediumImageUrl: String,
        originalImageUrl: String
    ): NetworkTvMazeShowLookupResponse {
        return NetworkTvMazeShowLookupResponse(
            _links = NetworkTvMazeShowLookupLinks(self = dummyLookupSelfLink, previousepisode = dummyLookupPreviousEpisodeLink),
            averageRuntime = 60,
            dvdCountry = null,
            externals = dummyLookupExternals,
            genres = listOf("Drama"),
            id = id,
            image = NetworkTvMazeShowLookupImage(
                medium = mediumImageUrl,
                original = originalImageUrl
            ),
            language = "English",
            name = name,
            network = dummyLookupNetwork,
            officialSite = "http://official.site",
            premiered = "2022-01-01",
            rating = dummyLookupRating,
            runtime = 60,
            schedule = dummyLookupSchedule,
            status = "Running",
            summary = "Summary for lookup",
            type = "Scripted",
            updated = 123456789,
            url = "http://show.url",
            webChannel = dummyLookupWebChannel,
            weight = 90
        )
    }

    @Test
    fun `getImages with valid imdbId returns image data`() = runTest {
        val imdbId = "tt1234567"
        val expectedTvMazeId = 123
        val expectedOriginalUrl = "http://example.com/original.jpg"
        val expectedMediumUrl = "http://example.com/medium.jpg"
        val mockResponse = createMockShowLookupResponse(
            id = expectedTvMazeId,
            name = "Test Show",
            mediumImageUrl = expectedMediumUrl,
            originalImageUrl = expectedOriginalUrl
        )
        fakeTvMazeService.mockShowLookupResponse = mockResponse

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
        fakeTvMazeService.showLookupError = HttpException(Response.error<Any>(404, responseBody))

        val result = repository.getImages(imdbId)
        assertNull(result.first)
        assertNull(result.second)
        assertNull(result.third)
    }

    @Test
    fun `getImages when service throws other HttpException returns null triple`() = runTest {
        val imdbId = "ttError"
        val responseBody = "".toResponseBody("application/json".toMediaTypeOrNull())
        fakeTvMazeService.showLookupError = HttpException(Response.error<Any>(500, responseBody))

        val result = repository.getImages(imdbId)
        assertNull(result.first)
        assertNull(result.second)
        assertNull(result.third)
    }

    @Test
    fun `getImages when service throws generic Exception returns null triple`() = runTest {
        val imdbId = "ttGenericError"
        fakeTvMazeService.showLookupError = RuntimeException("Network issue")

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
    private fun createMockShowInfoResponseForNextEpisode(showId: Int, nextEpisodeHref: String?): NetworkShowInfoResponse {
        val nextEpisodeLink = if (nextEpisodeHref != null) NetworkShowInfoNextEpisode(href = nextEpisodeHref) else null
        val links = NetworkShowInfoLinks(nextepisode = nextEpisodeLink, previousepisode = null, self = null)
        return NetworkShowInfoResponse(
            id = showId,
            name = "Test Show For Next Ep",
            image = dummyShowInfoImage,
            externals = dummyShowInfoExternals,
            genres = emptyList(),
            language = "English",
            _links = links,
            network = dummyShowInfoNetwork,
            officialSite = "http://official.site",
            premiered = "2023-01-01",
            rating = dummyShowInfoRating,
            runtime = 60,
            schedule = dummyShowInfoSchedule,
            status = "Running",
            summary = "Summary for Show Info",
            type = "Scripted",
            updated = 12345,
            url = "http://example.com/show/$showId",
            webChannel = Any(),
            weight = 100
        )
    }

    private fun createMockNextEpisodeInternalResponse(id: Int, name: String = "The Next One"): NetworkShowNextEpisodeResponse {
        val links = NetworkShowNextEpisodeLinks(self = dummyNextEpisodeSelf)
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
            _links = links,
            image = null, // Assuming image can be null or provide a dummy NetworkShowNextEpisodeImage
            mediumShowImageUrl = "http://example.com/medium_show.jpg", // These seem to be custom fields added to the model
            originalShowImageUrl = "http://example.com/original_show.jpg", // These seem to be custom fields added to the model
            tvMazeID = 123, // This seems to be custom field
            imdb = "tt1234567", // This seems to be custom field
            url = "http://example.com/mock_url" // This seems to be custom field
        )
    }

    @Test
    fun `getNextEpisode successfully retrieves next episode`() = runTest {
        val tvMazeID = 1
        val nextEpisodeId = "101"
        val nextEpisodeHref = "http://api.tvmaze.com/episodes/$nextEpisodeId"

        fakeTvMazeService.mockShowInfoResponse = createMockShowInfoResponseForNextEpisode(tvMazeID, nextEpisodeHref)
        fakeTvMazeService.mockNextEpisodeResponse = createMockNextEpisodeInternalResponse(nextEpisodeId.toInt())

        val result = repository.getNextEpisode(tvMazeID)

        assertNotNull(result)
        assertEquals(nextEpisodeId.toInt(), result?.id)
        assertEquals("The Next One", result?.name)
    }

    @Test
    fun `getNextEpisode returns null when show summary has no nextepisode link`() = runTest {
        val tvMazeID = 2
        fakeTvMazeService.mockShowInfoResponse = createMockShowInfoResponseForNextEpisode(tvMazeID, null) // No href

        val result = repository.getNextEpisode(tvMazeID)
        assertNull(result)
    }

    @Test
    fun `getNextEpisode returns null when nextepisode link is blank`() = runTest {
        val tvMazeID = 3
        fakeTvMazeService.mockShowInfoResponse = createMockShowInfoResponseForNextEpisode(tvMazeID, "  ") // Blank href

        val result = repository.getNextEpisode(tvMazeID)
        assertNull(result)
    }

    @Test
    fun `getNextEpisode returns null when nextepisode ID cannot be extracted`() = runTest {
        val tvMazeID = 4
        // Href that doesn't end with /ID
        fakeTvMazeService.mockShowInfoResponse = createMockShowInfoResponseForNextEpisode(tvMazeID, "http://api.tvmaze.com/episodes/showInvalid")

        // Explicitly tell FakeTvMazeService to throw an error when getNextEpisodeAsync("showInvalid") is called
        val responseBody = "".toResponseBody("application/json".toMediaTypeOrNull())
        fakeTvMazeService.nextEpisodeError = HttpException(Response.error<Any>(404, responseBody))

        val result = repository.getNextEpisode(tvMazeID)
        assertNull(result)
    }

    @Test
    fun `getNextEpisode returns null when getShowSummaryAsync fails`() = runTest {
        val tvMazeID = 5
        // Use shouldThrowGetShowSummaryError (throws IOException) or specific error
        fakeTvMazeService.showSummaryError = RuntimeException("Network error for getShowSummaryAsync")


        val result = repository.getNextEpisode(tvMazeID)
        assertNull(result)
    }

    @Test
    fun `getNextEpisode returns null when getNextEpisodeAsync fails`() = runTest {
        val tvMazeID = 6
        val nextEpisodeId = "102"
        val nextEpisodeHref = "http://api.tvmaze.com/episodes/$nextEpisodeId"

        fakeTvMazeService.mockShowInfoResponse = createMockShowInfoResponseForNextEpisode(tvMazeID, nextEpisodeHref)
        fakeTvMazeService.nextEpisodeError = RuntimeException("Network error for next episode")

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
        val todayString = DateUtils.formatTimestampToString(System.currentTimeMillis(), "yyyy-MM-dd")
        val entryDateString = DateUtils.formatTimestampToString(updatedEntry.last_updated, "yyyy-MM-dd")
        assertEquals("Entry date should be today", todayString, entryDateString)
    }
}
