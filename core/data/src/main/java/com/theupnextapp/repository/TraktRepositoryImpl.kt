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
import com.theupnextapp.domain.TraktCast
import com.theupnextapp.domain.TraktCheckInStatus
import com.theupnextapp.domain.TraktMostAnticipated
import com.theupnextapp.domain.TraktPopularShows
import com.theupnextapp.domain.TraktRelatedShows
import com.theupnextapp.domain.TraktShowRating
import com.theupnextapp.domain.TraktShowStats
import com.theupnextapp.domain.TraktTrendingShows
import com.theupnextapp.domain.TraktUserList
import com.theupnextapp.domain.TraktUserListItem
import com.theupnextapp.domain.isTraktAccessTokenValid
import com.theupnextapp.network.TvMazeService
import com.theupnextapp.network.models.trakt.NetworkTraktMyScheduleResponse
import com.theupnextapp.network.models.trakt.NetworkTraktPersonResponse
import com.theupnextapp.network.models.trakt.NetworkTraktPersonShowCreditsResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
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

    companion object {
        private const val FLOW_STOP_TIMEOUT_MS = 5000L
    }

    @Volatile
    private var hasMigratedFavoritesThisSession = false

    override val traktAccessToken: StateFlow<TraktAccessToken?> =
        traktDao.getTraktAccessData().map {
            it?.asDomainModel()
        }.stateIn(
            scope = repositoryScope,
            started = SharingStarted.WhileSubscribed(FLOW_STOP_TIMEOUT_MS),
            initialValue = null,
        )

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

    override suspend fun refreshWatchlist(token: String): Result<Unit> {
        if (token.isEmpty()) return Result.failure(IllegalArgumentException("Empty token"))
        if (_isLoadingFavoriteShows.value) return Result.success(Unit)

        _isLoadingFavoriteShows.value = true
        _favoriteShowsError.value = null

        // One-time migration: move shows from "Upnext Favorites" custom list
        // to the native Trakt watchlist. Runs once per app session.
        if (!hasMigratedFavoritesThisSession) {
            traktAccountDataSource.migrateFavoritesToWatchlist(token)
            hasMigratedFavoritesThisSession = true
        }

        val result = traktAccountDataSource.getWatchlist(token)
        if (result.isSuccess) {
            val responseItems = result.getOrNull()
            if (responseItems != null) {
                val favoriteShowsList = withContext(Dispatchers.IO) {
                    val jobs = responseItems.map { item ->
                        async {
                            val traktId = item.show.ids.trakt
                            val existingLocalShow = traktDao.getFavoriteShowByTraktId(traktId)

                            val needsImageFetch =
                                existingLocalShow == null ||
                                    existingLocalShow.originalImageUrl.isNullOrEmpty() ||
                                    existingLocalShow.mediumImageUrl.isNullOrEmpty() ||
                                    existingLocalShow.tvMazeID == null

                            var dbShow = com.theupnextapp.database.DatabaseFavoriteShows(
                                id = traktId,
                                title = item.show.title,
                                year = item.show.year.toString(),
                                mediumImageUrl = "",
                                originalImageUrl = "",
                                imdbID = item.show.ids.imdb,
                                slug = item.show.ids.slug,
                                tmdbID = item.show.ids.tmdb,
                                traktID = traktId,
                                tvdbID = item.show.ids.tvdb,
                                tvMazeID = item.show.ids.tvMazeID,
                                network = item.show.network,
                                status = item.show.status,
                                rating = item.show.rating
                            )

                            if (needsImageFetch) {
                                item.show.ids.imdb?.let { imdbId ->
                                    val (tvMazeIdResult, poster, heroImage) = traktAccountDataSource.getImages(imdbId)
                                    dbShow = dbShow.copy(
                                        originalImageUrl = poster ?: existingLocalShow?.originalImageUrl,
                                        mediumImageUrl = heroImage ?: existingLocalShow?.mediumImageUrl,
                                        tvMazeID = tvMazeIdResult ?: existingLocalShow?.tvMazeID
                                    )
                                }
                            } else if (existingLocalShow != null) {
                                dbShow = dbShow.copy(
                                    originalImageUrl = existingLocalShow.originalImageUrl,
                                    mediumImageUrl = existingLocalShow.mediumImageUrl,
                                    tvMazeID = existingLocalShow.tvMazeID
                                )
                            }
                            dbShow
                        }
                    }
                    jobs.awaitAll()
                }

                // The native Trakt watchlist is our single source of truth.
                // Upsert all shows from the API, then prune any local shows
                // that are no longer in the API response.
                withContext(Dispatchers.IO) {
                    traktDao.insertAllFavoriteShows(*favoriteShowsList.toTypedArray())

                    val apiTraktIds = favoriteShowsList.mapNotNull { it.traktID }.toSet()
                    val localTraktIds = traktDao.getAllFavoriteShowTraktIds().toSet()
                    val idsToRemove = (localTraktIds - apiTraktIds).toList()

                    if (idsToRemove.isNotEmpty()) {
                        traktDao.deleteFavoriteShowsByTraktIds(idsToRemove)
                    }
                }
            }
        } else {
            _favoriteShowsError.value = result.exceptionOrNull()?.message
        }

        _isLoadingFavoriteShows.value = false
        return Result.success(Unit)
    }

    override suspend fun addToWatchlist(
        traktId: Int,
        imdbID: String,
        token: String,
    ): Result<Unit> {
        _isLoadingFavoriteShows.value = true
        _favoriteShowsError.value = null

        val result = traktAccountDataSource.addToWatchlist(traktId, token)
        if (result.isFailure) {
            _favoriteShowsError.value = result.exceptionOrNull()?.message
        }
        _isLoadingFavoriteShows.value = false
        return result
    }

    override suspend fun removeFromWatchlist(
        traktId: Int,
        token: String,
    ): Result<Unit> {
        _isLoadingFavoriteShows.value = true
        _favoriteShowsError.value = null

        val result = traktAccountDataSource.removeFromWatchlist(traktId, token)
        if (result.isSuccess) {
            // Optimistic local delete — immediate UI feedback
            withContext(Dispatchers.IO) {
                traktDao.deleteFavoriteShowByTraktId(traktId)
            }
        } else {
            _favoriteShowsError.value = result.exceptionOrNull()?.message
        }
        _isLoadingFavoriteShows.value = false
        return result
    }

    @Deprecated("Use refreshWatchlist instead", ReplaceWith("refreshWatchlist(token)"))
    override suspend fun refreshFavoriteShows(
        forceRefresh: Boolean,
        token: String?,
    ) {
        refreshFavoriteShows(token ?: "")
    }

    @Deprecated("Use refreshWatchlist instead")
    override suspend fun refreshFavoriteShows(token: String): Result<Unit> {
        return refreshWatchlist(token)
    }

    @Deprecated("Use addToWatchlist instead")
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

    @Deprecated("Use addToWatchlist instead")
    override suspend fun addShowToList(
        imdbID: String?,
        token: String?,
    ) {
        if (imdbID != null && token != null) {
            addShowToFavorites(imdbID, token)
        }
    }

    @Deprecated("Use removeFromWatchlist instead")
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

    @Deprecated("Use removeFromWatchlist instead")
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
        showTraktId: Int,
        seasonNumber: Int,
        episodeNumber: Int,
    ) {
        val token = getTraktAccessTokenSync()?.access_token
        val status = traktAccountDataSource.checkInToShow(
            showTraktId = showTraktId,
            seasonNumber = seasonNumber,
            episodeNumber = episodeNumber,
            token = token
        )
        _traktCheckInEvent.emit(status)
    }

    override suspend fun cancelCheckIn() {
        val token = getTraktAccessTokenSync()?.access_token
        val result = traktAccountDataSource.cancelCheckIn(token)
        if (result.isSuccess) {
            // Emit an empty/null-like status to clear the check-in data from the UI
            _traktCheckInEvent.emit(com.theupnextapp.domain.TraktCheckInStatus(message = "Check-in cancelled"))
        } else {
            _traktCheckInEvent.emit(com.theupnextapp.domain.TraktCheckInStatus(message = result.exceptionOrNull()?.message ?: "Failed to cancel check-in"))
        }
    }

    override suspend fun rateShow(imdbId: String, rating: Int): Result<Unit> {
        val token = getTraktAccessTokenSync()?.access_token
            ?: return Result.failure(IllegalStateException("Not logged in"))
        val result = traktAccountDataSource.rateShow(imdbId, rating, token)
        if (result.isSuccess) {
            // Refresh aggregate rating to reflect the user's new vote
            getTraktShowRating(imdbId)
        }
        return result
    }

    override fun isAuthorizedOnTrakt(): StateFlow<Boolean> {
        return traktAccessToken.map { token ->
            token?.isTraktAccessTokenValid() == true
        }.stateIn(
            scope = repositoryScope,
            started = SharingStarted.WhileSubscribed(FLOW_STOP_TIMEOUT_MS),
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

    override suspend fun getTraktPersonSummary(id: String): Result<NetworkTraktPersonResponse> {
        return traktRecommendationsDataSource.getPersonSummary(id)
    }

    override suspend fun getTraktPersonShowCredits(id: String): Result<NetworkTraktPersonShowCreditsResponse> {
        return traktRecommendationsDataSource.getPersonShowCredits(id)
    }

    override suspend fun getTraktPersonIdLookup(tvMazeId: String): Result<Int?> {
        return traktRecommendationsDataSource.getTraktPersonIdFromTvMazeId(tvMazeId)
    }

    override suspend fun getTraktPersonIdSearch(name: String): Result<Int?> {
        return traktRecommendationsDataSource.getTraktPersonIdFromSearch(name)
    }

    override suspend fun getTraktAccessTokenSync(): TraktAccessToken? = withContext(Dispatchers.IO) {
        getTraktAccessTokenRaw()?.asDomainModel()
    }

    override suspend fun getTraktMySchedule(token: String, startDate: String, days: Int): Result<NetworkTraktMyScheduleResponse> {
        return traktAccountDataSource.getTraktMySchedule(token, startDate, days)
    }

    override suspend fun getShowCast(imdbID: String): Result<List<TraktCast>> {
        return traktRecommendationsDataSource.getShowCast(imdbID)
    }

    override suspend fun getRelatedShows(imdbID: String): Result<List<TraktRelatedShows>> {
        return traktRecommendationsDataSource.getRelatedShows(imdbID)
    }

    override suspend fun getTraktPlaybackProgress(token: String): Result<List<com.theupnextapp.network.models.trakt.NetworkTraktPlaybackResponse>> {
        return traktAccountDataSource.getTraktPlaybackProgress(token)
    }

    override suspend fun getTraktRecentHistory(token: String): Result<List<com.theupnextapp.network.models.trakt.NetworkTraktHistoryResponse>> {
        return traktAccountDataSource.getTraktRecentHistory(token)
    }

    override suspend fun getTraktShowProgress(token: String, showId: String): Result<com.theupnextapp.network.models.trakt.NetworkTraktShowProgressResponse> {
        return traktAccountDataSource.getTraktShowProgress(token, showId)
    }

    override suspend fun getTraktWatchedShows(token: String): Result<List<com.theupnextapp.network.models.trakt.NetworkTraktWatchedShowsResponse>> {
        return traktAccountDataSource.getTraktWatchedShows(token)
    }

    override suspend fun getTraktRecommendations(token: String): Result<com.theupnextapp.network.models.trakt.NetworkTraktRecommendationsResponse> {
        return traktAccountDataSource.getTraktRecommendations(token)
    }
}
