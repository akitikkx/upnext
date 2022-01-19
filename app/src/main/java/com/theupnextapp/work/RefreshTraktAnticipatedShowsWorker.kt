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
class RefreshTraktAnticipatedShowsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val repository: TraktRepository
) : BaseWorker(appContext, workerParameters) {

    override val contentTitle: String = "Refreshing Trakt Most Anticipated"

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    override suspend fun doWork(): Result = coroutineScope {
        try {
            setForeground(createForegroundInfo())
            refreshAnticipatedShows()
            val bundle = Bundle()
            bundle.putBoolean("Refresh shows job run", true)
            FirebaseAnalytics.getInstance(this@RefreshTraktAnticipatedShowsWorker.applicationContext)
                .logEvent("RefreshTraktAnticipatedShowsWorker", bundle)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun refreshAnticipatedShows() {
        repository.refreshTraktMostAnticipatedShows()
    }

    companion object {
        const val WORK_NAME = "RefreshTraktAnticipatedShowsWorker"
    }
}