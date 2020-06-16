package com.theupnextapp.ui.helpContent.watchlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class WatchlistInfoBottomSheetViewModel(application: Application) : AndroidViewModel(application) {

    class Factory(
        val app: Application
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WatchlistInfoBottomSheetViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return WatchlistInfoBottomSheetViewModel(
                    app
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}