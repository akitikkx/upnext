package com.theupnextapp.ui.collectionSeasonEpisodes

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.theupnextapp.domain.TraktCollectionArg
import com.theupnextapp.domain.TraktCollectionSeason
import com.theupnextapp.ui.common.TraktViewModel

class CollectionSeasonEpisodesViewModel(
    application: Application,
    collection: TraktCollectionArg?,
    collectionSeason: TraktCollectionSeason?
) : TraktViewModel(application) {

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

    class Factory(
        val app: Application,
        val collection: TraktCollectionArg?,
        val collectionSeason: TraktCollectionSeason?
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CollectionSeasonEpisodesViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CollectionSeasonEpisodesViewModel(
                    app,
                    collection,
                    collectionSeason
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}