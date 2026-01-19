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

import com.theupnextapp.database.DatabaseFavoriteShows
import com.theupnextapp.database.DatabaseTraktAccess
import com.theupnextapp.database.DatabaseTraktMostAnticipated
import com.theupnextapp.database.DatabaseTraktPopularShows
import com.theupnextapp.database.DatabaseTraktTrendingShows
import com.theupnextapp.database.TraktDao
import com.theupnextapp.network.TraktAuthApi
import com.theupnextapp.network.models.trakt.NetworkTraktAccessRefreshTokenRequest
import com.theupnextapp.network.models.trakt.NetworkTraktAccessRefreshTokenResponse
import com.theupnextapp.network.models.trakt.NetworkTraktAccessTokenRequest
import com.theupnextapp.network.models.trakt.NetworkTraktAccessTokenResponse
import com.theupnextapp.network.models.trakt.NetworkTraktRevokeAccessTokenRequest
import com.theupnextapp.network.models.trakt.NetworkTraktRevokeAccessTokenResponse
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
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
        val response = Response.Builder()
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
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()
            
        val refreshResponse = NetworkTraktAccessRefreshTokenResponse("new_token", "Bearer", 3600, "new_refresh", "scope", 12345)
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
        val response = Response.Builder()
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

    // FAKES

    class FakeTraktAuthApi : TraktAuthApi {
        var responseToReturn: NetworkTraktAccessRefreshTokenResponse? = null
        var shouldThrow: Boolean = false

        override fun getAccessTokenAsync(traktAccessTokenRequest: NetworkTraktAccessTokenRequest): Deferred<NetworkTraktAccessTokenResponse> {
            TODO("Not yet implemented")
        }

        override fun getAccessRefreshTokenAsync(traktAccessRefreshTokenRequest: NetworkTraktAccessRefreshTokenRequest): Deferred<NetworkTraktAccessRefreshTokenResponse> {
            if (shouldThrow) throw RuntimeException("Network Error")
            return CompletableDeferred(responseToReturn!!)
        }

        override fun revokeAccessTokenAsync(networkTraktRevokeAccessTokenRequest: NetworkTraktRevokeAccessTokenRequest): Deferred<NetworkTraktRevokeAccessTokenResponse> {
            TODO("Not yet implemented")
        }
    }

    class FakeTraktDao : TraktDao {
        var currentToken: DatabaseTraktAccess? = null

        override fun deleteTraktAccessData() {
            currentToken = null
        }

        override fun getTraktAccessData(): Flow<DatabaseTraktAccess?> {
            return flowOf(currentToken)
        }

        override fun getTraktAccessDataRaw(): DatabaseTraktAccess? {
            return currentToken
        }

        override fun insertAllTraktAccessData(databaseTraktAccess: DatabaseTraktAccess) {
            currentToken = databaseTraktAccess
        }

        // Unimplemented methods
        override suspend fun insertAllFavoriteShows(vararg shows: DatabaseFavoriteShows) {}
        override suspend fun insertFavoriteShow(databaseFavoriteShows: DatabaseFavoriteShows) {}
        override suspend fun deleteAllFavoriteShows() {}
        override fun getFavoriteShows(): Flow<List<DatabaseFavoriteShows>> = flowOf(emptyList())
        override fun getFavoriteShowsRaw(): List<DatabaseFavoriteShows> = emptyList()
        override fun getFavoriteShow(imdbID: String): DatabaseFavoriteShows? = null
        override fun updateFavoriteShowWithAirStamp(databaseFavoriteShows: DatabaseFavoriteShows) {}
        override fun getFavoriteShowRawByTvMazeId(tvMazeId: Int): DatabaseFavoriteShows = TODO()
        override suspend fun getFavoriteShowByTraktId(traktId: Int): DatabaseFavoriteShows? = null
        override suspend fun getAllFavoriteShowTraktIds(): List<Int> = emptyList()
        override suspend fun deleteFavoriteShowsByTraktIds(traktIds: List<Int>): Int = 0
        override suspend fun deleteFavoriteShowByTraktId(traktId: Int): Int = 0
        override suspend fun insertAllTraktPopular(vararg traktPopularShows: DatabaseTraktPopularShows) {}
        override fun getTraktPopular(): Flow<List<DatabaseTraktPopularShows>> = flowOf(emptyList())
        override fun getTraktPopularRaw(): List<DatabaseTraktPopularShows> = emptyList()
        override suspend fun clearPopularShows() {}
        override suspend fun checkIfPopularShowsIsEmpty(): Boolean = true
        override suspend fun deleteSpecificPopularShows(showIds: List<Int>) {}
        override suspend fun insertAllTraktTrending(vararg traktTrendingShows: DatabaseTraktTrendingShows) {}
        override fun getTraktTrending(): Flow<List<DatabaseTraktTrendingShows>> = flowOf(emptyList())
        override fun getTraktTrendingRaw(): List<DatabaseTraktTrendingShows> = emptyList()
        override suspend fun clearTrendingShows() {}
        override suspend fun checkIfTrendingShowsIsEmpty(): Boolean = true
        override suspend fun deleteSpecificTrendingShows(showIds: List<Int>) {}
        override suspend fun insertAllTraktMostAnticipated(vararg traktMostAnticipatedShows: DatabaseTraktMostAnticipated) {}
        override fun getTraktMostAnticipated(): Flow<List<DatabaseTraktMostAnticipated>> = flowOf(emptyList())
        override fun getTraktMostAnticipatedRaw(): List<DatabaseTraktMostAnticipated> = emptyList()
        override suspend fun clearMostAnticipatedShows() {}
        override suspend fun checkIfMostAnticipatedShowsIsEmpty(): Boolean = true
        override suspend fun deleteSpecificMostAnticipatedShows(showIds: List<Int>) {}
    }
}
