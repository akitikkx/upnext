package com.theupnextapp.network

import com.theupnextapp.network.models.tvmaze.NetworkShowCastResponse
import com.theupnextapp.network.models.tvmaze.NetworkShowInfoResponse
import com.theupnextapp.network.models.tvmaze.NetworkShowNextEpisodeResponse
import com.theupnextapp.network.models.tvmaze.NetworkShowPreviousEpisodeResponse
import com.theupnextapp.network.models.tvmaze.NetworkShowSearchResponse
import com.theupnextapp.network.models.tvmaze.NetworkShowSeasonsResponse
import com.theupnextapp.network.models.tvmaze.NetworkTodayScheduleResponse
import com.theupnextapp.network.models.tvmaze.NetworkTomorrowScheduleResponse
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeEpisodesResponse
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowImageResponse
import com.theupnextapp.network.models.tvmaze.NetworkTvMazeShowLookupResponse
import com.theupnextapp.network.models.tvmaze.NetworkYesterdayScheduleResponse
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import javax.inject.Singleton

@Singleton
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

    @GET("lookup/shows")
    fun getShowLookupAsync(@Query("imdb") imdbId: String): Deferred<NetworkTvMazeShowLookupResponse>

    @GET("shows/{id}/images")
    fun getShowImagesAsync(@Path("id") id: String): Deferred<NetworkTvMazeShowImageResponse>

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

    @GET("shows/{id}/seasons")
    fun getShowSeasonsAsync(@Path("id") id: String?): Deferred<NetworkShowSeasonsResponse>

    @GET("shows/{id}/episodes")
    fun getSeasonEpisodesAsync(@Path("id") id: String?): Deferred<NetworkTvMazeEpisodesResponse>
}

object TvMazeNetwork {
    const val BASE_URL = "http://api.tvmaze.com/"
}