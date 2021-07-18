package com.theupnextapp.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.theupnextapp.UpnextApplication
import com.theupnextapp.common.utils.TraktConnectionInterceptor
import com.theupnextapp.network.models.trakt.*
import kotlinx.coroutines.Deferred
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.File
import java.util.concurrent.TimeUnit

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
}

object TraktNetwork {
    private const val BASE_URL = "https://api.trakt.tv/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    var httpCacheDirectory: File = File(UpnextApplication.context?.cacheDir, "responses")

    private const val cacheSize = (5 * 1024 * 1024).toLong()

    private val client = OkHttpClient().newBuilder()
        .cache(Cache(httpCacheDirectory, cacheSize))
        .addInterceptor(loggingInterceptor)
        .addInterceptor(TraktConnectionInterceptor())
        .addInterceptor { chain ->
            var request = chain.request()
            request =
                request.newBuilder().header("Cache-Control", "public, max-age=" + 60 * 5).build()
            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .readTimeout(30, TimeUnit.SECONDS)
        .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
        .build()

    private val retrofit = Retrofit.Builder()
        .client(client)
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()

    val traktApi: TraktService = retrofit.create(TraktService::class.java)
}