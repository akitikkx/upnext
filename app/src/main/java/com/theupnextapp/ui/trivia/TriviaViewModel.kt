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
import com.theupnextapp.BuildConfig
import com.theupnextapp.common.utils.cleanUpJsonString
import com.theupnextapp.domain.TriviaQuestion
import com.theupnextapp.network.models.gemini.NetworkGeminiTriviaResponse
import com.theupnextapp.network.models.gemini.toTriviaQuestions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class TriviaViewModel @Inject constructor() : ViewModel() {

    private val _questions: MutableStateFlow<List<TriviaQuestion>?> = MutableStateFlow(null)

    private val _currentQuestion: MutableStateFlow<TriviaQuestion?> = MutableStateFlow(null)

    private val _previousQuestion: MutableStateFlow<TriviaQuestion?> = MutableStateFlow(null)

    private val _nextQuestion: MutableStateFlow<TriviaQuestion?> = MutableStateFlow(null)

    private val _correctAnswers: MutableStateFlow<Int> = MutableStateFlow(0)

    private val _isAtEnd: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val _uiState: MutableStateFlow<TriviaScreenUiState> =
        MutableStateFlow(TriviaScreenUiState.Initial)
    val uiState: StateFlow<TriviaScreenUiState> = _uiState.asStateFlow()

    private val generativeModel = GenerativeModel(
        modelName = GEMINI_MODEL,
        apiKey = BuildConfig.GEMINI_ACCESS_TOKEN
    )

    init {
        getTriviaFromGemini()

        // TODO - Base questions on a selected show
        // TODO - Use remote config to set the number of questions
        // TODO - Use remote config to toggle the quiz on or off
    }

    private fun getTriviaFromGemini() {
        val prompt = TRIVIA_PROMPT

        viewModelScope.launch {
            _uiState.value = TriviaScreenUiState.Loading
            try {
                val response = generativeModel.generateContent(prompt)

                val questions = parseGeminiResponse(response.text)

                if (questions.isNotEmpty()) {
                    _questions.value = questions
                    _currentQuestion.value = questions.firstOrNull()
                    determineNextQuestion()

                    _uiState.value = TriviaScreenUiState.Success(
                        questions = questions,
                        currentQuestion = _currentQuestion.value,
                        nextQuestion = _nextQuestion.value,
                        correctAnswers = _correctAnswers.value
                    )
                } else {
                    _uiState.value = TriviaScreenUiState.Error("No questions found")
                }
            } catch (e: Exception) {
                _uiState.value = TriviaScreenUiState.Error(
                    e.localizedMessage ?: "An expected error occurred. Please try again"
                )
            }
        }
    }

    private fun parseGeminiResponse(response: String?): List<TriviaQuestion> {
        val processedString = response?.cleanUpJsonString()

        if (processedString != null) {
            val decodedString =
                Json.decodeFromString<NetworkGeminiTriviaResponse>(processedString)
            return decodedString.triviaQuiz.toTriviaQuestions()
        } else {
            return emptyList()
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
        } else {
            return
        }
    }

    companion object {
        const val GEMINI_MODEL = "gemini-1.5-flash"

        const val TRIVIA_PROMPT = "Generate a trivia quiz for 5 very recent and popular TV shows. " +
                "The response needs to be a JSON object that contains " +
                "the questions and answers for each show in multiple-choice " +
                "as well as a publicly accessible URL of the show's poster " +
                "image. The JSON object should be valid and can be " +
                "correctly parsed (ensure all brackets of the JSON output " +
                "are closed correctly) and ensure the JSON matches the " +
                "following JSON structure { \"triviaQuiz\": [ { \"show\": " +
                "String, \"imageUrl\": String, \"question\": " +
                "String, \"choices\": [\"\"], \"answer\": String } ] }"
    }
}