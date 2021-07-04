package com.theupnextapp.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.theupnextapp.R

abstract class BaseWorker (
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters) {

    abstract val contentTitle: String

    protected fun createForegroundInfo(): ForegroundInfo {
        val notification = NotificationCompat.Builder(
            applicationContext, CHANNEL_ID
        )
            .setContentTitle(contentTitle)
            .setTicker(contentTitle)
            .setSmallIcon(R.drawable.ic_baseline_arrow_circle_down_24)
            .setOngoing(true)
            .setSilent(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        return ForegroundInfo(1, notification.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    protected fun createNotificationChannel() {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            applicationContext.getString(R.string.app_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = applicationContext.getString(R.string.app_name) + " Notifications"
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "ShowsUpdate"
    }

}