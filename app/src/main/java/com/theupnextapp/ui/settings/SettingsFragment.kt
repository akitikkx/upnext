/*
 * MIT License
 *
 * Copyright (c) 2022 Ahmed Tikiwa
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

package com.theupnextapp.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat
import com.theupnextapp.MainActivity
import com.theupnextapp.R

@Deprecated("Will be removed once Jetpack Navigation work has been completed")
class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    @Deprecated("Deprecated in Java")
    override fun onPrepareOptionsMenu(menu: Menu) {
        val settingsItem = menu.findItem(R.id.menu_settings)
        if (settingsItem != null) {
            settingsItem.isVisible = false
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.title_settings)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).hideBottomNavigation()
    }

    override fun onDestroy() {
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        (activity as MainActivity).showBottomNavigation()
    }

    override fun onStop() {
        super.onStop()
        (activity as MainActivity).showBottomNavigation()
    }

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
        val darkModeSelection = getString(R.string.dark_mode)
        p1?.let {
            if (it == darkModeSelection) p0?.let { sharedPreferences ->
                val darkModeOptions = resources.getStringArray(R.array.dark_mode_values)

                when (sharedPreferences.getString(darkModeSelection, darkModeOptions[0])) {
                    darkModeOptions[0] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    darkModeOptions[1] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    darkModeOptions[2] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    darkModeOptions[3] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                }
            }
        }
    }

}