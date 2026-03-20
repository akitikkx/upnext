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
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.theupnextapp.domain.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    private val THEME_STATE = stringPreferencesKey("theme_state")
    private val DATA_SAVER_ENABLED = booleanPreferencesKey("data_saver_enabled")
    private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")

    override val areNotificationsEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[NOTIFICATIONS_ENABLED] ?: true // Default to true
        }

    override val themeStream: Flow<Theme> = dataStore.data
        .map { preferences ->
            val themeName = preferences[THEME_STATE]
            if (themeName != null) {
                try {
                    Theme.valueOf(themeName)
                } catch (e: IllegalArgumentException) {
                    Theme.SYSTEM
                }
            } else {
                Theme.SYSTEM
            }
        }

    override val dataSaverStream: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[DATA_SAVER_ENABLED] ?: false // Default to false
        }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    override suspend fun setTheme(theme: Theme) {
        dataStore.edit { preferences ->
            preferences[THEME_STATE] = theme.name
        }
    }

    override suspend fun setDataSaverEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DATA_SAVER_ENABLED] = enabled
        }
    }

    override val isOnboardingCompleted: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[ONBOARDING_COMPLETED] ?: false
        }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }
}
