package com.theupnextapp.ui.library

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.theupnextapp.R
import com.theupnextapp.common.utils.DateUtils
import com.theupnextapp.common.utils.models.DatabaseTables
import com.theupnextapp.domain.LibraryList
import com.theupnextapp.domain.TableUpdate
import com.theupnextapp.ui.common.TraktViewModel

class LibraryViewModel(application: Application) : TraktViewModel(application) {

    private val _libraryList = MutableLiveData<MutableList<LibraryList>>(mutableListOf())

    val libraryList: LiveData<MutableList<LibraryList>> = _libraryList

    val isRemovingWatchlistData = traktRepository.isRemovingTraktWatchlist

    val isRemovingHistoryData = traktRepository.isRemovingTraktHistory

    val isRemovingCollectionData = traktRepository.isRemovingTraktCollection

    val isLoadingWatchlist = traktRepository.isLoadingTraktWatchlist

    val isLoadingCollection = traktRepository.isLoadingTraktCollection

    val isLoadingHistory = traktRepository.isLoadingTraktHistory

    val historyTableUpdate =
        traktRepository.tableUpdate(DatabaseTables.TABLE_HISTORY.tableName)

    val collectionTableUpdate =
        traktRepository.tableUpdate(DatabaseTables.TABLE_COLLECTION.tableName)

    val watchlistTableUpdate =
        traktRepository.tableUpdate(DatabaseTables.TABLE_WATCHLIST.tableName)

    fun onWatchlistTableUpdateReceived(tableUpdate: TableUpdate?) {
        val diff =
            tableUpdate?.lastUpdated?.let { it -> DateUtils.getTimeDifferenceForDisplay(it) }

        if (!_libraryList.value.isNullOrEmpty()) {
            val iterator = _libraryList.value!!.iterator()
            while(iterator.hasNext()) {
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
                lastUpdated = diff
            )
        )
        _libraryList.value = _libraryList.value
    }

    fun onCollectionTableUpdateReceived(tableUpdate: TableUpdate?) {
        val diff =
            tableUpdate?.lastUpdated?.let { it -> DateUtils.getTimeDifferenceForDisplay(it) }

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
        _libraryList.value = _libraryList.value
    }

    fun onHistoryTableUpdateReceived(tableUpdate: TableUpdate?) {
        val diff =
            tableUpdate?.lastUpdated?.let { it -> DateUtils.getTimeDifferenceForDisplay(it) }

        if (!_libraryList.value.isNullOrEmpty()) {
            val iterator = _libraryList.value!!.iterator()
            while(iterator.hasNext()) {
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
        _libraryList.value = _libraryList.value
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