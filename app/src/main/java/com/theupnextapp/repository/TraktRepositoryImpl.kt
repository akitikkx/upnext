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

package com.theupnextapp.repository

import com.theupnextapp.database.DatabaseTraktAccess
import com.theupnextapp.database.TraktDao
import com.theupnextapp.database.UpnextDao
import com.theupnextapp.database.asDomainModel
import com.theupnextapp.datasource.TraktAccountDataSource
import com.theupnextapp.datasource.TraktAuthDataSource
import com.theupnextapp.datasource.TraktRecommendationsDataSource
import com.theupnextapp.domain.ShowSeasonEpisode
import com.theupnextapp.domain.TableUpdate
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.domain.TraktCheckInStatus
import com.theupnextapp.domain.TraktMostAnticipated
import com.theupnextapp.domain.TraktPopularShows
import com.theupnextapp.domain.TraktShowRating
import com.theupnextapp.domain.TraktShowStats
import com.theupnextapp.domain.TraktTrendingShows
import com.theupnextapp.domain.TraktUserList
import com.theupnextapp.domain.TraktUserListItem
import com.theupnextapp.domain.isTraktAccessTokenValid
import com.theupnextapp.network.TvMazeService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class TraktRepositoryImpl(
    upnextDao: UpnextDao,
    tvMazeService: TvMazeService,
    private val traktDao: TraktDao,
    private val traktAuthDataSource: TraktAuthDataSource,
    private val traktRecommendationsDataSource: TraktRecommendationsDataSource,
    private val traktAccountDataSource: TraktAccountDataSource,
) : BaseRepository(upnextDao, tvMazeService), TraktRepository {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val traktAccessToken: Flow<TraktAccessToken?> =
        traktDao.getTraktAccessData().map {
            it?.asDomainModel()
        }

    private val _isLoadingTraktTrending = MutableStateFlow(false)
    override val isLoadingTraktTrending: StateFlow<Boolean> = _isLoadingTraktTrending.asStateFlow()

    override val traktTrendingShows: Flow<List<TraktTrendingShows>> =
        traktDao.getTraktTrending().map { list -> list.asDomainModel() }

    private val _isLoadingTraktPopular = MutableStateFlow(false)
    override val isLoadingTraktPopular: StateFlow<Boolean> = _isLoadingTraktPopular.asStateFlow()

    override val traktPopularShows: Flow<List<TraktPopularShows>> =
        traktDao.getTraktPopular().map { list -> list.asDomainModel() }

    private val _isLoadingTraktMostAnticipated = MutableStateFlow(false)
    override val isLoadingTraktMostAnticipated: StateFlow<Boolean> =
        _isLoadingTraktMostAnticipated.asStateFlow()

    override val traktMostAnticipatedShows: Flow<List<TraktMostAnticipated>> =
        traktDao.getTraktMostAnticipated().map { list -> list.asDomainModel() }

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _traktCheckInEvent = MutableSharedFlow<TraktCheckInStatus>(replay = 0)
    override val traktCheckInEvent: SharedFlow<TraktCheckInStatus> =
        _traktCheckInEvent.asSharedFlow()

    private val _isLoadingFavoriteShows = MutableStateFlow(false)
    override val isLoadingFavoriteShows: StateFlow<Boolean> = _isLoadingFavoriteShows.asStateFlow()

    override val traktFavoriteShows: Flow<List<TraktUserListItem>> =
        traktDao.getFavoriteShows().map { list ->
            list.asDomainModel()
        }

    private val _favoriteShowsError = MutableStateFlow<String?>(null)
    override val favoriteShowsError: StateFlow<String?> = _favoriteShowsError.asStateFlow()

    private val _traktShowRating = MutableStateFlow<TraktShowRating?>(null)
    override val traktShowRating: StateFlow<TraktShowRating?> = _traktShowRating.asStateFlow()

    private val _traktShowStats = MutableStateFlow<TraktShowStats?>(null)
    override val traktShowStats: StateFlow<TraktShowStats?> = _traktShowStats.asStateFlow()

    private val _favoriteShow = MutableStateFlow<TraktUserListItem?>(null)
    override val favoriteShow: StateFlow<TraktUserListItem?> = _favoriteShow.asStateFlow()

    // TODO: Implement Trakt User Lists logic in DataSource if needed, currently just empty flows/states
    private val _isLoadingUserCustomLists = MutableStateFlow(false)
    override val isLoadingUserCustomLists: StateFlow<Boolean> = _isLoadingUserCustomLists.asStateFlow()

    private val _userCustomListsError = MutableStateFlow<String?>(null)
    override val userCustomListsError: StateFlow<String?> = _userCustomListsError.asStateFlow()

    // Assuming we don't have this implemented yet or it was missing in my analysis
    override val traktUserCustomLists: Flow<List<TraktUserList>> = kotlinx.coroutines.flow.flowOf(emptyList())

    override suspend fun getTraktAccessToken(code: String): Result<TraktAccessToken> {
        return traktAuthDataSource.getAccessToken(code)
    }

    override suspend fun getTraktAccessRefreshToken(refreshToken: String?): Result<TraktAccessToken> {
        return traktAuthDataSource.getAccessRefreshToken(refreshToken)
    }

    override fun getTraktAccessTokenRaw(): DatabaseTraktAccess? {
        return traktDao.getTraktAccessDataRaw()
    }

    override suspend fun revokeTraktAccessToken(traktAccessToken: TraktAccessToken) {
        revokeTraktAccessToken(traktAccessToken.access_token ?: "")
    }

    override suspend fun revokeTraktAccessToken(token: String): Result<Unit> {
        if (token.isEmpty()) return Result.failure(IllegalArgumentException("Empty token"))
        return traktAuthDataSource.revokeAccessToken(token)
    }

    override suspend fun refreshTraktTrendingShows(forceRefresh: Boolean) {
        if (_isLoadingTraktTrending.value && !forceRefresh) return
        _isLoadingTraktTrending.value = true
        traktRecommendationsDataSource.refreshTraktTrendingShows(forceRefresh)
        _isLoadingTraktTrending.value = false
    }

    override suspend fun refreshTraktPopularShows(forceRefresh: Boolean) {
        if (_isLoadingTraktPopular.value && !forceRefresh) return
        _isLoadingTraktPopular.value = true
        traktRecommendationsDataSource.refreshTraktPopularShows(forceRefresh)
        _isLoadingTraktPopular.value = false
    }

    override suspend fun refreshTraktMostAnticipatedShows(forceRefresh: Boolean) {
        if (_isLoadingTraktMostAnticipated.value && !forceRefresh) return
        _isLoadingTraktMostAnticipated.value = true
        traktRecommendationsDataSource.refreshTraktMostAnticipatedShows(forceRefresh)
        _isLoadingTraktMostAnticipated.value = false
    }

    override suspend fun refreshFavoriteShows(
        forceRefresh: Boolean,
        token: String?,
    ) {
        refreshFavoriteShows(token ?: "")
    }

    override suspend fun refreshFavoriteShows(token: String): Result<Unit> {
        if (token.isEmpty()) return Result.failure(IllegalArgumentException("Empty token"))
        if (_isLoadingFavoriteShows.value) return Result.success(Unit)

        _isLoadingFavoriteShows.value = true
        _favoriteShowsError.value = null

        val result = traktAccountDataSource.refreshFavoriteShows(token)
        if (result.isFailure) {
            _favoriteShowsError.value = result.exceptionOrNull()?.message
        }

        _isLoadingFavoriteShows.value = false
        return result
    }

    override suspend fun addShowToFavorites(
        imdbId: String,
        token: String,
    ): Result<Unit> {
        if (imdbId.isEmpty() || token.isEmpty()) {
            val msg = "Invalid ID or Token"
            _favoriteShowsError.value = msg
            return Result.failure(IllegalArgumentException(msg))
        }
        _isLoadingFavoriteShows.value = true
        _favoriteShowsError.value = null

        val result = traktAccountDataSource.addShowToFavorites(imdbId, token)
        if (result.isFailure) {
            _favoriteShowsError.value = result.exceptionOrNull()?.message
        }
        _isLoadingFavoriteShows.value = false
        return result
    }

    override suspend fun addShowToList(
        imdbID: String?,
        token: String?,
    ) {
        if (imdbID != null && token != null) {
            addShowToFavorites(imdbID, token)
        }
    }

    override suspend fun removeShowFromFavorites(
        traktId: Int,
        imdbId: String,
        token: String,
    ): Result<Unit> {
        _isLoadingFavoriteShows.value = true
        _favoriteShowsError.value = null

        val result = traktAccountDataSource.removeShowFromFavorites(traktId, imdbId, token)
        if (result.isFailure) {
            _favoriteShowsError.value = result.exceptionOrNull()?.message
        }
        _isLoadingFavoriteShows.value = false
        return result
    }

    override suspend fun removeShowFromList(
        traktId: Int?,
        imdbID: String?,
        token: String?,
    ) {
        if (traktId != null && imdbID != null && token != null) {
            removeShowFromFavorites(traktId, imdbID, token)
        }
    }

    override suspend fun checkInToShow(
        showSeasonEpisode: ShowSeasonEpisode,
        token: String?,
    ) {
        _isLoading.value = true
        val status = traktAccountDataSource.checkInToShow(showSeasonEpisode, token)
        _traktCheckInEvent.emit(status)
        _isLoading.value = false
    }

    override fun isAuthorizedOnTrakt(): StateFlow<Boolean> {
        return traktAccessToken.map { token ->
            token?.isTraktAccessTokenValid() == true
        }.stateIn(
            scope = repositoryScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false,
        )
    }

    override suspend fun checkIfShowIsFavorite(imdbID: String?) {
        if (imdbID.isNullOrEmpty()) {
            _favoriteShow.value = null
            return
        }
        val favorite = traktDao.getFavoriteShow(imdbID)
        _favoriteShow.value = favorite?.asDomainModel()
    }

    override fun getFavoriteShowFlow(imdbID: String): Flow<TraktUserListItem?> {
        return traktDao.getFavoriteShowFlow(imdbID).map { it?.asDomainModel() }
    }

    override suspend fun clearFavorites() {
        traktDao.deleteAllFavoriteShows()
    }

    override fun tableUpdate(tableName: String): Flow<TableUpdate?> {
        return upnextDao.getTableLastUpdate(tableName).map { it?.asDomainModel() }
    }

    override suspend fun getTraktShowRating(imdbID: String?) {
        if (imdbID.isNullOrEmpty()) return

        val result = traktRecommendationsDataSource.getTraktShowRating(imdbID)
        if (result.isSuccess) {
            _traktShowRating.value = result.getOrNull()
        } else {
            // Optionally handle error or clear rating
             _traktShowRating.value = null
        }
    }

    override suspend fun getTraktShowStats(imdbID: String?) {
        if (imdbID.isNullOrEmpty()) return

        val result = traktRecommendationsDataSource.getTraktShowStats(imdbID)
        if (result.isSuccess) {
            _traktShowStats.value = result.getOrNull()
        } else {
             _traktShowStats.value = null
        }
    }

    override suspend fun getTraktIdLookup(imdbID: String): Result<Int?> {
        return traktRecommendationsDataSource.getTraktIdFromImdbId(imdbID)
    }
}
