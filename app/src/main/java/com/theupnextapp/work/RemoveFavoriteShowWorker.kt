package com.theupnextapp.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.theupnextapp.repository.TraktRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope

@HiltWorker
class RemoveFavoriteShowWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val traktRepository: TraktRepository
) : BaseWorker(appContext, workerParameters) {

    override val contentTitle = "Removing the show from your favorites"

    override suspend fun doWork(): Result = coroutineScope {
        try {
            setForeground(createForegroundInfo())
            val token = inputData.getString(ARG_TOKEN)
            val traktId = inputData.getInt(ARG_TRAKT_ID, NOT_FOUND)
            val imdbID = inputData.getString(ARG_IMDB_ID)
            token?.let {
                removeShowFromFavorites(
                    traktId = traktId,
                    token = token,
                    imdbID = imdbID
                )
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun removeShowFromFavorites(traktId: Int, imdbID: String?, token: String?) {
        traktRepository.removeShowFromList(
            traktId = traktId,
            imdbID = imdbID,
            token
        )
    }

    companion object {
        const val WORK_NAME = "RemoveFavoriteShowWorker"
        const val ARG_TOKEN = "arg_token"
        const val ARG_IMDB_ID = "arg_imdb_id"
        const val ARG_TRAKT_ID = "arg_trakt_id"
        const val NOT_FOUND = -1
    }
}