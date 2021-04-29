package com.theupnextapp.ui.history

import android.app.Application
import androidx.lifecycle.*
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.TraktHistory
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.ui.common.TraktViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    application: Application,
    traktRepository: TraktRepository
) : TraktViewModel(application, traktRepository) {

    private val _navigateToSelectedShow = MutableLiveData<ShowDetailArg?>()
    val navigateToSelectedShow: LiveData<ShowDetailArg?> = _navigateToSelectedShow

    val isLoadingHistory = traktRepository.isLoadingTraktHistory

    val traktHistory = traktRepository.traktHistory

    val historyEmpty = MediatorLiveData<Boolean>().apply {
        addSource(traktHistory) {
            value = it.isNullOrEmpty() == true
        }
    }

    fun displayShowDetails(showDetailArg: ShowDetailArg) {
        _navigateToSelectedShow.value = showDetailArg
    }

    fun displayShowDetailsComplete() {
        _navigateToSelectedShow.value = null
    }

    fun onRemoveClick(historyItem: TraktHistory) {

    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}