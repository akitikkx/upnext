package com.theupnextapp.repository.fakes

import com.theupnextapp.database.DatabaseTraktAccess
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
import com.theupnextapp.network.models.trakt.NetworkTraktHistoryResponse
import com.theupnextapp.network.models.trakt.NetworkTraktMyScheduleResponse
import com.theupnextapp.network.models.trakt.NetworkTraktPersonResponse
import com.theupnextapp.network.models.trakt.NetworkTraktPersonShowCreditsResponse
import com.theupnextapp.network.models.trakt.NetworkTraktPlaybackResponse
import com.theupnextapp.network.models.trakt.NetworkTraktRecommendationsResponse
import com.theupnextapp.network.models.trakt.NetworkTraktShowProgressResponse
import com.theupnextapp.network.models.trakt.NetworkTraktWatchedShowsResponse
import com.theupnextapp.repository.TraktRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeTraktRepository : TraktRepository {
    override fun tableUpdate(tableName: String): Flow<TableUpdate?> = flowOf(null)

    private val _traktPopularShows = MutableStateFlow<List<TraktPopularShows>>(emptyList())
    override val traktPopularShows: Flow<List<TraktPopularShows>> = _traktPopularShows.asStateFlow()

    private val _traktTrendingShows = MutableStateFlow<List<TraktTrendingShows>>(emptyList())
    override val traktTrendingShows: Flow<List<TraktTrendingShows>> = _traktTrendingShows.asStateFlow()

    private val _traktMostAnticipatedShows = MutableStateFlow<List<TraktMostAnticipated>>(emptyList())
    override val traktMostAnticipatedShows: Flow<List<TraktMostAnticipated>> = _traktMostAnticipatedShows.asStateFlow()

    private val _traktWatchlistShows = MutableStateFlow<List<TraktUserListItem>>(emptyList())
    override val traktWatchlistShows: Flow<List<TraktUserListItem>> = _traktWatchlistShows.asStateFlow()

    private val _traktAccessToken = MutableStateFlow<TraktAccessToken?>(null)
    override val traktAccessToken: StateFlow<TraktAccessToken?> = _traktAccessToken.asStateFlow()

    override suspend fun getTraktAccessTokenSync(): TraktAccessToken? = _traktAccessToken.value

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingTraktTrending = MutableStateFlow(false)
    override val isLoadingTraktTrending: StateFlow<Boolean> = _isLoadingTraktTrending.asStateFlow()

    private val _isLoadingTraktPopular = MutableStateFlow(false)
    override val isLoadingTraktPopular: StateFlow<Boolean> = _isLoadingTraktPopular.asStateFlow()

    private val _isLoadingTraktMostAnticipated = MutableStateFlow(false)
    override val isLoadingTraktMostAnticipated: StateFlow<Boolean> = _isLoadingTraktMostAnticipated.asStateFlow()

    private val _traktShowRating = MutableStateFlow<TraktShowRating?>(null)
    override val traktShowRating: StateFlow<TraktShowRating?> = _traktShowRating.asStateFlow()

    private val _traktShowStats = MutableStateFlow<TraktShowStats?>(null)
    override val traktShowStats: StateFlow<TraktShowStats?> = _traktShowStats.asStateFlow()

    private val _watchlistShow = MutableStateFlow<TraktUserListItem?>(null)
    override val watchlistShow: StateFlow<TraktUserListItem?> = _watchlistShow.asStateFlow()

    private val _traktUserCustomLists = MutableStateFlow<List<TraktUserList>>(emptyList())
    override val traktUserCustomLists: Flow<List<TraktUserList>> = _traktUserCustomLists.asStateFlow()

    private val _isLoadingWatchlistShows = MutableStateFlow(false)
    override val isLoadingWatchlistShows: StateFlow<Boolean> = _isLoadingWatchlistShows.asStateFlow()

    private val _watchlistShowsError = MutableStateFlow<String?>(null)
    override val watchlistShowsError: StateFlow<String?> = _watchlistShowsError.asStateFlow()

    private val _isLoadingUserCustomLists = MutableStateFlow(false)
    override val isLoadingUserCustomLists: StateFlow<Boolean> = _isLoadingUserCustomLists.asStateFlow()

    private val _userCustomListsError = MutableStateFlow<String?>(null)
    override val userCustomListsError: StateFlow<String?> = _userCustomListsError.asStateFlow()

    private val _traktCheckInEvent = MutableSharedFlow<TraktCheckInStatus>()
    override val traktCheckInEvent: SharedFlow<TraktCheckInStatus> = _traktCheckInEvent.asSharedFlow()

    // Test helper to control isAuthorized
    private val _isAuthorized = MutableStateFlow(false)

    override fun isAuthorizedOnTrakt(): StateFlow<Boolean> = _isAuthorized.asStateFlow()

    // Results mapping
    var getAccessTokenResult: kotlin.Result<TraktAccessToken> = kotlin.Result.failure(Exception("Not implemented"))

    override suspend fun getTraktAccessToken(code: String): kotlin.Result<TraktAccessToken> = getAccessTokenResult

    override suspend fun revokeTraktAccessToken(traktAccessToken: TraktAccessToken) {}

    var revokeTokenResult: kotlin.Result<Unit> = kotlin.Result.success(Unit)

    override suspend fun revokeTraktAccessToken(token: String): kotlin.Result<Unit> = revokeTokenResult

    var getRefreshTokenResult: kotlin.Result<TraktAccessToken> = kotlin.Result.failure(Exception("Not implemented"))

    override suspend fun getTraktAccessRefreshToken(refreshToken: String?): kotlin.Result<TraktAccessToken> = getRefreshTokenResult

    override fun getTraktAccessTokenRaw(): DatabaseTraktAccess? = null

    var refreshWatchlistResult: kotlin.Result<Unit> = kotlin.Result.success(Unit)
    var refreshWatchlistCallCount = 0
        private set

    override suspend fun refreshWatchlist(token: String): kotlin.Result<Unit> {
        refreshWatchlistCallCount++
        return refreshWatchlistResult
    }

    var addToWatchlistResult: kotlin.Result<Unit> = kotlin.Result.success(Unit)

    override suspend fun addToWatchlist(
        traktId: Int,
        imdbID: String,
        token: String,
        title: String?,
        originalImageUrl: String?,
        mediumImageUrl: String?,
    ): kotlin.Result<Unit> = addToWatchlistResult

    var removeFromWatchlistResult: kotlin.Result<Unit> = kotlin.Result.success(Unit)
    var lastRemovedTraktId: Int? = null
        private set
    var removeFromWatchlistCallCount = 0
        private set

    override suspend fun removeFromWatchlist(
        traktId: Int,
        token: String,
    ): kotlin.Result<Unit> {
        removeFromWatchlistCallCount++
        lastRemovedTraktId = traktId
        return removeFromWatchlistResult
    }

    override suspend fun refreshFavoriteShows(
        forceRefresh: Boolean,
        token: String?,
    ) {}

    override suspend fun refreshFavoriteShows(token: String): kotlin.Result<Unit> = kotlin.Result.success(Unit)

    override suspend fun addShowToFavorites(
        imdbId: String,
        token: String,
    ): kotlin.Result<Unit> = kotlin.Result.success(Unit)

    override suspend fun addShowToList(
        imdbID: String?,
        token: String?,
    ) {}

    override suspend fun removeShowFromList(
        traktId: Int?,
        imdbID: String?,
        token: String?,
    ) {}

    override suspend fun checkIfShowIsOnWatchlist(imdbID: String?) {}

    override fun getWatchlistShowFlow(imdbID: String): Flow<TraktUserListItem?> = flowOf(null)

    override suspend fun removeShowFromFavorites(
        traktId: Int,
        imdbId: String,
        token: String,
    ): kotlin.Result<Unit> =
        kotlin.Result.success(
            Unit,
        )

    override suspend fun clearWatchlist() {}

    override suspend fun refreshTraktTrendingShows(forceRefresh: Boolean) {}

    override suspend fun refreshTraktPopularShows(forceRefresh: Boolean) {}

    override suspend fun refreshTraktMostAnticipatedShows(forceRefresh: Boolean) {}

    override suspend fun getTraktShowRating(imdbID: String?) {}

    override suspend fun getTraktShowStats(imdbID: String?) {}

    override suspend fun checkInToShow(
        showTraktId: Int,
        seasonNumber: Int,
        episodeNumber: Int,
    ) {}

    override suspend fun cancelCheckIn() {}

    var rateShowCalls = mutableListOf<Pair<String, Int>>()
    var rateShowResult: Result<Unit> = Result.success(Unit)

    override suspend fun rateShow(
        imdbId: String,
        rating: Int,
    ): Result<Unit> {
        rateShowCalls.add(imdbId to rating)
        return rateShowResult
    }

    var fakeUserShowRating: Int? = null

    override suspend fun getUserShowRating(imdbId: String): Int? = fakeUserShowRating

    override suspend fun getTraktIdLookup(imdbID: String): kotlin.Result<Int?> = kotlin.Result.success(null)

    var personSummaryResult: kotlin.Result<NetworkTraktPersonResponse> =
        kotlin.Result.success(
            NetworkTraktPersonResponse(name = "Fake", ids = null, biography = null, birthday = null, death = null, birthplace = null, homepage = null, gender = null, known_for_department = null, social_ids = null),
        )

    override suspend fun getTraktPersonSummary(id: String): kotlin.Result<NetworkTraktPersonResponse> = personSummaryResult

    var personCreditsResult: kotlin.Result<NetworkTraktPersonShowCreditsResponse> =
        kotlin.Result.success(
            NetworkTraktPersonShowCreditsResponse(cast = emptyList()),
        )

    override suspend fun getTraktPersonShowCredits(id: String): kotlin.Result<NetworkTraktPersonShowCreditsResponse> = personCreditsResult

    override suspend fun getTraktPersonIdLookup(tvMazeId: String): kotlin.Result<Int?> = kotlin.Result.success(null)

    override suspend fun getTraktPersonIdSearch(name: String): kotlin.Result<Int?> = kotlin.Result.success(null)

    override suspend fun getTraktMySchedule(
        token: String,
        startDate: String,
        days: Int,
    ): kotlin.Result<NetworkTraktMyScheduleResponse> =
        kotlin.Result.success(
            NetworkTraktMyScheduleResponse(),
        )

    var showCastResult: kotlin.Result<List<TraktCast>> = kotlin.Result.success(emptyList())

    override suspend fun getShowCast(imdbID: String): kotlin.Result<List<TraktCast>> = showCastResult

    var relatedShowsResult: kotlin.Result<List<TraktRelatedShows>> = kotlin.Result.success(emptyList())

    override suspend fun getRelatedShows(imdbID: String): kotlin.Result<List<TraktRelatedShows>> = relatedShowsResult

    override suspend fun getTraktPlaybackProgress(token: String): kotlin.Result<List<NetworkTraktPlaybackResponse>> =
        kotlin.Result.success(
            emptyList(),
        )

    override suspend fun getTraktWatchedShows(token: String): kotlin.Result<List<NetworkTraktWatchedShowsResponse>> =
        kotlin.Result.success(
            emptyList(),
        )

    override suspend fun getTraktShowProgress(
        token: String,
        showId: String,
    ): kotlin.Result<NetworkTraktShowProgressResponse> =
        kotlin.Result.success(
            NetworkTraktShowProgressResponse(aired = 1, completed = 1, lastWatchedAt = "", lastEpisode = null, nextEpisode = null),
        )

    override suspend fun getTraktRecentHistory(token: String): kotlin.Result<List<NetworkTraktHistoryResponse>> =
        kotlin.Result.success(
            emptyList(),
        )

    override suspend fun getTraktRecommendations(token: String): kotlin.Result<NetworkTraktRecommendationsResponse> =
        kotlin.Result.success(
            NetworkTraktRecommendationsResponse(),
        )

    // TEST HELPERS
    fun setIsAuthorized(authorized: Boolean) {
        _isAuthorized.value = authorized
    }

    fun setAccessToken(token: TraktAccessToken?) {
        _traktAccessToken.value = token
    }

    fun setWatchlistShows(shows: List<TraktUserListItem>) {
        _traktWatchlistShows.value = shows
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    fun setTrendingShows(shows: List<TraktTrendingShows>) {
        _traktTrendingShows.value = shows
    }

    fun setPopularShows(shows: List<TraktPopularShows>) {
        _traktPopularShows.value = shows
    }

    fun setMostAnticipatedShows(shows: List<TraktMostAnticipated>) {
        _traktMostAnticipatedShows.value = shows
    }
}
