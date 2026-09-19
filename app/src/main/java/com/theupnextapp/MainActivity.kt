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
import android.view.Menu
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import com.theupnextapp.common.utils.TraktConstants
import com.theupnextapp.common.utils.customTab.CustomTabComponent
import com.theupnextapp.common.utils.customTab.TabConnectionCallback
import com.theupnextapp.core.designsystem.ui.theme.UpnextTheme
import com.theupnextapp.database.DatabaseTraktAccess
import com.theupnextapp.database.TraktDao
import com.theupnextapp.repository.SettingsRepository
import com.theupnextapp.ui.main.MainScreen
import com.theupnextapp.ui.onboarding.OnboardingScreen
import com.theupnextapp.ui.onboarding.OnboardingViewModel
import com.theupnextapp.ui.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    @Inject
    lateinit var traktDao: TraktDao

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val authCodeState: MutableState<String?> = mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initialCode = intent?.data?.getQueryParameter("code")
        if (!initialCode.isNullOrEmpty()) {
            authCodeState.value = initialCode
            lifecycleScope.launch(Dispatchers.IO) {
                settingsRepository.setOnboardingCompleted(true)
            }
        }
        if (BuildConfig.DEBUG) {
            handleTestHarness(intent)
        }
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val themeState by settingsViewModel.themeStream.collectAsState()

            val onboardingViewModel: OnboardingViewModel = hiltViewModel()
            val isOnboardingCompleted by onboardingViewModel.isOnboardingCompleted.collectAsState()
            val isTraktConnected by onboardingViewModel.isTraktConnected.collectAsState()

            val shouldShowMainScreen = isOnboardingCompleted == true || authCodeState.value != null

            UpnextTheme(themeState = themeState) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .semantics {
                                testTagsAsResourceId = true
                            },
                ) {
                    when {
                        shouldShowMainScreen -> {
                            MainScreen(
                                valueState = authCodeState,
                                onTraktAuthCompleted = {
                                    authCodeState.value = null
                                },
                            )
                        }
                        isOnboardingCompleted == false -> {
                            OnboardingScreen(
                                onComplete = {
                                    onboardingViewModel.completeOnboarding()
                                },
                                onConnectTrakt = {
                                    onboardingViewModel.completeOnboarding()
                                },
                                isTraktConnected = isTraktConnected,
                            )
                        }
                        else -> {
                            // Loading state — show nothing while DataStore resolves
                        }
                    }
                }
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
        customTabComponent.mayLaunchUrl(Uri.parse(TraktConstants.TRAKT_AUTH_URL), null, null)
    }

    override fun onTabDisconnected() {
        customTabComponent.mayLaunchUrl(null, null, null)
    }

    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val code = intent.data?.getQueryParameter("code")
        if (!code.isNullOrEmpty()) {
            authCodeState.value = code
            lifecycleScope.launch(Dispatchers.IO) {
                settingsRepository.setOnboardingCompleted(true)
            }
        }
        if (BuildConfig.DEBUG) {
            handleTestHarness(intent)
        }
    }

    private fun handleTestHarness(intent: Intent?) {
        if (intent == null) return
        val isMockAuth =
            intent.getBooleanExtra("mock_trakt_auth", false) ||
                intent.getStringExtra("mock_trakt_auth")?.toBoolean() == true ||
                intent.data?.getQueryParameter("code")?.startsWith("mock_") == true
        val isClearAuth =
            intent.getBooleanExtra("clear_auth_state", false) ||
                intent.getStringExtra("clear_auth_state")?.toBoolean() == true
        val isBypassOnboarding =
            intent.getBooleanExtra("bypass_onboarding", false) ||
                intent.getStringExtra("bypass_onboarding")?.toBoolean() == true

        if (isMockAuth) {
            lifecycleScope.launch(Dispatchers.IO) {
                traktDao.insertAllTraktAccessData(
                    DatabaseTraktAccess(
                        id = 1,
                        access_token = "mock_test_token",
                        created_at = System.currentTimeMillis() / 1000,
                        expires_in = 7776000L,
                        refresh_token = "mock_refresh_token",
                        scope = "public",
                        token_type = "bearer",
                    ),
                )
                settingsRepository.setOnboardingCompleted(true)
            }
        } else if (isClearAuth) {
            lifecycleScope.launch(Dispatchers.IO) {
                traktDao.deleteTraktAccessData()
                settingsRepository.setOnboardingCompleted(false)
            }
        } else if (isBypassOnboarding) {
            lifecycleScope.launch(Dispatchers.IO) {
                settingsRepository.setOnboardingCompleted(true)
            }
        }
    }

    companion object {
        const val REQUEST_CODE_INTERNET = 10
    }
}
