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

package com.theupnextapp.network

import com.theupnextapp.network.models.simkl.NetworkSimklAccessTokenRequest
import com.theupnextapp.network.models.simkl.NetworkSimklAccessTokenResponse
import com.theupnextapp.network.models.simkl.NetworkSimklActivityResponse
import com.theupnextapp.network.models.simkl.NetworkSimklAllItemsResponse
import com.theupnextapp.network.models.simkl.NetworkSimklLibraryResponse
import com.theupnextapp.network.models.simkl.NetworkSimklTrendingResponse
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit interface for the SIMKL API.
 * Defines the endpoints required for the `TrackingProvider` implementation.
 */
interface SimklService {

    @POST("/oauth/token")
    fun getAccessTokenAsync(
        @Body request: NetworkSimklAccessTokenRequest
    ): Deferred<NetworkSimklAccessTokenResponse>

    @GET("tv/trending")
    suspend fun getTrendingShows(
        @Header("Authorization") token: String?
    ): Response<List<NetworkSimklTrendingResponse>>

    @GET("tv/premieres")
    suspend fun getPremieres(
        @Header("Authorization") token: String?
    ): Response<List<NetworkSimklTrendingResponse>>

    @GET("tv/{id}")
    suspend fun getShowSummary(
        @Path("id") id: Int
    ): Response<NetworkSimklTrendingResponse>

    @GET("sync/all")
    suspend fun getSyncState(
        @Header("Authorization") token: String,
        @Query("client_id") clientId: String
    ): Response<Any>

    @GET("sync/activities")
    suspend fun getActivities(
        @Header("Authorization") token: String
    ): Response<com.theupnextapp.network.models.simkl.NetworkSimklActivityResponse>

    @GET("sync/shows")
    suspend fun getSyncShows(
        @Header("Authorization") token: String
    ): Response<List<com.theupnextapp.network.models.simkl.NetworkSimklLibraryResponse>>

    @GET("sync/all-items")
    suspend fun getAllItems(
        @Header("Authorization") token: String,
        @Query("date_from") dateFrom: String
    ): Response<com.theupnextapp.network.models.simkl.NetworkSimklAllItemsResponse>

    @POST("sync/add-to-list")
    suspend fun addToList(
        @Header("Authorization") token: String,
        @Body request: com.theupnextapp.network.models.simkl.NetworkSimklSyncRequest
    ): Response<Any>

    @POST("sync/ratings")
    suspend fun addRating(
        @Header("Authorization") token: String,
        @Body request: com.theupnextapp.network.models.simkl.NetworkSimklSyncRequest
    ): Response<Any>

    @POST("sync/history")
    suspend fun addHistory(
        @Header("Authorization") token: String,
        @Body request: com.theupnextapp.network.models.simkl.NetworkSimklSyncRequest
    ): Response<Any>

    @GET("sync/episodes")
    suspend fun getWatchedEpisodes(
        @Header("Authorization") token: String
    ): Response<List<com.theupnextapp.network.models.simkl.NetworkSimklHistoryResponse>>
}
