package com.theupnextapp.di

import android.content.Context
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.theupnextapp.common.utils.TraktConnectionInterceptor
import com.theupnextapp.network.TraktNetwork
import com.theupnextapp.network.TraktService
import com.theupnextapp.network.TvMazeNetwork.BASE_URL
import com.theupnextapp.network.TvMazeService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideNetworkClient(@ApplicationContext context: Context): OkHttpClient {
        return OkHttpClient().newBuilder()
            .cache(Cache(context.cacheDir, (5 * 1024 * 1024).toLong()))
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor { chain ->
                var request = chain.request()
                request =
                    request.newBuilder().header("Cache-Control", "public, max-age=" + 60 * 5)
                        .build()
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .readTimeout(30, TimeUnit.SECONDS)
            .connectionPool(ConnectionPool(0, 1, TimeUnit.SECONDS))
            .build()
    }

    @Singleton
    @Provides
    fun provideTvMazeService(
        networkClient: OkHttpClient
    ): TvMazeService {
        return Retrofit.Builder()
            .client(networkClient)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
            .create(TvMazeService::class.java)
    }

    @Singleton
    @Provides
    fun provideTraktService(networkClient: OkHttpClient): TraktService {
        return Retrofit.Builder()
            .client(networkClient.newBuilder().addInterceptor(TraktConnectionInterceptor()).build())
            .baseUrl(TraktNetwork.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
            .create(TraktService::class.java)
    }
}