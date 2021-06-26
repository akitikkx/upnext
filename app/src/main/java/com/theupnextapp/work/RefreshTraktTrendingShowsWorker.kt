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
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

@HiltWorker
class RefreshTraktTrendingShowsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val traktRepository: TraktRepository
) :
    CoroutineWorker(appContext, workerParameters) {

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    override suspend fun doWork(): Result = coroutineScope {
        try {
            refreshTrendingShows(traktRepository)
            val bundle = Bundle()
            bundle.putBoolean("Refresh shows job run", true)
            FirebaseAnalytics.getInstance(this@RefreshTraktTrendingShowsWorker.applicationContext)
                .logEvent("RefreshTraktTrendingShowsWorker", bundle)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun refreshTrendingShows(repository: TraktRepository) {
        repository.refreshTraktTrendingShows()
    }

    companion object {
        const val WORK_NAME = "RefreshTraktTrendingShowsWorker"
    }
}