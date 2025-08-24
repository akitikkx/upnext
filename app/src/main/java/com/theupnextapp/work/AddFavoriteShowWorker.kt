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
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
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

@HiltWorker
class AddFavoriteShowWorker
    @AssistedInject
    constructor(
        @Assisted appContext: Context,
        @Assisted workerParameters: WorkerParameters,
        private val traktRepository: TraktRepository,
    ) : BaseWorker(appContext, workerParameters) {
        override val notificationId: Int = NOTIFICATION_ID
        override val contentTitleText: String = "Adding show to your favorites"

        private val firebaseAnalytics: FirebaseAnalytics by lazy { Firebase.analytics }

        override suspend fun doWork(): Result =
            coroutineScope {
                Timber
                    .tag(TAG)
                    .d("Starting ${WORK_NAME}.")
                val token = inputData.getString(ARG_TOKEN)
                val imdbID = inputData.getString(ARG_IMDB_ID)

                if (token.isNullOrBlank()) {
                    Timber
                        .tag(TAG)
                        .e("$WORK_NAME failed: Missing or empty token in input data.")
                    logFailureToFirebase(
                        errorType = "MissingInputData",
                        errorMessage = "Auth token is missing.",
                    )
                    return@coroutineScope Result.failure()
                }

                if (imdbID.isNullOrBlank()) {
                    Timber
                        .tag(TAG)
                        .e("$WORK_NAME failed: Missing or empty IMDb ID in input data.")
                    logFailureToFirebase(
                        errorType = "MissingInputData",
                        errorMessage = "IMDb ID is missing.",
                    )
                    return@coroutineScope Result.failure()
                }

                return@coroutineScope try {
                    Timber
                        .tag(TAG)
                        .d("Adding show (IMDb ID: $imdbID) to favorites.")
                    addShowToFavorites(imdbID = imdbID, token = token)

                    logSuccessToFirebase(imdbID)
                    Timber
                        .tag(TAG)
                        .i("${WORK_NAME} completed successfully for IMDb ID: $imdbID.")
                    Result.success()
                } catch (e: Exception) {
                    Timber
                        .tag(TAG)
                        .e(e, "$WORK_NAME failed for IMDb ID: $imdbID.")
                    logFailureToFirebase(
                        imdbID = imdbID,
                        errorType = e.javaClass.simpleName,
                        errorMessage = e.message ?: "Unknown error while adding show to favorites.",
                    )
                    Result.failure() // Or Result.retry() if appropriate
                }
            }

        private suspend fun addShowToFavorites(
            imdbID: String,
            token: String,
        ) {
            traktRepository.addShowToList(
                imdbID = imdbID,
                token = token,
            )
            Timber
                .tag(TAG)
                .d("Successfully called repository to add show (IMDb ID: $imdbID) to list.")
        }

        private fun logSuccessToFirebase(imdbID: String?) {
            val bundle =
                Bundle().apply {
                    putBoolean("job_successful", true)
                    putString("job_name", WORK_NAME)
                    imdbID?.let { putString("imdb_id", it) }
                }
            firebaseAnalytics.logEvent("background_job_completed", bundle)
        }

        private fun logFailureToFirebase(
            imdbID: String? = null,
            errorType: String,
            errorMessage: String,
        ) {
            val bundle =
                Bundle().apply {
                    putBoolean("job_successful", false)
                    putString("job_name", WORK_NAME)
                    imdbID?.let { putString("imdb_id", it) }
                    putString("error_type", errorType)
                    putString("error_message", errorMessage)
                }
            firebaseAnalytics.logEvent("background_job_failed", bundle)
        }

        companion object {
            private const val TAG = "AddFavShowWrkr"
            const val WORK_NAME = "AddFavoriteShowWorker"

            const val ARG_TOKEN = "arg_token"
            const val ARG_IMDB_ID = "arg_imdb_id"

            // Define a unique notification ID for this specific worker.
            private const val NOTIFICATION_ID = 1004
        }
    }
