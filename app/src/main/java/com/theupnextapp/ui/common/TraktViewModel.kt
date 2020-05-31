package com.theupnextapp.ui.common

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
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

    protected val viewModelJob = SupervisorJob()

    protected val viewModelScope: CoroutineScope? = CoroutineScope(viewModelJob + Dispatchers.Main)

    protected val database = getDatabase(application)

    protected val traktRepository = TraktRepository(database)

    val isAuthorizedOnTrakt: LiveData<Boolean?> = _isAuthorizedOnTrakt

    protected fun ifValidAccessTokenExists(): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(getApplication())

        val refreshToken = preferences.getString(SHARED_PREF_TRAKT_ACCESS_TOKEN_REFRESH_TOKEN, null)
        val createdDateEpoch = preferences.getInt(SHARED_PREF_TRAKT_ACCESS_TOKEN_CREATED_AT, 0)
        val expiresInEpoch = preferences.getInt(SHARED_PREF_TRAKT_ACCESS_TOKEN_EXPIRES_IN, 0)
        val expiryDateEpoch = createdDateEpoch + expiresInEpoch
        val currentDateEpoch = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())

        if (!refreshToken.isNullOrEmpty() && currentDateEpoch >= expiryDateEpoch) {
            refreshAccessToken(refreshToken)
            return false
        }

        val accessToken = preferences.getString(SHARED_PREF_TRAKT_ACCESS_TOKEN, null)
        return accessToken != null
    }

    private fun refreshAccessToken(refreshToken: String?) {
        viewModelScope?.launch {
            traktRepository.getTraktAccessRefreshToken(refreshToken)
        }
    }

    protected fun getAccessToken(): String? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(getApplication())

        return preferences.getString(SHARED_PREF_TRAKT_ACCESS_TOKEN, null)
    }

    protected fun storeTraktAccessToken(traktAccessTokenResponse: TraktAccessToken) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(getApplication())

        preferences.edit()
            .putString(SHARED_PREF_TRAKT_ACCESS_TOKEN, traktAccessTokenResponse.access_token)
            .apply()
        traktAccessTokenResponse.created_at?.let { createdAt ->
            preferences.edit().putInt(
                SHARED_PREF_TRAKT_ACCESS_TOKEN_CREATED_AT,
                createdAt
            ).apply()
            traktAccessTokenResponse.expires_in?.let { expiresIn ->
                preferences.edit().putInt(
                    SHARED_PREF_TRAKT_ACCESS_TOKEN_EXPIRES_IN,
                    expiresIn
                ).apply()
            }
            preferences.edit().putString(
                SHARED_PREF_TRAKT_ACCESS_TOKEN_REFRESH_TOKEN,
                traktAccessTokenResponse.refresh_token
            ).apply()
            preferences.edit()
                .putString(SHARED_PREF_TRAKT_ACCESS_TOKEN_SCOPE, traktAccessTokenResponse.scope)
                .apply()
            preferences.edit()
                .putString(SHARED_PREF_TRAKT_ACCESS_TOKEN_TYPE, traktAccessTokenResponse.token_type)
                .apply()
        }
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