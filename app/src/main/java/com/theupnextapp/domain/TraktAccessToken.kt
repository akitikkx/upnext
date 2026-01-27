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

package com.theupnextapp.domain

import java.util.concurrent.TimeUnit

data class TraktAccessToken(
    val access_token: String?,
    val created_at: Long?,
    val expires_in: Long?,
    val refresh_token: String?,
    val scope: String?,
    val token_type: String?,
)

/**
 * Check if the Trakt access token is still valid
 */
fun TraktAccessToken.isTraktAccessTokenValid(): Boolean {
    var isValid = false
    val currentDateEpoch = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
    val expiryDateEpoch =
        expires_in?.let { created_at?.plus(it) }

    if (expiryDateEpoch != null) {
        if (expiryDateEpoch > currentDateEpoch) {
            isValid = true
        }
    }
    return isValid
}
