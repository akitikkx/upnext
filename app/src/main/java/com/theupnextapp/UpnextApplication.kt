package com.theupnextapp

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.theupnextapp.work.RefreshShowsWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class UpnextApplication : Application() {

    init {
        application = this
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

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
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    setRequiresDeviceIdle(true)
                }
            }.build()

        val refreshShowsRequest = PeriodicWorkRequestBuilder<RefreshShowsWorker>(4, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        val workManager = WorkManager.getInstance(this)

        workManager.enqueueUniquePeriodicWork(
            RefreshShowsWorker.UPNEXT_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            refreshShowsRequest
        )
    }

    companion object {
        private var application: UpnextApplication? = null

        @get:Synchronized
        val instance: UpnextApplication?
            get() {
                if (application == null) {
                    application = UpnextApplication()
                }
                return application
            }

        val context: Context?
            get() = application
    }

}