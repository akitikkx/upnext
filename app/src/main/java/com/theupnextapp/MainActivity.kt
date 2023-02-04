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

package com.theupnextapp

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.View
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.Consumer
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.theupnextapp.common.utils.FeedBackStatus
import com.theupnextapp.common.utils.Feedback
import com.theupnextapp.common.utils.customTab.CustomTabComponent
import com.theupnextapp.common.utils.customTab.TabConnectionCallback
import com.theupnextapp.ui.main.MainScreen
import com.theupnextapp.ui.theme.UpnextTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), TabConnectionCallback {

    private val navController: NavController
        get() = findNavController(R.id.nav_host_fragment)

    private var _bottomNavigationView: BottomNavigationView? = null
    private val bottomNavigationView get() = _bottomNavigationView

    private var _container: ConstraintLayout? = null
    private val container get() = _container

    private lateinit var snackbar: Snackbar

    @Inject
    lateinit var customTabComponent: CustomTabComponent

    @ExperimentalAnimationApi
    @ExperimentalFoundationApi
    @ExperimentalComposeUiApi
    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val dataString: MutableState<String?> = rememberSaveable { mutableStateOf("") }

            DisposableEffect(Unit) {
                val listener = Consumer<Intent> {
                    val code = it.data?.getQueryParameter("code")
                    dataString.value = code
                }
                addOnNewIntentListener(listener)
                onDispose { removeOnNewIntentListener(listener) }
            }

            UpnextTheme {
                MainScreen(dataString)
            }
        }

        customTabComponent.setConnectionCallback(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onStart() {
        super.onStart()
        customTabComponent.bindCustomService(this)
    }

    override fun onStop() {
        super.onStop()
        customTabComponent.unBindCustomTabService(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        _bottomNavigationView = null
        _container = null
        customTabComponent.setConnectionCallback(null)
    }

    override fun onTabConnected() {
        customTabComponent.mayLaunchUrl(Uri.parse(TRAKT_AUTH_URL), null, null)
    }

    override fun onTabDisconnected() {
        customTabComponent.mayLaunchUrl(null, null, null)
    }

    fun hideBottomNavigation() {
        if (bottomNavigationView != null) {
            if (bottomNavigationView?.visibility == View.VISIBLE) {
                bottomNavigationView?.visibility = View.GONE
            }
        }
    }

    fun showBottomNavigation() {
        if (bottomNavigationView != null) {
            if (bottomNavigationView?.visibility == View.GONE) {
                bottomNavigationView?.visibility = View.VISIBLE
            }
        }
    }

    fun displayConnectionErrorMessage() {
        // Network settings dialog only available from Q and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            container?.let {
                Feedback(this).showSnackBar(
                    view = it,
                    type = FeedBackStatus.NO_CONNECTION,
                    duration = Snackbar.LENGTH_INDEFINITE,
                    listener = { showNetworkSettings() }
                )
            }
        } else {
            container?.let {
                Feedback(this).showSnackBar(
                    view = it,
                    type = FeedBackStatus.NO_CONNECTION,
                    duration = Snackbar.LENGTH_INDEFINITE,
                    listener = null
                )
            }
        }
    }

    fun hideConnectionErrorMessage() {
        if (::snackbar.isInitialized) {
            if (snackbar.isShown) {
                snackbar.dismiss()
            }
        }
    }

    private fun showNetworkSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val intent = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
            startActivityForResult(intent, REQUEST_CODE_INTERNET)
        }
    }

    companion object {
        const val TRAKT_AUTH_URL =
            "https://trakt.tv/oauth/authorize?response_type=code&client_id=${BuildConfig.TRAKT_CLIENT_ID}&redirect_uri=${BuildConfig.TRAKT_REDIRECT_URI}"
        const val REQUEST_CODE_INTERNET = 10
    }
}
