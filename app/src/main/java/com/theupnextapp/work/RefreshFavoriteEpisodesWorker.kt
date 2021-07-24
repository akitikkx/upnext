package com.theupnextapp.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.theupnextapp.repository.TraktRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope

@HiltWorker
class RefreshFavoriteEpisodesWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val traktRepository: TraktRepository
) : BaseWorker(appContext, workerParameters) {

    override val contentTitle = "Refreshing your favorite's next episodes"

    override suspend fun doWork(): Result = coroutineScope {
        try {
            setForeground(createForegroundInfo())
            refreshFavoriteShows(repository = traktRepository)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun refreshFavoriteShows(repository: TraktRepository) {
        repository.refreshFavoriteNextEpisodes()
    }

    companion object {
        const val WORK_NAME = "RefreshFavoriteEpisodesWorker"
    }
}