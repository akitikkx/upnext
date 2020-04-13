package com.theupnextapp.ui.showDetail

import android.app.Application
import androidx.lifecycle.*
import com.theupnextapp.database.getDatabase
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.ShowInfo
import com.theupnextapp.domain.TraktHistory
import com.theupnextapp.repository.UpnextRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ShowDetailViewModel(
    application: Application,
    show: ShowDetailArg
) : AndroidViewModel(application) {

    private val viewModelJob = SupervisorJob()

    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val database = getDatabase(application)

    private val upnextRepository = UpnextRepository(database)

    private val _watchlistRecord = MutableLiveData<TraktHistory>()

    private val _onWatchlist = MutableLiveData<Boolean>()

    private val _notOnWatchlist = MutableLiveData<Boolean>()

    init {
        viewModelScope.launch {
            show.showId?.let { upnextRepository.getShowData(it) }
        }
    }

    val isLoading = upnextRepository.isLoading

    val showInfo = upnextRepository.showInfo

    val onWatchlist: LiveData<Boolean>
        get() = _onWatchlist

    val notOnWatchlist: LiveData<Boolean>
        get() = _notOnWatchlist

    val watchlistRecord: LiveData<TraktHistory>
        get() = _watchlistRecord

    fun onShowInfoReceived(showInfo: ShowInfo) {
        viewModelScope.launch {
            _watchlistRecord.value = upnextRepository.traktWatchlistItem(showInfo.imdbID).value
        }
    }

    fun onWatchlistRecordReceived(traktHistory: TraktHistory?) {
        if (traktHistory == null) {
            _notOnWatchlist.value = true
            _onWatchlist.value = false
        } else {
            _onWatchlist.value = true
            _notOnWatchlist.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    class Factory(
        val app: Application,
        private val show: ShowDetailArg
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ShowDetailViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ShowDetailViewModel(
                    app,
                    show
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}