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
import okhttp3.Response
import timber.log.Timber
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TvMazeRateLimitInterceptor(
    private val sleeper: (Long) -> Unit = { Thread.sleep(it) },
) : Interceptor {

    @Inject
    constructor() : this(sleeper = { Thread.sleep(it) })

    private val cooldownUntilMillis = AtomicLong(0L)

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        pauseIfInCooldown()

        var response = chain.proceed(request)

        if (response.code == HTTP_TOO_MANY_REQUESTS) {
            val retryAfterHeader = response.header("Retry-After")
            val retryAfterSeconds = retryAfterHeader?.toLongOrNull() ?: DEFAULT_RETRY_AFTER_SECONDS
            val waitMillis = (retryAfterSeconds * 1000L).coerceIn(MIN_RETRY_DELAY_MS, MAX_RETRY_DELAY_MS)
            cooldownUntilMillis.set(System.currentTimeMillis() + waitMillis)

            Timber.w(
                "TVMaze API returned 429 Too Many Requests for %s. Retrying after %d ms (Retry-After: %s)",
                request.url,
                waitMillis,
                retryAfterHeader,
            )

            response.close()

            try {
                sleeper(waitMillis)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            }

            response = chain.proceed(request)
        }

        return response
    }

    private fun pauseIfInCooldown() {
        val cooldownUntil = cooldownUntilMillis.get()
        val now = System.currentTimeMillis()
        if (now < cooldownUntil) {
            val waitTime = cooldownUntil - now
            if (waitTime in 1..MAX_PRE_REQUEST_WAIT_MS) {
                try {
                    sleeper(waitTime)
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            }
        }
    }

    companion object {
        const val HTTP_TOO_MANY_REQUESTS = 429
        const val DEFAULT_RETRY_AFTER_SECONDS = 2L
        const val MIN_RETRY_DELAY_MS = 1000L
        const val MAX_RETRY_DELAY_MS = 10000L
        const val MAX_PRE_REQUEST_WAIT_MS = 5000L
    }
}
