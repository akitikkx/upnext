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

package com.theupnextapp.ui.episodeDetail

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.theupnextapp.TestApplication
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = TestApplication::class)
class EpisodeDateUtilsTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun futureEpisodeAiringTomorrow_formatsWithAirsAndTomorrow() {
        // Current time: Sunday Sep 20, 2026 12:00 UTC
        val currentMillis = Instant.parse("2026-09-20T12:00:00Z").toEpochMilli()
        // Episode airs Monday morning 03:00 SAST (UTC+2) = 01:00 UTC
        val airDateString = "2026-09-21T01:00:00.000Z"
        val zone = ZoneId.of("Africa/Johannesburg")

        val result = formatRelativeDate(
            context = context,
            dateString = airDateString,
            currentTimeMillis = currentMillis,
            zoneId = zone,
        )

        assertTrue("Expected to start with Airs: but was $result", result.startsWith("Airs:"))
        assertTrue("Expected to contain Sep 21, 2026 but was $result", result.contains("Sep 21, 2026"))
        assertTrue("Expected to contain Tomorrow but was $result", result.contains("Tomorrow"))
    }

    @Test
    fun futureEpisodeInTwoMonths_formatsWithAirs() {
        val currentMillis = Instant.parse("2026-09-20T12:00:00Z").toEpochMilli()
        val airDateString = "2026-11-20T20:00:00.000Z"
        val zone = ZoneId.of("UTC")

        val result = formatRelativeDate(
            context = context,
            dateString = airDateString,
            currentTimeMillis = currentMillis,
            zoneId = zone,
        )

        assertTrue("Expected to start with Airs: but was $result", result.startsWith("Airs:"))
        assertTrue("Expected to contain Nov 20, 2026 but was $result", result.contains("Nov 20, 2026"))
    }

    @Test
    fun pastEpisodeAiredYesterday_formatsWithAiredAndYesterday() {
        // Current time: Sunday Sep 20, 2026 12:00 UTC
        val currentMillis = Instant.parse("2026-09-20T12:00:00Z").toEpochMilli()
        // Episode aired Saturday Sep 19, 2026 12:00 UTC
        val airDateString = "2026-09-19T12:00:00.000Z"
        val zone = ZoneId.of("UTC")

        val result = formatRelativeDate(
            context = context,
            dateString = airDateString,
            currentTimeMillis = currentMillis,
            zoneId = zone,
        )

        assertTrue("Expected to start with Aired: but was $result", result.startsWith("Aired:"))
        assertTrue("Expected to contain Sep 19, 2026 but was $result", result.contains("Sep 19, 2026"))
        assertTrue("Expected to contain Yesterday but was $result", result.contains("Yesterday"))
    }

    @Test
    fun pastEpisodeLongAgo_formatsWithAired() {
        val currentMillis = Instant.parse("2026-09-20T12:00:00Z").toEpochMilli()
        val airDateString = "2020-01-01T00:00:00.000Z"
        val zone = ZoneId.of("UTC")

        val result = formatRelativeDate(
            context = context,
            dateString = airDateString,
            currentTimeMillis = currentMillis,
            zoneId = zone,
        )

        assertTrue("Expected to start with Aired: but was $result", result.startsWith("Aired:"))
        assertTrue("Expected to contain Jan 1, 2020 but was $result", result.contains("Jan 1, 2020"))
    }

    @Test
    fun timezoneConversion_correctlyReflectsLocalCalendarDate() {
        // Sunday 9 PM EDT broadcast = 2026-09-21T01:00:00.000Z in UTC
        val airDateString = "2026-09-21T01:00:00.000Z"
        val currentMillis = Instant.parse("2026-09-20T12:00:00Z").toEpochMilli()

        // In New York (EDT, UTC-4), it airs Sunday Sep 20 at 9 PM
        val newYorkResult = formatRelativeDate(
            context = context,
            dateString = airDateString,
            currentTimeMillis = currentMillis,
            zoneId = ZoneId.of("America/New_York"),
        )
        assertTrue("Expected Sep 20, 2026 in NY but was $newYorkResult", newYorkResult.contains("Sep 20, 2026"))
        assertTrue("Expected Airs: in NY but was $newYorkResult", newYorkResult.startsWith("Airs:"))

        // In Johannesburg (SAST, UTC+2), it airs Monday Sep 21 at 3 AM
        val johannesburgResult = formatRelativeDate(
            context = context,
            dateString = airDateString,
            currentTimeMillis = currentMillis,
            zoneId = ZoneId.of("Africa/Johannesburg"),
        )
        assertTrue("Expected Sep 21, 2026 in SAST but was $johannesburgResult", johannesburgResult.contains("Sep 21, 2026"))
        assertTrue("Expected Airs: in SAST but was $johannesburgResult", johannesburgResult.startsWith("Airs:"))
    }

    @Test
    fun invalidDateString_fallsBackToAiredFormat() {
        val result = formatRelativeDate(
            context = context,
            dateString = "invalid-date",
        )

        assertEquals("Aired: invalid-date", result)
    }

    @Test
    fun dateWithoutTime_parsesSuccessfully() {
        val zone = ZoneId.of("UTC")
        val parsed = parseToZonedDateTime("2026-09-21", zone)

        assertEquals(2026, parsed.year)
        assertEquals(9, parsed.monthValue)
        assertEquals(21, parsed.dayOfMonth)
    }
}
