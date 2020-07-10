package com.theupnextapp.ui.collectionSeasons

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.theupnextapp.domain.TraktCollectionArg
import com.theupnextapp.domain.TraktCollectionSeason
import com.theupnextapp.domain.TraktCollectionSeasonEpisodeArg
import com.theupnextapp.ui.common.TraktViewModel

class CollectionSeasonsViewModel(
    application: Application,
    private val traktCollectionArg: TraktCollectionArg
) : TraktViewModel(application) {

    private val _collectionSeasonsEmpty = MutableLiveData(false)

    private val _collection = MutableLiveData(traktCollectionArg)

    private val _navigateToSelectedSeason = MutableLiveData<TraktCollectionSeasonEpisodeArg>()

    val collectionSeasonsEmpty: LiveData<Boolean> = _collectionSeasonsEmpty

    val navigateToSelectedSeason: LiveData<TraktCollectionSeasonEpisodeArg> =
        _navigateToSelectedSeason

    val collection: LiveData<TraktCollectionArg> = _collection

    val traktCollectionSeasons =
        traktCollectionArg.imdbID?.let { traktRepository.traktCollectionSeasons(it) }

    val isLoadingCollection = traktRepository.isLoadingTraktCollection

    fun onCollectionSeasonsEmpty(empty: Boolean) {
        _collectionSeasonsEmpty.value = empty
    }

    fun onSeasonClick(traktCollectionSeason: TraktCollectionSeason) {
        _navigateToSelectedSeason.value = TraktCollectionSeasonEpisodeArg(
            collection = traktCollectionArg,
            collectionSeason = traktCollectionSeason
        )
    }

    fun navigateToSelectedSeasonComplete() {
        _navigateToSelectedSeason.value = null
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    class Factory(
        val app: Application,
        val collection: TraktCollectionArg
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CollectionSeasonsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CollectionSeasonsViewModel(
                    app,
                    collection
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}