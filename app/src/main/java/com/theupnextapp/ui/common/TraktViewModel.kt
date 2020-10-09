package com.theupnextapp.ui.common

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.*
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.database.getDatabase
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.domain.TraktConnectionArg
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.repository.datastore.TraktUserManager
import com.theupnextapp.ui.collection.CollectionViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

open class TraktViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = TraktUserManager(application)

    private val _launchTraktConnectWindow = MutableLiveData<Boolean>()
    val launchTraktConnectWindow: LiveData<Boolean> = _launchTraktConnectWindow

    private val _fetchingAccessTokenInProgress = MutableLiveData<Boolean>()
    val fetchAccessTokenInProgress: LiveData<Boolean> = _fetchingAccessTokenInProgress

    private val _storingTraktAccessTokenInProgress = MutableLiveData<Boolean>()
    val storingTraktAccessTokenInProgress: LiveData<Boolean> = _storingTraktAccessTokenInProgress

    private val _transactionInProgress = MutableLiveData<Boolean>()

    private val _onDisconnectClick = MutableLiveData<Boolean>()
    val onDisconnectClick: LiveData<Boolean> = _onDisconnectClick

    private val _accessToken = MutableLiveData<String?>()
    val accessToken: LiveData<String?> = _accessToken

    protected val viewModelJob = SupervisorJob()

    protected val viewModelScope: CoroutineScope? = CoroutineScope(viewModelJob + Dispatchers.Main)

    protected val database = getDatabase(application)

    protected val traktRepository = TraktRepository(database)

    private val prefTraktAccessToken = preferences.traktAccessToken

    private val prefTraktAccessTokenExpiresIn = preferences.traktAccessTokenExpiresIn

    private val prefTraktAccessTokenCreatedAt = preferences.traktAccessTokenCreatedAt

    private val prefTraktRefreshToken = preferences.traktAccessTokenRefresh

    val traktAccessToken = traktRepository.traktAccessToken

    private val _isAuthorizedOnTrakt = MediatorLiveData<Boolean>().apply {
        var createdDateEpoch: Int? = null
        var refreshToken: String? = null
        var tokenExpiresIn: Int? = null
        var hasExpired = false
        var accessTokenExists = false

        addSource(prefTraktAccessToken.asLiveData()) {
            accessTokenExists = it.isNullOrEmpty()
            _accessToken.value = it
            value = !it.isNullOrEmpty()
        }

        addSource(prefTraktAccessTokenCreatedAt.asLiveData()) {
            createdDateEpoch = it
        }

        addSource(prefTraktAccessTokenExpiresIn.asLiveData()) {
            tokenExpiresIn = it
        }

        addSource(prefTraktRefreshToken.asLiveData()) {
            refreshToken = it
            value = !it.isNullOrEmpty()
        }

        val expiryDateEpoch = tokenExpiresIn?.let { createdDateEpoch?.plus(it) }
        val currentDateEpoch = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())

        if (expiryDateEpoch != null) {
            if (!refreshToken.isNullOrEmpty() && currentDateEpoch >= expiryDateEpoch) {
                refreshAccessToken(refreshToken)
                hasExpired = true
            }
        }

        value = !(accessTokenExists && hasExpired)

        Timber.d("Current state of auth: $value")
    }

    val isAuthorizedOnTrakt: LiveData<Boolean?> = _isAuthorizedOnTrakt

    private fun checkIfTokenHasBeenRevoked() {

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

    private fun storeTraktAccessToken(traktAccessTokenResponse: TraktAccessToken) {
        if (traktAccessTokenResponse.refresh_token.isNullOrEmpty()) {
            FirebaseCrashlytics.getInstance()
                .recordException(Exception("Refresh token not received"))
            return
        }

        viewModelScope?.launch {
            val preferences = TraktUserManager(getApplication())
            traktAccessTokenResponse.access_token?.let { preferences.saveTraktAccessToken(it) }
            traktAccessTokenResponse.created_at?.let { preferences.saveTraktAccessTokenCreatedAt(it) }
            traktAccessTokenResponse.expires_in?.let { preferences.saveTraktAccessTokenExpiresIn(it) }
            preferences.saveTraktAccessTokenRefresh(traktAccessTokenResponse.refresh_token)
            traktAccessTokenResponse.scope?.let { preferences.saveTraktAccessTokenScope(it) }
            traktAccessTokenResponse.token_type?.let { preferences.saveTraktAccessTokenType(it) }
        }

        _isAuthorizedOnTrakt.value == true
    }

    fun onDisconnectConfirm() {
        removeTraktData()
    }

    private fun removeTraktData() {
        viewModelScope?.launch {
            val preferences = TraktUserManager(getApplication())
            preferences.removeTraktAccessToken()
            preferences.removeTraktAccessTokenCreatedAt()
            preferences.removeTraktAccessTokenExpiresIn()
            preferences.removeTraktAccessTokenRefresh()
            preferences.removeTraktAccessTokenScope()
            preferences.removeTraktAccessTokenType()

            traktRepository.clearAllTraktData()
            _isAuthorizedOnTrakt.value = false
        }
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