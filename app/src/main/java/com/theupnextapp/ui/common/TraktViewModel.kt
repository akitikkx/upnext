package com.theupnextapp.ui.common

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.common.utils.UpnextPreferenceManager
import com.theupnextapp.database.getDatabase
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.repository.TraktRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

open class TraktViewModel(application: Application) : AndroidViewModel(application) {

    protected val _isAuthorizedOnTrakt = MutableLiveData<Boolean?>(ifValidAccessTokenExists())

    protected val _launchTraktConnectWindow = MutableLiveData<Boolean>()

    protected val viewModelJob = SupervisorJob()

    protected val viewModelScope: CoroutineScope? = CoroutineScope(viewModelJob + Dispatchers.Main)

    protected val database = getDatabase(application)

    protected val traktRepository = TraktRepository(database)

    val isAuthorizedOnTrakt: LiveData<Boolean?> = _isAuthorizedOnTrakt

    val invalidToken = traktRepository.invalidToken

    val invalidGrant = traktRepository.invalidGrant

    protected fun ifValidAccessTokenExists(): Boolean {
        val preferences = UpnextPreferenceManager(getApplication())

        val refreshToken = preferences.getTraktAccessTokenRefresh()
        val createdDateEpoch = preferences.getTraktAccessTokenCreatedAt()
        val expiresInEpoch = preferences.getTraktAccessTokenExpiresIn()
        val expiryDateEpoch = createdDateEpoch + expiresInEpoch
        val currentDateEpoch = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())

        if (!refreshToken.isNullOrEmpty() && currentDateEpoch >= expiryDateEpoch) {
            refreshAccessToken(refreshToken)
            return false
        }

        val accessToken = preferences.getTraktAccessToken()
        return accessToken != null
    }

    private fun refreshAccessToken(refreshToken: String?) {
        viewModelScope?.launch {
            traktRepository.getTraktAccessRefreshToken(refreshToken)
        }
    }

    fun launchConnectWindowComplete() {
        _launchTraktConnectWindow.value = false
    }

    fun onInvalidTokenResponseReceived(invalid: Boolean) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(getApplication())

        val refreshToken = preferences.getString(SHARED_PREF_TRAKT_ACCESS_TOKEN_REFRESH_TOKEN, null)

        if (invalid) {
            refreshAccessToken(refreshToken)
        }
    }

    fun onInvalidGrantResponseReceived(invalid: Boolean) {
        if (invalid) {
            _launchTraktConnectWindow.postValue(true)
        }
    }

    protected fun getAccessToken(): String? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(getApplication())

        return preferences.getString(SHARED_PREF_TRAKT_ACCESS_TOKEN, null)
    }

    protected fun storeTraktAccessToken(traktAccessTokenResponse: TraktAccessToken) {
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
    }

    companion object {
        const val EXTRA_TRAKT_URI = "extra_trakt_uri"
        const val SHARED_PREF_TRAKT_ACCESS_TOKEN = "trakt_access_token"
        const val SHARED_PREF_TRAKT_ACCESS_TOKEN_CREATED_AT = "trakt_access_token_created_at"
        const val SHARED_PREF_TRAKT_ACCESS_TOKEN_EXPIRES_IN = "trakt_access_token_expires_in"
        const val SHARED_PREF_TRAKT_ACCESS_TOKEN_REFRESH_TOKEN = "trakt_access_token_refresh_token"
        const val SHARED_PREF_TRAKT_ACCESS_TOKEN_SCOPE = "trakt_access_token_scope"
        const val SHARED_PREF_TRAKT_ACCESS_TOKEN_TYPE = "trakt_access_token_token_type"
    }
}