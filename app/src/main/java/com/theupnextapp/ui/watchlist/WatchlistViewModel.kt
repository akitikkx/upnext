package com.theupnextapp.ui.watchlist

import android.app.Application
import androidx.lifecycle.*
import com.theupnextapp.domain.TraktWatchlist
import com.theupnextapp.ui.common.TraktViewModel
import kotlinx.coroutines.launch

class WatchlistViewModel(application: Application) : TraktViewModel(application) {

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
                    accessToken.value,
                    imdbID
                )
            }
        }
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