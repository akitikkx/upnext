package com.theupnextapp.ui.collection

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.theupnextapp.domain.TraktCollectionArg
import com.theupnextapp.ui.common.TraktViewModel
import kotlinx.coroutines.launch

class CollectionViewModel(
    application: Application
) : TraktViewModel(application) {

    private val _collectionEmpty = MutableLiveData<Boolean>()

    private val _navigateToSelectedCollection = MutableLiveData<TraktCollectionArg>()

    val collectionEmpty: LiveData<Boolean> = _collectionEmpty

    val navigateToSelectedCollection: LiveData<TraktCollectionArg> = _navigateToSelectedCollection

    val traktCollection = traktRepository.traktCollection

    val isLoadingCollection = traktRepository.isLoadingTraktCollection

    init {
        _collectionEmpty.value = false
        if (ifValidAccessTokenExists()) {
            loadTraktCollection()
            _isAuthorizedOnTrakt.value = true
        }
    }

    fun onCollectionEmpty(empty: Boolean) {
        _collectionEmpty.value = empty
    }

    fun displaySeasons(traktCollectionArg: TraktCollectionArg) {
        _navigateToSelectedCollection.value = traktCollectionArg
    }

    fun displaySeasonsComplete() {
        _navigateToSelectedCollection.value = null
    }

    private fun loadTraktCollection() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(getApplication())
        val accessToken = preferences.getString(SHARED_PREF_TRAKT_ACCESS_TOKEN, null)

        viewModelScope?.launch {
            traktRepository.refreshTraktCollection(accessToken)
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