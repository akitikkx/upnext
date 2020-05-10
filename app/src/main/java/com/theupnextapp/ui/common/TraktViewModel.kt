package com.theupnextapp.ui.common

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.theupnextapp.domain.TraktAccessToken

open class TraktViewModel(application: Application) : AndroidViewModel(application) {

    protected val _isAuthorizedOnTrakt = MutableLiveData<Boolean?>(ifValidAccessTokenExists())

    val isAuthorizedOnTrakt: LiveData<Boolean?> = _isAuthorizedOnTrakt

    protected fun ifValidAccessTokenExists(): Boolean {
        // TODO Add functionality to determine if the access token has not yet expired, if so retrieve a new one

        val sharedPreferences = getApplication<Application>().getSharedPreferences(
            SHARED_PREF_NAME,
            Context.MODE_PRIVATE
        )

        val accessToken = sharedPreferences.getString(SHARED_PREF_TRAKT_ACCESS_TOKEN, null)
        return accessToken != null
    }

    protected fun getAccessToken(): String? {
        val sharedPreferences = getApplication<Application>().getSharedPreferences(
            SHARED_PREF_NAME,
            Context.MODE_PRIVATE
        )

        return sharedPreferences.getString(SHARED_PREF_TRAKT_ACCESS_TOKEN, null)
    }

    protected fun storeTraktAccessToken(traktAccessTokenResponse: TraktAccessToken) {
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
}