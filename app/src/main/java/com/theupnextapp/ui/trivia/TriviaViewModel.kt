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
import com.theupnextapp.domain.Result
import com.theupnextapp.domain.TriviaQuestion
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

    private val _questions: MutableStateFlow<List<TriviaQuestion>?> = MutableStateFlow(null)

    private val _currentQuestion: MutableStateFlow<TriviaQuestion?> = MutableStateFlow(null)

    private val _previousQuestion: MutableStateFlow<TriviaQuestion?> = MutableStateFlow(null)

    private val _nextQuestion: MutableStateFlow<TriviaQuestion?> = MutableStateFlow(null)

    private val _correctAnswers: MutableStateFlow<Int> = MutableStateFlow(0)

    private val _isAtEnd: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val _uiState: MutableStateFlow<TriviaScreenUiState> =
        MutableStateFlow(TriviaScreenUiState.Initial)
    val uiState: StateFlow<TriviaScreenUiState> = _uiState.asStateFlow()

    init {
        getTrivia()
    }

    private fun getTrivia() {
        val prompt = "5 random TV shows"

        viewModelScope.launch {
            try {
                vertexAIRepository.getTrivia(prompt).collect { result ->
                    when (result) {
                        is Result.Loading -> _uiState.value = TriviaScreenUiState.Loading
                        is Result.Success -> {
                            _questions.value = result.data
                            _currentQuestion.value = _questions.value?.firstOrNull()
                            determineNextQuestion()

                            _uiState.value = TriviaScreenUiState.Success(
                                questions = _questions.value,
                                currentQuestion = _currentQuestion.value,
                                nextQuestion = _nextQuestion.value,
                                correctAnswers = _correctAnswers.value
                            )
                        }

                        else -> _uiState.value = TriviaScreenUiState.Error(result.toString())
                    }
                }

            } catch (e: Exception) {
                _uiState.value = TriviaScreenUiState.Error(
                    e.localizedMessage ?: "An expected error occurred. Please try again"
                )
            }
        }
    }

    fun onQuestionAnswered(answer: String) {
        val correctAnswer = _currentQuestion.value?.answer

        if (answer == correctAnswer) {
            _correctAnswers.value = _correctAnswers.value.plus(1)

            _currentQuestion.value = _currentQuestion.value?.copy(
                hasAnswered = true,
                hasAnsweredCorrect = true
            )
        } else {
            _currentQuestion.value = _currentQuestion.value?.copy(
                hasAnswered = true
            )
        }

        _uiState.value = TriviaScreenUiState.Success(
            questions = _questions.value,
            currentQuestion = _currentQuestion.value,
            nextQuestion = _nextQuestion.value,
            correctAnswers = _correctAnswers.value,
            selectedAnswer = answer
        )
    }

    fun onNext() {
        determineNextQuestion()

        if (!_isAtEnd.value) {
            // The previous question becomes the current question that has just been
            // answered
            _previousQuestion.value = _currentQuestion.value
            // The current question becomes the next question already defined in _nextQuestion
            _currentQuestion.value = _nextQuestion.value
            _nextQuestion.value = _nextQuestion.value
        }

        _uiState.value = TriviaScreenUiState.Success(
            questions = _questions.value,
            currentQuestion = _currentQuestion.value,
            nextQuestion = _nextQuestion.value,
            correctAnswers = _correctAnswers.value,
            showEndOfQuiz = _isAtEnd.value
        )
    }

    private fun determineNextQuestion() {
        val questions = _questions.value
        val currentQuestion = _currentQuestion.value

        if (!_questions.value.isNullOrEmpty()) {
            // get the index of the current question to query on
            val currentQuestionIndex =
                questions?.indexOf(questions.find { it.question == currentQuestion?.question })

            // Check if there is an index in the list that equals current question's index
            // incremented by 1 meaning a next position exists, if it doesn't exist we have
            // already reached the end
            if (currentQuestionIndex?.plus(1)?.let { questions.getOrNull(it) } != null) {
                val nextQuestion = questions[currentQuestionIndex.plus(1)]
                _nextQuestion.value = nextQuestion
            } else {
                _isAtEnd.value = true
                _nextQuestion.value = null
            }
        }
    }
}