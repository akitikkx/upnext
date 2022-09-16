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
import com.theupnextapp.repository.DashboardRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@HiltWorker
class RefreshTodayShowsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val repository: DashboardRepository
) : BaseWorker(appContext, workerParameters) {

    override val contentTitle: String = "Refreshing Today's schedule"

    override suspend fun doWork(): Result = coroutineScope {
        try {
            setForeground(createForegroundInfo())
            refreshTodayShows()
            val bundle = Bundle()
            bundle.putBoolean("Refresh shows job run", true)
            FirebaseAnalytics.getInstance(this@RefreshTodayShowsWorker.applicationContext)
                .logEvent("RefreshTodayShowsWorker", bundle)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun refreshTodayShows() {
        repository.refreshTodayShows(
            DEFAULT_COUNTRY_CODE,
            currentDate()
        )
    }

    private fun currentDate(): String? {
        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return simpleDateFormat.format(calendar.time)
    }

    companion object {
        const val WORK_NAME = "RefreshTodayShowsWorker"
        const val DEFAULT_COUNTRY_CODE = "US"
    }
}
