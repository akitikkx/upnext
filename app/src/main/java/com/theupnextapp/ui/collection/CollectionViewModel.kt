package com.theupnextapp.ui.collection

import android.app.Application
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.theupnextapp.ui.common.TraktViewModel

class CollectionViewModel(
    application: Application
) : TraktViewModel(application) {

    val isLoadingCollection = traktRepository.isLoadingTraktCollection

    val traktCollection = traktRepository.traktCollection

    val collectionEmpty = MediatorLiveData<Boolean>().apply {
        addSource(traktCollection) {
            value = it.isNullOrEmpty() == true
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    companion object {
        const val EXTRA_TRAKT_URI = "extra_trakt_uri"
    }

    class Factory(val app: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CollectionViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CollectionViewModel(
                    app
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}