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
import com.theupnextapp.repository.WatchProgressRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope
import timber.log.Timber

@HiltWorker
class SyncWatchProgressWorker
@AssistedInject
constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val watchProgressRepository: WatchProgressRepository,
) : BaseWorker(appContext, workerParameters) {
    override val notificationId: Int = NOTIFICATION_ID
    override val contentTitleText: String = "Syncing watch progress"

    private val firebaseAnalytics: FirebaseAnalytics by lazy { Firebase.analytics }

    override suspend fun doWork(): Result =
        coroutineScope {
            Timber
                .tag(TAG)
                .d("Starting $WORK_NAME.")
            val token = inputData.getString(ARG_TOKEN)

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

            return@coroutineScope try {
                Timber
                    .tag(TAG)
                    .d("Syncing watch progress with Trakt.")

                val result = watchProgressRepository.syncWithTrakt(token)

                if (result.isSuccess) {
                    logSuccessToFirebase()
                    Timber
                        .tag(TAG)
                        .i("$WORK_NAME completed successfully.")
                    Result.success()
                } else {
                    val exception = result.exceptionOrNull()
                    Timber
                        .tag(TAG)
                        .e(exception, "$WORK_NAME failed during sync.")
                    logFailureToFirebase(
                        errorType = exception?.javaClass?.simpleName ?: "Unknown",
                        errorMessage = exception?.message ?: "Unknown error during sync.",
                    )
                    Result.retry()
                }
            } catch (e: Exception) {
                Timber
                    .tag(TAG)
                    .e(e, "$WORK_NAME failed with exception.")
                logFailureToFirebase(
                    errorType = e.javaClass.simpleName,
                    errorMessage = e.message ?: "Unknown error while syncing watch progress.",
                )
                Result.retry()
            }
        }

    private fun logSuccessToFirebase() {
        val bundle =
            Bundle().apply {
                putBoolean("job_successful", true)
                putString("job_name", WORK_NAME)
            }
        firebaseAnalytics.logEvent("background_job_completed", bundle)
    }

    private fun logFailureToFirebase(
        errorType: String,
        errorMessage: String,
    ) {
        val bundle =
            Bundle().apply {
                putBoolean("job_successful", false)
                putString("job_name", WORK_NAME)
                putString("error_type", errorType)
                putString("error_message", errorMessage)
            }
        firebaseAnalytics.logEvent("background_job_failed", bundle)
    }

    companion object {
        private const val TAG = "SyncWatchProgressWrkr"
        const val WORK_NAME = "SyncWatchProgressWorker"

        const val ARG_TOKEN = "arg_token"

        private const val NOTIFICATION_ID = 1007
    }
}
