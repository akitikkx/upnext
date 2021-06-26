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
class RefreshTraktPopularShowsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val traktRepository: TraktRepository
) :
    CoroutineWorker(appContext, workerParameters) {

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    override suspend fun doWork(): Result = coroutineScope {
        try {
            refreshPopularShows(traktRepository)
            val bundle = Bundle()
            bundle.putBoolean("Refresh shows job run", true)
            FirebaseAnalytics.getInstance(this@RefreshTraktPopularShowsWorker.applicationContext)
                .logEvent("RefreshTraktPopularShowsWorker", bundle)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun refreshPopularShows(repository: TraktRepository) {
        repository.refreshTraktPopularShows()
    }

    companion object {
        const val WORK_NAME = "RefreshTraktPopularShowsWorker"
    }
}