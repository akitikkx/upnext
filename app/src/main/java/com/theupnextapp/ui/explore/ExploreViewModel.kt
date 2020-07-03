package com.theupnextapp.ui.explore

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.theupnextapp.ui.common.TraktViewModel
import kotlinx.coroutines.launch

class ExploreViewModel(application: Application) : TraktViewModel(application) {

    init {
        viewModelScope?.launch {
            traktRepository.getTrendingShows()
        }
    }

    val trendingShows = traktRepository.trendingShows

    val isLoadingTraktTrending = traktRepository.isLoadingTraktTrending

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