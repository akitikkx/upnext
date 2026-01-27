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

import com.theupnextapp.BuildConfig
import com.theupnextapp.database.DatabaseTraktAccess
import com.theupnextapp.database.TraktDao
import com.theupnextapp.network.TraktAuthApi
import com.theupnextapp.network.models.trakt.NetworkTraktAccessRefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktAuthenticator
@Inject
constructor(
    private val traktAuthApi: TraktAuthApi,
    private val traktDao: TraktDao,
) : Authenticator {
    override fun authenticate(
        route: Route?,
        response: Response,
    ): Request? {
        // If we've failed 3 times, give up. - (Optional, good practice)
        if (responseCount(response) >= 3) {
            return null
        }

        synchronized(this) {
            val currentToken = traktDao.getTraktAccessDataRaw()

            // If we don't have a token, we can't refresh.
            if (currentToken == null || currentToken.refresh_token.isNullOrEmpty()) {
                return null
            }

            // Check if the request already has the "latest" token.
            // If the Authorization header in the request matches the current token in DB,
            // then we need to refresh.
            // If it DOESN'T match, it means another thread has already refreshed it.
            val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")

            if (requestToken != null && requestToken != currentToken.access_token) {
                // Token has already been refreshed by another thread.
                // Retry with the new token.
                return response.request.newBuilder()
                    .header("Authorization", "Bearer ${currentToken.access_token}")
                    .build()
            }

            // Refresh the token
            return try {
                runBlocking {
                    val refreshResponse =
                        traktAuthApi.getAccessRefreshTokenAsync(
                            NetworkTraktAccessRefreshTokenRequest(
                                refresh_token = currentToken.refresh_token,
                                client_id = BuildConfig.TRAKT_CLIENT_ID,
                                client_secret = BuildConfig.TRAKT_CLIENT_SECRET,
                                redirect_uri = BuildConfig.TRAKT_REDIRECT_URI,
                                grant_type = "refresh_token",
                            ),
                        ).await()

                    // Save new token to DB
                    val newDbToken =
                        DatabaseTraktAccess(
                            id = 0,
                            access_token = refreshResponse.access_token ?: "",
                            token_type = refreshResponse.token_type,
                            expires_in = refreshResponse.expires_in,
                            refresh_token = refreshResponse.refresh_token,
                            scope = refreshResponse.scope,
                            created_at = refreshResponse.created_at,
                        )
                    traktDao.insertAllTraktAccessData(newDbToken)

                    // Retry request with new token
                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${refreshResponse.access_token}")
                        .build()
                }
            } catch (e: Exception) {
                // Refresh failed. Logout.
                try {
                    traktDao.deleteTraktAccessData()
                } catch (e2: Exception) {
                    // Ignore
                }
                null
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            result++
            priorResponse = priorResponse.priorResponse
        }
        return result
    }
}
