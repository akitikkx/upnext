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
import androidx.work.WorkerParameters
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.theupnextapp.repository.DashboardRepository
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

abstract class BaseScheduleWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
    protected val dashboardRepository: DashboardRepository,
) : BaseWorker(appContext, workerParameters) {
    abstract val workerName: String

    protected abstract val dayOffset: Int

    private val firebaseAnalytics: FirebaseAnalytics by lazy { Firebase.analytics }

    protected abstract suspend fun refresh(date: String)

    override suspend fun doWork(): Result =
        coroutineScope {
            Timber.d("$workerName: Starting worker.")
            return@coroutineScope try {
                val date = getFormattedDate()
                if (date == null) {
                    Timber.w("$workerName: Could not determine date. Skipping refresh.")
                    Result.success()
                } else {
                    Timber.d("$workerName: Refreshing shows for country code $DEFAULT_COUNTRY_CODE for date $date.")
                    refresh(date)
                    logSuccessToFirebase()
                    Timber.i("$workerName: Worker completed successfully.")
                    Result.success()
                }
            } catch (e: Exception) {
                Timber.e(e, "$workerName: Worker failed.")
                logFailureToFirebase(
                    errorType = e.javaClass.simpleName,
                    errorMessage = e.message ?: "Unknown error while refreshing shows.",
                )
                Result.failure()
            }
        }

    private fun getFormattedDate(): String? {
        return try {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, dayOffset)
            val date = calendar.time
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            simpleDateFormat.format(date)
        } catch (e: Exception) {
            Timber.e(e, "$workerName: Error formatting date.")
            null
        }
    }

    private fun logSuccessToFirebase() {
        val bundle =
            Bundle().apply {
                putBoolean("job_successful", true)
                putString("job_name", workerName)
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
                putString("job_name", workerName)
                putString("country_code", DEFAULT_COUNTRY_CODE)
                putString("error_type", errorType)
                putString("error_message", errorMessage)
            }
        firebaseAnalytics.logEvent("background_job_failed", bundle)
    }

    companion object {
        const val DEFAULT_COUNTRY_CODE = "US"
    }
}
