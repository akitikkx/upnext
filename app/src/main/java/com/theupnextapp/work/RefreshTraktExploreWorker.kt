package com.theupnextapp.work

import android.content.Context
import android.os.Bundle
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.analytics.FirebaseAnalytics
import com.theupnextapp.database.getDatabase
import com.theupnextapp.repository.TraktRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class RefreshTraktExploreWorker(appContext: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result = coroutineScope {
        val database = getDatabase(applicationContext)
        val repository = TraktRepository(database)

        val jobs = (0 until 100).map {
            async {
                refreshShows(repository)
                val bundle = Bundle()
                bundle.putBoolean("Refresh shows job run", true)
                FirebaseAnalytics.getInstance(this@RefreshTraktExploreWorker.applicationContext).logEvent("RefreshShowsWorker", bundle)
            }
        }

        jobs.awaitAll()
        Result.success()
    }

    private suspend fun refreshShows(repository: TraktRepository) {
        repository.refreshTraktPopularShows()
        repository.refreshTraktTrendingShows()
    }

    companion object {
        const val WORK_NAME = "RefreshTraktExploreWorker"
    }
}