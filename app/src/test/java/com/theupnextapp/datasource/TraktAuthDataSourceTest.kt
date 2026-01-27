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

package com.theupnextapp.datasource

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.database.TraktDao
import com.theupnextapp.database.UpnextDao
import com.theupnextapp.network.TraktService
import com.theupnextapp.network.TvMazeService
import com.theupnextapp.network.models.trakt.NetworkTraktAccessTokenResponse
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class TraktAuthDataSourceTest {
    private val traktService: TraktService = mock()
    private val traktDao: TraktDao = mock()
    private val upnextDao: UpnextDao = mock()
    private val tvMazeService: TvMazeService = mock()
    private val firebaseCrashlytics: FirebaseCrashlytics = mock()

    private lateinit var dataSource: TraktAuthDataSource

    @Before
    fun setup() {
        dataSource =
            TraktAuthDataSource(
                traktService,
                traktDao,
                upnextDao,
                tvMazeService,
                firebaseCrashlytics,
            )
    }

    @Test
    fun getAccessToken_success() =
        runBlocking {
            val mockResponse =
                NetworkTraktAccessTokenResponse(
                    access_token = "token",
                    token_type = "bearer",
                    expires_in = 1234,
                    refresh_token = "refresh",
                    scope = "public",
                    created_at = 1234,
                )
            whenever(traktService.getAccessTokenAsync(any())).thenReturn(
                CompletableDeferred(mockResponse),
            )

            val result = dataSource.getAccessToken("code")

            assertTrue(result.isSuccess)
            assertEquals("token", result.getOrNull()?.access_token)
            verify(traktDao).deleteTraktAccessData()
            verify(traktDao).insertAllTraktAccessData(any())
        }
}
