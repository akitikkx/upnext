package com.theupnextapp.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.theupnextapp.CoroutineTestRule
import com.theupnextapp.database.DatabaseTableUpdate
import com.theupnextapp.network.models.tvmaze.NetworkScheduleExternals
import com.theupnextapp.network.models.tvmaze.NetworkScheduleImage
import com.theupnextapp.network.models.tvmaze.NetworkScheduleShow
import com.theupnextapp.network.models.tvmaze.NetworkShowEpisodeLinks
import com.theupnextapp.network.models.tvmaze.NetworkShowNextEpisodeSelf
import com.theupnextapp.network.models.tvmaze.NetworkTodayScheduleResponse
import com.theupnextapp.network.models.tvmaze.NetworkTomorrowScheduleResponse
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowImageMedium
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowImageOriginal
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowImageResolutions
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowImageResponse
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowImageResponseItem
import com.theupnextapp.network.models.tvmaze.NetworkYesterdayScheduleResponse
import com.theupnextapp.repository.fakes.FakeCrashlytics
import com.theupnextapp.repository.fakes.FakeTvMazeDao
import com.theupnextapp.repository.fakes.FakeTvMazeService
import com.theupnextapp.repository.fakes.FakeUpnextDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@ExperimentalCoroutinesApi
class DashboardRepositoryTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutineTestRule = CoroutineTestRule()

    private lateinit var fakeUpnextDao: FakeUpnextDao
    private lateinit var fakeTvMazeDao: FakeTvMazeDao
    private lateinit var fakeTvMazeService: FakeTvMazeService
    private lateinit var fakeTmdbService: com.theupnextapp.repository.fakes.FakeTmdbService
    private lateinit var fakeCrashlytics: FakeCrashlytics

    private lateinit var repository: DashboardRepository

    @Before
    fun setup() {
        fakeUpnextDao = FakeUpnextDao()
        fakeTvMazeDao = FakeTvMazeDao()
        fakeTvMazeService = FakeTvMazeService()
        fakeTmdbService = com.theupnextapp.repository.fakes.FakeTmdbService()
        fakeCrashlytics = FakeCrashlytics()

        fakeUpnextDao.clearAll()
        fakeTvMazeDao.clearAllData()
        fakeTvMazeService.reset()
        fakeTmdbService.reset()
        fakeCrashlytics.clear()

        repository =
            DashboardRepositoryImpl(
                upnextDao = fakeUpnextDao,
                tvMazeDao = fakeTvMazeDao,
                tvMazeService = fakeTvMazeService,
                tmdbService = fakeTmdbService,
                firebaseCrashlytics = fakeCrashlytics,
            )
    }

    @Test
    fun `refreshYesterdayShows - network call and db insert happen when update is needed`() =
        runTest {
            val mockResponse =
                listOf(
                    createMockYesterdayScheduleResponse(
                        MockScheduleConfig(
                            1,
                            "imdb1",
                            "image.png",
                        ),
                    ),
                )
            fakeTvMazeService.mockYesterdayScheduleResponse = mockResponse
            fakeTvMazeService.mockShowImagesResponse =
                NetworkTvMazeShowImageResponse().apply {
                    add(
                        NetworkTvMazeShowImageResponseItem(
                            id = 101, type = "poster", main = true,
                            resolutions =
                                NetworkTvMazeShowImageResolutions(
                                    original = NetworkTvMazeShowImageOriginal(url = "show_original_default.png"),
                                    medium = NetworkTvMazeShowImageMedium(url = "show_medium_default.png"),
                                ),
                        ),
                    )
                }
            val testTableName = "schedule_yesterday"

            repository.refreshYesterdayShows("US", "2023-01-01")

            assertEquals(1, fakeTvMazeDao.yesterdayShowsList.size)
            assertEquals(1, fakeTvMazeDao.yesterdayShowsList.first().id)
            assertEquals("show_original_default.png", fakeTvMazeDao.yesterdayShowsList.first().image)
            assertEquals("show_medium_default.png", fakeTvMazeDao.yesterdayShowsList.first().mediumImage)

            val tableUpdateLog = fakeUpnextDao.getTableLastUpdateTime(testTableName)
            assertNotNull(tableUpdateLog)
            assertEquals(testTableName, tableUpdateLog?.table_name)
        }

    @Test
    fun `refreshYesterdayShows - network call and db insert do NOT happen when not needed`() =
        runTest {
            val tableName = "schedule_yesterday"
            val recentUpdateTimestamp = System.currentTimeMillis()
            val initialLog = DatabaseTableUpdate(table_name = tableName, last_updated = recentUpdateTimestamp)
            fakeUpnextDao.insertTableUpdateLog(initialLog)

            repository.refreshYesterdayShows("US", "2023-01-01")

            assertTrue(fakeTvMazeDao.yesterdayShowsList.isEmpty())

            val finalLog = fakeUpnextDao.getTableLastUpdateTime(tableName)
            assertEquals(initialLog, finalLog)

            assertTrue(fakeCrashlytics.getRecordedExceptions().isEmpty())
        }

    @Test
    fun `refreshYesterdayShows - handles network exception gracefully`() =
        runTest {
            fakeTvMazeService.yesterdayScheduleError = IOException("Network failed")

            repository.refreshYesterdayShows("US", "2023-01-01")

            assertEquals(1, fakeCrashlytics.getRecordedExceptions().size)
            assertTrue(fakeCrashlytics.getRecordedExceptions().first() is IOException)
            assertEquals("Network failed", fakeCrashlytics.getRecordedExceptions().first().message)
            assertTrue(fakeTvMazeDao.yesterdayShowsList.isEmpty())
        }

    @Test
    fun `refreshYesterdayShows - filters out shows with no image or imdb id from initial fetch`() =
        runTest {
            val validShowNetwork =
                createMockYesterdayScheduleResponse(
                    MockScheduleConfig(
                        id = 1,
                        imdb = "imdb1",
                        image = "image.png",
                        showId = 10,
                    ),
                )
            val showWithoutImageNetwork =
                createMockYesterdayScheduleResponse(
                    MockScheduleConfig(
                        id = 2,
                        imdb = "imdb2",
                        image = null,
                        showId = 20,
                        showImageOriginal = null,
                        showImageMedium = null,
                    ),
                )
            val showWithoutImdbNetwork =
                createMockYesterdayScheduleResponse(
                    MockScheduleConfig(
                        id = 3,
                        imdb = null,
                        image = "image.png",
                        showId = 30,
                    ),
                )

            fakeTvMazeService.mockYesterdayScheduleResponse = listOf(validShowNetwork, showWithoutImageNetwork, showWithoutImdbNetwork)

            fakeTvMazeService.mockShowImagesResponse =
                NetworkTvMazeShowImageResponse().apply {
                    add(
                        NetworkTvMazeShowImageResponseItem(
                            id = 101, type = "poster", main = true,
                            resolutions =
                                NetworkTvMazeShowImageResolutions(
                                    original = NetworkTvMazeShowImageOriginal(url = "show_original_default.png"),
                                    medium = NetworkTvMazeShowImageMedium(url = "show_medium_default.png"),
                                ),
                        ),
                    )
                }

            repository.refreshYesterdayShows("US", "2023-01-01")

            assertEquals(1, fakeTvMazeDao.yesterdayShowsList.size)
            val insertedShow = fakeTvMazeDao.yesterdayShowsList.first()
            assertEquals(1, insertedShow.id)
            assertEquals(10, insertedShow.showId)
            assertEquals("show_original_default.png", insertedShow.image)
            assertEquals("show_medium_default.png", insertedShow.mediumImage)
        }

    @Test
    fun `refreshTodayShows - network call and db insert happen when update is needed`() =
        runTest {
            val mockResponse =
                listOf(
                    createMockTodayScheduleResponse(
                        MockScheduleConfig(
                            1,
                            "imdb1",
                            "image.png",
                        ),
                    ),
                )
            fakeTvMazeService.mockTodayScheduleResponse = mockResponse
            fakeTvMazeService.mockShowImagesResponse =
                NetworkTvMazeShowImageResponse().apply {
                    add(
                        NetworkTvMazeShowImageResponseItem(
                            id = 101, type = "poster", main = true,
                            resolutions =
                                NetworkTvMazeShowImageResolutions(
                                    original = NetworkTvMazeShowImageOriginal(url = "show_original_default.png"),
                                    medium = NetworkTvMazeShowImageMedium(url = "show_medium_default.png"),
                                ),
                        ),
                    )
                }
            val testTableName = "schedule_today"

            repository.refreshTodayShows("US", "2023-01-01")

            assertEquals(1, fakeTvMazeDao.todayShowsList.size)
            assertEquals(1, fakeTvMazeDao.todayShowsList.first().id)
            assertEquals("show_original_default.png", fakeTvMazeDao.todayShowsList.first().image)
            assertEquals("show_medium_default.png", fakeTvMazeDao.todayShowsList.first().mediumImage)

            val tableUpdateLog = fakeUpnextDao.getTableLastUpdateTime(testTableName)
            assertNotNull(tableUpdateLog)
            assertEquals(testTableName, tableUpdateLog?.table_name)
        }

    @Test
    fun `refreshTodayShows - network call and db insert do NOT happen when not needed`() =
        runTest {
            val tableName = "schedule_today"
            val recentUpdateTimestamp = System.currentTimeMillis()
            val initialLog = DatabaseTableUpdate(table_name = tableName, last_updated = recentUpdateTimestamp)
            fakeUpnextDao.insertTableUpdateLog(initialLog)

            repository.refreshTodayShows("US", "2023-01-01")

            assertTrue(fakeTvMazeDao.todayShowsList.isEmpty())

            val finalLog = fakeUpnextDao.getTableLastUpdateTime(tableName)
            assertEquals(initialLog, finalLog)

            assertTrue(fakeCrashlytics.getRecordedExceptions().isEmpty())
        }

    @Test
    fun `refreshTodayShows - handles network exception gracefully`() =
        runTest {
            fakeTvMazeService.todayScheduleError = IOException("Network failed")

            repository.refreshTodayShows("US", "2023-01-01")

            assertEquals(1, fakeCrashlytics.getRecordedExceptions().size)
            assertTrue(fakeCrashlytics.getRecordedExceptions().first() is IOException)
            assertEquals("Network failed", fakeCrashlytics.getRecordedExceptions().first().message)
            assertTrue(fakeTvMazeDao.todayShowsList.isEmpty())
        }

    @Test
    fun `refreshTodayShows - filters out shows with no image or imdb id from initial fetch`() =
        runTest {
            val validShowNetwork =
                createMockTodayScheduleResponse(
                    MockScheduleConfig(
                        id = 1,
                        imdb = "imdb1",
                        image = "image.png",
                        showId = 10,
                    ),
                )
            val showWithoutImageNetwork =
                createMockTodayScheduleResponse(
                    MockScheduleConfig(
                        id = 2,
                        imdb = "imdb2",
                        image = null,
                        showId = 20,
                        showImageOriginal = null,
                        showImageMedium = null,
                    ),
                )
            val showWithoutImdbNetwork =
                createMockTodayScheduleResponse(
                    MockScheduleConfig(
                        id = 3,
                        imdb = null,
                        image = "image.png",
                        showId = 30,
                    ),
                )

            fakeTvMazeService.mockTodayScheduleResponse = listOf(validShowNetwork, showWithoutImageNetwork, showWithoutImdbNetwork)

            fakeTvMazeService.mockShowImagesResponse =
                NetworkTvMazeShowImageResponse().apply {
                    add(
                        NetworkTvMazeShowImageResponseItem(
                            id = 101, type = "poster", main = true,
                            resolutions =
                                NetworkTvMazeShowImageResolutions(
                                    original = NetworkTvMazeShowImageOriginal(url = "show_original_default.png"),
                                    medium = NetworkTvMazeShowImageMedium(url = "show_medium_default.png"),
                                ),
                        ),
                    )
                }

            repository.refreshTodayShows("US", "2023-01-01")

            assertEquals(1, fakeTvMazeDao.todayShowsList.size)
            val insertedShow = fakeTvMazeDao.todayShowsList.first()
            assertEquals(1, insertedShow.id)
            assertEquals(10, insertedShow.showId)
            assertEquals("show_original_default.png", insertedShow.image)
            assertEquals("show_medium_default.png", insertedShow.mediumImage)
        }

    @Test
    fun `refreshTomorrowShows - network call and db insert happen when update is needed`() =
        runTest {
            val mockResponse =
                listOf(
                    createMockTomorrowScheduleResponse(
                        MockScheduleConfig(
                            1,
                            "imdb1",
                            "image.png",
                        ),
                    ),
                )
            fakeTvMazeService.mockTomorrowScheduleResponse = mockResponse
            fakeTvMazeService.mockShowImagesResponse =
                NetworkTvMazeShowImageResponse().apply {
                    add(
                        NetworkTvMazeShowImageResponseItem(
                            id = 101, type = "poster", main = true,
                            resolutions =
                                NetworkTvMazeShowImageResolutions(
                                    original = NetworkTvMazeShowImageOriginal(url = "show_original_default.png"),
                                    medium = NetworkTvMazeShowImageMedium(url = "show_medium_default.png"),
                                ),
                        ),
                    )
                }
            val testTableName = "schedule_tomorrow"

            repository.refreshTomorrowShows("US", "2023-01-01")

            assertEquals(1, fakeTvMazeDao.tomorrowShowsList.size)
            assertEquals(1, fakeTvMazeDao.tomorrowShowsList.first().id)
            assertEquals("show_original_default.png", fakeTvMazeDao.tomorrowShowsList.first().image)
            assertEquals("show_medium_default.png", fakeTvMazeDao.tomorrowShowsList.first().mediumImage)

            val tableUpdateLog = fakeUpnextDao.getTableLastUpdateTime(testTableName)
            assertNotNull(tableUpdateLog)
            assertEquals(testTableName, tableUpdateLog?.table_name)
        }

    @Test
    fun `refreshTomorrowShows - network call and db insert do NOT happen when not needed`() =
        runTest {
            val tableName = "schedule_tomorrow"
            val recentUpdateTimestamp = System.currentTimeMillis()
            val initialLog = DatabaseTableUpdate(table_name = tableName, last_updated = recentUpdateTimestamp)
            fakeUpnextDao.insertTableUpdateLog(initialLog)

            repository.refreshTomorrowShows("US", "2023-01-01")

            assertTrue(fakeTvMazeDao.tomorrowShowsList.isEmpty())

            val finalLog = fakeUpnextDao.getTableLastUpdateTime(tableName)
            assertEquals(initialLog, finalLog)

            assertTrue(fakeCrashlytics.getRecordedExceptions().isEmpty())
        }

    @Test
    fun `refreshTomorrowShows - handles network exception gracefully`() =
        runTest {
            fakeTvMazeService.tomorrowScheduleError = IOException("Network failed")

            repository.refreshTomorrowShows("US", "2023-01-01")

            assertEquals(1, fakeCrashlytics.getRecordedExceptions().size)
            assertTrue(fakeCrashlytics.getRecordedExceptions().first() is IOException)
            assertEquals("Network failed", fakeCrashlytics.getRecordedExceptions().first().message)
            assertTrue(fakeTvMazeDao.tomorrowShowsList.isEmpty())
        }

    @Test
    fun `refreshTomorrowShows - filters out shows with no image or imdb id from initial fetch`() =
        runTest {
            val validShowNetwork =
                createMockTomorrowScheduleResponse(
                    MockScheduleConfig(
                        id = 1,
                        imdb = "imdb1",
                        image = "image.png",
                        showId = 10,
                    ),
                )
            val showWithoutImageNetwork =
                createMockTomorrowScheduleResponse(
                    MockScheduleConfig(
                        id = 2,
                        imdb = "imdb2",
                        image = null,
                        showId = 20,
                        showImageOriginal = null,
                        showImageMedium = null,
                    ),
                )
            val showWithoutImdbNetwork =
                createMockTomorrowScheduleResponse(
                    MockScheduleConfig(
                        id = 3,
                        imdb = null,
                        image = "image.png",
                        showId = 30,
                    ),
                )

            fakeTvMazeService.mockTomorrowScheduleResponse = listOf(validShowNetwork, showWithoutImageNetwork, showWithoutImdbNetwork)

            fakeTvMazeService.mockShowImagesResponse =
                NetworkTvMazeShowImageResponse().apply {
                    add(
                        NetworkTvMazeShowImageResponseItem(
                            id = 101, type = "poster", main = true,
                            resolutions =
                                NetworkTvMazeShowImageResolutions(
                                    original = NetworkTvMazeShowImageOriginal(url = "show_original_default.png"),
                                    medium = NetworkTvMazeShowImageMedium(url = "show_medium_default.png"),
                                ),
                        ),
                    )
                }

            repository.refreshTomorrowShows("US", "2023-01-01")

            assertEquals(1, fakeTvMazeDao.tomorrowShowsList.size)
            val insertedShow = fakeTvMazeDao.tomorrowShowsList.first()
            assertEquals(1, insertedShow.id)
            assertEquals(10, insertedShow.showId)
            assertEquals("show_original_default.png", insertedShow.image)
            assertEquals("show_medium_default.png", insertedShow.mediumImage)
        }

    private data class MockScheduleConfig(
        val id: Int,
        val imdb: String?,
        val image: String?,
        val showId: Int = id * 10,
        val showImageOriginal: String? = "show_original_default.png",
        val showImageMedium: String? = "show_medium_default.png",
    )

    private fun createMockYesterdayScheduleResponse(config: MockScheduleConfig): NetworkYesterdayScheduleResponse {
        val mockLinks = NetworkShowEpisodeLinks(self = NetworkShowNextEpisodeSelf(href = "href_episode_${config.id}"))

        val episodeImageObject: NetworkScheduleImage? =
            config.image?.let {
                NetworkScheduleImage(original = it, medium = it)
            }

        val showImageObject: NetworkScheduleImage? =
            if (config.showImageOriginal != null || config.showImageMedium != null) {
                NetworkScheduleImage(original = config.showImageOriginal, medium = config.showImageMedium)
            } else {
                null
            }

        return NetworkYesterdayScheduleResponse(
            id = config.id,
            name = "Episode Name ${config.id}",
            airdate = "2023-01-01",
            airstamp = "sometime",
            airtime = "10:00",
            runtime = 30,
            season = 1,
            number = config.id,
            image = episodeImageObject,
            summary = "summary for episode ${config.id}",
            url = "url_episode_${config.id}",
            _links = mockLinks,
            show =
                NetworkScheduleShow(
                    id = config.showId,
                    name = "Test Show ${config.showId}",
                    externals =
                        config.imdb?.let {
                            NetworkScheduleExternals(imdb = it, thetvdb = 1, tvrage = 1)
                        },
                    image = showImageObject,
                    genres = emptyList(),
                    language = "English",
                    network = null,
                    officialSite = null,
                    premiered = null,
                    rating = null,
                    runtime = 30,
                    schedule = null,
                    status = "Running",
                    summary = "summary for show ${config.showId}",
                    type = "Scripted",
                    updated = 123,
                    url = "url_show_${config.showId}",
                    webChannel = null,
                    weight = 1,
                    _links = null,
                ),
        )
    }

    private fun createMockTodayScheduleResponse(config: MockScheduleConfig): NetworkTodayScheduleResponse {
        val mockLinks = NetworkShowEpisodeLinks(self = NetworkShowNextEpisodeSelf(href = "href_episode_${config.id}"))

        val episodeImageObject: NetworkScheduleImage? =
            config.image?.let {
                NetworkScheduleImage(original = it, medium = it)
            }

        val showImageObject: NetworkScheduleImage? =
            if (config.showImageOriginal != null || config.showImageMedium != null) {
                NetworkScheduleImage(original = config.showImageOriginal, medium = config.showImageMedium)
            } else {
                null
            }

        return NetworkTodayScheduleResponse(
            id = config.id,
            name = "Episode Name ${config.id}",
            airdate = "2023-01-01",
            airstamp = "sometime",
            airtime = "10:00",
            runtime = 30,
            season = 1,
            number = config.id,
            image = episodeImageObject,
            summary = "summary for episode ${config.id}",
            url = "url_episode_${config.id}",
            _links = mockLinks,
            imdbId = config.imdb,
            show =
                NetworkScheduleShow(
                    id = config.showId,
                    name = "Test Show ${config.showId}",
                    externals =
                        config.imdb?.let {
                            NetworkScheduleExternals(imdb = it, thetvdb = 1, tvrage = 1)
                        },
                    image = showImageObject,
                    genres = emptyList(),
                    language = "English",
                    network = null,
                    officialSite = null,
                    premiered = null,
                    rating = null,
                    runtime = 30,
                    schedule = null,
                    status = "Running",
                    summary = "summary for show ${config.showId}",
                    type = "Scripted",
                    updated = 123,
                    url = "url_show_${config.showId}",
                    webChannel = null,
                    weight = 1,
                    _links = null,
                ),
        )
    }

    private fun createMockTomorrowScheduleResponse(config: MockScheduleConfig): NetworkTomorrowScheduleResponse {
        val mockLinks = NetworkShowEpisodeLinks(self = NetworkShowNextEpisodeSelf(href = "href_episode_${config.id}"))

        val episodeImageObject: NetworkScheduleImage? =
            config.image?.let {
                NetworkScheduleImage(original = it, medium = it)
            }

        val showImageObject: NetworkScheduleImage? =
            if (config.showImageOriginal != null || config.showImageMedium != null) {
                NetworkScheduleImage(original = config.showImageOriginal, medium = config.showImageMedium)
            } else {
                null
            }

        return NetworkTomorrowScheduleResponse(
            id = config.id,
            name = "Episode Name ${config.id}",
            airdate = "2023-01-01",
            airstamp = "sometime",
            airtime = "10:00",
            runtime = 30,
            season = 1,
            number = config.id,
            image = episodeImageObject,
            summary = "summary for episode ${config.id}",
            url = "url_episode_${config.id}",
            _links = mockLinks,
            show =
                NetworkScheduleShow(
                    id = config.showId,
                    name = "Test Show ${config.showId}",
                    externals =
                        config.imdb?.let {
                            NetworkScheduleExternals(imdb = it, thetvdb = 1, tvrage = 1)
                        },
                    image = showImageObject,
                    genres = emptyList(),
                    language = "English",
                    network = null,
                    officialSite = null,
                    premiered = null,
                    rating = null,
                    runtime = 30,
                    schedule = null,
                    status = "Running",
                    summary = "summary for show ${config.showId}",
                    type = "Scripted",
                    updated = 123,
                    url = "url_show_${config.showId}",
                    webChannel = null,
                    weight = 1,
                    _links = null,
                ),
        )
    }

    @Test
    fun `getShowImageAndTvmazeId - uses TmdbService when tmdbId is provided`() =
        runTest {
            val tmdbId = 123
            val tvmazeId = 456
            fakeTmdbService.mockShowDetailsResponse = com.theupnextapp.network.models.tmdb.NetworkTmdbShowDetailsResponse(id = tmdbId, poster_path = "tmdb_poster.png", backdrop_path = "tmdb_backdrop.png")
            fakeTvMazeService.mockShowLookupResponse =
                com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupResponse(
                    id = tvmazeId,
                    image = com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupImage(medium = "tvm_med", original = "tvm_orig"),
                    _links = null,
                    externals = null,
                    name = "Show Title",
                    premiered = "2024-01-01",
                    status = "Running",
                    summary = "Summary",
                    updated = 123456789,
                    url = "http://example.com",
                    averageRuntime = 60,
                    dvdCountry = null,
                    genres = emptyList(),
                    language = "English",
                    network =
                        com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupNetwork(
                            id = 1,
                            name = "Network",
                            country =
                                com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupCountry(
                                    code = "US",
                                    name = "United States",
                                    timezone = "America/New_York",
                                ),
                        ),
                    officialSite = "http://example.com",
                    rating = com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupRating(average = 8.0),
                    runtime = 60,
                    schedule = com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupSchedule(days = emptyList(), time = "20:00"),
                    type = "Scripted",
                    webChannel =
                        com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupWebChannel(
                            id = 1,
                            name = "WebChannel",
                            country =
                                com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupCountryX(
                                    code = "US",
                                    name = "United States",
                                    timezone = "America/New_York",
                                ),
                        ),
                    weight = 100,
                )

            val result = repository.getShowImageAndTvmazeId("tt123", tmdbId)

            assertEquals(Pair("https://image.tmdb.org/t/p/originaltmdb_poster.png", null), result)
        }

    @Test
    fun `getShowImageAndTvmazeId - falls back to TvMazeService when tmdbId is null`() =
        runTest {
            val tvmazeId = 456
            fakeTvMazeService.mockShowLookupResponse =
                com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupResponse(
                    id = tvmazeId,
                    image = com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupImage(medium = "tvm_med", original = "tvm_orig"),
                    _links = null,
                    externals = null,
                    name = "Show Title",
                    premiered = "2024-01-01",
                    status = "Running",
                    summary = "Summary",
                    updated = 123456789,
                    url = "http://example.com",
                    averageRuntime = 60,
                    dvdCountry = null,
                    genres = emptyList(),
                    language = "English",
                    network =
                        com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupNetwork(
                            id = 1,
                            name = "Network",
                            country =
                                com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupCountry(
                                    code = "US",
                                    name = "United States",
                                    timezone = "America/New_York",
                                ),
                        ),
                    officialSite = "http://example.com",
                    rating = com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupRating(average = 8.0),
                    runtime = 60,
                    schedule = com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupSchedule(days = emptyList(), time = "20:00"),
                    type = "Scripted",
                    webChannel =
                        com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupWebChannel(
                            id = 1,
                            name = "WebChannel",
                            country =
                                com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupCountryX(
                                    code = "US",
                                    name = "United States",
                                    timezone = "America/New_York",
                                ),
                        ),
                    weight = 100,
                )
            fakeTvMazeService.mockShowImagesResponse =
                com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowImageResponse().apply {
                    add(
                        com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowImageResponseItem(
                            id = 101, type = "poster", main = true,
                            resolutions =
                                com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowImageResolutions(
                                    original = com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowImageOriginal(url = "tvm_orig_res"),
                                    medium = com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowImageMedium(url = "tvm_med_res"),
                                ),
                        ),
                    )
                }

            val result = repository.getShowImageAndTvmazeId("tt123", null)

            assertEquals(Pair("tvm_orig", tvmazeId), result)
        }
}
