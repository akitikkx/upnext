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

import com.theupnextapp.network.models.trakt.NetworkTraktAccessRefreshTokenRequest
import com.theupnextapp.network.models.trakt.NetworkTraktAccessRefreshTokenResponse
import com.theupnextapp.network.models.trakt.NetworkTraktAccessTokenRequest
import com.theupnextapp.network.models.trakt.NetworkTraktAccessTokenResponse
import com.theupnextapp.network.models.trakt.NetworkTraktAddShowToListRequest
import com.theupnextapp.network.models.trakt.NetworkTraktAddShowToListResponse
import com.theupnextapp.network.models.trakt.NetworkTraktCheckInRequest
import com.theupnextapp.network.models.trakt.NetworkTraktCheckInResponse
import com.theupnextapp.network.models.trakt.NetworkTraktCreateCustomListRequest
import com.theupnextapp.network.models.trakt.NetworkTraktCreateCustomListResponse
import com.theupnextapp.network.models.trakt.NetworkTraktIdLookupResponse
import com.theupnextapp.network.models.trakt.NetworkTraktMostAnticipatedResponse
import com.theupnextapp.network.models.trakt.NetworkTraktPopularShowsResponse
import com.theupnextapp.network.models.trakt.NetworkTraktRemoveShowFromListRequest
import com.theupnextapp.network.models.trakt.NetworkTraktRemoveShowFromListResponse
import com.theupnextapp.network.models.trakt.NetworkTraktRevokeAccessTokenRequest
import com.theupnextapp.network.models.trakt.NetworkTraktRevokeAccessTokenResponse
import com.theupnextapp.network.models.trakt.NetworkTraktShowInfoResponse
import com.theupnextapp.network.models.trakt.NetworkTraktShowRatingResponse
import com.theupnextapp.network.models.trakt.NetworkTraktShowStatsResponse
import com.theupnextapp.network.models.trakt.NetworkTraktTrendingShowsResponse
import com.theupnextapp.network.models.trakt.NetworkTraktUserListItemResponse
import com.theupnextapp.network.models.trakt.NetworkTraktUserListsResponse
import com.theupnextapp.network.models.trakt.NetworkTraktUserSettingsResponse
import kotlinx.coroutines.Deferred
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface TraktService {
    @POST(" oauth/token")
    fun getAccessTokenAsync(@Body traktAccessTokenRequest: NetworkTraktAccessTokenRequest): Deferred<NetworkTraktAccessTokenResponse>

    @POST(" oauth/token")
    fun getAccessRefreshTokenAsync(@Body traktAccessRefreshTokenRequest: NetworkTraktAccessRefreshTokenRequest): Deferred<NetworkTraktAccessRefreshTokenResponse>

    @POST("oauth/revoke")
    fun revokeAccessTokenAsync(@Body networkTraktRevokeAccessTokenRequest: NetworkTraktRevokeAccessTokenRequest): Deferred<NetworkTraktRevokeAccessTokenResponse>

    @GET("shows/{id}")
    fun getShowInfoAsync(
        @Path("id") imdbID: String
    ): Deferred<NetworkTraktShowInfoResponse>

    @GET("shows/{id}/ratings")
    fun getShowRatingsAsync(
        @Path("id") id: String
    ): Deferred<NetworkTraktShowRatingResponse>

    @GET("shows/{id}/stats")
    fun getShowStatsAsync(
        @Path("id") id: String
    ): Deferred<NetworkTraktShowStatsResponse>

    @GET("shows/trending")
    fun getTrendingShowsAsync(): Deferred<NetworkTraktTrendingShowsResponse>

    @GET("shows/popular")
    fun getPopularShowsAsync(): Deferred<NetworkTraktPopularShowsResponse>

    @GET("shows/anticipated")
    fun getMostAnticipatedShowsAsync(): Deferred<NetworkTraktMostAnticipatedResponse>

    @GET("users/settings")
    fun getUserSettingsAsync(
        @Header("Authorization") token: String
    ): Deferred<NetworkTraktUserSettingsResponse>

    @GET("users/{id}/lists")
    fun getUserCustomListsAsync(
        @Header("Authorization") token: String,
        @Path("id") userSlug: String
    ): Deferred<NetworkTraktUserListsResponse>

    @POST("users/{id}/lists")
    fun createCustomListAsync(
        @Header("Authorization") token: String,
        @Path("id") userSlug: String,
        @Body createCustomListRequest: NetworkTraktCreateCustomListRequest
    ): Deferred<NetworkTraktCreateCustomListResponse>

    @GET("users/{id}/lists/{list_id}/items/show")
    fun getCustomListItemsAsync(
        @Header("Authorization") token: String,
        @Path("id") userSlug: String,
        @Path("list_id") traktId: String
    ): Deferred<NetworkTraktUserListItemResponse>

    @POST("users/{id}/lists/{list_id}/items")
    fun addShowToCustomListAsync(
        @Header("Authorization") token: String,
        @Path("id") userSlug: String,
        @Path("list_id") traktId: String,
        @Body networkTraktAddShowToListRequest: NetworkTraktAddShowToListRequest
    ): Deferred<NetworkTraktAddShowToListResponse>

    @POST("users/{id}/lists/{list_id}/items/remove")
    fun removeShowFromCustomListAsync(
        @Header("Authorization") token: String,
        @Path("id") userSlug: String,
        @Path("list_id") traktId: String,
        @Body networkTraktRemoveShowFromListRequest: NetworkTraktRemoveShowFromListRequest
    ): Deferred<NetworkTraktRemoveShowFromListResponse>

    @GET("search/{id_type}/{id}?type=show")
    fun idLookupAsync(
        @Path("id_type") idType: String,
        @Path("id") id: String
    ): Deferred<NetworkTraktIdLookupResponse>

    @POST("checkin")
    fun checkInAsync(
        @Header("Authorization") token: String,
        @Body networkTraktCheckInRequest: NetworkTraktCheckInRequest
    ): Deferred<NetworkTraktCheckInResponse>
}

object TraktNetwork {
    const val BASE_URL = "https://api.trakt.tv/"
}
