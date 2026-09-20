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

package com.theupnextapp.common.utils

import com.theupnextapp.core.data.BuildConfig
import com.theupnextapp.database.TraktDao
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import javax.inject.Inject

class TraktAuthInterceptor
@Inject
constructor(
    private val traktDao: TraktDao,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath
        val token = traktDao.getTraktAccessDataRaw()
        val accessToken = token?.access_token
        val authHeader = originalRequest.header("Authorization")

        // In DEBUG mode with a test harness mock token, short-circuit private Trakt endpoints
        // with mock 200 JSON responses so automated E2E tests do not transmit invalid fake tokens
        // to live Trakt servers (which would trigger 401 and purge the test session).
        val isMock =
            BuildConfig.DEBUG &&
                (accessToken?.startsWith("mock_") == true || authHeader?.contains("mock_") == true)

        if (isMock) {
            if (isPrivateEndpoint(path)) {
                return createMockResponse(originalRequest, path)
            }
            // For public endpoints during mock tests, do not attach the fake token.
            return chain.proceed(
                if (authHeader != null) {
                    originalRequest.newBuilder().removeHeader("Authorization").build()
                } else {
                    originalRequest
                },
            )
        }

        val builder = originalRequest.newBuilder()
        if (!accessToken.isNullOrEmpty() && originalRequest.header("Authorization") == null) {
            builder.header("Authorization", "Bearer $accessToken")
        }

        return chain.proceed(builder.build())
    }

    private fun isPrivateEndpoint(path: String): Boolean {
        val privatePrefixes =
            listOf(
                "/sync",
                "/recommendations",
                "/calendars/my",
                "/checkin",
                "/users",
            )
        return privatePrefixes.any { path.startsWith(it) }
    }

    private fun createMockResponse(
        request: Request,
        path: String,
    ): Response {
        val json =
            when {
                path.startsWith("/users/settings") -> {
                    """{"user":{"username":"UpnextTester","name":"Tester","vip":false}}"""
                }
                path.startsWith("/calendars/my") ||
                    path.startsWith("/sync") ||
                    path.startsWith("/recommendations") ||
                    path.startsWith("/users") -> {
                    "[]"
                }
                else -> "{}"
            }

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(json.toResponseBody("application/json".toMediaType()))
            .build()
    }
}

