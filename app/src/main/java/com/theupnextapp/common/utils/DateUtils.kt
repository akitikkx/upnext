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

    fun getTimeDifferenceForDisplay(startTime: Long? = null, endTime: Long): TimeDifferenceForDisplay? {
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

    fun dateDifference(startTime: Long? = null, endTime: Long, type: String): Long? {
        val diffCount = startTime ?: Calendar.getInstance().timeInMillis - endTime
        var diff: Long = -1L

        when (type) {
            "days" -> diff = endTime.let { TimeUnit.MILLISECONDS.toDays(diffCount) }
            "hours" -> diff = endTime.let { TimeUnit.MILLISECONDS.toHours(diffCount) }
            "minutes" -> diff = endTime.let { TimeUnit.MILLISECONDS.toMinutes(diffCount) }
            "seconds" -> diff = endTime.let { TimeUnit.MILLISECONDS.toSeconds(diffCount) }
        }
        return diff
    }
}