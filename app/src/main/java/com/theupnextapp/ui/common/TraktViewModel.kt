package com.theupnextapp.ui.common

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.theupnextapp.domain.TraktAccessToken

open class TraktViewModel(application: Application) : AndroidViewModel(application) {

    protected val _isAuthorizedOnTrakt = MutableLiveData<Boolean?>(ifValidAccessTokenExists())

    val isAuthorizedOnTrakt: LiveData<Boolean?> = _isAuthorizedOnTrakt

    protected fun ifValidAccessTokenExists(): Boolean {
        // TODO Add functionality to determine if the access token has not yet expired, if so retrieve a new one

        val preferences = PreferenceManager.getDefaultSharedPreferences(getApplication())
        val accessToken = preferences.getString(SHARED_PREF_TRAKT_ACCESS_TOKEN, null)
        return accessToken != null
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