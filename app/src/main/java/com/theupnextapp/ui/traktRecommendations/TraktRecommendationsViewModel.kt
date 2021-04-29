package com.theupnextapp.ui.traktRecommendations

import android.app.Application
import androidx.lifecycle.*
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.ui.common.TraktViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TraktRecommendationsViewModel @Inject constructor(
    application: Application,
    traktRepository: TraktRepository
) : TraktViewModel(application, traktRepository) {

    private val _navigateToSelectedShow = MutableLiveData<ShowDetailArg?>()
    val navigateToSelectedShow: LiveData<ShowDetailArg?> = _navigateToSelectedShow

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

}