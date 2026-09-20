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

package com.theupnextapp.work

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.theupnextapp.TestApplication
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.network.models.trakt.NetworkTraktMyScheduleEpisode
import com.theupnextapp.network.models.trakt.NetworkTraktMyScheduleEpisodeIds
import com.theupnextapp.network.models.trakt.NetworkTraktMyScheduleResponse
import com.theupnextapp.network.models.trakt.NetworkTraktMyScheduleResponseItem
import com.theupnextapp.network.models.trakt.NetworkTraktMyScheduleShow
import com.theupnextapp.network.models.trakt.NetworkTraktMyScheduleShowIds
import com.theupnextapp.repository.SettingsRepository
import com.theupnextapp.repository.TraktRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = TestApplication::class)
class NotificationWorkerTest {
    private lateinit var context: Context
    private lateinit var mockSettingsRepository: SettingsRepository
    private lateinit var mockTraktRepository: TraktRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        mockSettingsRepository = mock()
        mockTraktRepository = mock()
    }

    @Test
    fun testNotificationWorker_returnsSuccess_whenNotificationsEnabled() {
        runBlocking {
            whenever(mockSettingsRepository.areNotificationsEnabled).thenReturn(flowOf(true))
            val accessToken =
                TraktAccessToken(
                    access_token = "token",
                    token_type = "bearer",
                    expires_in = 3600,
                    refresh_token = "refresh",
                    scope = "public",
                    created_at = 1234567890L,
                )
            whenever(mockTraktRepository.getTraktAccessTokenSync()).thenReturn(accessToken)
            whenever(
                mockTraktRepository.getTraktMySchedule(
                    any(),
                    any(),
                    any(),
                ),
            ).thenReturn(Result.success(NetworkTraktMyScheduleResponse()))

            val worker =
                TestListenableWorkerBuilder<NotificationWorker>(context)
                    .setWorkerFactory(
                        object : WorkerFactory() {
                            override fun createWorker(
                                appContext: Context,
                                workerClassName: String,
                                workerParameters: WorkerParameters,
                            ): ListenableWorker? {
                                return NotificationWorker(
                                    appContext,
                                    workerParameters,
                                    mockTraktRepository,
                                    mockSettingsRepository,
                                )
                            }
                        },
                    )
                    .build()

            val result = worker.doWork()
            assertEquals(ListenableWorker.Result.success(), result)
        }
    }

    @Test
    fun testNotificationWorker_returnsSuccess_whenNotificationsDisabled() {
        runBlocking {
            whenever(mockSettingsRepository.areNotificationsEnabled).thenReturn(flowOf(false))

            val worker =
                TestListenableWorkerBuilder<NotificationWorker>(context)
                    .setWorkerFactory(
                        object : WorkerFactory() {
                            override fun createWorker(
                                appContext: Context,
                                workerClassName: String,
                                workerParameters: WorkerParameters,
                            ): ListenableWorker? {
                                return NotificationWorker(
                                    appContext,
                                    workerParameters,
                                    mockTraktRepository,
                                    mockSettingsRepository,
                                )
                            }
                        },
                    )
                    .build()

            val result = worker.doWork()
            assertEquals(ListenableWorker.Result.success(), result)
        }
    }

    @Test
    fun filterEpisodesAiringToday_excludesEpisodeAiringTomorrowInUserTimezone() {
        // Episode airs Monday 01:00 UTC = 03:00 SAST (UTC+2)
        val scheduleItem = createScheduleItem(
            showTitle = "Lanterns",
            episodeTitle = "Bad Optics",
            season = 1,
            number = 6,
            episodeTraktId = 9991,
            firstAired = "2026-09-21T01:00:00.000Z",
        )

        // User's current day is Sunday Sep 20 in SAST
        val result = NotificationWorker.filterEpisodesAiringToday(
            schedule = listOf(scheduleItem),
            userToday = LocalDate.of(2026, 9, 20),
            userZone = ZoneId.of("Africa/Johannesburg"),
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun filterEpisodesAiringToday_includesEpisodeAiringTodayInUserTimezone() {
        // Episode airs Monday 01:00 UTC = 03:00 SAST (UTC+2)
        val scheduleItem = createScheduleItem(
            showTitle = "Lanterns",
            episodeTitle = "Bad Optics",
            season = 1,
            number = 6,
            episodeTraktId = 9991,
            firstAired = "2026-09-21T01:00:00.000Z",
        )

        // User's current day is Monday Sep 21 in SAST
        val result = NotificationWorker.filterEpisodesAiringToday(
            schedule = listOf(scheduleItem),
            userToday = LocalDate.of(2026, 9, 21),
            userZone = ZoneId.of("Africa/Johannesburg"),
        )

        assertEquals(1, result.size)
        assertEquals("Bad Optics", result[0].episode?.title)
    }

    @Test
    fun filterEpisodesAiringToday_handlesTimezoneBoundaryAcrossDateLine() {
        // US Sunday 9 PM EDT broadcast = 2026-09-21T01:00:00.000Z UTC = Sunday 2026-09-20 21:00 EDT
        val scheduleItem = createScheduleItem(
            showTitle = "Lanterns",
            episodeTitle = "Bad Optics",
            season = 1,
            number = 6,
            episodeTraktId = 9991,
            firstAired = "2026-09-21T01:00:00.000Z",
        )

        // User in New York on Sunday Sep 20: episode airs TODAY for them
        val result = NotificationWorker.filterEpisodesAiringToday(
            schedule = listOf(scheduleItem),
            userToday = LocalDate.of(2026, 9, 20),
            userZone = ZoneId.of("America/New_York"),
        )

        assertEquals(1, result.size)
        assertEquals("Bad Optics", result[0].episode?.title)
    }

    @Test
    fun filterEpisodesAiringToday_deduplicatesIdenticalEpisodes() {
        val scheduleItem1 = createScheduleItem(
            showTitle = "Lanterns",
            episodeTitle = "Bad Optics",
            season = 1,
            number = 6,
            episodeTraktId = 9991,
            firstAired = "2026-09-21T01:00:00.000Z",
        )
        val scheduleItem2 = createScheduleItem(
            showTitle = "Lanterns",
            episodeTitle = "Bad Optics",
            season = 1,
            number = 6,
            episodeTraktId = 9991,
            firstAired = "2026-09-21T01:00:00.000Z",
        )

        val result = NotificationWorker.filterEpisodesAiringToday(
            schedule = listOf(scheduleItem1, scheduleItem2),
            userToday = LocalDate.of(2026, 9, 21),
            userZone = ZoneId.of("Africa/Johannesburg"),
        )

        assertEquals(1, result.size)
    }

    @Test
    fun filterEpisodesAiringToday_ignoresInvalidOrNullAirDates() {
        val itemWithNull = createScheduleItem(
            showTitle = "Show",
            episodeTitle = "Episode",
            season = 1,
            number = 1,
            episodeTraktId = 1,
            firstAired = null,
        )
        val itemWithInvalid = createScheduleItem(
            showTitle = "Show",
            episodeTitle = "Episode",
            season = 1,
            number = 2,
            episodeTraktId = 2,
            firstAired = "not-a-valid-date",
        )

        val result = NotificationWorker.filterEpisodesAiringToday(
            schedule = listOf(itemWithNull, itemWithInvalid),
            userToday = LocalDate.of(2026, 9, 21),
            userZone = ZoneId.of("Africa/Johannesburg"),
        )

        assertTrue(result.isEmpty())
    }

    private fun createScheduleItem(
        showTitle: String,
        episodeTitle: String,
        season: Int,
        number: Int,
        episodeTraktId: Int,
        firstAired: String?,
    ): NetworkTraktMyScheduleResponseItem {
        return NetworkTraktMyScheduleResponseItem(
            first_aired = firstAired,
            episode = NetworkTraktMyScheduleEpisode(
                season = season,
                number = number,
                title = episodeTitle,
                ids = NetworkTraktMyScheduleEpisodeIds(
                    trakt = episodeTraktId,
                    tvdb = null,
                    imdb = null,
                    tmdb = null,
                ),
            ),
            show = NetworkTraktMyScheduleShow(
                title = showTitle,
                year = 2026,
                ids = NetworkTraktMyScheduleShowIds(
                    trakt = 100,
                    slug = "lanterns",
                    tvdb = null,
                    imdb = null,
                    tmdb = null,
                ),
            ),
        )
    }
}
