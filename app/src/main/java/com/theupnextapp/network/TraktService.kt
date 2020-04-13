package com.theupnextapp.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface TraktService {
    @POST(" oauth/token")
    fun getAccessTokenAsync(@Body traktAccessTokenRequest: NetworkTraktAccessTokenRequest): Deferred<TraktAccessTokenResponse>

    @GET("sync/collection/shows")
    fun getCollectionAsync(
        @Header("Content-Type") contentType: String,
        @Header("Authorization") token: String,
        @Header("trakt-api-version") version: String = "2",
        @Header("trakt-api-key") apiKey: String?
    ): Deferred<NetworkTraktCollectionResponse>

    @GET("sync/watchlist/shows/rank")
    fun getWatchlistAsync(
        @Header("Content-Type") contentType: String,
        @Header("Authorization") token: String,
        @Header("trakt-api-version") version: String = "2",
        @Header("trakt-api-key") apiKey: String?
    ): Deferred<NetworkTraktWatchlistResponse>

    @GET("sync/watched/shows")
    fun getWatchedAsync(
        @Header("Content-Type") contentType: String,
        @Header("Authorization") token: String,
        @Header("trakt-api-version") version: String = "2",
        @Header("trakt-api-key") apiKey: String?
    ): Deferred<NetworkTraktWatchedResponse>

    @GET("sync/history")
    fun getHistoryAsync(
        @Header("Content-Type") contentType: String,
        @Header("Authorization") token: String,
        @Header("trakt-api-version") version: String = "2",
        @Header("trakt-api-key") apiKey: String?
    ): Deferred<NetworkTraktHistoryResponse>
}

object TraktNetwork {
    private const val BASE_URL = "https://api.trakt.tv/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient().newBuilder()
        .addInterceptor(loggingInterceptor)
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