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
import android.os.Bundle
import android.view.Menu
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.core.util.Consumer
import com.theupnextapp.common.utils.customTab.CustomTabComponent
import com.theupnextapp.common.utils.customTab.TabConnectionCallback
import com.theupnextapp.ui.main.MainScreen
import com.theupnextapp.ui.theme.UpnextTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@ExperimentalMaterial3WindowSizeClassApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity(), TabConnectionCallback {

    @Inject
    lateinit var customTabComponent: CustomTabComponent

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
                MainScreen(
                    widthSizeClass = calculateWindowSizeClass(activity = this).widthSizeClass,
                    valueState = dataString
                )
            }
        }

        customTabComponent.setConnectionCallback(this)
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
        customTabComponent.setConnectionCallback(null)
    }

    override fun onTabConnected() {
        customTabComponent.mayLaunchUrl(Uri.parse(TRAKT_AUTH_URL), null, null)
    }

    override fun onTabDisconnected() {
        customTabComponent.mayLaunchUrl(null, null, null)
    }

    companion object {
        const val TRAKT_AUTH_URL =
            "https://trakt.tv/oauth/authorize?response_type=code&client_id=${BuildConfig.TRAKT_CLIENT_ID}&redirect_uri=${BuildConfig.TRAKT_REDIRECT_URI}"
        const val REQUEST_CODE_INTERNET = 10
    }
}
