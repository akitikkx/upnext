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

    @GET("sync/collection/shows")
    fun getCollectionAsync(
        @Header("Authorization") token: String
    ): Deferred<NetworkTraktCollectionResponse>

    @GET("sync/watchlist/shows/rank")
    fun getWatchlistAsync(
        @Header("Authorization") token: String
    ): Deferred<NetworkTraktWatchlistResponse>

    @GET("sync/watched/shows")
    fun getWatchedAsync(
        @Header("Authorization") token: String
    ): Deferred<NetworkTraktWatchedResponse>

    @GET("sync/history")
    fun getHistoryAsync(
        @Header("Authorization") token: String
    ): Deferred<NetworkTraktHistoryResponse>

    @GET("recommendations/shows")
    fun getRecommendationsAsync(
        @Header("Authorization") token: String,
        @Query("ignore_collected") hidden: String = "true"
    ): Deferred<NetworkTraktRecommendationsResponse>

    @GET("search/imdb/{id}")
    fun getIDLookupAsync(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Query("type") type: String
    ): Deferred<NetworkTraktIDLookupResponse>

    @POST("sync/watchlist")
    fun addToWatchlistAsync(
        @Header("Authorization") token: String,
        @Body request: NetworkTraktAddToWatchlistRequest
    ): Deferred<NetworkTraktAddToWatchlistResponse>

    @POST("sync/watchlist/remove")
    fun removeFromWatchlistAsync(
        @Header("Authorization") token: String,
        @Body request: NetworkTraktRemoveFromWatchlistRequest
    ): Deferred<NetworkTraktRemoveFromWatchlistResponse>

    @POST("sync/history")
    fun addToHistoryAsync(
        @Header("Authorization") token: String,
        @Body request: NetworkTraktAddToHistoryRequest
    ): Deferred<NetworkTraktAddToHistoryResponse>

    @POST("sync/history/remove")
    fun removeFromHistoryAsync(
        @Header("Authorization") token: String,
        @Body request: NetworkTraktRemoveSeasonFromHistoryRequest
    ): Deferred<NetworkTraktRemoveFromHistoryResponse>

    @POST("sync/collection")
    fun addToCollectionAsync(
        @Header("Authorization") token: String,
        @Body request: NetworkTraktAddToCollectionRequest
    ): Deferred<NetworkTraktAddToCollectionResponse>

    @POST("sync/collection/remove")
    fun removeFromCollectionAsync(
        @Header("Authorization") token: String,
        @Body request: NetworkTraktRemoveFromCollectionRequest
    ): Deferred<NetworkTraktRemoveFromCollectionResponse>

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

    @GET("shows/{id}/progress/watched")
    fun getShowWatchedProgressAsync(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Query("hidden") hidden: String = "false",
        @Query("specials") specials: String = "false",
        @Query("count_specials") countSpecials: String = "true"
    ): Deferred<NetworkTraktShowWatchedProgressResponse>
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