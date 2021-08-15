package com.theupnextapp.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.theupnextapp.repository.TraktRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope

@HiltWorker
class AddFavoriteShowWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val traktRepository: TraktRepository
) : BaseWorker(appContext, workerParameters) {

    override val contentTitle = "Adding show to your favorites"

    override suspend fun doWork(): Result = coroutineScope {
        try {
            setForeground(createForegroundInfo())
            val token = inputData.getString(ARG_TOKEN)
            val imdbID = inputData.getString(ARG_IMDB_ID)
            token?.let { addShowToFavorites(imdbID = imdbID, token = token) }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun addShowToFavorites(imdbID: String?, token: String) {
        traktRepository.addShowToList(
            imdbID,
            token
        )
    }

    companion object {
        const val WORK_NAME = "AddFavoriteShowWorker"
        const val ARG_TOKEN = "arg_token"
        const val ARG_IMDB_ID = "arg_imdb_id"
    }
}