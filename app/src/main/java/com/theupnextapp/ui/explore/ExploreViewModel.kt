package com.theupnextapp.ui.explore

import android.app.Application
import androidx.lifecycle.*
import com.theupnextapp.common.utils.DateUtils
import com.theupnextapp.common.utils.models.DatabaseTables
import com.theupnextapp.common.utils.models.TableUpdateInterval
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.TableUpdate
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.ui.common.TraktViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    application: Application,
    private val traktRepository: TraktRepository
) : TraktViewModel(application, traktRepository) {

    private val _navigateToSelectedShow = MutableLiveData<ShowDetailArg?>()
    val navigateToSelectedShow: LiveData<ShowDetailArg?> = _navigateToSelectedShow

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

        if (diffInMinutes != null && diffInMinutes != 0L) {
            if (diffInMinutes > TableUpdateInterval.TRAKT_POPULAR_ITEMS.intervalMins && (isLoadingTraktPopular.value == false || isLoadingTraktPopular.value == null)) {
                viewModelScope?.launch {
                    traktRepository.refreshTraktPopularShows()
                }
            }
            // no updates have been done yet for this table
        } else if ((popularShowsEmpty.value == true && (isLoadingTraktPopular.value == null || isLoadingTraktPopular.value == false)) && tableUpdate == null && diffInMinutes == null) {
            viewModelScope?.launch {
                traktRepository.refreshTraktPopularShows()
            }
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

        if (diffInMinutes != null && diffInMinutes != 0L) {
            if (diffInMinutes > TableUpdateInterval.TRAKT_TRENDING_ITEMS.intervalMins && (isLoadingTraktTrending.value == false || isLoadingTraktTrending.value == null)) {
                viewModelScope?.launch {
                    traktRepository.refreshTraktTrendingShows()
                }
            }
            // no updates have been done yet for this table
        } else if ((trendingShowsEmpty.value == true && (isLoadingTraktTrending.value == null || isLoadingTraktTrending.value == false)) && tableUpdate == null && diffInMinutes == null) {
            viewModelScope?.launch {
                traktRepository.refreshTraktTrendingShows()
            }
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

        if (diffInMinutes != null && diffInMinutes != 0L) {
            if (diffInMinutes > TableUpdateInterval.TRAKT_MOST_ANTICIPATED_ITEMS.intervalMins && (isLoadingTraktMostAnticipated.value == false || isLoadingTraktMostAnticipated.value == null)) {
                viewModelScope?.launch {
                    traktRepository.refreshTraktMostAnticipatedShows()
                }
            }
            // no updates have been done yet for this table
        } else if ((mostAnticipatedShowsEmpty.value == true && (isLoadingTraktMostAnticipated.value == false || isLoadingTraktMostAnticipated.value == null)) && tableUpdate == null && diffInMinutes == null) {
            viewModelScope?.launch {
                traktRepository.refreshTraktMostAnticipatedShows()
            }
        }
    }

    fun onExploreItemClick(showDetailArg: ShowDetailArg) {
        _navigateToSelectedShow.value = showDetailArg
    }

    fun displayShowDetailsComplete() {
        _navigateToSelectedShow.value = null
    }
}