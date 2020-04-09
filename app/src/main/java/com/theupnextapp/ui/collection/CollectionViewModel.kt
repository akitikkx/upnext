package com.theupnextapp.ui.collection

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.*
import com.theupnextapp.database.getDatabase
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.domain.TraktConnectionArg
import com.theupnextapp.repository.UpnextRepository
import com.theupnextapp.ui.splashscreen.SplashScreenViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CollectionViewModel(
    application: Application
) : AndroidViewModel(application) {

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

    }

    private fun ifValidAccessTokenExists(): Boolean {
        val sharedPreferences = getApplication<Application>().getSharedPreferences(
            SplashScreenViewModel.SHARED_PREF_NAME,
            Context.MODE_PRIVATE
        )

        val accessToken = sharedPreferences.getString(SHARED_PREF_TRAKT_ACCESS_TOKEN, null)
        return accessToken != null
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

        storeTraktAccessToken(traktAccessToken)
    }

    private fun storeTraktAccessToken(traktAccessTokenResponse: TraktAccessToken) {
        _storingTraktAccessTokenInProgress.value = true

        val sharedPreferences = getApplication<Application>().getSharedPreferences(
            SHARED_PREF_NAME,
            Context.MODE_PRIVATE
        )
        sharedPreferences.edit()
            .putString(SHARED_PREF_TRAKT_ACCESS_TOKEN, traktAccessTokenResponse.access_token)
            .apply()
        traktAccessTokenResponse.created_at?.let { createdAt ->
            sharedPreferences.edit().putInt(
                SHARED_PREF_TRAKT_ACCESS_TOKEN_CREATED_AT,
                createdAt
            ).apply()
            traktAccessTokenResponse.expires_in?.let { expiresIn ->
                sharedPreferences.edit().putInt(
                    SHARED_PREF_TRAKT_ACCESS_TOKEN_EXPIRES_IN,
                    expiresIn
                ).apply()
            }
            sharedPreferences.edit().putString(
                SHARED_PREF_TRAKT_ACCESS_TOKEN_REFRESH_TOKEN,
                traktAccessTokenResponse.refresh_token
            ).apply()
            sharedPreferences.edit()
                .putString(SHARED_PREF_TRAKT_ACCESS_TOKEN_SCOPE, traktAccessTokenResponse.scope)
                .apply()
            sharedPreferences.edit()
                .putString(SHARED_PREF_TRAKT_ACCESS_TOKEN_TYPE, traktAccessTokenResponse.token_type)
                .apply()

            _transactionInProgress.value = false
            _isAuthorizedOnTrakt.value = true
            _storingTraktAccessTokenInProgress.value = false
        }
    }

    companion object {
        const val EXTRA_TRAKT_URI = "extra_trakt_uri"
        const val SHARED_PREF_NAME = "UpnextPreferences"
        const val SHARED_PREF_TRAKT_ACCESS_TOKEN = "trakt_access_token"
        const val SHARED_PREF_TRAKT_ACCESS_TOKEN_CREATED_AT = "trakt_access_token_created_at"
        const val SHARED_PREF_TRAKT_ACCESS_TOKEN_EXPIRES_IN = "trakt_access_token_expires_in"
        const val SHARED_PREF_TRAKT_ACCESS_TOKEN_REFRESH_TOKEN = "trakt_access_token_refresh_token"
        const val SHARED_PREF_TRAKT_ACCESS_TOKEN_SCOPE = "trakt_access_token_scope"
        const val SHARED_PREF_TRAKT_ACCESS_TOKEN_TYPE = "trakt_access_token_token_type"
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