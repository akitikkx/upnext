package com.theupnextapp.ui.explore

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.theupnextapp.common.utils.DateUtils
import com.theupnextapp.common.utils.models.DatabaseTables
import com.theupnextapp.common.utils.models.TableUpdateInterval
import com.theupnextapp.domain.TableUpdate
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.work.RefreshTraktAnticipatedShowsWorker
import com.theupnextapp.work.RefreshTraktPopularShowsWorker
import com.theupnextapp.work.RefreshTraktTrendingShowsWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    traktRepository: TraktRepository,
    private val workManager: WorkManager
) : ViewModel() {

    val trendingShows = traktRepository.traktTrendingShows

    val popularShows = traktRepository.traktPopularShows

    val mostAnticipatedShows = traktRepository.traktMostAnticipatedShows

    val isLoadingTraktTrending = traktRepository.isLoadingTraktTrending

    val isLoadingTraktPopular = traktRepository.isLoadingTraktPopular

    val isLoadingTraktMostAnticipated = traktRepository.isLoadingTraktMostAnticipated

    val popularShowsTableUpdate =
        traktRepository.tableUpdate(DatabaseTables.TABLE_TRAKT_POPULAR.tableName)

    val trendingShowsTableUpdate =
        traktRepository.tableUpdate(DatabaseTables.TABLE_TRAKT_TRENDING.tableName)

    val mostAnticipatedShowsTableUpdate =
        traktRepository.tableUpdate(DatabaseTables.TABLE_TRAKT_MOST_ANTICIPATED.tableName)

    val trendingShowsEmpty = MediatorLiveData<Boolean>().apply {
        addSource(trendingShows) {
            value = it.isNullOrEmpty() == true
        }
    }

    val popularShowsEmpty = MediatorLiveData<Boolean>().apply {
        addSource(popularShows) {
            value = it.isNullOrEmpty() == true
        }
    }

    val mostAnticipatedShowsEmpty = MediatorLiveData<Boolean>().apply {
        addSource(popularShows) {
            value = it.isNullOrEmpty() == true
        }
    }

    val isLoading = MediatorLiveData<Boolean>().apply {
        addSource(isLoadingTraktPopular) {
            value = it
        }
        addSource(isLoadingTraktTrending) {
            value = it
        }
        addSource(isLoadingTraktMostAnticipated) {
            value = it
        }
    }

    fun onPopularShowsTableUpdateReceived(tableUpdate: TableUpdate?) {
        val diffInMinutes =
            tableUpdate?.lastUpdated?.let { it ->
                DateUtils.dateDifference(
                    endTime = it,
                    type = "minutes"
                )
            }

        val isPopularShowsEmpty = popularShowsEmpty.value == null || popularShowsEmpty.value == true

        if (diffInMinutes != null && diffInMinutes != 0L) {
            if (diffInMinutes > TableUpdateInterval.TRAKT_POPULAR_ITEMS.intervalMins && (isLoadingTraktPopular.value == false || isLoadingTraktPopular.value == null)) {
                workManager.enqueue(OneTimeWorkRequest.from(RefreshTraktPopularShowsWorker::class.java))
            }
            // no updates have been done yet for this table
        } else if (isPopularShowsEmpty && (isLoadingTraktPopular.value == null || isLoadingTraktPopular.value == false) && tableUpdate == null && diffInMinutes == null) {
            workManager.enqueue(OneTimeWorkRequest.from(RefreshTraktPopularShowsWorker::class.java))
        }
    }

    fun onTrendingShowsTableUpdateReceived(tableUpdate: TableUpdate?) {
        val diffInMinutes =
            tableUpdate?.lastUpdated?.let { it ->
                DateUtils.dateDifference(
                    endTime = it,
                    type = "minutes"
                )
            }

        val isTrendingShowsEmpty = trendingShowsEmpty.value == null || trendingShowsEmpty.value == true

        if (diffInMinutes != null && diffInMinutes != 0L) {
            if (diffInMinutes > TableUpdateInterval.TRAKT_TRENDING_ITEMS.intervalMins && (isLoadingTraktTrending.value == false || isLoadingTraktTrending.value == null)) {
                workManager.enqueue(OneTimeWorkRequest.from(RefreshTraktTrendingShowsWorker::class.java))
            }
            // no updates have been done yet for this table
        } else if (isTrendingShowsEmpty && (isLoadingTraktTrending.value == null || isLoadingTraktTrending.value == false) && tableUpdate == null && diffInMinutes == null) {
            workManager.enqueue(OneTimeWorkRequest.from(RefreshTraktTrendingShowsWorker::class.java))
        }
    }

    fun onMostAnticipatedShowsTableUpdateReceived(tableUpdate: TableUpdate?) {
        val diffInMinutes =
            tableUpdate?.lastUpdated?.let { it ->
                DateUtils.dateDifference(
                    endTime = it,
                    type = "minutes"
                )
            }

        val isMostAnticipatedShowsEmpty = mostAnticipatedShowsEmpty.value == null || mostAnticipatedShowsEmpty.value == true

        if (diffInMinutes != null && diffInMinutes != 0L) {
            if (diffInMinutes > TableUpdateInterval.TRAKT_MOST_ANTICIPATED_ITEMS.intervalMins && (isLoadingTraktMostAnticipated.value == false || isLoadingTraktMostAnticipated.value == null)) {
                workManager.enqueue(OneTimeWorkRequest.from(RefreshTraktAnticipatedShowsWorker::class.java))
            }
            // no updates have been done yet for this table
        } else if (isMostAnticipatedShowsEmpty && (isLoadingTraktMostAnticipated.value == null || isLoadingTraktMostAnticipated.value == false) && tableUpdate == null && diffInMinutes == null) {
            workManager.enqueue(OneTimeWorkRequest.from(RefreshTraktAnticipatedShowsWorker::class.java))
        }
    }
}