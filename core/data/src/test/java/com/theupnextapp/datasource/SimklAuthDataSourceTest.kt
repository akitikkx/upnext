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
import com.theupnextapp.database.SimklDao
import com.theupnextapp.database.UpnextDao
import com.theupnextapp.network.SimklService
import com.theupnextapp.network.TvMazeService
import com.theupnextapp.network.models.simkl.NetworkSimklAccessTokenRequest
import com.theupnextapp.network.models.simkl.NetworkSimklAccessTokenResponse
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

class SimklAuthDataSourceTest {

    private lateinit var simklService: SimklService
    private lateinit var simklDao: SimklDao
    private lateinit var upnextDao: UpnextDao
    private lateinit var tvMazeService: TvMazeService
    private lateinit var firebaseCrashlytics: FirebaseCrashlytics

    private lateinit var simklAuthDataSource: SimklAuthDataSource

    @Before
    fun setup() {
        simklService = mock()
        simklDao = mock()
        upnextDao = mock()
        tvMazeService = mock()
        firebaseCrashlytics = mock()

        simklAuthDataSource = SimklAuthDataSource(
            simklService = simklService,
            simklDao = simklDao,
            upnextDao = upnextDao,
            tvMazeService = tvMazeService,
            firebaseCrashlytics = firebaseCrashlytics
        )
    }

    @Test
    fun `getAccessToken returns token on success and updates db`() = runTest {
        val code = "test_code"
        val response = NetworkSimklAccessTokenResponse(
            accessToken = "access_token",
            tokenType = "bearer",
            scope = "public"
        )

        whenever(simklService.getAccessTokenAsync(any<NetworkSimklAccessTokenRequest>()))
            .thenReturn(CompletableDeferred(response))

        val result = simklAuthDataSource.getAccessToken(code)

        assertTrue(result.isSuccess)
        val domainToken = result.getOrThrow()
        assertEquals("access_token", domainToken.accessToken)

        verify(simklDao).deleteSimklAccessData()
        verify(simklDao).insertAllSimklAccessData(any())
    }

    @Test
    fun `getAccessToken fails when code is empty`() = runTest {
        val result = simklAuthDataSource.getAccessToken("")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }
}
