package com.theupnextapp.ui.traktRecommendations

import android.app.Application
import androidx.lifecycle.*
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.ui.common.TraktViewModel

class TraktRecommendationsViewModel(application: Application) : TraktViewModel(application) {

    private val _navigateToSelectedShow = MutableLiveData<ShowDetailArg>()

    val navigateToSelectedShow: LiveData<ShowDetailArg> = _navigateToSelectedShow

    val traktRecommendationsList = traktRepository.traktRecommendations

    val isLoadingTraktRecommendations = traktRepository.isLoadingTraktRecommendations

    val traktRecommendationsShowsEmpty = MediatorLiveData<Boolean>().apply {
        addSource(traktRecommendationsList) {
            value = it.isNullOrEmpty() == true
        }
    }

    fun onRecommendationsItemClick(showDetailArg: ShowDetailArg) {
        _navigateToSelectedShow.value = showDetailArg
    }

    fun displayShowDetailsComplete() {
        _navigateToSelectedShow.value = null
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TraktRecommendationsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TraktRecommendationsViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }


}