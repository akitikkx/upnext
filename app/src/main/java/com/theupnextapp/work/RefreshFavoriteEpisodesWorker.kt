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
class RefreshFavoriteEpisodesWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val traktRepository: TraktRepository
) : BaseWorker(appContext, workerParameters) {

    override val notificationId: Int = NOTIFICATION_ID
    override val contentTitleText: String = "Refreshing your favorite's next episodes"

    private val firebaseAnalytics: FirebaseAnalytics by lazy { Firebase.analytics }

    override suspend fun doWork(): Result = coroutineScope {
        Timber.d("${WORK_NAME}: Starting worker.")
        return@coroutineScope try {
            Timber.d("${WORK_NAME}: Refreshing favorite show's next episodes.")
            refreshFavoriteNextEpisodes()

            logSuccessToFirebase()
            Timber.i("${WORK_NAME}: Worker completed successfully.")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "${WORK_NAME}: Worker failed.")
            logFailureToFirebase(
                errorType = e.javaClass.simpleName,
                errorMessage = e.message ?: "Unknown error while refreshing favorite episodes."
            )
            Result.failure()
        }
    }

    private suspend fun refreshFavoriteNextEpisodes() {
        traktRepository.refreshFavoriteNextEpisodes()
        Timber.d(
            "${WORK_NAME}: Finished refreshing favorite show's " +
                    "next episodes from repository."
        )
    }

    private fun logSuccessToFirebase() {
        val bundle = Bundle().apply {
            putBoolean("job_successful", true)
            putString("job_name", WORK_NAME)
        }
        firebaseAnalytics.logEvent("background_job_completed", bundle)
    }

    private fun logFailureToFirebase(errorType: String, errorMessage: String) {
        val bundle = Bundle().apply {
            putBoolean("job_successful", false)
            putString("job_name", WORK_NAME)
            putString("error_type", errorType)
            putString("error_message", errorMessage)
        }
        firebaseAnalytics.logEvent("background_job_failed", bundle)
    }

    companion object {
        const val WORK_NAME = "RefreshFavoriteEpisodesWorker"
        private const val NOTIFICATION_ID = 1005
    }
}
