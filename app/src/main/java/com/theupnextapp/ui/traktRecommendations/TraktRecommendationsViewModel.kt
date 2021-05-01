package com.theupnextapp.ui.traktRecommendations

import android.app.Application
import androidx.lifecycle.MediatorLiveData
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.ui.common.TraktViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TraktRecommendationsViewModel @Inject constructor(
    application: Application,
    traktRepository: TraktRepository
) : TraktViewModel(application, traktRepository) {

    val traktRecommendationsList = traktRepository.traktRecommendations

    val isLoadingTraktRecommendations = traktRepository.isLoadingTraktRecommendations

    val traktRecommendationsShowsEmpty = MediatorLiveData<Boolean>().apply {
        addSource(traktRecommendationsList) {
            value = it.isNullOrEmpty() == true
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

}