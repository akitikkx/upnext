/*
 * MIT License
 *
 * Copyright (c) 2024 Ahmed Tikiwa
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

package com.theupnextapp.network.models.simkl

import com.google.gson.annotations.SerializedName

data class NetworkSimklSyncRequest(
    @SerializedName("shows")
    val shows: List<NetworkSimklSyncShow>? = null,
    
    @SerializedName("movies")
    val movies: List<NetworkSimklSyncMovie>? = null
)

data class NetworkSimklSyncShow(
    @SerializedName("ids")
    val ids: NetworkSimklSyncIds,
    
    @SerializedName("rating")
    val rating: Int? = null,
    
    @SerializedName("seasons")
    val seasons: List<NetworkSimklSyncSeason>? = null
)

data class NetworkSimklSyncMovie(
    @SerializedName("ids")
    val ids: NetworkSimklSyncIds,
    
    @SerializedName("rating")
    val rating: Int? = null
)

data class NetworkSimklSyncIds(
    @SerializedName("simkl")
    val simkl: Int? = null,
    @SerializedName("imdb")
    val imdb: String? = null,
    @SerializedName("tmdb")
    val tmdb: Int? = null,
    @SerializedName("tvdb")
    val tvdb: Int? = null,
    @SerializedName("trakt")
    val trakt: Int? = null
)

data class NetworkSimklSyncSeason(
    @SerializedName("number")
    val number: Int,
    @SerializedName("episodes")
    val episodes: List<NetworkSimklSyncEpisode>? = null
)

data class NetworkSimklSyncEpisode(
    @SerializedName("number")
    val number: Int
)
