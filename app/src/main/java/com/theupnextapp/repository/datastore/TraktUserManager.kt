package com.theupnextapp.repository.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.*
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "upnext_trakt_prefs")

class TraktUserManager(private val context: Context) {

    private val TRAKT_ACCESS_TOKEN = stringPreferencesKey("trakt_access_token")
    val traktAccessTokenFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[TRAKT_ACCESS_TOKEN].orEmpty()
    }

    private val TRAKT_ACCESS_TOKEN_REFRESH = stringPreferencesKey("trakt_access_token_refresh")
    val traktAccessTokenRefreshFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[TRAKT_ACCESS_TOKEN_REFRESH].orEmpty()
    }

    private val TRAKT_ACCESS_TOKEN_CREATED_AT = intPreferencesKey("trakt_access_token_created_at")
    val traktAccessTokenCreatedAtFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[TRAKT_ACCESS_TOKEN_CREATED_AT] ?: 0
    }

    val TRAKT_ACCESS_TOKEN_EXPIRES_IN = intPreferencesKey("trakt_access_token_expires_in")
    val traktAccessTokenExpiresInFlow: Flow<Int?> = context.dataStore.data.map {
        it[TRAKT_ACCESS_TOKEN_EXPIRES_IN]
    }

    private val TRAKT_ACCESS_TOKEN_SCOPE = stringPreferencesKey("trakt_access_token_scope")
    val traktAccessTokenScopeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[TRAKT_ACCESS_TOKEN_SCOPE].orEmpty()
    }

    private val TRAKT_ACCESS_TOKEN_TYPE = stringPreferencesKey("trakt_access_token_type")
    val traktAccessTokenTypeFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TRAKT_ACCESS_TOKEN_TYPE]
    }

    suspend fun saveTraktAccessToken(token: String) {
        context.dataStore.edit {
            it[TRAKT_ACCESS_TOKEN] = token
        }
    }

    suspend fun removeTraktAccessToken() {
        context.dataStore.edit {
            it.remove(TRAKT_ACCESS_TOKEN)
        }
    }

    suspend fun saveTraktAccessTokenRefresh(token: String) {
        context.dataStore.edit {
            it[TRAKT_ACCESS_TOKEN_REFRESH] = token
        }
    }

    suspend fun removeTraktAccessTokenRefresh() {
        context.dataStore.edit {
            it.remove(TRAKT_ACCESS_TOKEN_REFRESH)
        }
    }

    suspend fun saveTraktAccessTokenCreatedAt(createdAt: Int) {
        context.dataStore.edit {
            it[TRAKT_ACCESS_TOKEN_CREATED_AT] = createdAt
        }
    }

    suspend fun removeTraktAccessTokenCreatedAt() {
        context.dataStore.edit {
            it.remove(TRAKT_ACCESS_TOKEN_CREATED_AT)
        }
    }

    suspend fun saveTraktAccessTokenExpiresIn(expiresIn: Int) {
        context.dataStore.edit {
            it[TRAKT_ACCESS_TOKEN_EXPIRES_IN] = expiresIn
        }
    }

    suspend fun removeTraktAccessTokenExpiresIn() {
        context.dataStore.edit {
            it.remove(TRAKT_ACCESS_TOKEN_EXPIRES_IN)
        }
    }

    suspend fun saveTraktAccessTokenScope(token: String) {
        context.dataStore.edit {
            it[TRAKT_ACCESS_TOKEN_SCOPE] = token
        }
    }

    suspend fun removeTraktAccessTokenScope() {
        context.dataStore.edit {
            it.remove(TRAKT_ACCESS_TOKEN_SCOPE)
        }
    }

    suspend fun saveTraktAccessTokenType(token: String) {
        context.dataStore.edit {
            it[TRAKT_ACCESS_TOKEN_TYPE] = token
        }
    }

    suspend fun removeTraktAccessTokenType() {
        context.dataStore.edit {
            it.remove(TRAKT_ACCESS_TOKEN_TYPE)
        }
    }
}