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

package com.theupnextapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.theupnextapp.common.utils.UpnextPreferenceManager
import com.theupnextapp.common.utils.models.TableUpdateInterval
import com.theupnextapp.database.asDomainModel
import com.theupnextapp.domain.isTraktAccessTokenValid
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.work.BaseWorker
import com.theupnextapp.work.RefreshDashboardShowsWorker
import com.theupnextapp.work.RefreshFavoriteShowsWorker
import com.theupnextapp.work.RefreshTraktExploreWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class UpnextApplication : Application(), Configuration.Provider {

    private val applicationScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var traktRepository: TraktRepository

    @Inject
    lateinit var preferenceManager: UpnextPreferenceManager

    override fun onCreate() {
        super.onCreate()
        setupTheme()
        createNotificationChannels()
        initializeBackgroundTasks()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) Log.DEBUG else Log.INFO)
            .build()

    private fun setupTheme() {
        val selectedTheme = preferenceManager.getSelectedTheme()
        val nightMode = when (selectedTheme) {
            getString(R.string.dark_mode_follow_system) ->
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM

            getString(R.string.dark_mode_no) -> AppCompatDelegate.MODE_NIGHT_NO
            getString(R.string.dark_mode_yes) -> AppCompatDelegate.MODE_NIGHT_YES
            getString(R.string.dark_mode_auto_battery) ->
                AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY

            else -> {
                Timber
                    .tag("UpnextApplication")
                    .w(
                        message = "Unknown theme preference: $selectedTheme. " +
                                "Defaulting to MODE_NIGHT_YES."
                    )
                AppCompatDelegate.MODE_NIGHT_YES // Default theme
            }
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channelId = BaseWorker.NOTIFICATION_CHANNEL_ID
            if (notificationManager.getNotificationChannel(channelId) == null) {
                val channelName = getString(R.string.notification_channel_name)
                val channelDescription = getString(R.string.notification_channel_description)
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(channelId, channelName, importance).apply {
                    description = channelDescription
                }
                notificationManager.createNotificationChannel(channel)
                Timber
                    .tag("UpnextApplication")
                    .d("Notification channel '$channelId' created.")
            }
        }
    }

    private fun initializeBackgroundTasks() {
        applicationScope.launch {
            val workManager = WorkManager.getInstance(applicationContext)
            setupFavoriteShowsWorker(workManager)
            setupDashboardWorker(workManager)
            setupTraktExploreWorker(workManager)
        }
    }

    private fun setupFavoriteShowsWorker(workManager: WorkManager) {
        val traktAccessToken = try {
            traktRepository.getTraktAccessTokenRaw()?.asDomainModel()
        } catch (e: Exception) {
            Timber
                .tag("UpnextApplication")
                .e(
                    t = e,
                    message = "Error fetching Trakt token for worker setup"
                )
            null
        }

        if (traktAccessToken?.access_token?.isNotEmpty() == true && traktAccessToken.isTraktAccessTokenValid()) {
            val workerData = Data.Builder()
                .putString(RefreshFavoriteShowsWorker.ARG_TOKEN, traktAccessToken.access_token)
                .build()

            val refreshFavoriteShowsRequest =
                PeriodicWorkRequestBuilder<RefreshFavoriteShowsWorker>(
                    TableUpdateInterval.TRAKT_FAVORITE_SHOWS.intervalMins,
                    TimeUnit.MINUTES
                )
                    .setInputData(workerData)
                    // Optionally add constraints
                    .setConstraints(
                        Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                    )
                    .build()

            workManager.enqueueUniquePeriodicWork(
                RefreshFavoriteShowsWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // Or CANCEL_AND_REENQUEUE if new params are critical
                refreshFavoriteShowsRequest
            )
            Timber.tag("UpnextApplication").d("Enqueued ${RefreshFavoriteShowsWorker.WORK_NAME}")
        } else {
            Timber
                .tag("UpnextApplication")
                .i("Skipping favorite shows worker setup: Invalid or missing Trakt token.")
            workManager.cancelUniqueWork(RefreshFavoriteShowsWorker.WORK_NAME)
        }
    }

    private fun setupTraktExploreWorker(workManager: WorkManager) {
        val refreshExploreShowsRequest =
            PeriodicWorkRequestBuilder<RefreshTraktExploreWorker>(
                TableUpdateInterval.TRAKT_TRENDING_ITEMS.intervalMins,
                TimeUnit.MINUTES
            ).build()

        workManager.enqueueUniquePeriodicWork(
            RefreshTraktExploreWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Consider policy: KEEP or REPLACE/CANCEL_AND_REENQUEUE
            refreshExploreShowsRequest
        )
        Timber
            .tag(tag = "UpnextApplication")
            .d("Enqueued ${RefreshTraktExploreWorker.WORK_NAME}")
    }

    private fun setupDashboardWorker(workManager: WorkManager) {
        val refreshDashboardShowsRequest =
            PeriodicWorkRequestBuilder<RefreshDashboardShowsWorker>(
                TableUpdateInterval.DASHBOARD_ITEMS.intervalMins,
                TimeUnit.MINUTES
            ).build()

        workManager.enqueueUniquePeriodicWork(
            RefreshDashboardShowsWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Consider policy
            refreshDashboardShowsRequest
        )
        Timber
            .tag(tag = "UpnextApplication")
            .d(message = "Enqueued ${RefreshDashboardShowsWorker.WORK_NAME}")
    }
}
