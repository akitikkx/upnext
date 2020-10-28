package com.theupnextapp.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.theupnextapp.UpnextApplication
import com.theupnextapp.network.models.trakt.NetworkTraktTrendingShowsResponse
import com.theupnextapp.network.models.upnext.NetworkNewShowsResponse
import com.theupnextapp.network.models.upnextktor.NetworkUpnextKtorShowTrendingResponse
import com.theupnextapp.network.models.upnextktor.NetworkUpnextKtorShowTrendingResponseItem
import kotlinx.coroutines.Deferred
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import java.io.File
import java.util.concurrent.TimeUnit

interface UpnextKtorService {

    @GET("shows/trending")
    fun getTrendingShowsAsync(): Deferred<List<NetworkUpnextKtorShowTrendingResponseItem>>
}

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

object UpnextKtorNetwork {
    private const val BASE_URL = "https://api.theupnextapp.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    var httpCacheDirectory: File = File(UpnextApplication.context?.cacheDir, "responses")

    private const val cacheSize = (5 * 1024 * 1024).toLong()

    private val client = OkHttpClient().newBuilder()
        .cache(Cache(httpCacheDirectory, cacheSize))
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            var request = chain.request()
            request =
                request.newBuilder().header("Cache-Control", "public, max-age=" + 60 * 5).build()
            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .readTimeout(30, TimeUnit.SECONDS)
        .connectionPool(ConnectionPool(0, 1, TimeUnit.SECONDS))
        .build()

    private val retrofit = Retrofit.Builder()
        .client(client)
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()

    val upnextKtorApi: UpnextKtorService = retrofit.create(UpnextKtorService::class.java)
}