package com.theupnextapp.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.theupnextapp.repository.TraktRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope

@HiltWorker
class RefreshFavoriteShowsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val traktRepository: TraktRepository
) : BaseWorker(appContext, workerParameters) {

    override val contentTitle = "Refreshing your favorite shows"

    override suspend fun doWork(): Result = coroutineScope {
        try {
            setForeground(createForegroundInfo())
            val token = inputData.getString(ARG_TOKEN)
            token?.let { refreshFavoriteShows(token = it, repository = traktRepository) }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun refreshFavoriteShows(token: String, repository: TraktRepository) {
        repository.refreshFavoriteShows(token = token)
    }

    companion object {
        const val WORK_NAME = "RefreshFavoriteShowsWorker"
        const val ARG_TOKEN = "arg_token"
    }
}