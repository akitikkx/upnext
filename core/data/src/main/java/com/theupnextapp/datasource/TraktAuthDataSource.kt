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
import com.theupnextapp.core.data.BuildConfig
import com.theupnextapp.database.TraktDao
import com.theupnextapp.database.UpnextDao
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.network.TraktService
import com.theupnextapp.network.TvMazeService
import com.theupnextapp.network.models.trakt.NetworkTraktAccessRefreshTokenRequest
import com.theupnextapp.network.models.trakt.NetworkTraktAccessTokenRequest
import com.theupnextapp.network.models.trakt.NetworkTraktRevokeAccessTokenRequest
import com.theupnextapp.network.models.trakt.asDatabaseModel
import com.theupnextapp.network.models.trakt.asDomainModel
import javax.inject.Inject

open class TraktAuthDataSource
@Inject
constructor(
    private val traktService: TraktService,
    private val traktDao: TraktDao,
    upnextDao: UpnextDao,
    tvMazeService: TvMazeService,
    firebaseCrashlytics: FirebaseCrashlytics,
) : BaseTraktDataSource(upnextDao, tvMazeService, firebaseCrashlytics) {
    suspend fun getAccessToken(code: String): Result<TraktAccessToken> {
        if (code.isEmpty()) {
            val message = "Attempted to get access token with empty code."
            logTraktException(message)
            return Result.failure(IllegalArgumentException(message))
        }

        return safeApiCall {
            val request =
                NetworkTraktAccessTokenRequest(
                    code = code,
                    client_id = BuildConfig.TRAKT_CLIENT_ID,
                    client_secret = BuildConfig.TRAKT_CLIENT_SECRET,
                    redirect_uri = BuildConfig.TRAKT_REDIRECT_URI,
                    grant_type = "authorization_code",
                )
            val response = traktService.getAccessTokenAsync(request).await()
            traktDao.deleteTraktAccessData()
            traktDao.insertAllTraktAccessData(response.asDatabaseModel())
            response.asDomainModel()
        }
    }

    suspend fun revokeAccessToken(token: String): Result<Unit> {
        if (token.isEmpty()) {
            val message = "Attempted to revoke access token with empty token string."
            logTraktException(message)
            return Result.failure(IllegalArgumentException(message))
        }

        return safeApiCall {
            val request =
                NetworkTraktRevokeAccessTokenRequest(
                    client_id = BuildConfig.TRAKT_CLIENT_ID,
                    client_secret = BuildConfig.TRAKT_CLIENT_SECRET,
                    token = token,
                )
            traktService.revokeAccessTokenAsync(request).await()
            traktDao.deleteTraktAccessData() // Clear local token data
            Unit
        }
    }

    suspend fun getAccessRefreshToken(refreshToken: String?): Result<TraktAccessToken> {
        if (refreshToken.isNullOrEmpty()) {
            val message = "Attempted to refresh access token with null or empty refresh token."
            logTraktException(message)
            return Result.failure(IllegalArgumentException(message))
        }

        return safeApiCall {
            val request =
                NetworkTraktAccessRefreshTokenRequest(
                    refresh_token = refreshToken,
                    client_id = BuildConfig.TRAKT_CLIENT_ID,
                    client_secret = BuildConfig.TRAKT_CLIENT_SECRET,
                    redirect_uri = BuildConfig.TRAKT_REDIRECT_URI,
                    grant_type = "refresh_token",
                )
            val response = traktService.getAccessRefreshTokenAsync(request).await()
            traktDao.deleteTraktAccessData()
            traktDao.insertAllTraktAccessData(response.asDatabaseModel())
            response.asDomainModel()
        }
    }
}
