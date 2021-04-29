package com.theupnextapp.work

import android.content.Context
import android.os.Bundle
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.analytics.FirebaseAnalytics
import com.theupnextapp.repository.TraktRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

@HiltWorker
class RefreshTraktExploreWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val traktRepository: TraktRepository
) :
    CoroutineWorker(appContext, workerParameters) {

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    override suspend fun doWork(): Result = coroutineScope {
        val jobs = (0 until 100).map {
            async {
                refreshShows(traktRepository)
                val bundle = Bundle()
                bundle.putBoolean("Refresh shows job run", true)
                firebaseAnalytics.logEvent("RefreshShowsWorker", bundle)
            }
        }

        jobs.awaitAll()
        Result.success()
    }

    private suspend fun refreshShows(repository: TraktRepository) {
        repository.refreshTraktPopularShows()
        repository.refreshTraktTrendingShows()
        repository.refreshTraktMostAnticipatedShows()
    }

    companion object {
        const val WORK_NAME = "RefreshTraktExploreWorker"
    }
}