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

package com.theupnextapp.ui.showDetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.common.utils.TraktAuthManager
import com.theupnextapp.domain.Result
import com.theupnextapp.domain.ShowCast
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.ShowDetailSummary
import com.theupnextapp.domain.ShowNextEpisode
import com.theupnextapp.domain.ShowPreviousEpisode
import com.theupnextapp.domain.TmdbWatchProviders
import com.theupnextapp.domain.TraktCast
import com.theupnextapp.domain.TraktRelatedShows
import com.theupnextapp.domain.TraktShowRating
import com.theupnextapp.domain.TraktShowStats
import com.theupnextapp.domain.TraktUserListItem
import com.theupnextapp.domain.emptyShowData
import com.theupnextapp.repository.ShowDetailRepository
import com.theupnextapp.repository.TraktRepository
import com.theupnextapp.ui.common.BaseTraktViewModel
import com.theupnextapp.work.AddToWatchlistWorker
import com.theupnextapp.work.RemoveFromWatchlistWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ShowDetailViewModel
    @Inject
    constructor(
        private val showDetailRepository: ShowDetailRepository,
        private val workManager: WorkManager,
        private val traktRepository: TraktRepository,
        private val firebaseCrashlytics: FirebaseCrashlytics,
        val traktAuthManager: TraktAuthManager,
    ) : BaseTraktViewModel(
            traktRepository,
            workManager,
            traktAuthManager,
        ) {
        private val _show = MutableLiveData<ShowDetailArg?>()

        private val _navigateToSeasons = MutableStateFlow(false)
        val navigateToSeasons: StateFlow<Boolean> = _navigateToSeasons.asStateFlow()

        private val _uiState = MutableStateFlow(ShowDetailUiState())
        val uiState: StateFlow<ShowDetailUiState> = _uiState.asStateFlow()

        private val _isLoading = MutableStateFlow(false)
        val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

        private val observedWatchlistShow =
            _uiState.map { it.showSummary?.imdbID }
                .distinctUntilChanged()
                .flatMapLatest { imdbID ->
                    if (imdbID != null) {
                        traktRepository.getWatchlistShowFlow(imdbID)
                    } else {
                        kotlinx.coroutines.flow.flowOf(null)
                    }
                }
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000L),
                    null,
                )

        // Optimistic override: toggles immediately on click,
        // null means "use the DB flow value"
        private val _watchlistOverride = MutableStateFlow<Boolean?>(null)

        val isWatchlistShow: StateFlow<Boolean> =
            combine(observedWatchlistShow, _watchlistOverride) { dbValue, override ->
                override ?: (dbValue != null)
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000L),
                false,
            )

        val isWatchlistLoading: StateFlow<Boolean> =
            _uiState.map { it.showSummary?.imdbID }
                .distinctUntilChanged()
                .flatMapLatest { imdbID ->
                    if (imdbID != null) {
                        workManager.getWorkInfosByTagFlow(WORK_TAG_WATCHLIST_PREFIX + imdbID)
                            .map { workInfoList ->
                                val isLoading = workInfoList.any { !it.state.isFinished }
                                if (!isLoading) {
                                    // When worker finishes, release optimistic UI to allow native DB rendering
                                    _watchlistOverride.value = null
                                }
                                isLoading
                            }
                    } else {
                        kotlinx.coroutines.flow.flowOf(false)
                    }
                }
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000L),
                    false,
                )

        val showRating: StateFlow<TraktShowRating?> =
            traktRepository.traktShowRating
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000L),
                    null,
                )

        val showStats: StateFlow<TraktShowStats?> =
            traktRepository.traktShowStats
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000L),
                    null,
                )

        private val _traktId = MutableStateFlow<Int?>(null)
        val traktId: StateFlow<Int?> = _traktId.asStateFlow()

        data class ShowDetailUiState(
            val showSummary: ShowDetailSummary? = null,
            val showPreviousEpisode: ShowPreviousEpisode? = null,
            val showNextEpisode: ShowNextEpisode? = null,
            val showCast: List<ShowCast>? = null,
            val traktCast: List<TraktCast>? = null,
            val isLoadingSummary: Boolean = false,
            val isCastLoading: Boolean = false,
            val isPreviousEpisodeLoading: Boolean = false,
            val isNextEpisodeLoading: Boolean = false,
            val summaryErrorMessage: String? = null,
            val castErrorMessage: String? = null,
            val previousEpisodeErrorMessage: String? = null,
            val nextEpisodeErrorMessage: String? = null,
            val generalErrorMessage: String? = null,
            val watchlistShow: TraktUserListItem? = null,
            val similarShows: List<TraktRelatedShows>? = null,
            val isSimilarShowsLoading: Boolean = false,
            val watchProviders: TmdbWatchProviders? = null,
            val isWatchProvidersLoading: Boolean = false,
            val isRating: Boolean = false,
            val userRating: Int? = null,
            val ratingMessage: String? = null,
        )

        fun selectedShow(show: ShowDetailArg?) {
            if (show == null) {
                _isLoading.value = false
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoadingSummary = false,
                        generalErrorMessage = "No show information provided.",
                    )
                }
                return
            }

            val currentShow = _show.value
            val isSameShowId = show.showId != null && show.showId != "null" && currentShow?.showId == show.showId
            val isSameImdbId = show.imdbID != null && show.imdbID != "null" && currentShow?.imdbID == show.imdbID
            val hasValidSummary = _uiState.value.showSummary != null
            val isAlreadyLoading = _uiState.value.isLoadingSummary

            if ((isSameShowId || isSameImdbId) && (hasValidSummary || isAlreadyLoading)) {
                return
            }

            _uiState.update { ShowDetailUiState() }
            _isLoading.value = true

            _show.value = show
            _traktId.value = show.showTraktId
            _uiState.update { currentState ->
                currentState.copy(
                    isLoadingSummary = true,
                    summaryErrorMessage = null,
                    generalErrorMessage = null,
                    showSummary = null,
                    showCast = null,
                    traktCast = null,
                    showPreviousEpisode = null,
                    showNextEpisode = null,
                    similarShows = null,
                    watchProviders = null,
                )
            }
            getShowSummary(show)
        }

        private fun getShowSummary(show: ShowDetailArg) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoadingSummary = true, summaryErrorMessage = null) }

                val showId = show.showId
                if (!showId.isNullOrEmpty() && showId != "null") {
                    showDetailRepository.getShowSummary(showId.toInt()).collect { result ->
                        _isLoading.value = false

                        when (result) {
                            is Result.Success -> {
                                val summary = result.data
                                _uiState.update { currentState ->
                                    currentState.copy(
                                        showSummary = summary,
                                        isLoadingSummary = false,
                                    )
                                }
                                getShowPreviousEpisode(summary.previousEpisodeHref)
                                getShowNextEpisode(summary.nextEpisodeHref)
                                getTraktShowRating(summary.imdbID)
                                fetchUserRating(summary.imdbID)
                                getTraktShowStats(summary.imdbID)
                                getTraktId(summary.imdbID)
                                getShowCast(summary.imdbID)
                                getSimilarShows(summary.imdbID)
                                getShowWatchProviders(summary.imdbID)
                            }

                            is Result.GenericError -> {
                                val errorMessage =
                                    result.error?.message
                                        ?: "Error loading summary (Code: ${result.code ?: "Unknown"})"
                                firebaseCrashlytics.recordException(
                                    ShowDetailFetchException(
                                        message = "GenericError in getShowSummary: $errorMessage",
                                        errorResponse = result.error,
                                        cause = result.exception,
                                    ),
                                )
                                _uiState.update {
                                    it.copy(
                                        showSummary = emptyShowData(),
                                        isLoadingSummary = false,
                                        summaryErrorMessage = errorMessage,
                                    )
                                }
                            }

                            is Result.NetworkError -> {
                                val networkErrorMessage = "Network error loading summary."
                                firebaseCrashlytics.recordException(
                                    ShowDetailFetchException(
                                        message = networkErrorMessage,
                                        errorResponse = null,
                                        cause = result.exception,
                                    ),
                                )
                                _uiState.update {
                                    it.copy(
                                        showSummary = emptyShowData(),
                                        isLoadingSummary = false,
                                        summaryErrorMessage = networkErrorMessage,
                                    )
                                }
                            }

                            is Result.Error -> {
                                val errorMessage =
                                    result.message
                                        ?: "An unexpected error occurred while loading summary."
                                firebaseCrashlytics.recordException(
                                    ShowDetailFetchException(
                                        message = errorMessage,
                                        cause = result.exception,
                                    ),
                                )
                                _uiState.update {
                                    it.copy(
                                        showSummary = emptyShowData(),
                                        isLoadingSummary = false,
                                        summaryErrorMessage = errorMessage,
                                    )
                                }
                            }

                            is Result.Loading -> {
                                _uiState.update { it.copy(isLoadingSummary = result.status) }
                                if (!result.status && !_uiState.value.isCastLoading && !_uiState.value.isPreviousEpisodeLoading && !_uiState.value.isNextEpisodeLoading) {
                                    _isLoading.value = false
                                } else if (result.status) {
                                    _isLoading.value = true
                                }
                            }
                        }
                    }
                } else {
                    // showId (TVMaze ID) is null or invalid — try to resolve via IMDB lookup
                    val imdbId = show.imdbID
                    if (!imdbId.isNullOrEmpty()) {
                        Timber.d("showId is null/missing, attempting IMDB lookup for: $imdbId")
                        resolveShowViaImdbLookup(imdbId, show)
                    } else {
                        _isLoading.value = false
                        _uiState.update {
                            it.copy(
                                isLoadingSummary = false,
                                generalErrorMessage = "This show is missing a required TVMaze or IMDB ID.",
                            )
                        }
                    }
                }
            }
        }

        private suspend fun resolveShowViaImdbLookup(
            imdbId: String,
            show: ShowDetailArg,
        ) {
            showDetailRepository.getShowLookup(imdbId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        val tvMazeId = result.data.id
                        Timber.d("Resolved IMDB $imdbId to TVMaze ID: $tvMazeId")
                        // Re-invoke getShowSummary with the resolved TVMaze ID
                        getShowSummary(show.copy(showId = tvMazeId.toString()))
                    }

                    is Result.Loading -> {
                        // Already showing loading state from getShowSummary
                    }

                    else -> {
                        Timber.w("Failed to resolve TVMaze ID for IMDB: $imdbId")
                        _isLoading.value = false
                        _uiState.update {
                            it.copy(
                                isLoadingSummary = false,
                                generalErrorMessage =
                                    "This show is not available for detailed viewing yet.",
                            )
                        }
                    }
                }
            }
        }

        private fun getShowCast(imdbID: String?) {
            if (imdbID.isNullOrEmpty()) return

            viewModelScope.launch {
                _uiState.update { it.copy(isCastLoading = true, castErrorMessage = null) }
                traktRepository.getShowCast(imdbID)
                    .onSuccess { cast ->
                        _uiState.update { it.copy(traktCast = cast, isCastLoading = false) }
                    }
                    .onFailure { error ->
                        val errorMessage = error.message ?: "Failed to load cast."
                        _uiState.update { it.copy(isCastLoading = false, castErrorMessage = errorMessage) }
                        firebaseCrashlytics.recordException(
                            ShowDetailFetchException(message = "Error in getShowCast: $errorMessage", cause = error),
                        )
                    }
            }
        }

        private fun getShowPreviousEpisode(previousEpisodeHref: String?) {
            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        isPreviousEpisodeLoading = true,
                        previousEpisodeErrorMessage = null,
                    )
                }
                previousEpisodeHref?.takeIf { it.isNotEmpty() }?.let { href ->
                    showDetailRepository.getPreviousEpisode(href).collect { result ->
                        when (result) {
                            is Result.Success -> {
                                _uiState.update { currentState ->
                                    currentState.copy(
                                        showPreviousEpisode = result.data,
                                        isPreviousEpisodeLoading = false,
                                    )
                                }
                            }

                            is Result.GenericError -> {
                                val errorMessage =
                                    result.error?.message
                                        ?: "Failed to load previous episode (Code: ${result.code ?: "Unknown"})"
                                firebaseCrashlytics.recordException(
                                    ShowDetailFetchException(
                                        message = "GenericError in getShowPreviousEpisode: $errorMessage",
                                        errorResponse = result.error,
                                        cause = result.exception,
                                    ),
                                )
                                _uiState.update {
                                    it.copy(
                                        isPreviousEpisodeLoading = false,
                                        previousEpisodeErrorMessage = errorMessage,
                                    )
                                }
                            }

                            is Result.NetworkError -> {
                                val networkErrorMessage = "Network error loading previous episode."
                                firebaseCrashlytics.recordException(
                                    ShowDetailFetchException(
                                        message = networkErrorMessage,
                                        errorResponse = null,
                                        cause = result.exception,
                                    ),
                                )
                                _uiState.update {
                                    it.copy(
                                        isPreviousEpisodeLoading = false,
                                        previousEpisodeErrorMessage = networkErrorMessage,
                                    )
                                }
                            }

                            is Result.Error -> {
                                val errorMessage =
                                    result.message
                                        ?: "An unexpected error occurred while loading the previous episode."
                                firebaseCrashlytics.recordException(
                                    ShowDetailFetchException(
                                        message = errorMessage,
                                        cause = result.exception,
                                    ),
                                )
                                _uiState.update {
                                    it.copy(
                                        isPreviousEpisodeLoading = false,
                                        previousEpisodeErrorMessage = errorMessage,
                                    )
                                }
                            }

                            is Result.Loading -> {
                                _uiState.update { it.copy(isPreviousEpisodeLoading = result.status) }
                            }
                        }
                    }
                } ?: _uiState.update {
                    it.copy(
                        showPreviousEpisode = null,
                        isPreviousEpisodeLoading = false,
                    )
                }
            }
        }

        private fun getShowWatchProviders(imdbID: String?) {
            viewModelScope.launch {
                _uiState.update {
                    it.copy(isWatchProvidersLoading = true)
                }
                imdbID?.takeIf { it.isNotEmpty() }?.let { id ->
                    showDetailRepository.getShowWatchProviders(id).collect { result ->
                        when (result) {
                            is Result.Success -> {
                                _uiState.update { currentState ->
                                    currentState.copy(
                                        watchProviders = result.data,
                                        isWatchProvidersLoading = false,
                                    )
                                }
                            }
                            is Result.Error, is Result.GenericError, is Result.NetworkError -> {
                                // Gracefully fail and show empty providers if lookup fails
                                _uiState.update {
                                    it.copy(
                                        isWatchProvidersLoading = false,
                                        watchProviders = TmdbWatchProviders(id = null, providers = emptyList()),
                                    )
                                }
                            }
                            is Result.Loading -> {
                                _uiState.update { it.copy(isWatchProvidersLoading = result.status) }
                            }
                        }
                    }
                } ?: _uiState.update {
                    it.copy(
                        watchProviders = null,
                        isWatchProvidersLoading = false,
                    )
                }
            }
        }

        private fun getShowNextEpisode(nextEpisodeHref: String?) {
            viewModelScope.launch {
                _uiState.update { it.copy(isNextEpisodeLoading = true, nextEpisodeErrorMessage = null) }
                nextEpisodeHref?.takeIf { it.isNotEmpty() }?.let { href ->
                    showDetailRepository.getNextEpisode(href).collect { result ->
                        when (result) {
                            is Result.Success -> {
                                _uiState.update { currentState ->
                                    currentState.copy(
                                        showNextEpisode = result.data,
                                        isNextEpisodeLoading = false,
                                    )
                                }
                            }

                            is Result.GenericError -> {
                                val errorMessage =
                                    result.error?.message
                                        ?: "Failed to load next episode (Code: ${result.code ?: "Unknown"})"
                                firebaseCrashlytics.recordException(
                                    ShowDetailFetchException(
                                        message = "GenericError in getShowNextEpisode: $errorMessage",
                                        errorResponse = result.error,
                                        cause = result.exception,
                                    ),
                                )
                                _uiState.update {
                                    it.copy(
                                        isNextEpisodeLoading = false,
                                        nextEpisodeErrorMessage = errorMessage,
                                    )
                                }
                            }

                            is Result.NetworkError -> {
                                val networkErrorMessage = "Network error loading next episode."
                                firebaseCrashlytics.recordException(
                                    ShowDetailFetchException(
                                        message = networkErrorMessage,
                                        errorResponse = null,
                                        cause = result.exception,
                                    ),
                                )
                                _uiState.update {
                                    it.copy(
                                        isNextEpisodeLoading = false,
                                        nextEpisodeErrorMessage = networkErrorMessage,
                                    )
                                }
                            }

                            is Result.Error -> {
                                val errorMessage =
                                    result.message
                                        ?: "An unexpected error occurred while loading the next episode."
                                firebaseCrashlytics.recordException(
                                    ShowDetailFetchException(
                                        message = errorMessage,
                                        cause = result.exception,
                                    ),
                                )
                                _uiState.update {
                                    it.copy(
                                        isNextEpisodeLoading = false,
                                        nextEpisodeErrorMessage = errorMessage,
                                    )
                                }
                            }

                            is Result.Loading -> {
                                _uiState.update { it.copy(isNextEpisodeLoading = result.status) }
                            }
                        }
                    }
                } ?: _uiState.update {
                    it.copy(
                        showNextEpisode = null,
                        isNextEpisodeLoading = false,
                    )
                }
            }
        }

        private fun getTraktShowRating(imdbID: String?) {
            viewModelScope.launch {
                try {
                    traktRepository.getTraktShowRating(imdbID)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    firebaseCrashlytics.recordException(
                        ShowDetailFetchException(
                            message = "Error fetching Trakt show rating for IMDB ID: $imdbID",
                            cause = e,
                        ),
                    )
                }
            }
        }

        private fun getTraktShowStats(imdbID: String?) {
            viewModelScope.launch {
                try {
                    traktRepository.getTraktShowStats(imdbID)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    firebaseCrashlytics.recordException(
                        ShowDetailFetchException(
                            message = "Error fetching Trakt show stats for IMDB ID: $imdbID",
                            cause = e,
                        ),
                    )
                }
            }
        }

        private fun getTraktId(imdbID: String?) {
            if (imdbID.isNullOrEmpty() || _traktId.value != null) return

            viewModelScope.launch {
                try {
                    val result = traktRepository.getTraktIdLookup(imdbID)
                    if (result.isSuccess) {
                        val traktId = result.getOrNull()
                        _traktId.value = traktId
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    firebaseCrashlytics.recordException(
                        ShowDetailFetchException(
                            message = "Error fetching Trakt ID for IMDB ID: $imdbID",
                            cause = e,
                        ),
                    )
                }
            }
        }

        private fun getSimilarShows(imdbID: String?) {
            if (imdbID.isNullOrEmpty()) return

            viewModelScope.launch {
                _uiState.update { it.copy(isSimilarShowsLoading = true) }
                traktRepository.getRelatedShows(imdbID)
                    .onSuccess { similarShows ->
                        _uiState.update {
                            it.copy(
                                similarShows = similarShows,
                                isSimilarShowsLoading = false,
                            )
                        }
                    }
                    .onFailure {
                        _uiState.update { it.copy(isSimilarShowsLoading = false) }
                    }
            }
        }

        private val _navigateToShowDetail = MutableStateFlow<ShowDetailArg?>(null)
        val navigateToShowDetail: StateFlow<ShowDetailArg?> = _navigateToShowDetail.asStateFlow()

        fun onSimilarShowClicked(show: TraktRelatedShows) {
            viewModelScope.launch(Dispatchers.IO) {
                launchShowDetail(show.imdbID, show.title, show.traktID)
            }
        }

        private suspend fun launchShowDetail(
            imdbId: String?,
            title: String?,
            traktId: Int?,
        ) {
            if (!imdbId.isNullOrEmpty()) {
                _uiState.update { it.copy(isSimilarShowsLoading = true) }
                showDetailRepository.getShowLookup(imdbId).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            val tvMazeId = result.data.id
                            val arg =
                                ShowDetailArg(
                                    showId = tvMazeId.toString(),
                                    showTitle = title,
                                    showImageUrl = null,
                                    showBackgroundUrl = null,
                                    imdbID = imdbId,
                                    isAuthorizedOnTrakt = false,
                                    showTraktId = traktId,
                                )
                            _navigateToShowDetail.value = arg
                            _uiState.update { it.copy(isSimilarShowsLoading = false) }
                        }
                        is Result.Error, is Result.GenericError, is Result.NetworkError -> {
                            _uiState.update {
                                it.copy(
                                    isSimilarShowsLoading = false,
                                    generalErrorMessage = "Could not find show details",
                                )
                            }
                        }
                        is Result.Loading -> {
                        }
                    }
                }
            } else {
                _uiState.update { it.copy(generalErrorMessage = "No IMDB ID available for this show") }
            }
        }

        fun onShowDetailNavigationComplete() {
            _navigateToShowDetail.value = null
        }

        fun onSeasonsClick() {
            _navigateToSeasons.value = true
        }

        fun onAddRemoveWatchlistClick() {
            viewModelScope.launch {
                val currentAccessToken = traktRepository.traktAccessToken.firstOrNull()
                val currentWatchlistShow = observedWatchlistShow.value // Use source of truth
                val currentShowSummary = uiState.value.showSummary
                val imdbID = currentShowSummary?.imdbID

                if (currentAccessToken != null && imdbID != null) {
                    // Optimistic UI: toggle immediately
                    val wasOnWatchlist = currentWatchlistShow != null
                    _watchlistOverride.value = !wasOnWatchlist

                    if (currentWatchlistShow != null) {
                        val workerDataBuilder = Data.Builder()
                        currentWatchlistShow.traktID?.let {
                            workerDataBuilder.putInt(RemoveFromWatchlistWorker.ARG_TRAKT_ID, it)
                        }
                        workerDataBuilder.putString(
                            RemoveFromWatchlistWorker.ARG_IMDB_ID,
                            currentWatchlistShow.imdbID ?: imdbID,
                        )
                        workerDataBuilder.putString(
                            RemoveFromWatchlistWorker.ARG_TOKEN,
                            currentAccessToken.access_token,
                        )

                        val removeWatchlistWork =
                            OneTimeWorkRequest.Builder(RemoveFromWatchlistWorker::class.java)
                                .addTag(WORK_TAG_WATCHLIST_PREFIX + imdbID)
                                .setInputData(workerDataBuilder.build())
                                .build()
                        workManager.enqueue(removeWatchlistWork)
                    } else {
                        val workerDataBuilder = Data.Builder()
                        traktId.value?.let { workerDataBuilder.putInt(AddToWatchlistWorker.ARG_TRAKT_ID, it) }
                        imdbID?.let { workerDataBuilder.putString(AddToWatchlistWorker.ARG_IMDB_ID, it) }
                        workerDataBuilder.putString(
                            AddToWatchlistWorker.ARG_TOKEN,
                            currentAccessToken.access_token,
                        )
                        uiState.value.showSummary?.name?.let { workerDataBuilder.putString(AddToWatchlistWorker.ARG_TITLE, it) }
                        uiState.value.showSummary?.originalImageUrl?.let {
                            workerDataBuilder.putString(
                                AddToWatchlistWorker.ARG_ORIGINAL_IMAGE_URL,
                                it,
                            )
                        }
                        uiState.value.showSummary?.mediumImageUrl?.let {
                            workerDataBuilder.putString(
                                AddToWatchlistWorker.ARG_MEDIUM_IMAGE_URL,
                                it,
                            )
                        }
                        uiState.value.showSummary?.id?.let {
                            workerDataBuilder.putInt(AddToWatchlistWorker.ARG_TVMAZE_ID, it)
                        }
                        uiState.value.showSummary?.tmdbID?.let {
                            workerDataBuilder.putInt(AddToWatchlistWorker.ARG_TMDB_ID, it)
                        }
                        uiState.value.showSummary?.premiered?.takeIf { it.length >= 4 }?.substring(0, 4)?.let {
                            workerDataBuilder.putString(AddToWatchlistWorker.ARG_YEAR, it)
                        }
                        uiState.value.showSummary?.network?.let {
                            workerDataBuilder.putString(AddToWatchlistWorker.ARG_NETWORK, it)
                        }
                        uiState.value.showSummary?.status?.let {
                            workerDataBuilder.putString(AddToWatchlistWorker.ARG_STATUS, it)
                        }
                        val currentRating = showRating.value?.rating
                        if (currentRating != null) {
                            workerDataBuilder.putDouble(AddToWatchlistWorker.ARG_RATING, currentRating)
                        }

                        val addWatchlistWork =
                            OneTimeWorkRequest.Builder(AddToWatchlistWorker::class.java)
                                .addTag(WORK_TAG_WATCHLIST_PREFIX + imdbID)
                                .setInputData(workerDataBuilder.build())
                                .build()
                        workManager.enqueue(addWatchlistWork)
                    }
                } else {
                    if (currentAccessToken == null) {
                        firebaseCrashlytics.log("Cannot add/remove watchlist: Trakt access token is null.")
                        _uiState.update { it.copy(generalErrorMessage = "Please log in to Trakt to manage watchlists.") }
                    } else {
                        firebaseCrashlytics.log("Cannot add/remove watchlist: IMDB ID is null.")
                    }
                }
            }
        }

        companion object {
            const val WORK_TAG_WATCHLIST_PREFIX = "work_tag_watchlist_"
        }

        fun onSeasonsNavigationComplete() {
            _navigateToSeasons.value = false
        }

        fun onRateShow(rating: Int) {
            val imdbId = uiState.value.showSummary?.imdbID ?: return
            viewModelScope.launch(Dispatchers.IO) {
                _uiState.update { it.copy(isRating = true, ratingMessage = null) }
                val result = traktRepository.rateShow(imdbId, rating)
                if (result.isSuccess) {
                    _uiState.update {
                        it.copy(
                            isRating = false,
                            userRating = rating,
                            ratingMessage = "Rated $rating/10",
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isRating = false,
                            ratingMessage =
                                result.exceptionOrNull()?.message
                                    ?: "Failed to submit rating",
                        )
                    }
                }
            }
        }

        fun clearRatingMessage() {
            _uiState.update { it.copy(ratingMessage = null) }
        }

        private fun fetchUserRating(imdbId: String?) {
            if (imdbId.isNullOrEmpty()) return
            viewModelScope.launch(Dispatchers.IO) {
                val existingRating = traktRepository.getUserShowRating(imdbId)
                if (existingRating != null) {
                    _uiState.update { it.copy(userRating = existingRating) }
                }
            }
        }
    }
