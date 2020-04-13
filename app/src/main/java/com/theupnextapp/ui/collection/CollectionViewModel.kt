package com.theupnextapp.ui.collection

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.theupnextapp.database.getDatabase
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.domain.TraktConnectionArg
import com.theupnextapp.repository.UpnextRepository
import com.theupnextapp.ui.common.TraktViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CollectionViewModel(
    application: Application
) : TraktViewModel(application) {

    private val viewModelJob = SupervisorJob()

    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val database = getDatabase(application)

    private val upnextRepository = UpnextRepository(database)

    private val _launchTraktConnectWindow = MutableLiveData<Boolean>()

    private val _fetchingAccessTokenInProgress = MutableLiveData<Boolean>()

    private val _storingTraktAccessTokenInProgress = MutableLiveData<Boolean>()

    private val _transactionInProgress = MutableLiveData<Boolean>()

    private val _isAuthorizedOnTrakt = MutableLiveData<Boolean>(ifValidAccessTokenExists())

    val launchTraktConnectWindow: LiveData<Boolean>
        get() = _launchTraktConnectWindow

    val fetchAccessTokenInProgress: LiveData<Boolean>
        get() = _fetchingAccessTokenInProgress

    val storingTraktAccessTokenInProgress: LiveData<Boolean>
        get() = _storingTraktAccessTokenInProgress

    val transactionInProgress: LiveData<Boolean>
        get() = _transactionInProgress

    val isAuthorizedOnTrakt: LiveData<Boolean>
        get() = _isAuthorizedOnTrakt

    fun onConnectClick() {
        _launchTraktConnectWindow.value = true
    }

    fun launchConnectWindowComplete() {
        _launchTraktConnectWindow.value = false
    }

    init {
        if (ifValidAccessTokenExists()) {
            _isAuthorizedOnTrakt.value = true
        }
    }

    fun loadTraktCollection() {
        val sharedPreferences = getApplication<Application>().getSharedPreferences(
            SHARED_PREF_NAME,
            Context.MODE_PRIVATE
        )

        val accessToken = sharedPreferences.getString(SHARED_PREF_TRAKT_ACCESS_TOKEN, null)

        viewModelScope.launch {
            upnextRepository.refreshTraktCollection(accessToken)
        }
    }

    val traktAccessToken = upnextRepository.traktAccessToken

    fun onTraktConnectionBundleReceived(bundle: Bundle?) {
        _transactionInProgress.value = true
        extractCode(bundle)
    }

    private fun extractCode(bundle: Bundle?) {
        val traktConnectionArg = bundle?.getParcelable<TraktConnectionArg>(EXTRA_TRAKT_URI)

        _fetchingAccessTokenInProgress.value = true

        viewModelScope.launch {
            upnextRepository.getTraktAccessToken(traktConnectionArg?.code)
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