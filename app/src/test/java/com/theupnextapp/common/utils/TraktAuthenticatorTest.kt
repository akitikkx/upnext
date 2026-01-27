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

package com.theupnextapp.common.utils

import com.theupnextapp.database.DatabaseTraktAccess
import com.theupnextapp.database.fakes.FakeTraktDao
import com.theupnextapp.network.fakes.FakeTraktAuthApi
import com.theupnextapp.network.models.trakt.NetworkTraktAccessRefreshTokenResponse
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class TraktAuthenticatorTest {
    private lateinit var fakeTraktAuthApi: FakeTraktAuthApi
    private lateinit var fakeTraktDao: FakeTraktDao
    private lateinit var authenticator: TraktAuthenticator

    @Before
    fun setup() {
        fakeTraktAuthApi = FakeTraktAuthApi()
        fakeTraktDao = FakeTraktDao()
        authenticator = TraktAuthenticator(fakeTraktAuthApi, fakeTraktDao)
    }

    @Test
    fun `authenticate returns null when no token in db`() {
        fakeTraktDao.currentToken = null

        val request = Request.Builder().url("https://api.trakt.tv/").build()
        val response =
            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(401)
                .message("Unauthorized")
                .build()

        val result = authenticator.authenticate(null, response)

        assertNull(result)
    }

    @Test
    fun `authenticate returns request with new token when refresh succeeds`() { // runBlocking inside authenticator
        // Setup
        val oldToken = DatabaseTraktAccess(0, "old_token", 0L, 0L, "refresh_token", "scope", "Bearer")
        fakeTraktDao.currentToken = oldToken

        val request = Request.Builder().url("https://api.trakt.tv/").header("Authorization", "Bearer old_token").build()
        val response =
            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(401)
                .message("Unauthorized")
                .build()

        val refreshResponse =
            NetworkTraktAccessRefreshTokenResponse("new_token", "Bearer", 3600, "new_refresh", "scope", 12345)
        fakeTraktAuthApi.responseToReturn = refreshResponse

        // Execute
        val result = authenticator.authenticate(null, response)

        // Verify
        assertEquals("Bearer new_token", result?.header("Authorization"))

        // Verify DB update
        assertEquals("new_token", fakeTraktDao.currentToken?.access_token)
        assertEquals("new_refresh", fakeTraktDao.currentToken?.refresh_token)
    }

    @Test
    fun `authenticate deletes token when refresh fails`() {
        // Setup
        val oldToken = DatabaseTraktAccess(0, "old_token", 0L, 0L, "refresh_token", "scope", "Bearer")
        fakeTraktDao.currentToken = oldToken

        val request = Request.Builder().url("https://api.trakt.tv/").header("Authorization", "Bearer old_token").build()
        val response =
            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(401)
                .message("Unauthorized")
                .build()

        fakeTraktAuthApi.shouldThrow = true

        // Execute
        val result = authenticator.authenticate(null, response)

        // Verify
        assertNull(result)
        assertNull(fakeTraktDao.currentToken) // Should be deleted
    }
}
