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
                R.drawable.ic_baseline_playlist_add_check_24,
                "Watchlist",
                R.drawable.ic_baseline_chevron_right_24,
                LibraryFragmentDirections.actionLibraryFragmentToWatchlistFragment()
            )
        )

        _libraryList.value = list
    }

    fun onWatchlistItemClick() {
        // TODO navigate to watchlist
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