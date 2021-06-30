package com.theupnextapp.work

import android.content.Context
import android.os.Bundle
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.analytics.FirebaseAnalytics
import com.theupnextapp.repository.UpnextRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope
import java.text.SimpleDateFormat
import java.util.*

@HiltWorker
class RefreshTomorrowShowsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val upnextRepository: UpnextRepository
) : BaseWorker(appContext, workerParameters) {

    override val contentTitle: String = "Refreshing tomorrow's schedule"

    override suspend fun doWork(): Result = coroutineScope {
        try {
            setForeground(createForegroundInfo())
            refreshTomorrowShows(upnextRepository)
            val bundle = Bundle()
            bundle.putBoolean("Refresh shows job run", true)
            FirebaseAnalytics.getInstance(this@RefreshTomorrowShowsWorker.applicationContext)
                .logEvent("RefreshTomorrowShowsWorker", bundle)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun refreshTomorrowShows(repository: UpnextRepository) {
        repository.refreshTomorrowShows(
            DEFAULT_COUNTRY_CODE,
            tomorrowDate()
        )
    }

    private fun tomorrowDate(): String? {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrow = calendar.time
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return simpleDateFormat.format(tomorrow)
    }

    companion object {
        const val WORK_NAME = "RefreshTomorrowShowsWorker"
        const val DEFAULT_COUNTRY_CODE = "US"
    }
}