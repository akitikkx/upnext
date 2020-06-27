package com.theupnextapp.ui.history

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.ui.common.TraktViewModel
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : TraktViewModel(application) {

    private val _navigateToSelectedShow = MutableLiveData<ShowDetailArg>()

    private val _historyEmpty = MutableLiveData<Boolean>()

    val isLoadingHistory = traktRepository.isLoadingTraktHistory

    val navigateToSelectedShow: LiveData<ShowDetailArg> = _navigateToSelectedShow

    val historyEmpty: LiveData<Boolean>
        get() = _historyEmpty

    fun onHistoryEmpty(empty: Boolean) {
        _historyEmpty.value = empty
    }

    init {
        _historyEmpty.value = false
        if (ifValidAccessTokenExists()) {
            loadTraktHistory()
            _isAuthorizedOnTrakt.value = true
        }
    }

    private fun loadTraktHistory() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(getApplication())
        val accessToken = preferences.getString(SHARED_PREF_TRAKT_ACCESS_TOKEN, null)

        viewModelScope?.launch {
            traktRepository.refreshTraktHistory(accessToken)
        }
    }

    val traktHistory = traktRepository.traktHistory

    fun displayShowDetails(showDetailArg: ShowDetailArg) {
        _navigateToSelectedShow.value = showDetailArg
    }

    fun displayShowDetailsComplete() {
        _navigateToSelectedShow.value = null
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