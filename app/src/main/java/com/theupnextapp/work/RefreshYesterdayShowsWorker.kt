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

/**
 * A [BaseWorker] class that fetches yesterday's TV show schedule from the network and
 * stores it in the local database. This worker is intended to run periodically in the background.
 *
 * This worker extends [BaseWorker] to leverage common functionalities like notification display
 * during its execution.
 *
 * It uses [DashboardRepository] to interact with the data layer for fetching and storing show data.
 * The worker also logs its success or failure to Firebase Analytics for monitoring purposes.
 *
 * The [doWork] method is the main entry point where the work is performed. It calculates
 * yesterday's date, then calls the repository to refresh the shows for that date.
 *
 * @param appContext The application [Context].
 * @param workerParameters Parameters to configure the worker.
 * @param dashboardRepository The repository responsible for fetching and saving TV show data.
 */
@HiltWorker
class RefreshYesterdayShowsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val dashboardRepository: DashboardRepository
) : BaseWorker(appContext, workerParameters) {

    override val notificationId: Int = NOTIFICATION_ID
    override val contentTitleText: String = "Refreshing yesterday's schedule"

    private val firebaseAnalytics: FirebaseAnalytics by lazy { Firebase.analytics }

    override suspend fun doWork(): Result = coroutineScope {
        Timber.d("${WORK_NAME}: Starting worker.")
        return@coroutineScope try {
            Timber.d("${WORK_NAME}: Refreshing yesterday's shows for country code $DEFAULT_COUNTRY_CODE.")
            refreshYesterdayShows()

            logSuccessToFirebase()
            Timber.i("${WORK_NAME}: Worker completed successfully.")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "${WORK_NAME}: Worker failed.")
            logFailureToFirebase(
                errorType = e.javaClass.simpleName,
                errorMessage = e.message ?: "Unknown error while refreshing yesterday's shows."
            )
            Result.failure() // Or Result.retry() if appropriate
        }
    }

    private suspend fun refreshYesterdayShows() {
        val date = yesterdayDate()
        if (date == null) {
            Timber.w("${WORK_NAME}: Could not determine yesterday's date. Skipping refresh.")
            return
        }
        Timber.d("${WORK_NAME}: Yesterday's date for refresh: $date")
        dashboardRepository.refreshYesterdayShows(
            DEFAULT_COUNTRY_CODE,
            date
        )
        Timber.d("${WORK_NAME}: Finished refreshing yesterday's shows from repository.")
    }

    /**
     * Gets yesterday's date formatted as "yyyy-MM-dd".
     * @return The formatted date string, or null if formatting fails (should be rare).
     */
    private fun yesterdayDate(): String? {
        return try {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1) // Subtract one day to get yesterday
            val yesterday = calendar.time
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            simpleDateFormat.format(yesterday)
        } catch (e: Exception) {
            Timber.e(e, "${WORK_NAME}: Error formatting yesterday's date.")
            null // Return null if date formatting fails
        }
    }

    private fun logSuccessToFirebase() {
        val bundle = Bundle().apply {
            putBoolean("job_successful", true)
            putString("job_name", WORK_NAME)
            putString("country_code", DEFAULT_COUNTRY_CODE)
        }
        firebaseAnalytics.logEvent("background_job_completed", bundle)
    }

    private fun logFailureToFirebase(errorType: String, errorMessage: String) {
        val bundle = Bundle().apply {
            putBoolean("job_successful", false)
            putString("job_name", WORK_NAME)
            putString("country_code", DEFAULT_COUNTRY_CODE)
            putString("error_type", errorType)
            putString("error_message", errorMessage)
        }
        firebaseAnalytics.logEvent("background_job_failed", bundle)
    }

    companion object {
        const val WORK_NAME = "RefreshYesterdayShowsWorker"
        const val DEFAULT_COUNTRY_CODE = "US"
        private const val NOTIFICATION_ID = 1003
    }
}
