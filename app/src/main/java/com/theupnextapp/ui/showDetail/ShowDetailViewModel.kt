package com.theupnextapp.ui.showDetail

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.theupnextapp.database.getDatabase
import com.theupnextapp.domain.ShowCast
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.TraktAddToWatchlist
import com.theupnextapp.domain.TraktRemoveFromWatchlist
import com.theupnextapp.domain.TraktHistory
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.repository.UpnextRepository
import com.theupnextapp.ui.common.TraktViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ShowDetailViewModel(
    application: Application,
    show: ShowDetailArg
) : TraktViewModel(application) {

    private val viewModelJob = SupervisorJob()

    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val database = getDatabase(application)

    private val upnextRepository = UpnextRepository(database)

    private val traktRepository = TraktRepository(database)

    private val _onWatchList = MutableLiveData<Boolean>()

    private val _notOnWatchlist = MutableLiveData<Boolean>()

    private val _show = MutableLiveData(show)

    private val _showCastEmpty = MutableLiveData<Boolean>()

    private val _showCastBottomSheet = MutableLiveData<ShowCast>()

    private val _launchTraktConnectWindow = MutableLiveData<Boolean>()

    private val isUpnextRepositoryLoading = upnextRepository.isLoading

    private val isTraktRepositoryLoading = traktRepository.isLoading

    val isLoading = MediatorLiveData<Boolean>()

    val showInfo = upnextRepository.showInfo

    val showCast = upnextRepository.showCast

    val addToWatchlistResponse = traktRepository.addToWatchlistResponse

    val removeFromWatchlistResponse = traktRepository.removeFromWatchlistResponse

    val onWatchlist: LiveData<Boolean> = _onWatchList

    val notOnWatchlist: LiveData<Boolean> = _notOnWatchlist

    fun displayCastBottomSheetComplete() {
        _showCastBottomSheet.value = null
    }

    fun onConnectClick() {
        _launchTraktConnectWindow.value = true
    }

    init {
        viewModelScope.launch {
            show.showId?.let {
                upnextRepository.getShowData(it)
                upnextRepository.getShowCast(it)
            }
        }

        isLoading.addSource(isUpnextRepositoryLoading) { result ->
            isLoading.value = result == true
        }
        isLoading.addSource(isTraktRepositoryLoading) { result ->
            isLoading.value = result == true
        }
    }

    val showCastEmpty: LiveData<Boolean> = _showCastEmpty

    val watchlistRecord = Transformations.switchMap(showInfo) { showInfo ->
        showInfo.imdbID?.let { it -> traktRepository.traktWatchlistItem(it) }
    }

    val showDetailArg: LiveData<ShowDetailArg> = _show

    val showCastBottomSheet: LiveData<ShowCast> = _showCastBottomSheet

    val launchTraktConnectWindow: LiveData<Boolean> = _launchTraktConnectWindow

    fun launchConnectWindowComplete() {
        _launchTraktConnectWindow.value = false
    }

    fun onShowCastInfoReceived(showCast: List<ShowCast>) {
        _showCastEmpty.value = showCast.isNullOrEmpty()
    }

    fun onShowCastItemClicked(showCast: ShowCast) {
        _showCastBottomSheet.value = showCast
    }

    fun onWatchlistRecordReceived(traktHistory: TraktHistory?) {
        if (_isAuthorizedOnTrakt.value == false) {
            _notOnWatchlist.value = false
            _onWatchList.value = false
        } else {
            if (traktHistory == null) {
                _notOnWatchlist.value = true
                _onWatchList.value = false
            } else {
                _onWatchList.value = true
                _notOnWatchlist.value = false
            }
        }
    }

    fun onAddToWatchlistClick() {
        onWatchlistAction(WATCHLIST_ACTION_ADD)
    }

    fun onRemoveFromWatchlistClick() {
        onWatchlistAction(WATCHLIST_ACTION_REMOVE)
    }

    private fun onWatchlistAction(action: String) {
        if (ifValidAccessTokenExists()) {
            _isAuthorizedOnTrakt.value = true
            val accessToken = getAccessToken()

            when (action) {
                WATCHLIST_ACTION_ADD -> {
                    viewModelScope.launch {
                        showInfo.value?.imdbID?.let { imdbID ->
                            traktRepository.traktAddToWatchlist(
                                accessToken,
                                imdbID
                            )
                        }
                    }
                }
                WATCHLIST_ACTION_REMOVE -> {
                    viewModelScope.launch {
                        showInfo.value?.imdbID?.let { imdbID ->
                            traktRepository.traktRemoveFromWatchlist(
                                accessToken,
                                imdbID
                            )
                        }
                    }
                }
            }
        } else {
            _isAuthorizedOnTrakt.value = false
        }
    }

    fun onAddToWatchlistResponseReceived(addToWatchlist: TraktAddToWatchlist) {
        requestWatchlistRefresh()
    }

    fun onRemoveFromWatchlistResponseReceived(removeFromWatchlist: TraktRemoveFromWatchlist) {
        requestWatchlistRefresh()
    }

    private fun requestWatchlistRefresh() {
        if (ifValidAccessTokenExists()) {
            loadTraktWatchilist()
        }
    }

    private fun loadTraktWatchilist() {
        val sharedPreferences = getApplication<Application>().getSharedPreferences(
            SHARED_PREF_NAME,
            Context.MODE_PRIVATE
        )

        val accessToken = sharedPreferences.getString(SHARED_PREF_TRAKT_ACCESS_TOKEN, null)

        viewModelScope.launch {
            traktRepository.refreshTraktWatchlist(accessToken)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    companion object {
        const val WATCHLIST_ACTION_ADD = "add_to_watchlist"
        const val WATCHLIST_ACTION_REMOVE = "remove_from_watchlist"
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