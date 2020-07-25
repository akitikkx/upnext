package com.theupnextapp.ui.explore

import android.app.Application
import androidx.lifecycle.*
import com.theupnextapp.common.utils.DateUtils
import com.theupnextapp.common.utils.models.DatabaseTables
import com.theupnextapp.common.utils.models.TableUpdateInterval
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.TableUpdate
import com.theupnextapp.ui.common.TraktViewModel
import kotlinx.coroutines.launch

class ExploreViewModel(application: Application) : TraktViewModel(application) {

    private val _navigateToSelectedShow = MutableLiveData<ShowDetailArg>()

    val navigateToSelectedShow: LiveData<ShowDetailArg> = _navigateToSelectedShow

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
        } else if ((popularShowsEmpty.value == null || popularShowsEmpty.value == true) && tableUpdate == null && diffInMinutes == null) {
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
        } else if ((trendingShowsEmpty.value == null || trendingShowsEmpty.value == true) && tableUpdate == null && diffInMinutes == null) {
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
        } else if ((mostAnticipatedShowsEmpty.value == null || mostAnticipatedShowsEmpty.value == true) && tableUpdate == null && diffInMinutes == null) {
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

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ExploreViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ExploreViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}