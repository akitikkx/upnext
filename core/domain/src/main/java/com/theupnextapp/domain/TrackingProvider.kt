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

package com.theupnextapp.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * A generic interface defining the core functionalities required by any TV show tracking provider
 * (e.g., Trakt, SIMKL) supported by Upnext.
 */
interface TrackingProvider {
    val providerId: String
    val isAuthorized: StateFlow<Boolean>

    suspend fun getTrendingShows(): Result<List<TraktTrendingShows>>
    suspend fun getPopularShows(): Result<List<TraktPopularShows>>
    suspend fun getMostAnticipatedShows(): Result<List<TraktMostAnticipated>>
    
    suspend fun addToWatchlist(imdbId: String): Result<Unit>
    suspend fun removeFromWatchlist(imdbId: String): Result<Unit>
    
    // Additional methods will be abstracted here over time
}
