/*
 * MIT License
 *
 * Copyright (c) 2026 Ahmed Tikiwa
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
class SyncWatchHistoryWorker
@AssistedInject
constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val traktRepository: TraktRepository,
) : BaseWorker(appContext, workerParameters) {
    override val notificationId: Int = NOTIFICATION_ID
    override val contentTitleText: String = "Syncing watch history"

    private val firebaseAnalytics: FirebaseAnalytics by lazy { Firebase.analytics }

    override suspend fun doWork(): Result =
        coroutineScope {
            Timber.tag(TAG).d("Starting $WORK_NAME.")
            val forceFull = inputData.getBoolean(ARG_FORCE_FULL, false)

            return@coroutineScope try {
                val syncResult = traktRepository.syncWatchHistory(forceFull = forceFull)
                if (syncResult.isSuccess) {
                    logSuccessToFirebase()
                    Timber.tag(TAG).i("$WORK_NAME completed successfully.")
                    Result.success()
                } else {
                    val error = syncResult.exceptionOrNull()
                    Timber.tag(TAG).e(error, "$WORK_NAME failed.")
                    logFailureToFirebase(error?.message ?: "Unknown error", error?.javaClass?.simpleName)
                    Result.retry()
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "$WORK_NAME failed with exception.")
                logFailureToFirebase(e.message ?: "Unknown error", e.javaClass.simpleName)
                Result.failure()
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
        errorMessage: String,
        errorType: String? = null,
    ) {
        val bundle =
            Bundle().apply {
                putBoolean("job_successful", false)
                putString("job_name", WORK_NAME)
                putString("error_message", errorMessage)
                errorType?.let { putString("error_type", it) }
            }
        firebaseAnalytics.logEvent("background_job_failed", bundle)
    }

    companion object {
        private const val TAG = "SyncWatchHistoryWrkr"
        const val WORK_NAME = "SyncWatchHistoryWorker"
        const val ARG_FORCE_FULL = "arg_force_full"

        private const val NOTIFICATION_ID = 1004
    }
}
