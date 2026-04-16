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

package com.theupnextapp.ui.personDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theupnextapp.domain.Result
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.network.models.tmdb.NetworkTmdbPersonProfile
import com.theupnextapp.network.models.trakt.NetworkTraktPersonResponse
import com.theupnextapp.repository.ShowDetailRepository
import com.theupnextapp.repository.TraktRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonDetailViewModel
    @Inject
    constructor(
        private val traktRepository: TraktRepository,
        private val showDetailRepository: ShowDetailRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(PersonDetailUiState())
        val uiState: StateFlow<PersonDetailUiState> = _uiState.asStateFlow()

        fun getPersonDetails(personId: String) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                try {
                    val summaryDeferred = async { traktRepository.getTraktPersonSummary(personId) }
                    val creditsDeferred = async { traktRepository.getTraktPersonShowCredits(personId) }

                    val summaryResult = summaryDeferred.await()
                    val creditsResult = creditsDeferred.await()

                    if (summaryResult.isSuccess && creditsResult.isSuccess) {
                        val personTmdbId = summaryResult.getOrNull()?.ids?.tmdb
                        val traktCreditsRaw = creditsResult.getOrNull()?.cast ?: emptyList()

                        // Filter deeply orphaned Trakt entities missing IMDB mappings
                        val validTraktCredits = traktCreditsRaw.filter { !it.show?.ids?.imdb.isNullOrEmpty() }

                        var finalUiCredits =
                            validTraktCredits.map { credit ->
                                PersonCreditUiModel(
                                    title = credit.show?.title ?: "Unknown Show",
                                    character = credit.character ?: credit.characters?.joinToString(", ") ?: "Unknown",
                                    year = credit.show?.year?.toString() ?: "",
                                    imdbId = credit.show?.ids?.imdb,
                                    traktId = credit.show?.ids?.trakt,
                                    tmdbId = credit.show?.ids?.tmdb,
                                    posterUrl = null,
                                )
                            }

                        var loadedPersonImages: List<NetworkTmdbPersonProfile> =
                            emptyList()

                        if (personTmdbId != null) {
                            val tmdbCreditsDeferred =
                                async {
                                    showDetailRepository.getPersonTvCredits(personTmdbId).firstOrNull { it !is Result.Loading }
                                }
                            val tmdbImagesDeferred =
                                async {
                                    showDetailRepository.getPersonImages(personTmdbId).firstOrNull { it !is Result.Loading }
                                }

                            val tmdbCreditsResult = tmdbCreditsDeferred.await()
                            val tmdbImagesResult = tmdbImagesDeferred.await()

                            if (tmdbImagesResult is Result.Success) {
                                loadedPersonImages = tmdbImagesResult.data.profiles ?: emptyList()
                            }

                            if (tmdbCreditsResult is Result.Success) {
                                val tmdbCast = tmdbCreditsResult.data.cast ?: emptyList()
                                val posterMap = tmdbCast.associateBy({ it.id }, { it.poster_path })

                                finalUiCredits =
                                    finalUiCredits.map { uiModel ->
                                        val posterPath = posterMap[uiModel.tmdbId]
                                        val fullPosterUrl = if (!posterPath.isNullOrEmpty()) "https://image.tmdb.org/t/p/w500$posterPath" else null
                                        uiModel.copy(posterUrl = fullPosterUrl)
                                    }
                            }
                        }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                personSummary = summaryResult.getOrNull(),
                                personCredits = finalUiCredits,
                                personImages = loadedPersonImages,
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = summaryResult.exceptionOrNull()?.message ?: "Failed to load person details.",
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "An unexpected error occurred.",
                        )
                    }
                }
            }
        }

        private val _navigateToShowDetail = MutableStateFlow<ShowDetailArg?>(null)
        val navigateToShowDetail: StateFlow<ShowDetailArg?> = _navigateToShowDetail.asStateFlow()

        fun onCreditClicked(
            imdbId: String?,
            title: String?,
            traktId: Int?,
        ) {
            if (!imdbId.isNullOrEmpty()) {
                viewModelScope.launch(Dispatchers.IO) {
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
                            }
                            is Result.Error, is Result.GenericError, is Result.NetworkError -> {
                                _uiState.update {
                                    it.copy(
                                        errorMessage = "The show details are not available at present",
                                    )
                                }
                            }
                            is Result.Loading -> {}
                        }
                    }
                }
            } else {
                _uiState.update { it.copy(errorMessage = "No IMDB ID available for this show") }
            }
        }

        fun onShowDetailNavigationComplete() {
            _navigateToShowDetail.value = null
        }

        fun clearErrorMessage() {
            _uiState.update { it.copy(errorMessage = null) }
        }

        data class PersonDetailUiState(
            val isLoading: Boolean = false,
            val personSummary: NetworkTraktPersonResponse? = null,
            val personCredits: List<PersonCreditUiModel> = emptyList(),
            val personImages: List<NetworkTmdbPersonProfile> = emptyList(),
            val errorMessage: String? = null,
        )

        data class PersonCreditUiModel(
            val title: String,
            val character: String,
            val year: String,
            val imdbId: String?,
            val traktId: Int?,
            val tmdbId: Int?,
            val posterUrl: String?,
        )
    }
