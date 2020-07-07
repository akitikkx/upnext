package com.theupnextapp.ui.watchlist

import android.app.Application
import androidx.lifecycle.*
import com.theupnextapp.common.utils.DateUtils
import com.theupnextapp.common.utils.UpnextPreferenceManager
import com.theupnextapp.common.utils.models.DatabaseTables
import com.theupnextapp.common.utils.models.TableUpdateInterval
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.TableUpdate
import com.theupnextapp.domain.TraktWatchlist
import com.theupnextapp.ui.common.TraktViewModel
import kotlinx.coroutines.launch

class WatchlistViewModel(application: Application) : TraktViewModel(application) {

    private val _navigateToSelectedShow = MutableLiveData<ShowDetailArg>()

    val navigateToSelectedShow: LiveData<ShowDetailArg> = _navigateToSelectedShow

    val watchlistEmpty = MediatorLiveData<Boolean>()

    val isLoadingWatchlist = traktRepository.isLoadingTraktWatchlist

    val traktWatchlist = traktRepository.traktWatchlist

    val watchlistTableUpdate =
        traktRepository.tableUpdate(DatabaseTables.TABLE_WATCHLIST.tableName)

    init {
        watchlistEmpty.addSource(traktWatchlist) {
            watchlistEmpty.value = it.isNullOrEmpty() == true
        }
    }

    private fun loadTraktWatchlist() {
        val preferences = UpnextPreferenceManager(getApplication())

        viewModelScope?.launch {
            traktRepository.refreshTraktWatchlist(preferences.getTraktAccessToken())
        }
    }

    fun onWatchlistTableUpdateReceived(tableUpdate: TableUpdate?) {
        if (isAuthorizedOnTrakt.value == false) {
            return
        }

        val diffInMinutes =
            tableUpdate?.lastUpdated?.let { it -> DateUtils.dateDifference(it, "minutes") }

        // Only perform an update if there has been enough time before the previous update
        if (diffInMinutes != null) {
            if (diffInMinutes >= TableUpdateInterval.WATCHLIST_ITEMS.intervalMins) {
                loadTraktWatchlist()
            }
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