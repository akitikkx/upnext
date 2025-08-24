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
import com.theupnextapp.repository.DashboardRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@HiltWorker
class RefreshTodayShowsWorker
    @AssistedInject
    constructor(
        @Assisted appContext: Context,
        @Assisted workerParameters: WorkerParameters,
        private val dashboardRepository: DashboardRepository,
    ) : BaseWorker(appContext, workerParameters) {
        override val notificationId: Int = NOTIFICATION_ID
        override val contentTitleText: String = "Refreshing Today's schedule"

        private val firebaseAnalytics: FirebaseAnalytics by lazy { Firebase.analytics }

        override suspend fun doWork(): Result =
            coroutineScope {
                Timber.d("${WORK_NAME}: Starting worker.")
                return@coroutineScope try {
                    Timber.d("${WORK_NAME}: Refreshing today's shows for country code $DEFAULT_COUNTRY_CODE.")
                    refreshTodayShows()

                    logSuccessToFirebase()
                    Timber.i("${WORK_NAME}: Worker completed successfully.")
                    Result.success()
                } catch (e: Exception) {
                    Timber.e(e, "${WORK_NAME}: Worker failed.")
                    logFailureToFirebase(
                        errorType = e.javaClass.simpleName,
                        errorMessage = e.message ?: "Unknown error while refreshing today's shows.",
                    )
                    Result.failure()
                }
            }

        private suspend fun refreshTodayShows() {
            val date = currentDate()
            if (date == null) {
                Timber.w("${WORK_NAME}: Could not determine current date. Skipping refresh.")
                return
            }
            Timber.d("${WORK_NAME}: Current date for refresh: $date")
            dashboardRepository.refreshTodayShows(
                DEFAULT_COUNTRY_CODE,
                date,
            )
            Timber.d("${WORK_NAME}: Finished refreshing today's shows from repository.")
        }

        /**
         * Gets the current date formatted as "yyyy-MM-dd".
         * @return The formatted date string, or null if formatting fails (should be rare).
         */
        private fun currentDate(): String? {
            return try {
                val calendar = Calendar.getInstance()
                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                simpleDateFormat.format(calendar.time)
            } catch (e: Exception) {
                Timber.e(e, "${WORK_NAME}: Error formatting current date.")
                null
            }
        }

        private fun logSuccessToFirebase() {
            val bundle =
                Bundle().apply {
                    putBoolean("job_successful", true)
                    putString("job_name", WORK_NAME)
                    putString("country_code", DEFAULT_COUNTRY_CODE)
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
                    putString("country_code", DEFAULT_COUNTRY_CODE)
                    putString("error_type", errorType)
                    putString("error_message", errorMessage)
                }
            firebaseAnalytics.logEvent("background_job_failed", bundle)
        }

        companion object {
            const val WORK_NAME = "RefreshTodayShowsWorker"
            const val DEFAULT_COUNTRY_CODE = "US"
            private const val NOTIFICATION_ID = 1001
        }
    }
