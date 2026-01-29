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

package com.theupnextapp.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.theupnextapp.core.common.R
import timber.log.Timber

abstract class BaseWorker(
    private val
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters) {
    abstract val notificationId: Int
    abstract val contentTitleText: String

    override suspend fun getForegroundInfo(): ForegroundInfo {
        ensureNotificationChannelCreated()
        return createForegroundInfo(
            notificationId = notificationId,
            channelId = NOTIFICATION_CHANNEL_ID,
            contentText = contentTitleText,
            // TODO: Replace with your app's icon
            smallIconRes = R.drawable.ic_baseline_arrow_circle_down_24,
        )
    }

    private fun createForegroundInfo(
        notificationId: Int,
        channelId: String,
        contentText: String,
        smallIconRes: Int,
    ): ForegroundInfo {
        val notification =
            NotificationCompat.Builder(appContext, channelId)
                .setContentTitle(contentText)
                .setSmallIcon(smallIconRes)
                .setOngoing(true)
                .setSilent(true)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .build()

        return ForegroundInfo(notificationId, notification)
    }

    protected fun buildForegroundInfo(): ForegroundInfo {
        ensureNotificationChannelCreated()
        return createForegroundInfo(
            notificationId = notificationId,
            channelId = NOTIFICATION_CHANNEL_ID,
            contentText = contentTitleText,
            smallIconRes = R.drawable.ic_baseline_arrow_circle_down_24,
        )
    }

    private fun ensureNotificationChannelCreated() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                appContext.getSystemService(Context.NOTIFICATION_SERVICE)
                    as? NotificationManager
            if (notificationManager == null) {
                Timber.tag(TAG).e("NotificationManager not available.")
                return
            }

            if (notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)
                == null
            ) {
                val channelName =
                    appContext.getString(R.string.notification_channel_name)
                val channelDescription =
                    appContext.getString(R.string.notification_channel_description)
                val importance = NotificationManager.IMPORTANCE_DEFAULT

                val channel =
                    NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance).apply {
                        description = channelDescription
                    }
                notificationManager.createNotificationChannel(channel)
                Timber.tag(TAG).d(
                    message = "Notification channel '$NOTIFICATION_CHANNEL_ID' created.",
                )
            }
        }
    }

    companion object {
        private const val TAG = "BaseWorker"
        const val NOTIFICATION_CHANNEL_ID = "AppUpdatesChannel"
    }
}
