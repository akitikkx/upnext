package com.theupnextapp.work

import android.content.Context
import android.os.Bundle
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.analytics.FirebaseAnalytics
import com.theupnextapp.database.getDatabase
import com.theupnextapp.repository.UpnextRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.text.SimpleDateFormat
import java.util.*

class RefreshShowsWorker(appContext: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result = coroutineScope {
        val database = getDatabase(applicationContext)
        val repository = UpnextRepository(database)

        val jobs = (0 until 100).map {
            async {
                refreshShows(repository)
                val bundle = Bundle()
                bundle.putBoolean("Refresh shows job run", true)
                FirebaseAnalytics.getInstance(this@RefreshShowsWorker.applicationContext).logEvent("RefreshShowsWorker", bundle)
            }
        }

        jobs.awaitAll()
        Result.success()
    }

    private suspend fun refreshShows(repository: UpnextRepository) {
        repository.refreshNewShows()
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
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        return simpleDateFormat.format(calendar.time)
    }

    private fun tomorrowDate(): String? {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrow = calendar.time
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        return simpleDateFormat.format(tomorrow)
    }

    private fun yesterdayDate(): String? {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = calendar.time
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        return simpleDateFormat.format(yesterday)
    }

    companion object {
        const val UPNEXT_WORK_NAME = "RefreshUpnextShowsWorker"
        const val TRAKT_WORK_NAME = "RefreshTraktShowsWorker"
        const val DEFAULT_COUNTRY_CODE = "US"
    }
}