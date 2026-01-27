/*
 * MIT License
 *
 * Copyright (c) 2022 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.work

import android.content.Context
import android.os.Bundle
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.theupnextapp.repository.TraktRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope
import timber.log.Timber

/**
 * This worker removes a show from the user's Trakt favorites list.
 * It requires the Trakt access token, and either a Trakt ID or an IMDb ID for the show.
 * The worker logs its success or failure to Firebase Analytics.
 *
 * @param appContext The application context.
 * @param workerParameters Parameters to setup the worker, like input data.
 * @param traktRepository Repository for interacting with the Trakt API.
 */
@HiltWorker
class RemoveFavoriteShowWorker
@AssistedInject
constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val traktRepository: TraktRepository,
) : BaseWorker(appContext, workerParameters) {
    override val notificationId: Int = NOTIFICATION_ID
    override val contentTitleText: String =
        "Removing show from your favorites"

    private val firebaseAnalytics: FirebaseAnalytics by lazy { Firebase.analytics }

    override suspend fun doWork(): Result =
        coroutineScope {
            Timber.d("${WORK_NAME}: Starting worker.")

            val token = inputData.getString(ARG_TOKEN)
            val traktId = inputData.getInt(ARG_TRAKT_ID, NOT_FOUND)
            val imdbId = inputData.getString(ARG_IMDB_ID)

            if (token == null) {
                Timber.e("${WORK_NAME}: Access token is missing. Cannot remove show from favorites.")
                logFailureToFirebase(
                    errorType = "MissingInputData",
                    errorMessage = "Access token (ARG_TOKEN) was null.",
                    traktId = traktId,
                    imdbId = imdbId,
                )
                return@coroutineScope Result.failure() // Cannot proceed without a token
            }

            if (traktId == NOT_FOUND) {
                Timber.w(
                    "${WORK_NAME}: Trakt ID is missing. This might " +
                        "affect removal if IMDb ID is also missing.",
                )
            }

            Timber.d("${WORK_NAME}: Attempting to remove show (Trakt ID: $traktId, IMDb ID: $imdbId).")

            return@coroutineScope try {
                removeShowFromFavorites(
                    traktId = traktId,
                    imdbID = imdbId,
                    token = token,
                )

                logSuccessToFirebase(traktId = traktId, imdbId = imdbId)
                Timber.i("${WORK_NAME}: Show (Trakt ID: $traktId, IMDb ID: $imdbId) removed successfully.")
                Result.success()
            } catch (e: Exception) {
                Timber.e(
                    e,
                    "${WORK_NAME}: Failed to remove show (Trakt ID: $traktId, IMDb ID: $imdbId).",
                )
                logFailureToFirebase(
                    errorType = e.javaClass.simpleName,
                    errorMessage = e.message ?: "Unknown error while removing show from favorites.",
                    traktId = traktId,
                    imdbId = imdbId,
                )
                Result.failure() // Or Result.retry() if appropriate
            }
        }

    private suspend fun removeShowFromFavorites(
        traktId: Int,
        imdbID: String?,
        token: String,
    ) {
        traktRepository.removeShowFromList(
            traktId = traktId,
            imdbID = imdbID,
            token = token,
        )
        Timber.d(
            "${WORK_NAME}: Call to repository to " +
                "remove show (Trakt ID: $traktId) completed.",
        )
    }

    private fun logSuccessToFirebase(
        traktId: Int,
        imdbId: String?,
    ) {
        val bundle =
            Bundle().apply {
                putBoolean("job_successful", true)
                putString("job_name", WORK_NAME)
                if (traktId != NOT_FOUND) putInt("trakt_id", traktId)
                imdbId?.let { putString("imdb_id", it) }
            }
        firebaseAnalytics.logEvent("background_job_completed", bundle)
    }

    private fun logFailureToFirebase(
        errorType: String,
        errorMessage: String,
        traktId: Int,
        imdbId: String?,
    ) {
        val bundle =
            Bundle().apply {
                putBoolean("job_successful", false)
                putString("job_name", WORK_NAME)
                if (traktId != NOT_FOUND) putInt("trakt_id", traktId)
                imdbId?.let { putString("imdb_id", it) }
                putString("error_type", errorType)
                putString("error_message", errorMessage)
            }
        firebaseAnalytics.logEvent("background_job_failed", bundle)
    }

    companion object {
        const val WORK_NAME = "RemoveFavoriteShowWorker"
        const val ARG_TOKEN = "arg_token"
        const val ARG_IMDB_ID = "arg_imdb_id"
        const val ARG_TRAKT_ID = "arg_trakt_id"
        const val NOT_FOUND = -1 // Standard way to indicate not found for Int

        private const val NOTIFICATION_ID = 1004
    }
}
