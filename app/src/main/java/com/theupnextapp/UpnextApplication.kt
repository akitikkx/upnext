package com.theupnextapp

import android.app.Application
import android.os.Build
import androidx.work.*
import com.theupnextapp.work.RefreshShowsWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class UpnextApplication : Application() {

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        delayedInit()
    }

    private fun delayedInit() {
        applicationScope.launch {
            launchBackgroundTasks()
        }
    }

    private fun launchBackgroundTasks() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    setRequiresDeviceIdle(true)
                }
            }.build()

        val refreshShowsRequest = PeriodicWorkRequestBuilder<RefreshShowsWorker>(4, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        val refreshTraktData = PeriodicWorkRequestBuilder<RefreshShowsWorker>(30, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        val workManager = WorkManager.getInstance(this)

        workManager.enqueueUniquePeriodicWork(
            RefreshShowsWorker.UPNEXT_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            refreshShowsRequest
        )

        workManager.enqueueUniquePeriodicWork(
            RefreshShowsWorker.TRAKT_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            refreshTraktData
        )
    }

}