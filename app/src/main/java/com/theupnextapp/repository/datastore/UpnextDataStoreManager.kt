package com.theupnextapp.repository.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.theupnextapp.domain.TraktAccessToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "upnext_prefs")

class UpnextDataStoreManager constructor(context: Context) {

    private val dataStore = context.dataStore

    val traktAccessTokenFlow: Flow<TraktAccessToken> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            TraktAccessToken(
                access_token = preferences[TRAKT_ACCESS_TOKEN],
                created_at = preferences[TRAKT_ACCESS_TOKEN_CREATED_AT] ?: NOT_FOUND,
                expires_in = preferences[TRAKT_ACCESS_TOKEN_EXPIRES_IN] ?: NOT_FOUND,
                refresh_token = preferences[TRAKT_REFRESH_ACCESS_TOKEN].orEmpty(),
                scope = preferences[TRAKT_ACCESS_TOKEN_SCOPE].orEmpty(),
                token_type = preferences[TRAKT_ACCESS_TOKEN_TYPE].orEmpty(),
            )
        }

    suspend fun saveTraktAccessToken(accessToken: String) {
        dataStore.edit {
            it[TRAKT_ACCESS_TOKEN] = accessToken
        }
    }

    suspend fun saveTraktAccessTokenCreatedAt(accessTokenCreatedAt: Int) {
        dataStore.edit {
            it[TRAKT_ACCESS_TOKEN_CREATED_AT] = accessTokenCreatedAt
        }
    }

    suspend fun saveTraktAccessTokenExpiredIn(accessTokenExpiredIn: Int) {
        dataStore.edit {
            it[TRAKT_ACCESS_TOKEN_EXPIRES_IN] = accessTokenExpiredIn
        }
    }

    suspend fun saveRefreshTraktAccessToken(refreshAccessToken: String) {
        dataStore.edit {
            it[TRAKT_REFRESH_ACCESS_TOKEN] = refreshAccessToken
        }
    }

    suspend fun saveTraktAccessScope(accessTokenScope: String) {
        dataStore.edit {
            it[TRAKT_ACCESS_TOKEN_SCOPE] = accessTokenScope
        }
    }

    suspend fun saveTraktAccessTokenType(accessTokenType: String) {
        dataStore.edit {
            it[TRAKT_ACCESS_TOKEN_TYPE] = accessTokenType
        }
    }

    companion object {
        val TRAKT_ACCESS_TOKEN = stringPreferencesKey("trakt_access_token")
        val TRAKT_ACCESS_TOKEN_CREATED_AT = intPreferencesKey("trakt_access_token_created_at")
        val TRAKT_ACCESS_TOKEN_EXPIRES_IN = intPreferencesKey("trakt_access_token_expires_in")
        val TRAKT_REFRESH_ACCESS_TOKEN = stringPreferencesKey("trakt_refresh_access_token")
        val TRAKT_ACCESS_TOKEN_SCOPE = stringPreferencesKey("trakt_access_token_scope")
        val TRAKT_ACCESS_TOKEN_TYPE = stringPreferencesKey("trakt_access_token_type")
        const val NOT_FOUND = -1
    }

}