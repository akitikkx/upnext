package com.theupnextapp

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.theupnextapp.common.utils.UpnextPreferenceManager
import com.theupnextapp.common.utils.models.TableUpdateInterval
import com.theupnextapp.database.asDomainModel
import com.theupnextapp.domain.isTraktAccessTokenValid
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.work.RefreshDashboardShowsWorker
import com.theupnextapp.work.RefreshFavoriteEpisodesWorker
import com.theupnextapp.work.RefreshFavoriteShowsWorker
import com.theupnextapp.work.RefreshTraktExploreWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    lateinit var traktRepository: TraktRepository

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

        setupFavoritesWorker(workManager)

        setupDashboardWorker(workManager)

        setupTraktExploreWorker(workManager)
    }

    private fun setupFavoritesWorker(workManager: WorkManager) {
        runBlocking(Dispatchers.IO) {
            val traktAccessToken = traktRepository.getTraktAccessTokenRaw()?.asDomainModel()
            if (traktAccessToken != null) {
                if (!traktAccessToken.access_token.isNullOrEmpty() && traktAccessToken.isTraktAccessTokenValid()) {
                    val workerData = Data.Builder().putString(
                        RefreshFavoriteShowsWorker.ARG_TOKEN,
                        traktAccessToken.access_token
                    )
                    val refreshFavoriteShowsRequest =
                        PeriodicWorkRequestBuilder<RefreshFavoriteShowsWorker>(
                            TableUpdateInterval.TRAKT_FAVORITE_SHOWS.intervalMins,
                            TimeUnit.MINUTES
                        ).setInputData(workerData.build()).build()

                    val refreshFavoriteEpisodesRequest =
                        PeriodicWorkRequestBuilder<RefreshFavoriteEpisodesWorker>(
                            TableUpdateInterval.TRAKT_FAVORITE_EPISODES.intervalMins,
                            TimeUnit.MINUTES
                        ).build()

                    workManager.enqueueUniquePeriodicWork(
                        RefreshFavoriteShowsWorker.WORK_NAME,
                        ExistingPeriodicWorkPolicy.REPLACE,
                        refreshFavoriteShowsRequest
                    )

                    workManager.enqueueUniquePeriodicWork(
                        RefreshFavoriteEpisodesWorker.WORK_NAME,
                        ExistingPeriodicWorkPolicy.REPLACE,
                        refreshFavoriteEpisodesRequest
                    )
                }
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