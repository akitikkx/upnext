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

package com.theupnextapp.common.utils.customTab

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mock
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.never
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

    @Test
    fun unBindCustomTabService_whenConnectionIsNull_shouldNotCallUnbindService() {
        customTabComponent.unBindCustomTabService(activity)
        verify(activity, never()).unbindService(any())
    }

    @Test
    fun unBindCustomTabService_whenConnectionIsNotNull_shouldCallUnbindService() {
        val mockConnection = org.mockito.Mockito.mock(androidx.browser.customtabs.CustomTabsServiceConnection::class.java)
        setPrivateField(customTabComponent, "tabServiceConnection", mockConnection)

        customTabComponent.unBindCustomTabService(activity)

        verify(activity).unbindService(mockConnection)
    }

    @Test
    fun unBindCustomTabService_whenUnbindThrowsIllegalArgumentException_shouldHandleGracefully() {
        val mockConnection = org.mockito.Mockito.mock(androidx.browser.customtabs.CustomTabsServiceConnection::class.java)
        setPrivateField(customTabComponent, "tabServiceConnection", mockConnection)

        doThrow(IllegalArgumentException("Service not registered")).`when`(activity).unbindService(mockConnection)

        // Should not crash
        customTabComponent.unBindCustomTabService(activity)

        verify(activity).unbindService(mockConnection)
    }

    @Test
    fun onTabServiceConnected_shouldWarmupClientAndNotifyCallback() {
        val mockClient = org.mockito.Mockito.mock(CustomTabsClient::class.java)
        val mockCallback = org.mockito.Mockito.mock(TabConnectionCallback::class.java)
        customTabComponent.setConnectionCallback(mockCallback)

        customTabComponent.onTabServiceConnected(mockClient)

        verify(mockClient).warmup(0L)
        verify(mockCallback).onTabConnected()
    }

    @Test
    fun onTabServiceDisconnected_shouldClearClientAndSessionAndNotifyCallback() {
        val mockCallback = org.mockito.Mockito.mock(TabConnectionCallback::class.java)
        customTabComponent.setConnectionCallback(mockCallback)

        val mockClient = org.mockito.Mockito.mock(CustomTabsClient::class.java)
        val mockSession = org.mockito.Mockito.mock(CustomTabsSession::class.java)
        setPrivateField(customTabComponent, "client", mockClient)
        setPrivateField(customTabComponent, "customTabSession", mockSession)

        customTabComponent.onTabServiceDisconnected()

        verify(mockCallback).onTabDisconnected()
        assertNull(customTabComponent.getSession())
    }

    @Test
    fun getSession_whenClientIsNull_shouldReturnNull() {
        setPrivateField(customTabComponent, "client", null)
        assertNull(customTabComponent.getSession())
    }

    @Test
    fun getSession_whenClientIsNotNullAndSessionIsNull_shouldCreateNewSession() {
        val mockClient = org.mockito.Mockito.mock(CustomTabsClient::class.java)
        val mockSession = org.mockito.Mockito.mock(CustomTabsSession::class.java)
        `when`(mockClient.newSession(null)).thenReturn(mockSession)
        setPrivateField(customTabComponent, "client", mockClient)
        setPrivateField(customTabComponent, "customTabSession", null)

        val session = customTabComponent.getSession()

        assertNotNull(session)
        assertEquals(mockSession, session)
        verify(mockClient).newSession(null)
    }

    @Test
    fun getSession_whenClientIsNotNullAndSessionIsNotNull_shouldReturnExistingSession() {
        val mockClient = org.mockito.Mockito.mock(CustomTabsClient::class.java)
        val mockSession = org.mockito.Mockito.mock(CustomTabsSession::class.java)
        setPrivateField(customTabComponent, "client", mockClient)
        setPrivateField(customTabComponent, "customTabSession", mockSession)

        val session = customTabComponent.getSession()

        assertEquals(mockSession, session)
        verify(mockClient, never()).newSession(any())
    }

    @Test
    fun mayLaunchUrl_whenClientIsNull_shouldReturnFalse() {
        setPrivateField(customTabComponent, "client", null)
        val uri = Uri.parse("https://trakt.tv")
        val result = customTabComponent.mayLaunchUrl(uri, null, null)
        assertFalse(result)
    }

    @Test
    fun mayLaunchUrl_whenClientIsNotNull_shouldDelegateToSession() {
        val mockClient = org.mockito.Mockito.mock(CustomTabsClient::class.java)
        val mockSession = org.mockito.Mockito.mock(CustomTabsSession::class.java)
        val uri = Uri.parse("https://trakt.tv")
        val bundle = Bundle()
        val likelyBundles = listOf(Bundle())

        `when`(mockClient.newSession(null)).thenReturn(mockSession)
        `when`(mockSession.mayLaunchUrl(uri, bundle, likelyBundles)).thenReturn(true)

        setPrivateField(customTabComponent, "client", mockClient)
        setPrivateField(customTabComponent, "customTabSession", mockSession)

        val result = customTabComponent.mayLaunchUrl(uri, bundle, likelyBundles)

        assertTrue(result)
        verify(mockSession).mayLaunchUrl(uri, bundle, likelyBundles)
    }

    private fun setPrivateField(obj: Any, fieldName: String, value: Any?) {
        val field = obj.javaClass.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(obj, value)
    }
}
