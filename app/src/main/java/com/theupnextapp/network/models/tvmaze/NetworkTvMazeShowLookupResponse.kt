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

package com.theupnextapp.network.models.tvmaze

data class NetworkTvMazeShowLookupResponse(
    val _links: NetworkTvMazeShowLookupLinks?,
    val averageRuntime: Int?,
    val dvdCountry: Any?,
    val externals: NetworkTvMazeShowLookupExternals?,
    val genres: List<String>?,
    val id: Int,
    val image: NetworkTvMazeShowLookupImage,
    val language: String,
    val name: String,
    val network: NetworkTvMazeShowLookupNetwork,
    val officialSite: String,
    val premiered: String,
    val rating: NetworkTvMazeShowLookupRating,
    val runtime: Int,
    val schedule: NetworkTvMazeShowLookupSchedule,
    val status: String,
    val summary: String,
    val type: String,
    val updated: Int,
    val url: String,
    val webChannel: NetworkTvMazeShowLookupWebChannel,
    val weight: Int,
)
