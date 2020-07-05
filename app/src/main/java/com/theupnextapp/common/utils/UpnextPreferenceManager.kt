package com.theupnextapp.common.utils

import android.app.Application
import androidx.preference.PreferenceManager
import com.theupnextapp.ui.common.TraktViewModel

class UpnextPreferenceManager(val application: Application) {

    fun saveTraktAccessToken(token: String?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(application)

        preferences.edit()
            .putString(TraktViewModel.SHARED_PREF_TRAKT_ACCESS_TOKEN, token)
            .apply()
    }

    fun removeTraktAccessToken() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(application)
        preferences.edit().remove(TraktViewModel.SHARED_PREF_TRAKT_ACCESS_TOKEN).apply()
    }

    fun getTraktAccessToken(): String? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(application)
        return preferences.getString(SHARED_PREF_TRAKT_ACCESS_TOKEN, null)
    }

    fun saveTraktAccessTokenRefresh(token: String?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(application)

        preferences.edit()
            .putString(TraktViewModel.SHARED_PREF_TRAKT_ACCESS_TOKEN_REFRESH_TOKEN, token)
            .apply()
    }

    fun removeTraktAccessTokenRefresh() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(application)
        preferences.edit().remove(TraktViewModel.SHARED_PREF_TRAKT_ACCESS_TOKEN_REFRESH_TOKEN).apply()
    }

    fun getTraktAccessTokenRefresh(): String? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(application)
        return preferences.getString(SHARED_PREF_TRAKT_ACCESS_TOKEN_REFRESH_TOKEN, null)
    }

    fun saveTraktAccessTokenCreatedAt(createdAt: Int) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(application)

        preferences.edit()
            .putInt(TraktViewModel.SHARED_PREF_TRAKT_ACCESS_TOKEN_CREATED_AT, createdAt)
            .apply()
    }

    fun getTraktAccessTokenCreatedAt(): Int {
        val preferences = PreferenceManager.getDefaultSharedPreferences(application)
        return preferences.getInt(SHARED_PREF_TRAKT_ACCESS_TOKEN_CREATED_AT, 0)
    }

    fun removeTraktAccessTokenCreatedAt() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(application)
        preferences.edit().remove(SHARED_PREF_TRAKT_ACCESS_TOKEN_CREATED_AT).apply()
    }

    fun saveTraktAccessTokenExpiresIn(expiresIn: Int) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(application)

        preferences.edit()
            .putInt(TraktViewModel.SHARED_PREF_TRAKT_ACCESS_TOKEN_EXPIRES_IN, expiresIn)
            .apply()
    }

    fun getTraktAccessTokenExpiresIn(): Int {
        val preferences = PreferenceManager.getDefaultSharedPreferences(application)
        return preferences.getInt(SHARED_PREF_TRAKT_ACCESS_TOKEN_EXPIRES_IN, 0)
    }

    fun removeTraktAccessTokenExpiresIn() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(application)
        preferences.edit().remove(SHARED_PREF_TRAKT_ACCESS_TOKEN_EXPIRES_IN).apply()
    }

    fun saveTraktAccessTokenScope(token: String?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(application)

        preferences.edit()
            .putString(TraktViewModel.SHARED_PREF_TRAKT_ACCESS_TOKEN_SCOPE, token)
            .apply()
    }

    fun removeTraktAccessTokenScope() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(application)
        preferences.edit().remove(SHARED_PREF_TRAKT_ACCESS_TOKEN_SCOPE).apply()
    }

    fun saveTraktAccessTokenType(token: String?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(application)

        preferences.edit()
            .putString(TraktViewModel.SHARED_PREF_TRAKT_ACCESS_TOKEN_TYPE, token)
            .apply()
    }

    fun removeTraktAccessTokenType() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(application)
        preferences.edit().remove(SHARED_PREF_TRAKT_ACCESS_TOKEN_TYPE).apply()
    }


    companion object {
        const val SHARED_PREF_TRAKT_ACCESS_TOKEN = "trakt_access_token"
        const val SHARED_PREF_TRAKT_ACCESS_TOKEN_CREATED_AT = "trakt_access_token_created_at"
        const val SHARED_PREF_TRAKT_ACCESS_TOKEN_EXPIRES_IN = "trakt_access_token_expires_in"
        const val SHARED_PREF_TRAKT_ACCESS_TOKEN_REFRESH_TOKEN = "trakt_access_token_refresh_token"
        const val SHARED_PREF_TRAKT_ACCESS_TOKEN_SCOPE = "trakt_access_token_scope"
        const val SHARED_PREF_TRAKT_ACCESS_TOKEN_TYPE = "trakt_access_token_token_type"
    }

}