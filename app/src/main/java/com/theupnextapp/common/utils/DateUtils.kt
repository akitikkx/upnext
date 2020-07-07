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

    fun getTimeDifferenceForDisplay(time: Long): TimeDifferenceForDisplay? {
        var timeDifferenceForDisplay: TimeDifferenceForDisplay? = null

        val daysDiff = dateDifference(time, "days")
        val hoursDiff = dateDifference(time, "hours")
        val minutesDiff = dateDifference(time, "minutes")
        val secondsDiff = dateDifference(time, "seconds")

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
        } else if (daysDiff != 0L && hoursDiff == 0L) {
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
        }
        return timeDifferenceForDisplay
    }

    fun dateDifference(time: Long, type: String): Long? {
        val diffCount = Calendar.getInstance().timeInMillis - time
        var diff: Long = -1L

        when (type) {
            "days" -> diff = time.let { TimeUnit.MILLISECONDS.toDays(diffCount) }
            "hours" -> diff = time.let { TimeUnit.MILLISECONDS.toHours(diffCount) }
            "minutes" -> diff = time.let { TimeUnit.MILLISECONDS.toMinutes(diffCount) }
            "seconds" -> diff = time.let { TimeUnit.MILLISECONDS.toSeconds(diffCount) }
        }
        return diff
    }
}