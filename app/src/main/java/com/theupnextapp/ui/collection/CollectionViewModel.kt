package com.theupnextapp.ui.collection

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.domain.TraktCollectionArg
import com.theupnextapp.domain.TraktConnectionArg
import com.theupnextapp.ui.common.TraktViewModel
import kotlinx.coroutines.launch

class CollectionViewModel(
    application: Application
) : TraktViewModel(application) {

    private val _collectionEmpty = MutableLiveData<Boolean>()

    private val _navigateToSelectedCollection = MutableLiveData<TraktCollectionArg>()

    private val _fetchingAccessTokenInProgress = MutableLiveData<Boolean>()

    private val _storingTraktAccessTokenInProgress = MutableLiveData<Boolean>()

    private val _transactionInProgress = MutableLiveData<Boolean>()

    val launchTraktConnectWindow: LiveData<Boolean> = _launchTraktConnectWindow

    val fetchAccessTokenInProgress: LiveData<Boolean> = _fetchingAccessTokenInProgress

    val storingTraktAccessTokenInProgress: LiveData<Boolean> = _storingTraktAccessTokenInProgress

    val transactionInProgress: LiveData<Boolean> = _transactionInProgress

    val collectionEmpty: LiveData<Boolean> = _collectionEmpty

    val navigateToSelectedCollection: LiveData<TraktCollectionArg> = _navigateToSelectedCollection

    val traktCollection = traktRepository.traktCollection

    val isLoadingCollection = traktRepository.isLoadingTraktCollection

    fun onConnectClick() {
        _launchTraktConnectWindow.value = true
    }

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

    val traktAccessToken = traktRepository.traktAccessToken

    fun onTraktConnectionBundleReceived(bundle: Bundle?) {
        _transactionInProgress.value = true
        extractCode(bundle)
    }

    private fun extractCode(bundle: Bundle?) {
        val traktConnectionArg = bundle?.getParcelable<TraktConnectionArg>(EXTRA_TRAKT_URI)

        _fetchingAccessTokenInProgress.value = true

        viewModelScope?.launch {
            traktRepository.getTraktAccessToken(traktConnectionArg?.code)
        }
    }

    fun onTraktAccessTokenReceived(traktAccessToken: TraktAccessToken) {
        _fetchingAccessTokenInProgress.value = false
        _storingTraktAccessTokenInProgress.value = true

        storeTraktAccessToken(traktAccessToken)

        _transactionInProgress.value = false
        _isAuthorizedOnTrakt.value = true
        _storingTraktAccessTokenInProgress.value = false
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