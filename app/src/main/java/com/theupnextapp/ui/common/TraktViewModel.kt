package com.theupnextapp.ui.common

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.*
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.common.utils.UpnextPreferenceManager
import com.theupnextapp.database.getDatabase
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.domain.TraktConnectionArg
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.ui.collection.CollectionViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

open class TraktViewModel(application: Application) : AndroidViewModel(application) {

    protected val _isAuthorizedOnTrakt = MutableLiveData(ifValidAccessTokenExists())

    private val _launchTraktConnectWindow = MutableLiveData<Boolean>()
    val launchTraktConnectWindow: LiveData<Boolean> = _launchTraktConnectWindow

    private val _fetchingAccessTokenInProgress = MutableLiveData<Boolean>()

    private val _storingTraktAccessTokenInProgress = MutableLiveData<Boolean>()

    private val _transactionInProgress = MutableLiveData<Boolean>()

    private val _onDisconnectClick = MutableLiveData<Boolean>()

    protected val viewModelJob = SupervisorJob()

    protected val viewModelScope: CoroutineScope? = CoroutineScope(viewModelJob + Dispatchers.Main)

    protected val database = getDatabase(application)

    protected val traktRepository = TraktRepository(database)

    val fetchAccessTokenInProgress: LiveData<Boolean> = _fetchingAccessTokenInProgress

    val isAuthorizedOnTrakt: LiveData<Boolean?> = _isAuthorizedOnTrakt

    val storingTraktAccessTokenInProgress: LiveData<Boolean> = _storingTraktAccessTokenInProgress

    val onDisconnectClick: LiveData<Boolean> = _onDisconnectClick

    val traktAccessToken = traktRepository.traktAccessToken

    val invalidToken = traktRepository.invalidToken

    val invalidGrant = traktRepository.invalidGrant

    init {
        _isAuthorizedOnTrakt.value = ifValidAccessTokenExists()
    }

    protected fun ifValidAccessTokenExists(): Boolean {
        val preferences = UpnextPreferenceManager(getApplication())

        if (checkIfTokenHasExpired()) {
            return false
        }

        val accessToken = preferences.getTraktAccessToken()
        return accessToken != null
    }

    private fun checkIfTokenHasBeenRevoked() {

    }

    private fun checkIfTokenHasExpired(): Boolean {
        val preferences = UpnextPreferenceManager(getApplication())

        val refreshToken = preferences.getTraktAccessTokenRefresh()
        val createdDateEpoch = preferences.getTraktAccessTokenCreatedAt()
        val expiresInEpoch = preferences.getTraktAccessTokenExpiresIn()
        val expiryDateEpoch = createdDateEpoch + expiresInEpoch
        val currentDateEpoch = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())

        if (!refreshToken.isNullOrEmpty() && currentDateEpoch >= expiryDateEpoch) {
            refreshAccessToken(refreshToken)
            return true
        }

        return false
    }

    private fun refreshAccessToken(refreshToken: String?) {
        viewModelScope?.launch {
            traktRepository.getTraktAccessRefreshToken(refreshToken)
        }
    }

    fun onConnectClick() {
        _launchTraktConnectWindow.value = true
    }

    fun onDisconnectClick() {
        _onDisconnectClick.value = true
    }

    fun launchConnectWindowComplete() {
        _launchTraktConnectWindow.value = false
    }

    fun onTraktAccessTokenReceived(traktAccessToken: TraktAccessToken) {
        _fetchingAccessTokenInProgress.value = false
        _storingTraktAccessTokenInProgress.value = true

        storeTraktAccessToken(traktAccessToken)

        _transactionInProgress.value = false
        _isAuthorizedOnTrakt.value = true
        _storingTraktAccessTokenInProgress.value = false

        viewModelScope?.launch {
            traktRepository.refreshTraktCollection(traktAccessToken.access_token)
            traktRepository.refreshTraktHistory(traktAccessToken.access_token)
            traktRepository.refreshTraktWatchlist(traktAccessToken.access_token)
        }
    }

    fun onInvalidTokenResponseReceived(invalid: Boolean) {
        val preferences = UpnextPreferenceManager(getApplication())

        val refreshToken = preferences.getTraktAccessTokenRefresh()

        if (invalid) {
            refreshAccessToken(refreshToken)
        }
    }

    fun onTraktConnectionBundleReceived(bundle: Bundle?) {
        _transactionInProgress.value = true
        extractCode(bundle)
        _transactionInProgress.value = false
    }

    private fun extractCode(bundle: Bundle?) {
        val traktConnectionArg =
            bundle?.getParcelable<TraktConnectionArg>(CollectionViewModel.EXTRA_TRAKT_URI)

        _fetchingAccessTokenInProgress.value = true

        viewModelScope?.launch {
            traktRepository.getTraktAccessToken(traktConnectionArg?.code)
        }
        _fetchingAccessTokenInProgress.value = false
    }

    fun onInvalidGrantResponseReceived(invalid: Boolean) {
        if (invalid) {
            _launchTraktConnectWindow.postValue(true)
        }
    }

    private fun storeTraktAccessToken(traktAccessTokenResponse: TraktAccessToken) {
        val preferences = UpnextPreferenceManager(getApplication())

        preferences.saveTraktAccessToken(traktAccessTokenResponse.access_token)

        traktAccessTokenResponse.created_at?.let { createdAt ->
            preferences.saveTraktAccessTokenCreatedAt(createdAt)
        }

        traktAccessTokenResponse.expires_in?.let { expiresIn ->
            preferences.saveTraktAccessTokenExpiresIn(expiresIn)
        }

        if (traktAccessTokenResponse.refresh_token.isNullOrEmpty()) {
            FirebaseCrashlytics.getInstance()
                .recordException(Exception("Refresh token not received"))
        }

        preferences.saveTraktAccessTokenRefresh(traktAccessTokenResponse.refresh_token)

        preferences.saveTraktAccessTokenScope(traktAccessTokenResponse.scope)

        preferences.saveTraktAccessTokenType(traktAccessTokenResponse.token_type)

        _isAuthorizedOnTrakt.value == true
    }

    fun removeTraktData() {
        val preferences = UpnextPreferenceManager(getApplication())

        preferences.removeTraktAccessToken()
        preferences.removeTraktAccessTokenCreatedAt()
        preferences.removeTraktAccessTokenExpiresIn()
        preferences.removeTraktAccessTokenRefresh()
        preferences.removeTraktAccessTokenScope()
        preferences.removeTraktAccessTokenType()

        viewModelScope?.launch {
            traktRepository.clearAllTraktData()
            _isAuthorizedOnTrakt.value = false
        }
    }

    companion object {
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
            if (modelClass.isAssignableFrom(TraktViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TraktViewModel(
                    app
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}