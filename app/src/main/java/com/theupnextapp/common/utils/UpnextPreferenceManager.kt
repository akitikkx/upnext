package com.theupnextapp.common.utils

import android.app.Application
import androidx.preference.PreferenceManager
import com.theupnextapp.R

@Deprecated(message = "SharedPreferences no longer recommended")
class UpnextPreferenceManager(val application: Application) {

    fun getSelectedTheme(): String? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(application)

        return preferences.getString(
            application.getString(R.string.dark_mode),
            application.getString(R.string.dark_mode_yes)
        )
    }
}