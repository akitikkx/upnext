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
import com.theupnextapp.network.models.trakt.NetworkTraktPersonResponse
import com.theupnextapp.network.models.trakt.NetworkTraktPersonShowCreditsResponse
import com.theupnextapp.repository.TraktRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonDetailViewModel @Inject constructor(
    private val traktRepository: TraktRepository
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
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            personSummary = summaryResult.getOrNull(),
                            personCredits = creditsResult.getOrNull()
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = summaryResult.exceptionOrNull()?.message ?: "Failed to load person details."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "An unexpected error occurred."
                    )
                }
            }
        }
    }

    data class PersonDetailUiState(
        val isLoading: Boolean = false,
        val personSummary: NetworkTraktPersonResponse? = null,
        val personCredits: NetworkTraktPersonShowCreditsResponse? = null,
        val errorMessage: String? = null
    )
}
