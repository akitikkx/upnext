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

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class TvMazeRateLimitInterceptorTest {
    private val chain: Interceptor.Chain = mock()
    private val sleepCalls = mutableListOf<Long>()
    private lateinit var interceptor: TvMazeRateLimitInterceptor

    @Before
    fun setup() {
        sleepCalls.clear()
        interceptor = TvMazeRateLimitInterceptor(sleeper = { sleepCalls.add(it) })
    }

    private fun create200Response(request: Request): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body("{}".toResponseBody("application/json".toMediaType()))
            .build()
    }

    private fun create429Response(
        request: Request,
        retryAfterHeader: String? = null,
    ): Response {
        val builder =
            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(429)
                .message("Too Many Requests")
                .body("{}".toResponseBody("application/json".toMediaType()))

        if (retryAfterHeader != null) {
            builder.addHeader("Retry-After", retryAfterHeader)
        }
        return builder.build()
    }

    @Test
    fun `GET request proceeds without throttling when no cooldown active`() {
        val request = Request.Builder().url("https://api.tvmaze.com/shows/1").build()
        whenever(chain.request()).thenReturn(request)
        whenever(chain.proceed(any())).thenReturn(create200Response(request))

        val response = interceptor.intercept(chain)

        assertEquals(200, response.code)
        assertTrue(sleepCalls.isEmpty())
        verify(chain, times(1)).proceed(request)
    }

    @Test
    fun `429 Too Many Requests response triggers retry after Retry-After header duration`() {
        val request = Request.Builder().url("https://api.tvmaze.com/shows/1").build()
        whenever(chain.request()).thenReturn(request)
        whenever(chain.proceed(any()))
            .thenReturn(create429Response(request, retryAfterHeader = "3"))
            .thenReturn(create200Response(request))

        val response = interceptor.intercept(chain)

        assertEquals(200, response.code)
        verify(chain, times(2)).proceed(request)
        assertEquals(1, sleepCalls.size)
        assertEquals(3000L, sleepCalls.first())
    }

    @Test
    fun `429 with missing Retry-After header defaults to 2 seconds wait`() {
        val request = Request.Builder().url("https://api.tvmaze.com/shows/1").build()
        whenever(chain.request()).thenReturn(request)
        whenever(chain.proceed(any()))
            .thenReturn(create429Response(request, retryAfterHeader = null))
            .thenReturn(create200Response(request))

        val response = interceptor.intercept(chain)

        assertEquals(200, response.code)
        verify(chain, times(2)).proceed(request)
        assertEquals(1, sleepCalls.size)
        assertEquals(2000L, sleepCalls.first())
    }

    @Test
    fun `429 clamps Retry-After delay to minimum and maximum bounds`() {
        val request = Request.Builder().url("https://api.tvmaze.com/shows/1").build()
        whenever(chain.request()).thenReturn(request)
        whenever(chain.proceed(any()))
            .thenReturn(create429Response(request, retryAfterHeader = "30")) // Exceeds 10s max
            .thenReturn(create200Response(request))

        val response = interceptor.intercept(chain)

        assertEquals(200, response.code)
        assertEquals(1, sleepCalls.size)
        assertEquals(10000L, sleepCalls.first())
    }
}
