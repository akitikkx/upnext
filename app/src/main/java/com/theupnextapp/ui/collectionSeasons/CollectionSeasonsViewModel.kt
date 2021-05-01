package com.theupnextapp.ui.collectionSeasons

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.theupnextapp.domain.TraktCollectionArg
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.ui.common.TraktViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class CollectionSeasonsViewModel @AssistedInject constructor(
    application: Application,
    traktRepository: TraktRepository,
    @Assisted traktCollectionArg: TraktCollectionArg
) : TraktViewModel(application, traktRepository) {

    private val _collectionSeasonsEmpty = MutableLiveData(false)
    val collectionSeasonsEmpty: LiveData<Boolean> = _collectionSeasonsEmpty

    private val _collection = MutableLiveData(traktCollectionArg)
    val collection: LiveData<TraktCollectionArg> = _collection

    val traktCollectionSeasons =
        traktCollectionArg.imdbID?.let { traktRepository.traktCollectionSeasons(it) }

    val isLoadingCollection = traktRepository.isLoadingTraktCollection

    fun onCollectionSeasonsEmpty(empty: Boolean) {
        _collectionSeasonsEmpty.value = empty
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    @AssistedFactory
    interface CollectionSeasonsViewModelFactory {
        fun create(
            traktCollectionArg: TraktCollectionArg
        ): CollectionSeasonsViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: CollectionSeasonsViewModelFactory,
            traktCollectionArg: TraktCollectionArg
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(traktCollectionArg) as T
            }
        }
    }
}