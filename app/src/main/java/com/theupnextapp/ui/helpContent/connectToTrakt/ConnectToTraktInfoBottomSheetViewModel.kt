package com.theupnextapp.ui.helpContent.connectToTrakt

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ConnectToTraktInfoBottomSheetViewModel(application: Application) : AndroidViewModel(application) {


    class Factory(
        val app: Application
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ConnectToTraktInfoBottomSheetViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ConnectToTraktInfoBottomSheetViewModel(
                    app
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}