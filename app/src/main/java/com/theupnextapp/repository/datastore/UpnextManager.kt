package com.theupnextapp.repository.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "upnext_prefs")

class UpnextManager constructor(context: Context) {

    private val dataStore = context.dataStore

    private val _versionCode = intPreferencesKey("version_code")
    val versionCodeFlow: Flow<Int?> = dataStore.data.map { preferences ->
        preferences[_versionCode] ?: 0
    }

    suspend fun saveVersionCode(versionCode: Int) {
        dataStore.edit {
            it[_versionCode] = versionCode
        }
    }
    companion object {
        const val SHARED_PREF_NOT_FOUND = -1
    }

}