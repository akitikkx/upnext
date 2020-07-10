package com.theupnextapp.ui.collection

import android.app.Application
import androidx.lifecycle.*
import com.theupnextapp.domain.TraktCollection
import com.theupnextapp.domain.TraktCollectionArg
import com.theupnextapp.ui.common.TraktViewModel

class CollectionViewModel(
    application: Application
) : TraktViewModel(application) {

    private val _navigateToSelectedCollection = MutableLiveData<TraktCollectionArg>()

    val navigateToSelectedCollection: LiveData<TraktCollectionArg> = _navigateToSelectedCollection

    val isLoadingCollection = traktRepository.isLoadingTraktCollection

    val traktCollection = traktRepository.traktCollection

    val collectionEmpty = MediatorLiveData<Boolean>().apply {
        addSource(traktCollection) {
            value = it.isNullOrEmpty() == true
        }
    }

    fun onCollectionClick(traktCollection: TraktCollection) {
        _navigateToSelectedCollection.value = TraktCollectionArg(
            imdbID = traktCollection.imdbID,
            title = traktCollection.title,
            mediumImageUrl = traktCollection.mediumImageUrl,
            originalImageUrl = traktCollection.originalImageUrl,
            lastCollectedAt = traktCollection.lastCollectedAt,
            lastUpdatedAt = traktCollection.lastUpdatedAt
        )
    }

    fun navigateToSelectedCollectionComplete() {
        _navigateToSelectedCollection.value = null
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