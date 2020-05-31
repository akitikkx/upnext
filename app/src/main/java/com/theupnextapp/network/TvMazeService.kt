package com.theupnextapp.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.theupnextapp.network.models.tvmaze.*
import kotlinx.coroutines.Deferred
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface TvMazeService {
    @GET("schedule")
    fun getYesterdayScheduleAsync(
        @Query("country") countryCode: String, @Query("date") date: String?
    ): Deferred<List<NetworkYesterdayScheduleResponse>>

    @GET("schedule")
    fun getTodayScheduleAsync(
        @Query("country") countryCode: String, @Query("date") date: String?
    ): Deferred<List<NetworkTodayScheduleResponse>>

    @GET("schedule")
    fun getTomorrowScheduleAsync(
        @Query("country") countryCode: String, @Query("date") date: String?
    ): Deferred<List<NetworkTomorrowScheduleResponse>>

    @GET("search/shows")
    fun getSuggestionListAsync(@Query("q") name: String): Deferred<List<NetworkShowSearchResponse>>

    @GET("shows/{id}")
    fun getShowSummaryAsync(@Path("id") id: String?): Deferred<NetworkShowInfoResponse>

    @GET("/episodes/{id}")
    fun getNextEpisodeAsync(@Path("id") name: String?): Deferred<NetworkShowNextEpisodeResponse>

    @GET("/episodes/{id}")
    fun getPreviousEpisodeAsync(@Path("id") name: String?): Deferred<NetworkShowPreviousEpisodeResponse>

    @GET("shows/{id}/cast")
    fun getShowCastAsync(@Path("id") id: String?): Deferred<NetworkShowCastResponse>
}

object TvMazeNetwork {
    private const val BASE_URL = "http://api.tvmaze.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient().newBuilder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .readTimeout(30, TimeUnit.SECONDS)
        .connectionPool(ConnectionPool(0, 1, TimeUnit.SECONDS))
        .build()

    private val retrofit = Retrofit.Builder()
        .client(client)
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()

    val tvMazeApi: TvMazeService = retrofit.create(TvMazeService::class.java)
}