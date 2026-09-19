/*
 * MIT License
 *
 * Copyright (c) 2026 Ahmed Tikiwa
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

package com.theupnextapp.common.utils

import com.theupnextapp.database.DatabaseTraktAccess
import com.theupnextapp.database.TraktDao
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class TraktAuthInterceptorTest {
    private val traktDao: TraktDao = mock()
    private val chain: Interceptor.Chain = mock()
    private lateinit var interceptor: TraktAuthInterceptor

    @Before
    fun setup() {
        interceptor = TraktAuthInterceptor(traktDao)
    }

    private fun mockChainResponse(request: Request): Response {
        val dummyResponse =
            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("{}".toResponseBody("application/json".toMediaType()))
                .build()
        whenever(chain.request()).thenReturn(request)
        whenever(chain.proceed(any())).thenAnswer { invocation ->
            val req = invocation.getArgument<Request>(0)
            Response.Builder()
                .request(req)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("{}".toResponseBody("application/json".toMediaType()))
                .build()
        }
        return dummyResponse
    }

    @Test
    fun `intercept when no token in DB proceeds without auth header`() {
        whenever(traktDao.getTraktAccessDataRaw()).thenReturn(null)
        val request = Request.Builder().url("https://api.trakt.tv/shows/trending").build()
        mockChainResponse(request)

        val response = interceptor.intercept(chain)

        assertEquals(200, response.code)
        assertNull(response.request.header("Authorization"))
        verify(chain).proceed(any())
    }

    @Test
    fun `intercept when real token in DB attaches Bearer token`() {
        val realToken =
            DatabaseTraktAccess(
                id = 1,
                access_token = "valid_real_token",
                created_at = 123456L,
                expires_in = 3600L,
                refresh_token = "refresh",
                scope = "public",
                token_type = "bearer",
            )
        whenever(traktDao.getTraktAccessDataRaw()).thenReturn(realToken)
        val request = Request.Builder().url("https://api.trakt.tv/shows/trending").build()
        mockChainResponse(request)

        val response = interceptor.intercept(chain)

        assertEquals(200, response.code)
        assertEquals("Bearer valid_real_token", response.request.header("Authorization"))
        verify(chain).proceed(any())
    }

    @Test
    fun `intercept when mock token in DB and private endpoint returns mock response without calling network`() {
        val mockToken =
            DatabaseTraktAccess(
                id = 1,
                access_token = "mock_test_token",
                created_at = 123456L,
                expires_in = 3600L,
                refresh_token = "mock_refresh",
                scope = "public",
                token_type = "bearer",
            )
        whenever(traktDao.getTraktAccessDataRaw()).thenReturn(mockToken)
        val request = Request.Builder().url("https://api.trakt.tv/calendars/my/shows").build()
        whenever(chain.request()).thenReturn(request)

        val response = interceptor.intercept(chain)

        assertEquals(200, response.code)
        assertEquals("[]", response.body?.string())
        verify(chain, never()).proceed(any())
    }

    @Test
    fun `intercept when mock token in DB and user settings endpoint returns mock user settings`() {
        val mockToken =
            DatabaseTraktAccess(
                id = 1,
                access_token = "mock_test_token",
                created_at = 123456L,
                expires_in = 3600L,
                refresh_token = "mock_refresh",
                scope = "public",
                token_type = "bearer",
            )
        whenever(traktDao.getTraktAccessDataRaw()).thenReturn(mockToken)
        val request = Request.Builder().url("https://api.trakt.tv/users/settings").build()
        whenever(chain.request()).thenReturn(request)

        val response = interceptor.intercept(chain)

        assertEquals(200, response.code)
        val body = response.body?.string()
        assertEquals(true, body?.contains("UpnextTester"))
        verify(chain, never()).proceed(any())
    }

    @Test
    fun `intercept when mock token in DB and public endpoint proceeds without attaching mock header`() {
        val mockToken =
            DatabaseTraktAccess(
                id = 1,
                access_token = "mock_test_token",
                created_at = 123456L,
                expires_in = 3600L,
                refresh_token = "mock_refresh",
                scope = "public",
                token_type = "bearer",
            )
        whenever(traktDao.getTraktAccessDataRaw()).thenReturn(mockToken)
        val request = Request.Builder().url("https://api.trakt.tv/shows/trending").build()
        mockChainResponse(request)

        val response = interceptor.intercept(chain)

        assertEquals(200, response.code)
        assertNull(response.request.header("Authorization"))
        verify(chain).proceed(request)
    }
}
