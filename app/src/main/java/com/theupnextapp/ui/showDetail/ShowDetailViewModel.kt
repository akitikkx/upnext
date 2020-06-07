package com.theupnextapp.ui.showDetail

import android.app.Application
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.theupnextapp.domain.*
import com.theupnextapp.repository.UpnextRepository
import com.theupnextapp.ui.common.TraktViewModel
import kotlinx.coroutines.launch

class ShowDetailViewModel(
    application: Application,
    show: ShowDetailArg
) : TraktViewModel(application) {

    private val upnextRepository = UpnextRepository(database)

    private val _onWatchList = MutableLiveData<Boolean>()

    private val _notOnWatchlist = MutableLiveData<Boolean>()

    private val _show = MutableLiveData(show)

    private val _showCastEmpty = MutableLiveData<Boolean>()

    private val _showCastBottomSheet = MutableLiveData<ShowCast>()

    private val _showWatchedProgressBottomSheet = MutableLiveData<TraktShowWatchedProgress>()

    private val _showSeasonsBottomSheet = MutableLiveData<List<ShowSeason>>()

    val onWatchlist: LiveData<Boolean> = _onWatchList

    val notOnWatchlist: LiveData<Boolean> = _notOnWatchlist

    val isLoading = MediatorLiveData<Boolean>()

    val showDetailArg: LiveData<ShowDetailArg> = _show

    val showCastBottomSheet: LiveData<ShowCast> = _showCastBottomSheet

    val showSeasonsBottomSheet: LiveData<List<ShowSeason>> = _showSeasonsBottomSheet

    val showWatchedProgressBottomSheet: LiveData<TraktShowWatchedProgress> =
        _showWatchedProgressBottomSheet

    val launchTraktConnectWindow: LiveData<Boolean> = _launchTraktConnectWindow

    val showCastEmpty: LiveData<Boolean> = _showCastEmpty

    private val isUpnextRepositoryLoading = upnextRepository.isLoading

    private val isTraktRepositoryLoading = traktRepository.isLoading

    val showInfo = upnextRepository.showInfo

    val watchlistRecord = Transformations.switchMap(showInfo) { showInfo ->
        showInfo.imdbID?.let { it -> traktRepository.traktWatchlistItem(it) }
    }

    val showCast = upnextRepository.showCast

    val showSeasons = upnextRepository.showSeasons

    val addToWatchlistResponse = traktRepository.addToWatchlistResponse

    val removeFromWatchlistResponse = traktRepository.removeFromWatchlistResponse

    val showRating = traktRepository.traktShowRating

    val showStats = traktRepository.traktShowStats

    val watchedProgress = traktRepository.traktWatchedProgress

    init {
        viewModelScope?.launch {
            show.showId?.let {
                upnextRepository.getShowData(it)
                upnextRepository.getShowCast(it)
                upnextRepository.getShowSeasons(it)
            }
            traktRepository.getTraktShowRating(getAccessToken(), showInfo.value?.imdbID)
            traktRepository.getTraktShowStats(getAccessToken(), showInfo.value?.imdbID)
            if (_isAuthorizedOnTrakt.value == true) {
                traktRepository.getTraktWatchedProgress(
                    getAccessToken(),
                    showInfo.value?.imdbID
                )
            }

        }

        isLoading.addSource(isUpnextRepositoryLoading) { result ->
            isLoading.value = result == true
        }
        isLoading.addSource(isTraktRepositoryLoading) { result ->
            isLoading.value = result == true
        }
    }

    fun displayCastBottomSheetComplete() {
        _showCastBottomSheet.value = null
    }

    fun displayShowSeasonsBottomSheetComplete() {
        _showSeasonsBottomSheet.value = null
    }

    fun onConnectClick() {
        _launchTraktConnectWindow.value = true
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

    fun onWatchedProgressClick() {
        _showWatchedProgressBottomSheet.value = watchedProgress.value
    }

    fun onSeasonsClick() {
        _showSeasonsBottomSheet.value = showSeasons.value
    }

    private fun onWatchlistAction(action: String) {
        if (ifValidAccessTokenExists()) {
            _isAuthorizedOnTrakt.value = true
            val accessToken = getAccessToken()

            when (action) {
                WATCHLIST_ACTION_ADD -> {
                    viewModelScope?.launch {
                        showInfo.value?.imdbID?.let { imdbID ->
                            traktRepository.traktAddToWatchlist(
                                accessToken,
                                imdbID
                            )
                        }
                    }
                }
                WATCHLIST_ACTION_REMOVE -> {
                    viewModelScope?.launch {
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
        viewModelScope?.launch {
            showInfo.value?.imdbID?.let { traktRepository.removeFromWatchlist(it) }
        }
    }

    private fun requestWatchlistRefresh() {
        if (ifValidAccessTokenExists()) {
            loadTraktWatchilist()
        }
    }

    private fun loadTraktWatchilist() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(getApplication())
        val accessToken = preferences.getString(SHARED_PREF_TRAKT_ACCESS_TOKEN, null)

        viewModelScope?.launch {
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