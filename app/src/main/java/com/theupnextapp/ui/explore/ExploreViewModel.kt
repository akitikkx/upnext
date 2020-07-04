package com.theupnextapp.ui.explore

import android.app.Application
import androidx.lifecycle.*
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.ui.common.TraktViewModel
import kotlinx.coroutines.launch

class ExploreViewModel(application: Application) : TraktViewModel(application) {

    private val _navigateToSelectedShow = MutableLiveData<ShowDetailArg>()

    val trendingShowsEmpty = MediatorLiveData<Boolean>()

    val popularShowsEmpty = MediatorLiveData<Boolean>()

    val navigateToSelectedShow: LiveData<ShowDetailArg> = _navigateToSelectedShow

    val trendingShows = traktRepository.trendingShows

    val popularShows = traktRepository.popularShows

    val isLoadingTraktTrending = traktRepository.isLoadingTraktTrending

    val isLoadingTraktPopular = traktRepository.isLoadingTraktPopular

    init {
        viewModelScope?.launch {
            traktRepository.getTrendingShows()
            traktRepository.getPopularShows()
        }

        trendingShowsEmpty.addSource(trendingShows) {
            trendingShowsEmpty.value = it.isNullOrEmpty() == true
        }

        popularShowsEmpty.addSource(popularShows) {
            popularShowsEmpty.value = it.isNullOrEmpty() == true
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