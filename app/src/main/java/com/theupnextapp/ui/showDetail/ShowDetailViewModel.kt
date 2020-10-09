package com.theupnextapp.ui.showDetail

import android.app.Application
import androidx.lifecycle.*
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
    val onWatchlist: LiveData<Boolean> = _onWatchList

    private val _inCollection = MutableLiveData<Boolean>(false)
    val inCollection: LiveData<Boolean> = _inCollection

    private val _notOnWatchlist = MutableLiveData<Boolean>()
    val notOnWatchlist: LiveData<Boolean> = _notOnWatchlist

    private val _notInCollection = MutableLiveData<Boolean>(false)
    val notInCollection: LiveData<Boolean> = _notInCollection

    private val _show = MutableLiveData(show)
    val showDetailArg: LiveData<ShowDetailArg> = _show

    private val _showCastEmpty = MutableLiveData<Boolean>()
    val showCastEmpty: LiveData<Boolean> = _showCastEmpty

    private val _showCastBottomSheet = MutableLiveData<ShowCast>()
    val showCastBottomSheet: LiveData<ShowCast> = _showCastBottomSheet

    private val _showWatchedProgressBottomSheet = MutableLiveData<TraktShowWatchedProgress>()
    val showWatchedProgressBottomSheet: LiveData<TraktShowWatchedProgress> =
        _showWatchedProgressBottomSheet

    private val _showSeasonsBottomSheet = MutableLiveData<List<ShowSeason>>()
    val showSeasonsBottomSheet: LiveData<List<ShowSeason>> = _showSeasonsBottomSheet

    private val _showWatchlistInfoBottomSheet = MutableLiveData<Boolean>()
    val showWatchlistInfoBottomSheet: LiveData<Boolean> = _showWatchlistInfoBottomSheet

    private val _showConnectToTraktInfoBottomSheet = MutableLiveData<Boolean>()
    val showConnectToTraktInfoBottomSheet: LiveData<Boolean> = _showConnectToTraktInfoBottomSheet

    private val _showCollectionInfoBottomSheet = MutableLiveData<Boolean>()
    val showCollectionInfoBottomSheet: LiveData<Boolean> = _showCollectionInfoBottomSheet

    private val _showConnectionToTraktRequiredError = MutableLiveData<Boolean>()
    val showConnectionToTraktRequiredError: LiveData<Boolean> = _showConnectionToTraktRequiredError

    val isLoading = MediatorLiveData<Boolean>()

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
            traktRepository.getTraktShowRating(showInfo.value?.imdbID)
            traktRepository.getTraktShowStats(showInfo.value?.imdbID)

            traktRepository.getTraktWatchedProgress(
                accessToken.value,
                showInfo.value?.imdbID
            )
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

    fun showConnectionToTraktRequiredComplete() {
        _showConnectionToTraktRequiredError.value = false
    }

    fun showWatchlistInfoBottomSheetComplete() {
        _showWatchlistInfoBottomSheet.value = false
    }

    fun showCollectionInfoBottomSheetComplete() {
        _showCollectionInfoBottomSheet.value = false
    }

    fun showConnectToTraktInfoBottomSheetComplete() {
        _showConnectToTraktInfoBottomSheet.value = false
    }

    fun onShowCastInfoReceived(showCast: List<ShowCast>) {
        _showCastEmpty.value = showCast.isNullOrEmpty()
    }

    fun onShowCastItemClicked(showCast: ShowCast) {
        _showCastBottomSheet.value = showCast
    }

    fun onWatchlistRecordReceived(traktHistory: TraktHistory?) {
        if (isAuthorizedOnTrakt.value == false) {
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

    fun onAddRemoveWatchlistClick() {
        if (isAuthorizedOnTrakt.value == true) {
            if (_onWatchList.value == true) {
                onRemoveFromWatchlistClick()
            } else {
                onAddToWatchlistClick()
            }
        } else {
            _showConnectionToTraktRequiredError.value = true
        }
    }

    private fun onAddToWatchlistClick() {
        onWatchlistAction(WATCHLIST_ACTION_ADD)
    }

    private fun onRemoveFromWatchlistClick() {
        onWatchlistAction(WATCHLIST_ACTION_REMOVE)
    }

    fun onAddRemoveCollectionClick() {
        if (_inCollection.value == true) {
            onRemoveFromCollectionClick()
        } else {
            onAddToCollectionClick()
        }
    }

    private fun onAddToCollectionClick() {
        onCollectionAction(COLLECTION_ACTION_ADD)
    }

    private fun onRemoveFromCollectionClick() {
        onCollectionAction(COLLECTION_ACTION_REMOVE)
    }

    fun onWatchedProgressClick() {
        _showWatchedProgressBottomSheet.value = watchedProgress.value
    }

    fun onSeasonsClick() {
        _showSeasonsBottomSheet.value = showSeasons.value
    }

    fun onConnectToTraktInfoClick() {
        _showConnectToTraktInfoBottomSheet.value = true
    }

    fun onAddRemoveWatchlistInfoClick() {
        _showWatchlistInfoBottomSheet.value = true
    }

    fun onAddRemoveCollectionInfoClick() {
        _showCollectionInfoBottomSheet.value = true
    }

    private fun onWatchlistAction(action: String) {
        if (isAuthorizedOnTrakt.value == true) {

            when (action) {
                WATCHLIST_ACTION_ADD -> {
                    viewModelScope?.launch {
                        showInfo.value?.imdbID?.let { imdbID ->
                            traktRepository.traktAddToWatchlist(
                                accessToken.value,
                                imdbID
                            )
                        }
                    }
                }
                WATCHLIST_ACTION_REMOVE -> {
                    viewModelScope?.launch {
                        showInfo.value?.imdbID?.let { imdbID ->
                            traktRepository.traktRemoveFromWatchlist(
                                accessToken.value,
                                imdbID
                            )
                        }
                    }
                }
            }
        }
    }

    fun onAddToWatchlistResponseReceived(addToWatchlist: TraktAddToWatchlist) {
        requestWatchlistRefresh()
    }

    fun onRemoveFromWatchlistResponseReceived(removeFromWatchlist: TraktRemoveFromWatchlist) {
        viewModelScope?.launch {
            showInfo.value?.imdbID?.let { traktRepository.removeFromCachedWatchlist(it) }
        }
    }


    private fun onCollectionAction(action: String) {
        if (isAuthorizedOnTrakt.value == true) {
            when (action) {
                COLLECTION_ACTION_ADD -> {

                }
                COLLECTION_ACTION_REMOVE -> {

                }
            }
        }
    }

    fun onAddToCollectionResponseReceived() {
        requestCollectionRefresh()
    }

    fun onRemoveFromCollectionResponseReceived() {

    }

    private fun requestWatchlistRefresh() {
        if (isAuthorizedOnTrakt.value == true) {
            loadTraktWatchlist()
        }
    }

    private fun loadTraktWatchlist() {
        viewModelScope?.launch {
            traktRepository.refreshTraktWatchlist(accessToken.value)
        }
    }

    private fun requestCollectionRefresh() {
        loadTraktCollection()
    }

    private fun loadTraktCollection() {
        viewModelScope?.launch {
            traktRepository.refreshTraktCollection(accessToken.value)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    companion object {
        const val WATCHLIST_ACTION_ADD = "add_to_watchlist"
        const val WATCHLIST_ACTION_REMOVE = "remove_from_watchlist"
        const val COLLECTION_ACTION_ADD = "add_to_collection"
        const val COLLECTION_ACTION_REMOVE = "remove_from_collection"
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