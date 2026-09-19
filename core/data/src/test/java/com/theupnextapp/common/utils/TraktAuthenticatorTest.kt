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
import com.theupnextapp.network.TraktAuthApi
import com.theupnextapp.network.models.trakt.NetworkTraktAccessRefreshTokenResponse
import kotlinx.coroutines.CompletableDeferred
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class TraktAuthenticatorTest {
    private val traktAuthApi: TraktAuthApi = mock()
    private val traktDao: TraktDao = mock()

    private lateinit var authenticator: TraktAuthenticator

    @Before
    fun setup() {
        authenticator = TraktAuthenticator(traktAuthApi, traktDao)
    }

    private fun create401Response(
        url: String,
        authHeader: String? = null,
        priorResponse: Response? = null,
    ): Response {
        val requestBuilder = Request.Builder().url(url)
        if (authHeader != null) {
            requestBuilder.header("Authorization", authHeader)
        }
        val request = requestBuilder.build()

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .priorResponse(priorResponse)
            .build()
    }

    @Test
    fun authenticate_whenExceedsMaxRetryCount_returnsNull() {
        val response1 = create401Response("https://api.trakt.tv/shows/trending")
        val response2 = create401Response("https://api.trakt.tv/shows/trending", priorResponse = response1)
        val response3 = create401Response("https://api.trakt.tv/shows/trending", priorResponse = response2)

        val result = authenticator.authenticate(null, response3)

        assertNull(result)
        verifyNoInteractions(traktDao)
        verifyNoInteractions(traktAuthApi)
    }

    @Test
    fun authenticate_whenNoTokenInDbAndPublicEndpoint_stripsAuthHeaderAndRetries() {
        whenever(traktDao.getTraktAccessDataRaw()).thenReturn(null)
        val response = create401Response("https://api.trakt.tv/shows/trending", authHeader = "Bearer stale_token")

        val result = authenticator.authenticate(null, response)

        assertNotNull(result)
        assertNull(result?.header("Authorization"))
        verify(traktDao, never()).deleteTraktAccessData()
        verifyNoInteractions(traktAuthApi)
    }

    @Test
    fun authenticate_whenNoTokenInDbAndPrivateEndpoint_returnsNull() {
        whenever(traktDao.getTraktAccessDataRaw()).thenReturn(null)
        val response = create401Response("https://api.trakt.tv/sync/history", authHeader = "Bearer stale_token")

        val result = authenticator.authenticate(null, response)

        assertNull(result)
        verify(traktDao, never()).deleteTraktAccessData()
        verifyNoInteractions(traktAuthApi)
    }

    @Test
    fun authenticate_whenTokenIsMockTokenAndPublicEndpoint_clearsDbTokenAndRetriesWithoutAuth() {
        val mockToken =
            DatabaseTraktAccess(
                id = 1,
                access_token = "mock_test_token",
                created_at = 123456L,
                expires_in = 3600L,
                refresh_token = "mock_refresh_token",
                scope = "public",
                token_type = "bearer",
            )
        whenever(traktDao.getTraktAccessDataRaw()).thenReturn(mockToken)
        val response = create401Response("https://api.trakt.tv/shows/trending", authHeader = "Bearer mock_test_token")

        val result = authenticator.authenticate(null, response)

        assertNotNull(result)
        assertNull(result?.header("Authorization"))
        verify(traktDao).deleteTraktAccessData()
        verifyNoInteractions(traktAuthApi)
    }

    @Test
    fun authenticate_whenTokenIsMockTokenAndPrivateEndpoint_clearsDbTokenAndReturnsNull() {
        val mockToken =
            DatabaseTraktAccess(
                id = 1,
                access_token = "mock_test_token",
                created_at = 123456L,
                expires_in = 3600L,
                refresh_token = "mock_refresh_token",
                scope = "public",
                token_type = "bearer",
            )
        whenever(traktDao.getTraktAccessDataRaw()).thenReturn(mockToken)
        val response = create401Response("https://api.trakt.tv/sync/history", authHeader = "Bearer mock_test_token")

        val result = authenticator.authenticate(null, response)

        assertNull(result)
        verify(traktDao).deleteTraktAccessData()
        verifyNoInteractions(traktAuthApi)
    }

    @Test
    fun authenticate_whenRefreshTokenMissing_clearsDbTokenAndRetriesPublicWithoutAuth() {
        val tokenWithoutRefresh =
            DatabaseTraktAccess(
                id = 1,
                access_token = "valid_looking_token",
                created_at = 123456L,
                expires_in = 3600L,
                refresh_token = null,
                scope = "public",
                token_type = "bearer",
            )
        whenever(traktDao.getTraktAccessDataRaw()).thenReturn(tokenWithoutRefresh)
        val response = create401Response("https://api.trakt.tv/shows/popular", authHeader = "Bearer valid_looking_token")

        val result = authenticator.authenticate(null, response)

        assertNotNull(result)
        assertNull(result?.header("Authorization"))
        verify(traktDao).deleteTraktAccessData()
        verifyNoInteractions(traktAuthApi)
    }

    @Test
    fun authenticate_whenRefreshSucceeds_updatesDbAndRetriesWithNewToken() {
        val oldToken =
            DatabaseTraktAccess(
                id = 0,
                access_token = "old_token",
                created_at = 123456L,
                expires_in = 3600L,
                refresh_token = "valid_refresh_token",
                scope = "public",
                token_type = "bearer",
            )
        whenever(traktDao.getTraktAccessDataRaw()).thenReturn(oldToken)

        val refreshResponse =
            NetworkTraktAccessRefreshTokenResponse(
                access_token = "new_refreshed_token",
                token_type = "bearer",
                expires_in = 7200L,
                refresh_token = "new_refresh_token",
                scope = "public",
                created_at = 123457L,
            )
        whenever(traktAuthApi.getAccessRefreshTokenAsync(any())).thenReturn(
            CompletableDeferred(refreshResponse),
        )

        val response = create401Response("https://api.trakt.tv/sync/history", authHeader = "Bearer old_token")

        val result = authenticator.authenticate(null, response)

        assertNotNull(result)
        assertEquals("Bearer new_refreshed_token", result?.header("Authorization"))
        verify(traktDao).insertAllTraktAccessData(any())
        verify(traktDao, never()).deleteTraktAccessData()
    }

    @Test
    fun authenticate_whenRefreshFailsAndPublicEndpoint_clearsDbTokenAndRetriesWithoutAuth() {
        val oldToken =
            DatabaseTraktAccess(
                id = 0,
                access_token = "old_token",
                created_at = 123456L,
                expires_in = 3600L,
                refresh_token = "revoked_refresh_token",
                scope = "public",
                token_type = "bearer",
            )
        whenever(traktDao.getTraktAccessDataRaw()).thenReturn(oldToken)

        val failedDeferred = CompletableDeferred<NetworkTraktAccessRefreshTokenResponse>()
        failedDeferred.completeExceptionally(RuntimeException("401 Invalid Refresh Token"))
        whenever(traktAuthApi.getAccessRefreshTokenAsync(any())).thenReturn(failedDeferred)

        val response = create401Response("https://api.trakt.tv/shows/anticipated", authHeader = "Bearer old_token")

        val result = authenticator.authenticate(null, response)

        assertNotNull(result)
        assertNull(result?.header("Authorization"))
        verify(traktDao).deleteTraktAccessData()
    }

    @Test
    fun authenticate_whenRefreshFailsAndPrivateEndpoint_clearsDbTokenAndReturnsNull() {
        val oldToken =
            DatabaseTraktAccess(
                id = 0,
                access_token = "old_token",
                created_at = 123456L,
                expires_in = 3600L,
                refresh_token = "revoked_refresh_token",
                scope = "public",
                token_type = "bearer",
            )
        whenever(traktDao.getTraktAccessDataRaw()).thenReturn(oldToken)

        val failedDeferred = CompletableDeferred<NetworkTraktAccessRefreshTokenResponse>()
        failedDeferred.completeExceptionally(RuntimeException("401 Invalid Refresh Token"))
        whenever(traktAuthApi.getAccessRefreshTokenAsync(any())).thenReturn(failedDeferred)

        val response = create401Response("https://api.trakt.tv/sync/history", authHeader = "Bearer old_token")

        val result = authenticator.authenticate(null, response)

        assertNull(result)
        verify(traktDao).deleteTraktAccessData()
    }

    @Test
    fun isPublicEndpoint_verifiesPathsCorrectly() {
        val publicTrending = Request.Builder().url("https://api.trakt.tv/shows/trending").build()
        val publicPopular = Request.Builder().url("https://api.trakt.tv/shows/popular").build()
        val publicAnticipated = Request.Builder().url("https://api.trakt.tv/shows/anticipated").build()
        val publicShowSummary = Request.Builder().url("https://api.trakt.tv/shows/breaking-bad").build()
        val publicSearch = Request.Builder().url("https://api.trakt.tv/search/show?query=reacher").build()

        assertTrue(authenticator.isPublicEndpoint(publicTrending))
        assertTrue(authenticator.isPublicEndpoint(publicPopular))
        assertTrue(authenticator.isPublicEndpoint(publicAnticipated))
        assertTrue(authenticator.isPublicEndpoint(publicShowSummary))
        assertTrue(authenticator.isPublicEndpoint(publicSearch))

        val privateSync = Request.Builder().url("https://api.trakt.tv/sync/history").build()
        val privateRecommendations = Request.Builder().url("https://api.trakt.tv/recommendations/shows").build()
        val privateCalendar = Request.Builder().url("https://api.trakt.tv/calendars/my/shows").build()
        val privateCheckin = Request.Builder().url("https://api.trakt.tv/checkin").build()
        val privateUsers = Request.Builder().url("https://api.trakt.tv/users/settings").build()

        assertFalse(authenticator.isPublicEndpoint(privateSync))
        assertFalse(authenticator.isPublicEndpoint(privateRecommendations))
        assertFalse(authenticator.isPublicEndpoint(privateCalendar))
        assertFalse(authenticator.isPublicEndpoint(privateCheckin))
        assertFalse(authenticator.isPublicEndpoint(privateUsers))
    }
}
