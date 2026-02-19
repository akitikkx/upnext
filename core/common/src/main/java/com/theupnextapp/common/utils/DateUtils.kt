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

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object DateUtils {
    /**
     * Calculates the difference between two time points in the specified time unit.
     * Returns endTime - startTime.
     * A positive result means endTime is after startTime.
     * A negative result means endTime is before startTime.
     */
    fun calculateDifference(
        startTimeMillis: Long,
        endTimeMillis: Long,
        unit: TimeUnit,
    ): Long {
        val diffMillis = endTimeMillis - startTimeMillis
        return unit.convert(diffMillis, TimeUnit.MILLISECONDS)
    }

    fun dateDifference(
        startTime: Long? = null,
        endTime: Long,
        type: String,
    ): Long {
        val diffCount =
            startTime
                ?: (Calendar.getInstance().timeInMillis - endTime)
        var diff: Long = -1L

        when (type) {
            DAYS ->
                diff = endTime.let { TimeUnit.MILLISECONDS.toDays(diffCount) }

            HOURS ->
                diff = endTime.let { TimeUnit.MILLISECONDS.toHours(diffCount) }

            MINUTES ->
                diff = endTime.let { TimeUnit.MILLISECONDS.toMinutes(diffCount) }

            SECONDS -> {
                diff = endTime.let { TimeUnit.MILLISECONDS.toSeconds(diffCount) }
            }
        }
        return diff
    }

    fun getDisplayDate(airstamp: String): String? {
        val date = parseDate(airstamp) ?: return null
        val displayFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return displayFormat.format(date)
    }

    private fun parseDate(dateString: String): Date? {
        // Try ISO 8601 first
        try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            return format.parse(dateString)
        } catch (e: Exception) {
            // Fallback to simple date
             try {
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                return format.parse(dateString)
            } catch (e: Exception) {
                return null
            }
        }
    }

    fun getDisplayDateFromDateStamp(dateStamp: String): Date? {
        return parseDate(dateStamp)
    }

    /**
     * Formats a given timestamp (in milliseconds) into a String representation.
     * @param timestampMillis The time in milliseconds since epoch.
     * @param formatPattern The desired date format pattern (e.g., "yyyy-MM-dd").
     * @return Formatted date string, or null if formatting fails (should be rare with valid inputs).
     */
    fun formatTimestampToString(
        timestampMillis: Long,
        formatPattern: String = "yyyy-MM-dd",
    ): String? {
        return try {
            val sdf = SimpleDateFormat(formatPattern, Locale.getDefault())
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timestampMillis
            sdf.format(calendar.time)
        } catch (_: Exception) {
            // Log error or handle appropriately
            null // Or throw an exception if this case should not happen
        }
    }

    fun getRelativeTimeSpanString(dateString: String): String? {
        // Try parsing ISO 8601 first
        var date: Date? = null
        try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            format.timeZone = java.util.TimeZone.getTimeZone("UTC")
            date = format.parse(dateString)
        } catch (e: Exception) {
            // Fallback or ignore
        }

        // If that fails, try simpler format
        if (date == null) {
            try {
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                date = format.parse(dateString)
            } catch (e: Exception) {
                return null
            }
        }

        return date?.let {
            val now = System.currentTimeMillis()
            android.text.format.DateUtils.getRelativeTimeSpanString(
                it.time,
                now,
                android.text.format.DateUtils.MINUTE_IN_MILLIS,
                android.text.format.DateUtils.FORMAT_ABBREV_RELATIVE
            ).toString()
        }
    }

    const val DAYS = "days"
    const val HOURS = "hours"
    const val MINUTES = "minutes"
    const val SECONDS = "seconds"
}
