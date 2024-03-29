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

package com.theupnextapp.ui.explore

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
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

    val trendingShows = traktRepository.traktTrendingShows.asLiveData()

    val popularShows = traktRepository.traktPopularShows.asLiveData()

    val mostAnticipatedShows = traktRepository.traktMostAnticipatedShows.asLiveData()

    val isLoadingTraktTrending = traktRepository.isLoadingTraktTrending

    val isLoadingTraktPopular = traktRepository.isLoadingTraktPopular

    val isLoadingTraktMostAnticipated = traktRepository.isLoadingTraktMostAnticipated

    val popularShowsTableUpdate =
        traktRepository.tableUpdate(DatabaseTables.TABLE_TRAKT_POPULAR.tableName).asLiveData()

    val trendingShowsTableUpdate =
        traktRepository.tableUpdate(DatabaseTables.TABLE_TRAKT_TRENDING.tableName).asLiveData()

    val mostAnticipatedShowsTableUpdate =
        traktRepository.tableUpdate(DatabaseTables.TABLE_TRAKT_MOST_ANTICIPATED.tableName).asLiveData()

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
