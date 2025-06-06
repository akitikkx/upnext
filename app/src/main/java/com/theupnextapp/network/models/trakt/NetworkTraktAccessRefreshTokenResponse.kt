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

package com.theupnextapp.network.models.trakt

import com.theupnextapp.database.DatabaseTraktAccess
import com.theupnextapp.domain.TraktAccessToken

data class NetworkTraktAccessRefreshTokenResponse(
    val access_token: String?,
    val token_type: String?,
    val expires_in: Long?,
    val refresh_token: String?,
    val scope: String?,
    val created_at: Long?
)

fun NetworkTraktAccessRefreshTokenResponse.asDatabaseModel(): DatabaseTraktAccess {
    return DatabaseTraktAccess(
        id = 1,
        access_token = access_token,
        token_type = token_type,
        expires_in = expires_in,
        refresh_token = refresh_token,
        scope = scope,
        created_at = created_at
    )
}

fun NetworkTraktAccessRefreshTokenResponse.asDomainModel(): TraktAccessToken {
    return TraktAccessToken(
        access_token = access_token,
        token_type = token_type,
        expires_in = expires_in,
        refresh_token = refresh_token,
        scope = scope,
        created_at = created_at
    )
}
