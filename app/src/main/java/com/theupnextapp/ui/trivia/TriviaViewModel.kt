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

package com.theupnextapp.ui.trivia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.theupnextapp.domain.Result
import com.theupnextapp.repository.VertexAIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TriviaViewModel @Inject constructor(
    private val vertexAIRepository: VertexAIRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<TriviaScreenUiState> =
        MutableStateFlow(TriviaScreenUiState.Initial)
    val uiState: StateFlow<TriviaScreenUiState> = _uiState.asStateFlow()

    init {
        getTrivia()
    }

    private fun getTrivia() {
        val prompt = "Can you generate a trivia quiz from the show suits? The response " +
                "should contain both the questions and answers in multiple choice and in " +
                "a structured json format"

        viewModelScope.launch {
            try {
                vertexAIRepository.getTrivia().collect { result ->
                    when (result) {
                        is Result.Loading -> _uiState.value = TriviaScreenUiState.Loading
                        is Result.Success -> _uiState.value = TriviaScreenUiState.Success(result.data)
                        else -> _uiState.value = TriviaScreenUiState.Error(result.toString())
                    }
                }


//                vertexAIRepository.getTriviaFromImage(prompt = prompt).collect { result ->
//                    when(result){
//                        is Result.Success -> {
//                            _uiState.value = TriviaScreenUiState.Success(result.data)
//                        }
//                        else -> {
//                            _uiState.value = TriviaScreenUiState.Error(result.toString())
//                        }
//                    }
//                }

            } catch (e: Exception) {
//                _uiState.value = TriviaScreenUiState.Error(e.localizedMessage ?: "")
            }
        }
    }
}