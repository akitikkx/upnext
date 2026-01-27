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
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.common.utils.customTab

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE, application = Application::class)
class CustomTabComponentTest {
    @Mock
    private lateinit var activity: Activity

    @Mock
    private lateinit var packageManager: PackageManager

    @Mock
    private lateinit var customTabFallback: CustomTabFallback

    private lateinit var customTabComponent: CustomTabComponent
    private lateinit var customTabsIntent: CustomTabsIntent

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        customTabComponent = CustomTabComponent()
        `when`(activity.packageManager).thenReturn(packageManager)
        customTabsIntent = CustomTabsIntent.Builder().build()
    }

    @Test
    fun openCustomTab_whenChromeIsAvailable_shouldLaunchCustomTab() {
        val uri = Uri.parse("https://trakt.tv")
        val resolveInfo =
            ResolveInfo().apply {
                activityInfo =
                    ActivityInfo().apply {
                        packageName = "com.android.chrome"
                    }
            }

        `when`(packageManager.resolveActivity(any(Intent::class.java), anyInt())).thenReturn(resolveInfo)
        `when`(packageManager.queryIntentActivities(any(Intent::class.java), anyInt())).thenReturn(listOf(resolveInfo))
        `when`(packageManager.resolveService(any(Intent::class.java), anyInt())).thenReturn(resolveInfo)

        customTabComponent.openCustomTab(activity, customTabsIntent, uri, customTabFallback)

        val intentCaptor = ArgumentCaptor.forClass(Intent::class.java)
        verify(activity).startActivity(intentCaptor.capture(), any())

        val capturedIntent = intentCaptor.value
        assertEquals("com.android.chrome", capturedIntent.`package`)
        assertEquals(uri, capturedIntent.data)
    }

    @Test
    fun openCustomTab_whenChromeIsNotAvailable_shouldUseFallback() {
        val uri = Uri.parse("https://trakt.tv")

        `when`(packageManager.resolveActivity(any(Intent::class.java), anyInt())).thenReturn(null)
        `when`(packageManager.queryIntentActivities(any(Intent::class.java), anyInt())).thenReturn(emptyList())

        customTabComponent.openCustomTab(activity, customTabsIntent, uri, customTabFallback)

        verify(customTabFallback).openUri(activity, uri)
    }
}
