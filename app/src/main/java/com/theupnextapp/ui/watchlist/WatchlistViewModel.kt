package com.theupnextapp.ui.watchlist

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.theupnextapp.common.utils.UpnextPreferenceManager
import com.theupnextapp.domain.*
import com.theupnextapp.ui.common.TraktViewModel
import kotlinx.coroutines.launch

class WatchlistViewModel(application: Application) : TraktViewModel(application) {

    private val _fetchingAccessTokenInProgress = MutableLiveData<Boolean>()

    private val _storingTraktAccessTokenInProgress = MutableLiveData<Boolean>()

    private val _transactionInProgress = MutableLiveData<Boolean>()

    private val _navigateToSelectedShow = MutableLiveData<ShowDetailArg>()

    private val _watchlistEmpty = MutableLiveData<Boolean>()

    val transactionInProgress: LiveData<Boolean> = _transactionInProgress

    val navigateToSelectedShow: LiveData<ShowDetailArg> = _navigateToSelectedShow

    val watchlistEmpty: LiveData<Boolean> = _watchlistEmpty

    val isLoadingWatchlist = traktRepository.isLoadingTraktWatchlist

    fun onWatchlistEmpty(empty: Boolean) {
        _watchlistEmpty.value = empty
    }

    init {
        _watchlistEmpty.value = false
        if (ifValidAccessTokenExists()) {
            loadTraktWatchlist()
            _isAuthorizedOnTrakt.value = true
        }
    }

    private fun loadTraktWatchlist() {
        val preferences = UpnextPreferenceManager(getApplication())

        viewModelScope?.launch {
            traktRepository.refreshTraktWatchlist(preferences.getTraktAccessToken())
        }
    }

    val traktWatchlist = traktRepository.traktWatchlist

    private fun extractCode(bundle: Bundle?) {
        val traktConnectionArg = bundle?.getParcelable<TraktConnectionArg>(EXTRA_TRAKT_URI)

        _fetchingAccessTokenInProgress.value = true

        viewModelScope?.launch {
            traktRepository.getTraktAccessToken(traktConnectionArg?.code)
        }
    }

    fun onWatchlistItemDeleteClick(watchlistItem: TraktWatchlist) {
        viewModelScope?.launch {
            watchlistItem.imdbID?.let { imdbID ->
                traktRepository.removeFromWatchlist(imdbID)
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