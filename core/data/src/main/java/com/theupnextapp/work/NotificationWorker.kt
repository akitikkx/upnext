/*
 * MIT License
 *
 * Copyright (c) 2024 Ahmed Tikiwa
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

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.theupnextapp.core.common.R
import com.theupnextapp.network.models.trakt.NetworkTraktMyScheduleResponseItem
import com.theupnextapp.repository.SettingsRepository
import com.theupnextapp.repository.TraktRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val traktRepository: TraktRepository,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val areNotificationsEnabled = settingsRepository.areNotificationsEnabled.first()
        if (!areNotificationsEnabled) {
            return Result.success()
        }

        val accessToken = traktRepository.traktAccessToken.first()
        if (accessToken?.access_token.isNullOrEmpty()) {
            return Result.success()
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayDate = dateFormat.format(Date())

        val scheduleResult = traktRepository.getTraktMySchedule(
            token = accessToken!!.access_token!!,
            startDate = todayDate,
            days = 1
        )

        if (scheduleResult.isSuccess) {
            val schedule = scheduleResult.getOrNull()
            if (!schedule.isNullOrEmpty()) {
                sendConsolidatedNotification(schedule)
            }
        }

        return Result.success()
    }

    private fun sendConsolidatedNotification(schedule: List<NetworkTraktMyScheduleResponseItem>) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "New Episodes",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        if (schedule.size == 1) {
            // Single Show Formatting
            val item = schedule.first()
            val showTitle = item.show?.title ?: "New Episode"
            val seasonNumber = item.episode?.season
            val episodeNumber = item.episode?.number
            val episodeTitle = item.episode?.title
            val traktId = item.show?.ids?.trakt
            
            val title = "New Episode: $showTitle"
            val message = if (seasonNumber != null && episodeNumber != null) {
                "S${seasonNumber}E$episodeNumber - $episodeTitle airing today!"
            } else {
                "$episodeTitle airing today!"
            }

            builder.setContentTitle(title)
                .setContentText(message)

            // Deep link directly to the episode if it's a single show
            val deepLinkUri = android.net.Uri.parse("theupnextapp://show/${traktId}/season/${seasonNumber}/episode/${episodeNumber}")
            val intent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                applicationContext,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
            builder.setContentIntent(pendingIntent)

        } else {
            // Multiple Shows Formatting (InboxStyle Digest)
            val inboxStyle = NotificationCompat.InboxStyle()
            inboxStyle.setBigContentTitle("${schedule.size} New Episodes Airing Today!")

            schedule.take(5).forEach { item ->
                val showTitle = item.show?.title ?: "Unknown Show"
                val season = item.episode?.season
                val episode = item.episode?.number
                val line = if (season != null && episode != null) {
                    "$showTitle (S${season}E$episode)"
                } else {
                    showTitle
                }
                inboxStyle.addLine(line)
            }
            
            if (schedule.size > 5) {
                inboxStyle.setSummaryText("+${schedule.size - 5} more shows")
            }

            builder.setContentTitle("${schedule.size} shows are returning today!")
                .setContentText("Expand to see your daily watchlist.")
                .setStyle(inboxStyle)

            // Deep link opens the app dashboard for the digest
            val intent = applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            if (intent != null) {
                val pendingIntent: PendingIntent = PendingIntent.getActivity(
                    applicationContext,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
                builder.setContentIntent(pendingIntent)
            }
        }

        // Use a static ID (e.g., 1) so it overrides previous daily digests instead of stacking
        notificationManager.notify(1, builder.build())
    }

    companion object {
        const val CHANNEL_ID = "new_episodes_channel"
        const val WORK_NAME = "NotificationWorker"
    }
}
