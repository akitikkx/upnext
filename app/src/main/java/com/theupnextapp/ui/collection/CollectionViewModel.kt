package com.theupnextapp.ui.collection

import android.app.Application
import androidx.lifecycle.MediatorLiveData
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.ui.common.TraktViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CollectionViewModel @Inject constructor(
    application: Application, traktRepository: TraktRepository
) : TraktViewModel(application, traktRepository) {

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
}