/*
 * MIT License
 *
 * Copyright (c) 2022 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.repository

import com.theupnextapp.domain.ShowSeasonEpisode
import com.theupnextapp.domain.TableUpdate
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.domain.TraktCheckInStatus
import com.theupnextapp.domain.TraktMostAnticipated
import com.theupnextapp.domain.TraktPopularShows
import com.theupnextapp.domain.TraktShowRating
import com.theupnextapp.domain.TraktShowStats
import com.theupnextapp.domain.TraktTrendingShows
import com.theupnextapp.domain.TraktUserListItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface TraktRepository {
    fun tableUpdate(tableName: String): Flow<TableUpdate?>
    val traktPopularShows: Flow<List<TraktPopularShows>>
    val traktTrendingShows: Flow<List<TraktTrendingShows>>
    val traktMostAnticipatedShows: Flow<List<TraktMostAnticipated>>
    val traktFavoriteShows: Flow<List<TraktUserListItem>>
    val traktAccessToken: Flow<TraktAccessToken?>

    // StateFlows for UI state
    val isLoading: StateFlow<Boolean>
    val isLoadingTraktTrending: StateFlow<Boolean>
    val isLoadingTraktPopular: StateFlow<Boolean>
    val isLoadingTraktMostAnticipated: StateFlow<Boolean>

    val traktShowRating: StateFlow<TraktShowRating?>
    val traktShowStats: StateFlow<TraktShowStats?>
    val favoriteShow: StateFlow<TraktUserListItem?>

    val traktCheckInEvent: SharedFlow<TraktCheckInStatus>

    // Authentication
    suspend fun getTraktAccessToken(code: String?)
    suspend fun revokeTraktAccessToken(traktAccessToken: TraktAccessToken)
    suspend fun getTraktAccessRefreshToken(refreshToken: String?)
    fun getTraktAccessTokenRaw(): Any?

    // Favorite Shows Management
    suspend fun refreshFavoriteShows(forceRefresh: Boolean = false, token: String?)
    suspend fun addShowToList(imdbID: String?, token: String?)
    suspend fun removeShowFromList(traktId: Int?, imdbID: String?, token: String?)
    suspend fun checkIfShowIsFavorite(imdbID: String?)
    suspend fun clearFavorites()

    // Public lists refresh
    suspend fun refreshTraktTrendingShows(forceRefresh: Boolean)
    suspend fun refreshTraktPopularShows(forceRefresh: Boolean)
    suspend fun refreshTraktMostAnticipatedShows(forceRefresh: Boolean)

    // Show Details
    suspend fun getTraktShowRating(imdbID: String?)
    suspend fun getTraktShowStats(imdbID: String?)

    // Check-in
    suspend fun checkInToShow(showSeasonEpisode: ShowSeasonEpisode, token: String?)

    suspend fun refreshImagesForFavoriteShows(forceRefresh: Boolean = false)
}