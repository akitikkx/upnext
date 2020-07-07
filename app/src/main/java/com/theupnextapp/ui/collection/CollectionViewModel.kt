package com.theupnextapp.ui.collection

import android.app.Application
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.theupnextapp.common.utils.DateUtils
import com.theupnextapp.common.utils.models.DatabaseTables
import com.theupnextapp.common.utils.models.TableUpdateInterval
import com.theupnextapp.domain.TableUpdate
import com.theupnextapp.domain.TraktCollection
import com.theupnextapp.domain.TraktCollectionArg
import com.theupnextapp.ui.common.TraktViewModel
import kotlinx.coroutines.launch

class CollectionViewModel(
    application: Application
) : TraktViewModel(application) {

    private val _navigateToSelectedCollection = MutableLiveData<TraktCollectionArg>()

    val collectionEmpty = MediatorLiveData<Boolean>()

    val navigateToSelectedCollection: LiveData<TraktCollectionArg> = _navigateToSelectedCollection

    val traktCollection = traktRepository.traktCollection

    val isLoadingCollection = traktRepository.isLoadingTraktCollection

    val collectionTableUpdate =
        traktRepository.tableUpdate(DatabaseTables.TABLE_COLLECTION.tableName)

    init {
        collectionEmpty.addSource(traktCollection) {
            collectionEmpty.value = it.isNullOrEmpty() == true
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

    private fun loadTraktCollection() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(getApplication())
        val accessToken = preferences.getString(SHARED_PREF_TRAKT_ACCESS_TOKEN, null)

        viewModelScope?.launch {
            traktRepository.refreshTraktCollection(accessToken)
        }
    }

    fun onCollectionTableUpdateReceived(tableUpdate: TableUpdate?) {
        if (isAuthorizedOnTrakt.value == false) {
            return
        }

        val diffInMinutes =
            tableUpdate?.lastUpdated?.let { it -> DateUtils.dateDifference(it, "minutes") }

        // Only perform an update if there has been enough time before the previous update
        if (diffInMinutes != null && collectionEmpty.value != true) {
            if (diffInMinutes >= TableUpdateInterval.COLLECTION_ITEMS.intervalMins) {
                loadTraktCollection()
            }
        } else if (collectionEmpty.value == true) {
            loadTraktCollection()
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