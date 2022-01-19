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
import java.util.*

@HiltWorker
class RefreshDashboardShowsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val repository: DashboardRepository
) : BaseWorker(appContext, workerParameters) {

    override val contentTitle: String = "Refreshing dashboard shows"

    override suspend fun doWork(): Result = coroutineScope {
        try {
            setForeground(createForegroundInfo())
            refreshShows()
            val bundle = Bundle()
            bundle.putBoolean("Refresh shows job run", true)
            FirebaseAnalytics.getInstance(this@RefreshDashboardShowsWorker.applicationContext)
                .logEvent("RefreshShowsWorker", bundle)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun refreshShows() {
        repository.refreshYesterdayShows(
            DEFAULT_COUNTRY_CODE,
            yesterdayDate()
        )
        repository.refreshTodayShows(
            DEFAULT_COUNTRY_CODE,
            currentDate()
        )
        repository.refreshTomorrowShows(
            DEFAULT_COUNTRY_CODE,
            tomorrowDate()
        )
    }

    private fun currentDate(): String? {
        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return simpleDateFormat.format(calendar.time)
    }

    private fun tomorrowDate(): String? {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrow = calendar.time
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return simpleDateFormat.format(tomorrow)
    }

    private fun yesterdayDate(): String? {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = calendar.time
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return simpleDateFormat.format(yesterday)
    }

    companion object {
        const val WORK_NAME = "RefreshUpnextShowsWorker"
        const val DEFAULT_COUNTRY_CODE = "US"
    }
}