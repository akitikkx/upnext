package com.theupnextapp.ui.collectionSeasonEpisodes

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.theupnextapp.domain.TraktCollectionArg
import com.theupnextapp.domain.TraktCollectionSeason
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.ui.common.TraktViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class CollectionSeasonEpisodesViewModel @AssistedInject constructor(
    application: Application,
    traktRepository: TraktRepository,
    @Assisted collection: TraktCollectionArg?,
    @Assisted collectionSeason: TraktCollectionSeason?
) : TraktViewModel(application, traktRepository) {

    private val _collection = MutableLiveData<TraktCollectionArg>(collection)

    private val _collectionSeason = MutableLiveData<TraktCollectionSeason>(collectionSeason)

    private val _collectionSeasonEpisodesEmpty = MutableLiveData<Boolean>()

    val collection: LiveData<TraktCollectionArg> = _collection

    val collectionSeason: LiveData<TraktCollectionSeason> = _collectionSeason

    val collectionSeasonEpisodesEmpty: LiveData<Boolean> = _collectionSeasonEpisodesEmpty

    val traktCollectionSeasonEpisodes =
        collection?.imdbID?.let { imdbID ->
            collectionSeason?.seasonNumber?.let { seasonNumber ->
                traktRepository.traktCollectionSeasonEpisodes(
                    imdbID,
                    seasonNumber
                )
            }
        }

    val isLoadingCollection = traktRepository.isLoadingTraktCollection

    fun onCollectionSeasonEpisodesEmpty(empty: Boolean) {
        _collectionSeasonEpisodesEmpty.value = empty
    }

    @AssistedFactory
    interface CollectionSeasonEpisodesViewModelFactory {
        fun create(
            collection: TraktCollectionArg?,
            collectionSeason: TraktCollectionSeason?
        ): CollectionSeasonEpisodesViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: CollectionSeasonEpisodesViewModelFactory,
            collection: TraktCollectionArg?,
            collectionSeason: TraktCollectionSeason?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(collection, collectionSeason) as T
            }
        }
    }
}