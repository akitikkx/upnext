package com.theupnextapp.ui.history

import android.app.Application
import androidx.lifecycle.*
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.TraktHistory
import com.theupnextapp.ui.common.TraktViewModel

class HistoryViewModel(application: Application) : TraktViewModel(application) {

    private val _navigateToSelectedShow = MutableLiveData<ShowDetailArg>()

    val isLoadingHistory = traktRepository.isLoadingTraktHistory

    val navigateToSelectedShow: LiveData<ShowDetailArg> = _navigateToSelectedShow

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

    class Factory(val app: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HistoryViewModel(
                    app
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}