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
import com.theupnextapp.database.DatabaseWatchHistory
import com.theupnextapp.database.DatabaseWatchlistShows
import com.theupnextapp.database.TraktDao
import com.theupnextapp.database.UpnextDao
import com.theupnextapp.database.asDomainModel
import com.theupnextapp.datasource.TraktAccountDataSource
import com.theupnextapp.datasource.TraktAuthDataSource
import com.theupnextapp.datasource.TraktRecommendationsDataSource
import com.theupnextapp.domain.ShowSeasonEpisode
import com.theupnextapp.domain.TableUpdate
import com.theupnextapp.domain.TrackingProvider
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
import com.theupnextapp.domain.TrendingShow
import com.theupnextapp.domain.WatchHistoryItem
import com.theupnextapp.domain.WatchHistorySyncResult
import com.theupnextapp.domain.isTraktAccessTokenValid
import com.theupnextapp.network.TvMazeService
import com.theupnextapp.network.models.trakt.NetworkTraktHistoryResponse
import com.theupnextapp.network.models.trakt.NetworkTraktMyScheduleResponse
import com.theupnextapp.network.models.trakt.NetworkTraktPersonResponse
import com.theupnextapp.network.models.trakt.NetworkTraktPersonShowCreditsResponse
import com.theupnextapp.network.models.trakt.NetworkTraktPlaybackResponse
import com.theupnextapp.network.models.trakt.NetworkTraktRecommendationsResponse
import com.theupnextapp.network.models.trakt.NetworkTraktShowProgressResponse
import com.theupnextapp.network.models.trakt.NetworkTraktWatchedShowsResponse
import com.theupnextapp.network.models.trakt.TraktHistoryPage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Provider

class TraktRepositoryImpl(
    upnextDao: UpnextDao,
    tvMazeService: TvMazeService,
    private val traktDao: TraktDao,
    private val traktAuthDataSource: TraktAuthDataSource,
    private val traktRecommendationsDataSource: TraktRecommendationsDataSource,
    private val traktAccountDataSource: TraktAccountDataSource,
    private val dashboardRepositoryProvider: Provider<DashboardRepository>,
) : BaseRepository(upnextDao, tvMazeService), TraktRepository, TrackingProvider {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val FLOW_STOP_TIMEOUT_MS = 5000L
        private const val SYNC_HISTORY_PAGE_SIZE = 100
        private const val OLDER_HISTORY_PAGE_SIZE = 50
    }

    override val providerId: String = "trakt"

    @Volatile
    private var hasMigratedWatchlistThisSession = false

    override val traktAccessToken: StateFlow<TraktAccessToken?> =
        traktDao.getTraktAccessData().map {
            it?.asDomainModel()
        }.stateIn(
            scope = repositoryScope,
            started = SharingStarted.WhileSubscribed(FLOW_STOP_TIMEOUT_MS),
            initialValue = null,
        )

    override val isAuthorized: StateFlow<Boolean> = isAuthorizedOnTrakt()

    override val watchHistory: Flow<List<WatchHistoryItem>> =
        traktDao.getWatchHistoryFlow().map { list ->
            list.asDomainModel()
        }

    private val _isLoadingWatchHistory = MutableStateFlow(false)
    override val isLoadingWatchHistory: StateFlow<Boolean> = _isLoadingWatchHistory.asStateFlow()

    private val _watchHistoryError = MutableStateFlow<String?>(null)
    override val watchHistoryError: StateFlow<String?> = _watchHistoryError.asStateFlow()

    private val _isLoadingTraktTrending = MutableStateFlow(false)
    override val isLoadingTraktTrending: StateFlow<Boolean> = _isLoadingTraktTrending.asStateFlow()
    override val isLoadingTrending: StateFlow<Boolean> = _isLoadingTraktTrending.asStateFlow()

    override val traktTrendingShows: Flow<List<TraktTrendingShows>> =
        traktDao.getTrendingShows(providerId).map { list ->
            list.asDomainModel().map {
                TraktTrendingShows(
                    id = it.id,
                    title = it.title,
                    year = it.year,
                    mediumImageUrl = it.mediumImageUrl,
                    originalImageUrl = it.originalImageUrl,
                    imdbID = it.imdbID,
                    slug = null,
                    tmdbID = it.tmdbID,
                    traktID = it.id,
                    tvdbID = null,
                    tvMazeID = it.tvMazeID
                )
            }.filter { !it.mediumImageUrl.isNullOrEmpty() || !it.originalImageUrl.isNullOrEmpty() }
        }

    override val trendingShows: Flow<List<TrendingShow>> =
        traktDao.getTrendingShows(providerId).map { list ->
            list.asDomainModel().filter { !it.mediumImageUrl.isNullOrEmpty() || !it.originalImageUrl.isNullOrEmpty() }
        }

    private val _isLoadingTraktPopular = MutableStateFlow(false)
    override val isLoadingTraktPopular: StateFlow<Boolean> = _isLoadingTraktPopular.asStateFlow()

    override val traktPopularShows: Flow<List<TraktPopularShows>> =
        traktDao.getTraktPopular().map { list ->
            list.asDomainModel().filter { !it.mediumImageUrl.isNullOrEmpty() || !it.originalImageUrl.isNullOrEmpty() }
        }

    override val popularShows: Flow<List<TraktPopularShows>> = traktPopularShows

    private val _isLoadingTraktMostAnticipated = MutableStateFlow(false)
    override val isLoadingTraktMostAnticipated: StateFlow<Boolean> =
        _isLoadingTraktMostAnticipated.asStateFlow()

    override val traktMostAnticipatedShows: Flow<List<TraktMostAnticipated>> =
        traktDao.getTraktMostAnticipated().map { list ->
            list.asDomainModel().filter { !it.mediumImageUrl.isNullOrEmpty() || !it.originalImageUrl.isNullOrEmpty() }
        }
        
    override val mostAnticipatedShows: Flow<List<TraktMostAnticipated>> = traktMostAnticipatedShows

    override suspend fun refreshTrendingShows() = refreshTraktTrendingShows(forceRefresh = true)
    override suspend fun refreshPopularShows() = refreshTraktPopularShows(forceRefresh = true)
    override suspend fun refreshMostAnticipatedShows() = refreshTraktMostAnticipatedShows(forceRefresh = true)

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _traktCheckInEvent = MutableSharedFlow<TraktCheckInStatus>(replay = 0)
    override val traktCheckInEvent: SharedFlow<TraktCheckInStatus> =
        _traktCheckInEvent.asSharedFlow()

    private val _isLoadingWatchlistShows = MutableStateFlow(false)
    override val isLoadingWatchlistShows: StateFlow<Boolean> = _isLoadingWatchlistShows.asStateFlow()

    override val traktWatchlistShows: Flow<List<TraktUserListItem>> =
        traktDao.getWatchlistShows().map { list ->
            list.asDomainModel()
        }

    private val _watchlistShowsError = MutableStateFlow<String?>(null)
    override val watchlistShowsError: StateFlow<String?> = _watchlistShowsError.asStateFlow()

    private val _traktShowRating = MutableStateFlow<TraktShowRating?>(null)
    override val traktShowRating: StateFlow<TraktShowRating?> = _traktShowRating.asStateFlow()

    private val _traktShowStats = MutableStateFlow<TraktShowStats?>(null)
    override val traktShowStats: StateFlow<TraktShowStats?> = _traktShowStats.asStateFlow()

    private val _watchlistShow = MutableStateFlow<TraktUserListItem?>(null)
    override val watchlistShow: StateFlow<TraktUserListItem?> = _watchlistShow.asStateFlow()

    private val _isLoadingUserCustomLists = MutableStateFlow(false)
    override val isLoadingUserCustomLists: StateFlow<Boolean> = _isLoadingUserCustomLists.asStateFlow()

    private val _userCustomListsError = MutableStateFlow<String?>(null)
    override val userCustomListsError: StateFlow<String?> = _userCustomListsError.asStateFlow()

    override val traktUserCustomLists: Flow<List<TraktUserList>> =
        traktDao.getCustomListsFlow().map { it.asDomainModel() }

    override fun getCustomListItems(listTraktId: Int): Flow<List<TraktUserListItem>> =
        traktDao.getCustomListItemsFlow(listTraktId).map { it.asDomainModel() }

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
        if (_isLoadingWatchlistShows.value) return Result.success(Unit)

        _isLoadingWatchlistShows.value = true
        _watchlistShowsError.value = null

        val result = traktAccountDataSource.refreshWatchlistShows(token)
        if (result.isFailure) {
            _watchlistShowsError.value = result.exceptionOrNull()?.message
        }

        _isLoadingWatchlistShows.value = false
        return result
    }

    override suspend fun addToWatchlist(
        traktId: Int,
        imdbID: String,
        token: String,
        title: String?,
        originalImageUrl: String?,
        mediumImageUrl: String?,
        tvMazeID: Int?,
        tmdbID: Int?,
        year: String?,
        network: String?,
        status: String?,
        rating: Double?,
    ): Result<Unit> {
        _isLoadingWatchlistShows.value = true
        _watchlistShowsError.value = null

        val result = traktAccountDataSource.addToWatchlist(traktId, token)
        if (result.isSuccess) {
            // Optimistic local insert — immediate UI feedback bypasses Trakt caching delay
            withContext(Dispatchers.IO) {
                traktDao.insertWatchlistShow(
                    DatabaseWatchlistShows(
                        id = traktId, traktID = traktId, imdbID = imdbID, title = title,
                        originalImageUrl = originalImageUrl, mediumImageUrl = mediumImageUrl,
                        year = year, tvMazeID = tvMazeID, slug = null, tmdbID = tmdbID,
                        tvdbID = null, network = network, status = status, rating = rating,
                    )
                )
            }
        } else {
            _watchlistShowsError.value = result.exceptionOrNull()?.message
        }
        _isLoadingWatchlistShows.value = false
        return result
    }

    override suspend fun removeFromWatchlist(
        traktId: Int,
        token: String,
    ): Result<Unit> {
        _isLoadingWatchlistShows.value = true
        _watchlistShowsError.value = null

        val result = traktAccountDataSource.removeFromWatchlist(traktId, token)
        if (result.isSuccess) {
            // Optimistic local delete — immediate UI feedback
            withContext(Dispatchers.IO) {
                traktDao.deleteWatchlistShowByTraktId(traktId)
            }
        } else {
            _watchlistShowsError.value = result.exceptionOrNull()?.message
        }
        _isLoadingWatchlistShows.value = false
        return result
    }

    @Deprecated("Use refreshWatchlist instead", ReplaceWith("refreshWatchlist(token)"))
    override suspend fun refreshFavoriteShows(forceRefresh: Boolean, token: String?) {
        refreshWatchlist(token ?: "")
    }

    @Deprecated("Use refreshWatchlist instead")
    override suspend fun refreshFavoriteShows(token: String): Result<Unit> = refreshWatchlist(token)

    @Deprecated("Use addToWatchlist instead")
    override suspend fun addShowToFavorites(imdbId: String, token: String): Result<Unit> {
        val traktId = getTraktIdLookup(imdbId).getOrNull()
        return if (traktId != null) addToWatchlist(traktId, imdbId, token) else Result.failure(Exception("Trakt ID not found for $imdbId"))
    }

    @Deprecated("Use addToWatchlist instead")
    override suspend fun addShowToList(imdbID: String?, token: String?) {
        if (imdbID != null && token != null) addShowToFavorites(imdbID, token)
    }

    @Deprecated("Use removeFromWatchlist instead")
    override suspend fun removeShowFromFavorites(traktId: Int, imdbId: String, token: String): Result<Unit> =
        removeFromWatchlist(traktId, token)

    @Deprecated("Use removeFromWatchlist instead")
    override suspend fun removeShowFromList(traktId: Int?, imdbID: String?, token: String?) {
        if (traktId != null && imdbID != null && token != null) removeShowFromFavorites(traktId, imdbID, token)
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
            _traktCheckInEvent.emit(TraktCheckInStatus(message = "Check-in cancelled"))
        } else {
            _traktCheckInEvent.emit(TraktCheckInStatus(message = result.exceptionOrNull()?.message ?: "Failed to cancel check-in"))
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

    override suspend fun getUserShowRating(imdbId: String): Int? {
        val token = getTraktAccessTokenSync()?.access_token ?: return null
        return traktAccountDataSource.getUserShowRating(imdbId, token)
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

    override suspend fun checkIfShowIsOnWatchlist(imdbID: String?) {
        if (imdbID.isNullOrEmpty()) {
            _watchlistShow.value = null
            return
        }
        val watchlistShow = traktDao.getWatchlistShow(imdbID)
        _watchlistShow.value = watchlistShow?.asDomainModel()
    }

    override fun getWatchlistShowFlow(imdbID: String): Flow<TraktUserListItem?> {
        return traktDao.getWatchlistShowFlow(imdbID).map { it?.asDomainModel() }
    }

    override suspend fun clearWatchlist() {
        traktDao.deleteAllWatchlistShows()
        traktDao.clearCustomLists()
        traktDao.clearAllCustomListItems()
    }

    override suspend fun refreshUserCustomLists(token: String): Result<Unit> {
        if (token.isEmpty()) return Result.failure(IllegalArgumentException("Token is empty"))
        return withContext(Dispatchers.IO) {
            _isLoadingUserCustomLists.value = true
            _userCustomListsError.value = null
            val result = traktAccountDataSource.refreshUserCustomLists(token)
            if (result.isFailure) {
                _userCustomListsError.value = result.exceptionOrNull()?.message
            }
            _isLoadingUserCustomLists.value = false
            result
        }
    }

    override suspend fun refreshCustomListItems(token: String, listTraktId: Int): Result<Unit> {
        if (token.isEmpty()) return Result.failure(IllegalArgumentException("Token is empty"))
        return withContext(Dispatchers.IO) {
            val result = traktAccountDataSource.refreshCustomListItems(token, listTraktId)
            if (result.isSuccess) {
                enrichCustomListImages(listTraktId)
            }
            result
        }
    }

    private fun enrichCustomListImages(listTraktId: Int) {
        repositoryScope.launch(Dispatchers.IO) {
            val items = traktDao.getCustomListItemsRaw(listTraktId)
            val missing = items.filter { it.originalImageUrl.isNullOrEmpty() && !it.imdbID.isNullOrEmpty() }
            if (missing.isEmpty()) return@launch

            val dashboardRepo = dashboardRepositoryProvider.get()
            for (item in missing) {
                try {
                    val (posterUrl, tvMazeId) = dashboardRepo.getShowImageAndTvmazeId(item.imdbID)
                    if (!posterUrl.isNullOrEmpty() || tvMazeId != null) {
                        traktDao.updateCustomListItemImages(
                            showTraktId = item.traktID,
                            posterUrl = posterUrl,
                            heroImageUrl = item.mediumImageUrl,
                            tvMazeId = tvMazeId,
                        )
                    }
                } catch (e: Exception) {
                    Timber.d(e, "Failed image enrichment for custom list item: ${item.traktID}")
                }
            }
        }
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

    override suspend fun getTraktShowCertification(imdbID: String): Result<String?> {
        return traktRecommendationsDataSource.getTraktShowCertification(imdbID)
    }

    override suspend fun getRegionalTrendingShows(countryCode: String): Result<List<TraktTrendingShows>> {
        return traktRecommendationsDataSource.getRegionalTrendingShows(countryCode)
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

    override suspend fun getTraktPlaybackProgress(token: String): Result<List<NetworkTraktPlaybackResponse>> {
        return traktAccountDataSource.getTraktPlaybackProgress(token)
    }

    override suspend fun getTraktRecentHistory(
        token: String,
        page: Int,
        limit: Int,
        startAt: String?,
        endAt: String?,
    ): Result<TraktHistoryPage> {
        return traktAccountDataSource.getTraktRecentHistory(
            token = token,
            page = page,
            limit = limit,
            startAt = startAt,
            endAt = endAt,
        )
    }

    override suspend fun syncWatchHistory(forceFull: Boolean): Result<WatchHistorySyncResult> {
        return withContext(Dispatchers.IO) {
            _isLoadingWatchHistory.value = true
            _watchHistoryError.value = null
            try {
                val token = traktAccessToken.value?.access_token
                    ?: getTraktAccessTokenSync()?.access_token
                    ?: run {
                        _isLoadingWatchHistory.value = false
                        return@withContext Result.failure(Exception("Not authorized on Trakt"))
                    }

                val latestTimestamp = if (forceFull) null else traktDao.getLatestWatchedTimestamp()
                val response = traktAccountDataSource.getTraktRecentHistory(
                    token = token,
                    page = 1,
                    limit = SYNC_HISTORY_PAGE_SIZE,
                    startAt = latestTimestamp,
                )

                if (response.isSuccess) {
                    val historyPage = response.getOrNull()
                    val items = historyPage?.items.orEmpty()
                    val dbItems = items.mapNotNull { mapToDatabaseWatchHistory(it) }
                    if (dbItems.isNotEmpty()) {
                        traktDao.insertWatchHistory(dbItems)
                        enrichHistoryImages(dbItems)
                    }
                    _isLoadingWatchHistory.value = false
                    Result.success(
                        WatchHistorySyncResult(
                            totalItemCount = historyPage?.totalItemCount,
                            itemsFetchedCount = items.size,
                        ),
                    )
                } else {
                    val error = response.exceptionOrNull() ?: Exception("Failed to sync watch history")
                    _watchHistoryError.value = error.message
                    _isLoadingWatchHistory.value = false
                    Result.failure(error)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to sync watch history")
                _watchHistoryError.value = e.message
                _isLoadingWatchHistory.value = false
                Result.failure(e)
            }
        }
    }

    override suspend fun loadOlderWatchHistory(): Result<WatchHistorySyncResult> {
        return withContext(Dispatchers.IO) {
            _isLoadingWatchHistory.value = true
            try {
                val token = traktAccessToken.value?.access_token
                    ?: getTraktAccessTokenSync()?.access_token
                    ?: run {
                        _isLoadingWatchHistory.value = false
                        return@withContext Result.failure(Exception("Not authorized on Trakt"))
                    }

                val oldestTimestamp = traktDao.getOldestWatchedTimestamp()
                val response = traktAccountDataSource.getTraktRecentHistory(
                    token = token,
                    page = 1,
                    limit = OLDER_HISTORY_PAGE_SIZE,
                    endAt = oldestTimestamp,
                )

                if (response.isSuccess) {
                    val historyPage = response.getOrNull()
                    val items = historyPage?.items.orEmpty()
                    val dbItems = items.mapNotNull { mapToDatabaseWatchHistory(it) }
                    if (dbItems.isNotEmpty()) {
                        traktDao.insertWatchHistory(dbItems)
                        enrichHistoryImages(dbItems)
                    }
                    _isLoadingWatchHistory.value = false
                    Result.success(
                        WatchHistorySyncResult(
                            totalItemCount = historyPage?.totalItemCount,
                            itemsFetchedCount = items.size,
                        ),
                    )
                } else {
                    val error = response.exceptionOrNull() ?: Exception("Failed to load older watch history")
                    _isLoadingWatchHistory.value = false
                    Result.failure(error)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load older watch history")
                _isLoadingWatchHistory.value = false
                Result.failure(e)
            }
        }
    }

    override suspend fun clearWatchHistory() {
        withContext(Dispatchers.IO) {
            traktDao.clearWatchHistory()
        }
    }

    private fun enrichHistoryImages(history: List<DatabaseWatchHistory>) {
        if (history.isEmpty()) return
        repositoryScope.launch(Dispatchers.IO) {
            val distinctShows = history.mapNotNull { item ->
                if (item.showImdbId != null) item.showTraktId to item.showImdbId else null
            }.distinctBy { it.first }

            val dashboardRepo = dashboardRepositoryProvider.get()

            for ((showTraktId, imdbId) in distinctShows) {
                try {
                    val (showPosterUrl, _) = dashboardRepo.getShowImageAndTvmazeId(imdbId)
                    if (!showPosterUrl.isNullOrEmpty()) {
                        traktDao.updateShowPosterForShow(showTraktId, showPosterUrl)
                    }
                } catch (e: Exception) {
                    Timber.d(e, "Failed to resolve show poster for showTraktId: $showTraktId")
                }
            }

            val missingEpisodeImages = history.filter { it.episodeImageUrl.isNullOrEmpty() && it.showImdbId != null }
            for (item in missingEpisodeImages) {
                try {
                    val (episodeStillUrl, _) = dashboardRepo.getEpisodeImageAndTvmazeId(
                        item.showImdbId,
                        item.seasonNumber,
                        item.episodeNumber,
                    )
                    if (!episodeStillUrl.isNullOrEmpty()) {
                        traktDao.updateWatchHistoryImages(
                            historyId = item.historyId,
                            episodeImageUrl = episodeStillUrl,
                            showPosterUrl = item.showPosterUrl,
                        )
                    }
                } catch (e: Exception) {
                    Timber.d(e, "Failed to resolve episode image for historyId: ${item.historyId}")
                }
            }
        }
    }

    private fun mapToDatabaseWatchHistory(item: NetworkTraktHistoryResponse): DatabaseWatchHistory? {
        val historyId = item.id ?: return null
        val traktId = item.show?.ids?.trakt ?: return null
        val watchedAt = item.watchedAt.orEmpty()
        val watchedAtEpochMillis = parseIsoToEpochMillis(watchedAt)
        return DatabaseWatchHistory(
            historyId = historyId,
            watchedAt = watchedAt,
            watchedAtEpochMillis = watchedAtEpochMillis,
            showTraktId = traktId,
            episodeTraktId = item.episode?.ids?.trakt,
            showTvmazeId = null,
            showImdbId = item.show?.ids?.imdb,
            showTitle = item.show?.title.orEmpty(),
            seasonNumber = item.episode?.season ?: 0,
            episodeNumber = item.episode?.number ?: 0,
            episodeTitle = item.episode?.title.orEmpty(),
            episodeImageUrl = null,
            showPosterUrl = null,
        )
    }

    private fun parseIsoToEpochMillis(dateString: String): Long {
        if (dateString.isEmpty()) return System.currentTimeMillis()
        return try {
            Instant.parse(dateString).toEpochMilli()
        } catch (e: Exception) {
            try {
                ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_ZONED_DATE_TIME)
                    .toInstant()
                    .toEpochMilli()
            } catch (e2: Exception) {
                System.currentTimeMillis()
            }
        }
    }

    override suspend fun getTraktShowProgress(token: String, showId: String): Result<NetworkTraktShowProgressResponse> {
        return traktAccountDataSource.getTraktShowProgress(token, showId)
    }

    override suspend fun getTraktWatchedShows(token: String): Result<List<NetworkTraktWatchedShowsResponse>> {
        return traktAccountDataSource.getTraktWatchedShows(token)
    }

    override suspend fun getTraktRecommendations(token: String): Result<NetworkTraktRecommendationsResponse> {
        return traktAccountDataSource.getTraktRecommendations(token)
    }

    override fun invalidateShowProgress(traktId: Int) {
        traktAccountDataSource.invalidateShowProgressCache(traktId)
    }
}
