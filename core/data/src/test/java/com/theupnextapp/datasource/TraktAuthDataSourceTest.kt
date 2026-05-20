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
import com.theupnextapp.network.models.trakt.NetworkTraktAccessRefreshTokenRequest
import com.theupnextapp.network.models.trakt.NetworkTraktAccessRefreshTokenResponse
import com.theupnextapp.network.models.trakt.NetworkTraktAccessTokenRequest
import com.theupnextapp.network.models.trakt.NetworkTraktAccessTokenResponse
import com.theupnextapp.network.models.trakt.NetworkTraktRevokeAccessTokenRequest
import com.theupnextapp.network.models.trakt.NetworkTraktRevokeAccessTokenResponse
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class TraktAuthDataSourceTest {

    private lateinit var traktService: TraktService
    private lateinit var traktDao: TraktDao
    private lateinit var upnextDao: UpnextDao
    private lateinit var tvMazeService: TvMazeService
    private lateinit var tmdbService: com.theupnextapp.network.TmdbService
    private lateinit var firebaseCrashlytics: FirebaseCrashlytics

    private lateinit var traktAuthDataSource: TraktAuthDataSource

    @Before
    fun setup() {
        traktService = mock()
        traktDao = mock()
        upnextDao = mock()
        tvMazeService = mock()
        tmdbService = mock()
        firebaseCrashlytics = mock()

        traktAuthDataSource = TraktAuthDataSource(
            traktService = traktService,
            traktDao = traktDao,
            upnextDao = upnextDao,
            tvMazeService = tvMazeService,
            tmdbService = tmdbService,
            firebaseCrashlytics = firebaseCrashlytics
        )
    }

    @Test
    fun `getAccessToken returns token on success and updates db`() = runTest {
        val code = "test_code"
        val response = NetworkTraktAccessTokenResponse(
            access_token = "access_token",
            created_at = 123456789,
            expires_in = 7200,
            refresh_token = "refresh_token",
            scope = "public",
            token_type = "bearer"
        )

        whenever(traktService.getAccessTokenAsync(any<NetworkTraktAccessTokenRequest>()))
            .thenReturn(CompletableDeferred(response))

        val result = traktAuthDataSource.getAccessToken(code)

        assertTrue(result.isSuccess)
        val domainToken = result.getOrThrow()
        assertEquals("access_token", domainToken.access_token)

        verify(traktDao).deleteTraktAccessData()
        verify(traktDao).insertAllTraktAccessData(any())
    }

    @Test
    fun `getAccessToken fails when code is empty`() = runTest {
        val result = traktAuthDataSource.getAccessToken("")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `revokeAccessToken succeeds and clears db`() = runTest {
        val token = "test_token"

        whenever(traktService.revokeAccessTokenAsync(any<NetworkTraktRevokeAccessTokenRequest>()))
            .thenReturn(CompletableDeferred(NetworkTraktRevokeAccessTokenResponse()))

        val result = traktAuthDataSource.revokeAccessToken(token)

        assertTrue(result.isSuccess)
        verify(traktDao).deleteTraktAccessData()
    }

    @Test
    fun `getAccessRefreshToken returns new token on success and updates db`() = runTest {
        val refreshToken = "refresh_token"
        val response = NetworkTraktAccessRefreshTokenResponse(
            access_token = "new_access_token",
            created_at = 123456789,
            expires_in = 7200,
            refresh_token = "new_refresh_token",
            scope = "public",
            token_type = "bearer"
        )

        whenever(traktService.getAccessRefreshTokenAsync(any<NetworkTraktAccessRefreshTokenRequest>()))
            .thenReturn(CompletableDeferred(response))

        val result = traktAuthDataSource.getAccessRefreshToken(refreshToken)

        assertTrue(result.isSuccess)
        val domainToken = result.getOrThrow()
        assertEquals("new_access_token", domainToken.access_token)

        verify(traktDao).deleteTraktAccessData()
        verify(traktDao).insertAllTraktAccessData(any())
    }
}
