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
import com.theupnextapp.R
import com.theupnextapp.common.utils.DateUtils
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

internal fun formatRelativeDate(
    context: Context,
    dateString: String,
    currentTimeMillis: Long = System.currentTimeMillis(),
    zoneId: ZoneId = ZoneId.systemDefault(),
): String {
    return try {
        val localZoned = parseToZonedDateTime(dateString, zoneId)
        val timeMillis = localZoned.toInstant().toEpochMilli()
        val isFuture = timeMillis > currentTimeMillis
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        val formattedDate = localZoned.format(formatter)
        val relativeTime = DateUtils.getRelativeTimeSpanString(timeMillis, currentTimeMillis).toString()

        if (isFuture) {
            if (relativeTime.equals(formattedDate, ignoreCase = true)) {
                context.getString(R.string.episode_detail_airs_date, formattedDate)
            } else {
                context.getString(R.string.episode_detail_airs_relative_date, relativeTime, formattedDate)
            }
        } else {
            if (relativeTime.equals(formattedDate, ignoreCase = true)) {
                context.getString(R.string.episode_detail_aired_date, formattedDate)
            } else {
                context.getString(R.string.episode_detail_aired_relative_date, relativeTime, formattedDate)
            }
        }
    } catch (_: Exception) {
        context.getString(R.string.episode_detail_aired_date, dateString)
    }
}

internal fun parseToZonedDateTime(
    dateString: String,
    zoneId: ZoneId,
): ZonedDateTime {
    return try {
        ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME).withZoneSameInstant(zoneId)
    } catch (_: Exception) {
        try {
            Instant.parse(dateString).atZone(zoneId)
        } catch (_: Exception) {
            LocalDate.parse(dateString).atStartOfDay(zoneId)
        }
    }
}
