/*
 * MIT License
 *
 * Copyright (c) 2024 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Manages the currently active tracking provider (e.g. Trakt vs SIMKL).
 */
class ProviderManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        const val PROVIDER_TRAKT = "trakt"
        const val PROVIDER_SIMKL = "simkl"
        private val ACTIVE_PROVIDER = stringPreferencesKey("active_tracking_provider")
        private val SIMKL_LAST_SYNC_DATE = stringPreferencesKey("simkl_last_sync_date")
        private val SIMKL_LAST_ACTIVITY_HASH = stringPreferencesKey("simkl_last_activity_hash")
    }

    val activeProvider: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[ACTIVE_PROVIDER] ?: PROVIDER_TRAKT // Trakt is the default
        }

    val simklLastSyncDate: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[SIMKL_LAST_SYNC_DATE]
        }

    val simklLastActivityHash: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[SIMKL_LAST_ACTIVITY_HASH]
        }

    suspend fun setActiveProvider(providerId: String) {
        dataStore.edit { preferences ->
            preferences[ACTIVE_PROVIDER] = providerId
        }
    }

    suspend fun setSimklLastSyncDate(date: String?) {
        dataStore.edit { preferences ->
            if (date != null) {
                preferences[SIMKL_LAST_SYNC_DATE] = date
            } else {
                preferences.remove(SIMKL_LAST_SYNC_DATE)
            }
        }
    }

    suspend fun setSimklLastActivityHash(hash: String?) {
        dataStore.edit { preferences ->
            if (hash != null) {
                preferences[SIMKL_LAST_ACTIVITY_HASH] = hash
            } else {
                preferences.remove(SIMKL_LAST_ACTIVITY_HASH)
            }
        }
    }
}
