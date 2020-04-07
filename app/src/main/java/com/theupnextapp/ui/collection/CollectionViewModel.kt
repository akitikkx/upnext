package com.theupnextapp.ui.collection

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CollectionViewModel(application: Application) : AndroidViewModel(application) {

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CollectionViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CollectionViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}