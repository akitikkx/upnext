package com.theupnextapp.ui.watchlist

import android.app.Application
import androidx.lifecycle.*
import com.theupnextapp.domain.TraktWatchlist
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.ui.common.TraktViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    application: Application,
    private val traktRepository: TraktRepository
) : TraktViewModel(application, traktRepository) {

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
}