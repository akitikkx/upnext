/*
 * MIT License
 *
 * Copyright (c) 2022 Ahmed Tikiwa
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

package com.theupnextapp.common.utils

import com.theupnextapp.common.utils.models.TimeDifferenceForDisplay
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


object DateUtils {

    fun currentDate(): String? {
        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return simpleDateFormat.format(calendar.time)
    }

    fun tomorrowDate(): String? {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrow = calendar.time
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return simpleDateFormat.format(tomorrow)
    }

    fun yesterdayDate(): String? {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = calendar.time
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return simpleDateFormat.format(yesterday)
    }

    fun getTimeDifferenceForDisplay(
        startTime: Long? = null,
        endTime: Long
    ): TimeDifferenceForDisplay? {
        var timeDifferenceForDisplay: TimeDifferenceForDisplay? = null

        val daysDiff = dateDifference(startTime = startTime, endTime = endTime, type = "days")
        val hoursDiff = dateDifference(startTime = startTime, endTime = endTime, type = "hours")
        val minutesDiff = dateDifference(startTime = startTime, endTime = endTime, type = "minutes")
        val secondsDiff = dateDifference(startTime = startTime, endTime = endTime, type = "seconds")

        if (daysDiff == 0L && hoursDiff == 0L && minutesDiff != -0L) {
            timeDifferenceForDisplay =
                TimeDifferenceForDisplay(
                    minutesDiff,
                    "minutes"
                )
        } else if (daysDiff == 0L && hoursDiff != 0L) {
            timeDifferenceForDisplay =
                TimeDifferenceForDisplay(
                    hoursDiff,
                    "hours"
                )
        } else if (daysDiff != 0L && hoursDiff != 0L) {
            timeDifferenceForDisplay =
                TimeDifferenceForDisplay(
                    daysDiff,
                    "days"
                )
        } else if (daysDiff == 0L && hoursDiff == 0L && minutesDiff == 0L && secondsDiff != 0L) {
            timeDifferenceForDisplay =
                TimeDifferenceForDisplay(
                    secondsDiff,
                    "seconds"
                )
        } else if (daysDiff == 0L && hoursDiff == 0L && minutesDiff == 0L && secondsDiff == 0L) {
            timeDifferenceForDisplay =
                TimeDifferenceForDisplay(
                    0L,
                    "seconds"
                )
        }
        return timeDifferenceForDisplay
    }

    fun dateDifference(startTime: Long? = null, endTime: Long, type: String): Long {
        val diffCount = startTime ?: Calendar.getInstance().timeInMillis - endTime
        var diff: Long = -1L

        when (type) {
            DAYS -> diff = endTime.let { TimeUnit.MILLISECONDS.toDays(diffCount) }
            HOURS -> diff = endTime.let { TimeUnit.MILLISECONDS.toHours(diffCount) }
            MINUTES -> diff = endTime.let { TimeUnit.MILLISECONDS.toMinutes(diffCount) }
            SECONDS -> diff = endTime.let { TimeUnit.MILLISECONDS.toSeconds(diffCount) }
        }
        return diff
    }

    fun getDisplayDateFromDateStamp(dateStamp: String): Date? {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return format.parse(dateStamp)
    }

    const val DAYS = "days"
    const val HOURS = "hours"
    const val MINUTES = "minutes"
    const val SECONDS = "seconds"
}