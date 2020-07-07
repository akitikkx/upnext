package com.theupnextapp.ui.collectionSeasons

import android.app.Application
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.theupnextapp.common.utils.DateUtils
import com.theupnextapp.common.utils.models.DatabaseTables
import com.theupnextapp.common.utils.models.TableUpdateInterval
import com.theupnextapp.domain.TableUpdate
import com.theupnextapp.domain.TraktCollectionArg
import com.theupnextapp.domain.TraktCollectionSeason
import com.theupnextapp.domain.TraktCollectionSeasonEpisodeArg
import com.theupnextapp.ui.common.TraktViewModel
import kotlinx.coroutines.launch

class CollectionSeasonsViewModel(
    application: Application,
    private val traktCollectionArg: TraktCollectionArg
) : TraktViewModel(application) {

    private val _collectionSeasonsEmpty = MutableLiveData<Boolean>(false)

    private val _collection = MutableLiveData<TraktCollectionArg>(traktCollectionArg)

    private val _navigateToSelectedSeason = MutableLiveData<TraktCollectionSeasonEpisodeArg>()

    val collectionSeasonsEmpty: LiveData<Boolean> = _collectionSeasonsEmpty

    val navigateToSelectedSeason: LiveData<TraktCollectionSeasonEpisodeArg> =
        _navigateToSelectedSeason

    val collection: LiveData<TraktCollectionArg> = _collection

    val traktCollectionSeasons =
        traktCollectionArg.imdbID?.let { traktRepository.traktCollectionSeasons(it) }

    val isLoadingCollection = traktRepository.isLoadingTraktCollection

    val collectionTableUpdate =
        traktRepository.tableUpdate(DatabaseTables.TABLE_COLLECTION.tableName)

    private fun loadTraktCollection() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(getApplication())
        val accessToken = preferences.getString(SHARED_PREF_TRAKT_ACCESS_TOKEN, null)

        viewModelScope?.launch {
            traktRepository.refreshTraktCollection(accessToken)
        }
    }

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

    fun onCollectionTableUpdateReceived(tableUpdate: TableUpdate?) {
        if (isAuthorizedOnTrakt.value == false) {
            return
        }

        val diffInMinutes =
            tableUpdate?.lastUpdated?.let { it -> DateUtils.dateDifference(it, "minutes") }

        // Only perform an update if there has been enough time before the previous update
        if (diffInMinutes != null) {
            if (diffInMinutes >= TableUpdateInterval.COLLECTION_ITEMS.intervalMins) {
                loadTraktCollection()
            }
        }
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