package com.theupnextapp.ui.library

import android.app.Application
import androidx.lifecycle.*
import com.theupnextapp.R
import com.theupnextapp.common.utils.DateUtils
import com.theupnextapp.common.utils.UpnextPreferenceManager
import com.theupnextapp.common.utils.models.DatabaseTables
import com.theupnextapp.common.utils.models.TableUpdateInterval
import com.theupnextapp.domain.LibraryList
import com.theupnextapp.domain.TableUpdate
import com.theupnextapp.ui.common.TraktViewModel
import kotlinx.coroutines.launch

class LibraryViewModel(application: Application) : TraktViewModel(application) {

    private val _libraryList = MutableLiveData<MutableList<LibraryList>>(mutableListOf())

    val libraryList: LiveData<MutableList<LibraryList>> = _libraryList

    private val traktWatchlist = traktRepository.traktWatchlist

    private val traktCollection = traktRepository.traktCollection

    private val traktHistory = traktRepository.traktHistory

    private val traktRecommendations = traktRepository.traktRecommendations

    private val isRepoLoading = traktRepository.isLoading

    val isRemovingWatchlistData = traktRepository.isRemovingTraktWatchlist

    val isRemovingHistoryData = traktRepository.isRemovingTraktHistory

    val isRemovingRecommendationsData = traktRepository.isRemovingTraktRecommendations

    val isRemovingCollectionData = traktRepository.isRemovingTraktCollection

    val isLoadingWatchlist = traktRepository.isLoadingTraktWatchlist

    val isLoadingCollection = traktRepository.isLoadingTraktCollection

    val isLoadingHistory = traktRepository.isLoadingTraktHistory

    val isLoadingRecommendations = traktRepository.isLoadingTraktRecommendations

    val historyTableUpdate =
        traktRepository.tableUpdate(DatabaseTables.TABLE_TRAKT_HISTORY.tableName)

    val collectionTableUpdate =
        traktRepository.tableUpdate(DatabaseTables.TABLE_TRAKT_COLLECTION.tableName)

    val watchlistTableUpdate =
        traktRepository.tableUpdate(DatabaseTables.TABLE_TRAKT_WATCHLIST.tableName)

    val recommendationsTableUpdate =
        traktRepository.tableUpdate(DatabaseTables.TABLE_TRAKT_RECOMMENDATIONS.tableName)

    private val watchlistEmpty = MediatorLiveData<Boolean>().apply {
        addSource(traktWatchlist) {
            value = it.isNullOrEmpty() == true
        }
    }

    private val collectionEmpty = MediatorLiveData<Boolean>().apply {
        addSource(traktCollection) {
            value = it.isNullOrEmpty() == true
        }
    }

    private val historyEmpty = MediatorLiveData<Boolean>().apply {
        addSource(traktHistory) {
            value = it.isNullOrEmpty() == true
        }
    }

    private val recommendationsEmpty = MediatorLiveData<Boolean>().apply {
        addSource(traktRecommendations) {
            value = it.isNullOrEmpty() == true
        }
    }

    val isLoading = MediatorLiveData<Boolean>().apply {
        addSource(isLoadingCollection) {
            value = it
        }
        addSource(isLoadingHistory) {
            value = it
        }
        addSource(isLoadingWatchlist) {
            value = it
        }
        addSource(isLoadingRecommendations) {
            value = it
        }
        addSource(fetchAccessTokenInProgress) {
            value = it
        }
        addSource(storingTraktAccessTokenInProgress) {
            value = it
        }
        addSource(isRepoLoading) {
            value = it
        }
    }

    fun onWatchlistTableUpdateReceived(tableUpdate: TableUpdate?) {
        if (isAuthorizedOnTrakt.value == false) {
            return
        }

        val timeDifferenceToDisplay =
            tableUpdate?.lastUpdated?.let { it -> DateUtils.getTimeDifferenceForDisplay(endTime = it) }

        // Only perform an update if there has been enough time before the previous update
        shouldUpdateWatchlist(tableUpdate)

        if (!_libraryList.value.isNullOrEmpty()) {
            val iterator = _libraryList.value!!.iterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                if (item.title == "Trakt Watchlist") {
                    iterator.remove()
                }
            }
        }

        _libraryList.value?.add(
            LibraryList(
                leftIcon = R.drawable.ic_baseline_playlist_add_check_24,
                title = "Trakt Watchlist",
                rightIcon = R.drawable.ic_baseline_chevron_right_24,
                link = LibraryFragmentDirections.actionLibraryFragmentToWatchlistFragment(),
                lastUpdated = timeDifferenceToDisplay
            )
        )
        _libraryList.value?.sortBy { it.title }
        _libraryList.value = _libraryList.value
    }

    private fun shouldUpdateWatchlist(tableUpdate: TableUpdate?) {
        val diffInMinutes =
            tableUpdate?.lastUpdated?.let { it ->
                DateUtils.dateDifference(
                    endTime = it,
                    type = "minutes"
                )
            }

        if (diffInMinutes != null && diffInMinutes != 0L) {
            if (diffInMinutes > TableUpdateInterval.TRAKT_WATCHLIST_ITEMS.intervalMins && (isLoadingWatchlist.value == false || isLoadingWatchlist.value == null)) {
                viewModelScope?.launch {
                    traktRepository.refreshTraktWatchlist(UpnextPreferenceManager(getApplication()).getTraktAccessToken())
                }
            }
            // no updates have been done yet for this table
        } else if (((watchlistEmpty.value == null || watchlistEmpty.value == true) && (isLoadingWatchlist.value == null || isLoadingWatchlist.value == false)) && tableUpdate == null && diffInMinutes == null) {
            viewModelScope?.launch {
                traktRepository.refreshTraktWatchlist(UpnextPreferenceManager(getApplication()).getTraktAccessToken())
            }
        }
    }

    fun onCollectionTableUpdateReceived(tableUpdate: TableUpdate?) {
        if (isAuthorizedOnTrakt.value == false) {
            return
        }

        val diff =
            tableUpdate?.lastUpdated?.let { it -> DateUtils.getTimeDifferenceForDisplay(endTime = it) }

        shouldUpdateCollection(tableUpdate)

        if (!_libraryList.value.isNullOrEmpty()) {
            val iterator = _libraryList.value!!.iterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                if (item.title == "Trakt Collection") {
                    iterator.remove()
                }
            }
        }

        _libraryList.value?.add(
            LibraryList(
                leftIcon = R.drawable.ic_baseline_library_add_check_24,
                title = "Trakt Collection",
                rightIcon = R.drawable.ic_baseline_chevron_right_24,
                link = LibraryFragmentDirections.actionLibraryFragmentToCollectionFragment(),
                lastUpdated = diff
            )
        )
        _libraryList.value?.sortBy { it.title }
        _libraryList.value = _libraryList.value
    }

    private fun shouldUpdateCollection(tableUpdate: TableUpdate?) {
        val diffInMinutes =
            tableUpdate?.lastUpdated?.let { it ->
                DateUtils.dateDifference(
                    endTime = it,
                    type = "minutes"
                )
            }

        if (diffInMinutes != null && diffInMinutes != 0L) {
            if (diffInMinutes > TableUpdateInterval.TRAKT_COLLECTION_ITEMS.intervalMins && (isLoadingCollection.value == false || isLoadingCollection.value == null)) {
                viewModelScope?.launch {
                    traktRepository.refreshTraktCollection(UpnextPreferenceManager(getApplication()).getTraktAccessToken())
                }
            }
            // no updates have been done yet for this table
        } else if (((collectionEmpty.value == null || collectionEmpty.value == true) && (isLoadingCollection.value == null || isLoadingCollection.value == false)) && tableUpdate == null && diffInMinutes == null) {
            viewModelScope?.launch {
                traktRepository.refreshTraktCollection(UpnextPreferenceManager(getApplication()).getTraktAccessToken())
            }
        }
    }

    fun onHistoryTableUpdateReceived(tableUpdate: TableUpdate?) {
        if (isAuthorizedOnTrakt.value == false) {
            return
        }

        val diff =
            tableUpdate?.lastUpdated?.let { it -> DateUtils.getTimeDifferenceForDisplay(endTime = it) }

        shouldUpdateHistory(tableUpdate)

        if (!_libraryList.value.isNullOrEmpty()) {
            val iterator = _libraryList.value!!.iterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                if (item.title == "Trakt History") {
                    iterator.remove()
                }
            }
        }

        _libraryList.value?.add(
            LibraryList(
                leftIcon = R.drawable.ic_history_white_24dp,
                title = "Trakt History",
                rightIcon = R.drawable.ic_baseline_chevron_right_24,
                link = LibraryFragmentDirections.actionLibraryFragmentToHistoryFragment(),
                lastUpdated = diff
            )
        )
        _libraryList.value?.sortBy { it.title }
        _libraryList.value = _libraryList.value
    }

    private fun shouldUpdateHistory(tableUpdate: TableUpdate?) {
        val diffInMinutes =
            tableUpdate?.lastUpdated?.let { it ->
                DateUtils.dateDifference(
                    endTime = it,
                    type = "minutes"
                )
            }

        if (diffInMinutes != null && diffInMinutes != 0L) {
            if (diffInMinutes > TableUpdateInterval.TRAKT_HISTORY_ITEMS.intervalMins && (isLoadingHistory.value == false || isLoadingHistory.value == null)) {
                viewModelScope?.launch {
                    traktRepository.refreshTraktHistory(UpnextPreferenceManager(getApplication()).getTraktAccessToken())
                }
            }
            // no updates have been done yet for this table
        } else if (((historyEmpty.value == null || historyEmpty.value == true) && (isLoadingHistory.value == null || isLoadingHistory.value == false)) && tableUpdate == null && diffInMinutes == null) {
            viewModelScope?.launch {
                traktRepository.refreshTraktHistory(UpnextPreferenceManager(getApplication()).getTraktAccessToken())
            }
        }
    }

    fun onRecommendationsTableUpdateReceived(tableUpdate: TableUpdate?) {
        if (isAuthorizedOnTrakt.value == false) {
            return
        }

        val diff =
            tableUpdate?.lastUpdated?.let { it -> DateUtils.getTimeDifferenceForDisplay(endTime = it) }

        shouldUpdateRecommendations(tableUpdate)

        if (!_libraryList.value.isNullOrEmpty()) {
            val iterator = _libraryList.value!!.iterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                if (item.title == "Your Trakt Recommendations") {
                    iterator.remove()
                }
            }
        }

        _libraryList.value?.add(
            LibraryList(
                leftIcon = R.drawable.ic_baseline_queue_play_next_24,
                title = "Your Trakt Recommendations",
                rightIcon = R.drawable.ic_baseline_chevron_right_24,
                link = LibraryFragmentDirections.actionLibraryFragmentToTraktRecommendationsFragment(),
                lastUpdated = diff
            )
        )
        _libraryList.value?.sortBy { it.title }
        _libraryList.value = _libraryList.value
    }

    private fun shouldUpdateRecommendations(tableUpdate: TableUpdate?) {
        val diffInMinutes =
            tableUpdate?.lastUpdated?.let { it ->
                DateUtils.dateDifference(
                    endTime = it,
                    type = "minutes"
                )
            }

        if (diffInMinutes != null && diffInMinutes != 0L) {
            if (diffInMinutes > TableUpdateInterval.TRAKT_RECOMMENDED_ITEMS.intervalMins && (isLoadingRecommendations.value == false || isLoadingRecommendations.value == null)) {
                viewModelScope?.launch {
                    traktRepository.refreshTraktRecommendations(
                        UpnextPreferenceManager(
                            getApplication()
                        ).getTraktAccessToken()
                    )
                }
            }
            // no updates have been done yet for this table
        } else if (((recommendationsEmpty.value == null || recommendationsEmpty.value == true) && (isLoadingRecommendations.value == null || isLoadingRecommendations.value == false)) && tableUpdate == null && diffInMinutes == null) {
            viewModelScope?.launch {
                traktRepository.refreshTraktRecommendations(UpnextPreferenceManager(getApplication()).getTraktAccessToken())
            }
        }
    }

    fun onDisconnectConfirm() {
        removeTraktData()
    }

    class Factory(
        val app: Application
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LibraryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LibraryViewModel(
                    app
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}