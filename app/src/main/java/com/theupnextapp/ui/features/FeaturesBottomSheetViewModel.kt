package com.theupnextapp.ui.features

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FeaturesBottomSheetViewModel(application: Application) : AndroidViewModel(application) {

    fun onGotItClick() {

    }

    class Factory(
        val app: Application
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FeaturesBottomSheetViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FeaturesBottomSheetViewModel(
                    app
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}