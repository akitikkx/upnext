package com.theupnextapp

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.theupnextapp.common.utils.UpnextPreferenceManager
import com.theupnextapp.common.utils.models.TableUpdateInterval
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.repository.datastore.UpnextDataStoreManager
import com.theupnextapp.work.RefreshDashboardShowsWorker
import com.theupnextapp.work.RefreshFavoriteShowsWorker
import com.theupnextapp.work.RefreshTraktExploreWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class UpnextApplication : Application(), Configuration.Provider {

    init {
        application = this
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var upnextDataStoreManager: UpnextDataStoreManager

    override fun onCreate() {
        super.onCreate()
        delayedInit()
        setupTheme()
    }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

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
                AppCompatDelegate.MODE_NIGHT_YES
            )
        }
    }

    private fun launchBackgroundTasks() {
        val workManager = WorkManager.getInstance(this)

        setupTraktFavoritesWorker(workManager)

        setupDashboardWorker(workManager)

        setupTraktExploreWorker(workManager)
    }

    /**
     * Setup the worker for updating the favorites stored by Trakt
     * only if the access token is still valid
     */
    private fun setupTraktFavoritesWorker(workManager: WorkManager) {
        val upnextDataStoreManager = UpnextDataStoreManager(this)

        val traktAccessToken: TraktAccessToken?
        runBlocking(Dispatchers.IO) {
            traktAccessToken = upnextDataStoreManager.traktAccessTokenFlow.first()
        }
        if (traktAccessToken != null) {
            if (!traktAccessToken.access_token.isNullOrEmpty() && isTraktAccessTokenValid(
                    traktAccessToken
                )
            ) {
                val workerData = Data.Builder().putString(
                    RefreshFavoriteShowsWorker.ARG_TOKEN,
                    traktAccessToken.access_token
                )
                val refreshFavoriteShowsRequest =
                    PeriodicWorkRequestBuilder<RefreshFavoriteShowsWorker>(
                        TableUpdateInterval.TRAKT_FAVORITE_SHOWS.intervalMins,
                        TimeUnit.MINUTES
                    ).setInputData(workerData.build()).build()

                workManager.enqueueUniquePeriodicWork(
                    RefreshFavoriteShowsWorker.WORK_NAME,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    refreshFavoriteShowsRequest
                )
            }
        }
    }

    /**
     * Setup the worker for updating the explore shows
     */
    private fun setupTraktExploreWorker(workManager: WorkManager) {
        val refreshExploreShowsRequest = PeriodicWorkRequestBuilder<RefreshTraktExploreWorker>(
            TableUpdateInterval.TRAKT_TRENDING_ITEMS.intervalMins,
            TimeUnit.MINUTES
        ).build()

        workManager.enqueueUniquePeriodicWork(
            RefreshTraktExploreWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            refreshExploreShowsRequest
        )
    }

    /**
     * Setup the worker for updating the dashboard shows
     */
    private fun setupDashboardWorker(workManager: WorkManager) {
        val refreshDashboardShowsRequest = PeriodicWorkRequestBuilder<RefreshDashboardShowsWorker>(
            TableUpdateInterval.DASHBOARD_ITEMS.intervalMins,
            TimeUnit.MINUTES
        ).build()

        workManager.enqueueUniquePeriodicWork(
            RefreshDashboardShowsWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            refreshDashboardShowsRequest
        )
    }

    /**
     * Check if the Trakt access token is still valid
     */
    private fun isTraktAccessTokenValid(traktAccessToken: TraktAccessToken): Boolean {
        var isValid = false
        val currentDateEpoch = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
        val expiryDateEpoch =
            traktAccessToken.expires_in?.let { traktAccessToken.created_at?.plus(it) }

        if (expiryDateEpoch != null) {
            if (expiryDateEpoch > currentDateEpoch) {
                isValid = true
            }
        }
        return isValid
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