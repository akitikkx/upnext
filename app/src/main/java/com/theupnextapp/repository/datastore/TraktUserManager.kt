package com.theupnextapp.repository.datastore

import android.content.Context
import androidx.datastore.DataStore
import androidx.datastore.preferences.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TraktUserManager(context: Context) {

    private val dataStore: DataStore<Preferences> = context.createDataStore(
        name = "upnext_trakt_prefs"
    )

    val traktAccessToken: Flow<String?> = dataStore.data.map {
        it[SHARED_PREF_TRAKT_ACCESS_TOKEN]
    }

    val traktAccessTokenRefresh: Flow<String?> = dataStore.data.map {
        it[SHARED_PREF_TRAKT_ACCESS_TOKEN_REFRESH_TOKEN]
    }

    val traktAccessTokenCreatedAt: Flow<Int?> = dataStore.data.map {
        it[SHARED_PREF_TRAKT_ACCESS_TOKEN_CREATED_AT]
    }

    val traktAccessTokenExpiresIn: Flow<Int?> = dataStore.data.map {
        it[SHARED_PREF_TRAKT_ACCESS_TOKEN_EXPIRES_IN]
    }

    val traktAccessTokenScope: Flow<String> = dataStore.data.map {
        it[SHARED_PREF_TRAKT_ACCESS_TOKEN_SCOPE] ?: ""
    }

    val traktAccessTokenType: Flow<String?> = dataStore.data.map {
        it[SHARED_PREF_TRAKT_ACCESS_TOKEN_TYPE]
    }

    suspend fun saveTraktAccessToken(token: String) {
        dataStore.edit {
            it[SHARED_PREF_TRAKT_ACCESS_TOKEN] = token
        }
    }

    suspend fun removeTraktAccessToken() {
        dataStore.edit {
            it.remove(SHARED_PREF_TRAKT_ACCESS_TOKEN)
        }
    }

    suspend fun saveTraktAccessTokenRefresh(token: String) {
        dataStore.edit {
            it[SHARED_PREF_TRAKT_ACCESS_TOKEN_REFRESH_TOKEN] = token
        }
    }

    suspend fun removeTraktAccessTokenRefresh() {
        dataStore.edit {
            it.remove(SHARED_PREF_TRAKT_ACCESS_TOKEN_REFRESH_TOKEN)
        }
    }

    suspend fun saveTraktAccessTokenCreatedAt(createdAt: Int) {
        dataStore.edit {
            it[SHARED_PREF_TRAKT_ACCESS_TOKEN_CREATED_AT] = createdAt
        }
    }

    suspend fun removeTraktAccessTokenCreatedAt() {
        dataStore.edit {
            it.remove(SHARED_PREF_TRAKT_ACCESS_TOKEN_CREATED_AT)
        }
    }

    suspend fun saveTraktAccessTokenExpiresIn(expiresIn: Int) {
        dataStore.edit {
            it[SHARED_PREF_TRAKT_ACCESS_TOKEN_EXPIRES_IN] = expiresIn
        }
    }

    suspend fun removeTraktAccessTokenExpiresIn() {
        dataStore.edit {
            it.remove(SHARED_PREF_TRAKT_ACCESS_TOKEN_EXPIRES_IN)
        }
    }

    suspend fun saveTraktAccessTokenScope(token: String) {
        dataStore.edit {
            it[SHARED_PREF_TRAKT_ACCESS_TOKEN_SCOPE] = token
        }
    }

    suspend fun removeTraktAccessTokenScope() {
        dataStore.edit {
            it.remove(SHARED_PREF_TRAKT_ACCESS_TOKEN_SCOPE)
        }
    }

    suspend fun saveTraktAccessTokenType(token: String) {
        dataStore.edit {
            it[SHARED_PREF_TRAKT_ACCESS_TOKEN_TYPE] = token
        }
    }

    suspend fun removeTraktAccessTokenType() {
        dataStore.edit {
            it.remove(SHARED_PREF_TRAKT_ACCESS_TOKEN_TYPE)
        }
    }

    companion object {
        val SHARED_PREF_TRAKT_ACCESS_TOKEN = preferencesKey<String>("trakt_access_token")
        val SHARED_PREF_TRAKT_ACCESS_TOKEN_CREATED_AT =
            preferencesKey<Int>("trakt_access_token_created_at")
        val SHARED_PREF_TRAKT_ACCESS_TOKEN_EXPIRES_IN =
            preferencesKey<Int>("trakt_access_token_expires_in")
        val SHARED_PREF_TRAKT_ACCESS_TOKEN_REFRESH_TOKEN =
            preferencesKey<String>("trakt_access_token_refresh_token")
        val SHARED_PREF_TRAKT_ACCESS_TOKEN_SCOPE =
            preferencesKey<String>("trakt_access_token_scope")
        val SHARED_PREF_TRAKT_ACCESS_TOKEN_TYPE =
            preferencesKey<String>("trakt_access_token_token_type")
    }
}