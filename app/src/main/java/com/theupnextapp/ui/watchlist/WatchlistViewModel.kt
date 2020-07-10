package com.theupnextapp.ui.watchlist

import android.app.Application
import androidx.lifecycle.*
import com.theupnextapp.common.utils.UpnextPreferenceManager
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.TraktWatchlist
import com.theupnextapp.ui.common.TraktViewModel
import kotlinx.coroutines.launch

class WatchlistViewModel(application: Application) : TraktViewModel(application) {

    private val _navigateToSelectedShow = MutableLiveData<ShowDetailArg>()

    val navigateToSelectedShow: LiveData<ShowDetailArg> = _navigateToSelectedShow

    val isLoadingWatchlist = traktRepository.isLoadingTraktWatchlist

    val traktWatchlist = traktRepository.traktWatchlist

    val watchlistEmpty = MediatorLiveData<Boolean>().apply {
        addSource(traktWatchlist) {
            value = it.isNullOrEmpty() == true
        }
    }

    fun onWatchlistItemDeleteClick(watchlistItem: TraktWatchlist) {
        viewModelScope?.launch {
            watchlistItem.imdbID?.let { imdbID ->
                traktRepository.removeFromCachedWatchlist(imdbID)
                traktRepository.traktRemoveFromWatchlist(
                    UpnextPreferenceManager(getApplication()).getTraktAccessToken(),
                    imdbID
                )
            }
        }
    }

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

    companion object {
        const val EXTRA_TRAKT_URI = "extra_trakt_uri"
    }

    class Factory(val app: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WatchlistViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return WatchlistViewModel(
                    app
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}