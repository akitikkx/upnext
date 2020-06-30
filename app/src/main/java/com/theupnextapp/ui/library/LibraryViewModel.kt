package com.theupnextapp.ui.library

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.theupnextapp.R
import com.theupnextapp.domain.LibraryList
import com.theupnextapp.ui.common.TraktViewModel

class LibraryViewModel(application: Application) : TraktViewModel(application) {

    private val _libraryList = MutableLiveData<List<LibraryList>>()

    val libraryList: LiveData<List<LibraryList>> = _libraryList

    init {
        val list = mutableListOf<LibraryList>()

        list.add(
            LibraryList(
                leftIcon = R.drawable.ic_baseline_playlist_add_check_24,
                title = "Trakt Watchlist",
                rightIcon = R.drawable.ic_baseline_chevron_right_24,
                link = LibraryFragmentDirections.actionLibraryFragmentToWatchlistFragment()
            )
        )

        list.add(
            LibraryList(
                leftIcon = R.drawable.ic_baseline_library_add_check_24,
                title = "Trakt Collection",
                rightIcon = R.drawable.ic_baseline_chevron_right_24,
                link = LibraryFragmentDirections.actionLibraryFragmentToHistoryFragment()
            )
        )

        list.add(
            LibraryList(
                leftIcon = R.drawable.ic_history_white_24dp,
                title = "Trakt History",
                rightIcon = R.drawable.ic_baseline_chevron_right_24,
                link = LibraryFragmentDirections.actionLibraryFragmentToHistoryFragment()
            )
        )

        _libraryList.value = list
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