package com.theupnextapp

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.theupnextapp.common.utils.UpnextPreferenceManager
import com.theupnextapp.common.utils.models.TableUpdateInterval
import com.theupnextapp.work.RefreshDashboardShowsWorker
import com.theupnextapp.work.RefreshTraktExploreWorker
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
        setupTheme()
    }

    private fun delayedInit() {
        applicationScope.launch {
            launchBackgroundTasks()
        }
    }

    private fun setupTheme() {
        when (UpnextPreferenceManager(this).getSelectedTheme()) {
            resources.getString(R.string.dark_mode_follow_system) -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            )
            resources.getString(R.string.dark_mode_no) -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )
            resources.getString(R.string.dark_mode_yes) -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
            )
            resources.getString(R.string.dark_mode_auto_battery) -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
            )
            else -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    private fun launchBackgroundTasks() {
        val constraints = Constraints.Builder()
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    setRequiresDeviceIdle(true)
                }
            }.build()

        val workManager = WorkManager.getInstance(this)

        val refreshDashboardShowsRequest = PeriodicWorkRequestBuilder<RefreshDashboardShowsWorker>(
            TableUpdateInterval.DASHBOARD_ITEMS.intervalHours,
            TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            RefreshDashboardShowsWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            refreshDashboardShowsRequest
        )

        val refreshExploreShowsRequest = PeriodicWorkRequestBuilder<RefreshTraktExploreWorker>(
            TableUpdateInterval.TRAKT_TRENDING_ITEMS.intervalHours,
            TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            RefreshTraktExploreWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            refreshExploreShowsRequest
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