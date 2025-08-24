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
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.theupnextapp.repository.DashboardRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@HiltWorker
class RefreshDashboardShowsWorker
    @AssistedInject
    constructor(
        @Assisted appContext: Context,
        @Assisted workerParameters: WorkerParameters,
        private val dashboardRepository: DashboardRepository,
    ) : BaseWorker(appContext, workerParameters) {
        // Unique ID for this worker's notifications
        override val notificationId: Int = NOTIFICATION_ID

        // Title for the notification
        override val contentTitleText: String = "Refreshing dashboard shows"

        private val firebaseAnalytics by lazy { Firebase.analytics } // Get instance lazily

        @RequiresApi(Build.VERSION_CODES.O)
        override suspend fun doWork(): Result =
            coroutineScope {
                Timber.tag(TAG).d("Starting ${WORK_NAME}.")
                return@coroutineScope try {
                    refreshShows()

                    val bundle =
                        Bundle().apply {
                            putBoolean("refresh_shows_job_run_successful", true)
                            putString("job_name", WORK_NAME)
                        }
                    firebaseAnalytics.logEvent("background_job_completed", bundle)
                    Timber.tag(TAG).i("$WORK_NAME completed successfully.")
                    Result.success()
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "${WORK_NAME} failed.")
                    val bundle =
                        Bundle().apply {
                            putBoolean("refresh_shows_job_run_successful", false)
                            putString("job_name", WORK_NAME)
                            putString("error_message", e.message ?: "Unknown error")
                        }
                    firebaseAnalytics.logEvent("background_job_failed", bundle)
                    Result.failure()
                }
            }

        @RequiresApi(Build.VERSION_CODES.O)
        private suspend fun refreshShows() {
            Timber.tag(TAG).d("Refreshing shows for yesterday, today, and tomorrow.")
            val today = LocalDate.now()
            val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE // yyyy-MM-dd

            dashboardRepository.refreshYesterdayShows(
                DEFAULT_COUNTRY_CODE,
                today.minusDays(1).format(dateFormatter),
            )
            dashboardRepository.refreshTodayShows(
                DEFAULT_COUNTRY_CODE,
                today.format(dateFormatter),
            )
            dashboardRepository.refreshTomorrowShows(
                DEFAULT_COUNTRY_CODE,
                today.plusDays(1).format(dateFormatter),
            )
            Timber.tag(TAG).d("Finished refreshing shows from repository.")
        }

        companion object {
            private const val TAG = "RefreshDashboardShows"
            const val WORK_NAME = "RefreshDashboardShowsWorker"
            const val DEFAULT_COUNTRY_CODE = "US"
            private const val NOTIFICATION_ID = 1001 // Unique notification ID for this worker
        }
    }
