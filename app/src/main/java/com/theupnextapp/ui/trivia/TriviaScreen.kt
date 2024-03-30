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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.ramcosta.composedestinations.annotation.Destination
import com.theupnextapp.domain.TriviaQuestion
import com.theupnextapp.extensions.ReferenceDevices
import com.theupnextapp.ui.trivia.TriviaScreenConfig.getAnimatedAnsweredState
import com.theupnextapp.ui.trivia.TriviaScreenConfig.getAnimatedDefaultState

@Destination
@Composable
fun TriviaScreen(
    viewModel: TriviaViewModel = hiltViewModel()
) {
    val triviaUiState: TriviaScreenUiState by viewModel.uiState.collectAsState()

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        when (triviaUiState) {
            is TriviaScreenUiState.Initial -> {}

            is TriviaScreenUiState.Loading -> {
                Loading(modifier = Modifier.align(Alignment.Center))
            }

            is TriviaScreenUiState.Success -> {
                if ((triviaUiState as TriviaScreenUiState.Success).showEndOfQuiz) {
                    TriviaComplete(
                        correctAnswers = (triviaUiState as TriviaScreenUiState.Success).correctAnswers,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    (triviaUiState as TriviaScreenUiState.Success).currentQuestion?.let { question ->
                        TriviaQuestion(
                            triviaQuestion = question,
                            selectedChoice = (triviaUiState as TriviaScreenUiState.Success).selectedAnswer,
                            modifier = Modifier.align(Alignment.Center),
                            onChoiceSelected = { viewModel.onQuestionAnswered(it) },
                        )

                        AnimatedVisibility(
                            visible = question.hasAnswered,
                            enter = slideInVertically(initialOffsetY = { it }),
                            exit = slideOutVertically(targetOffsetY = { it }),
                            modifier = Modifier.align(Alignment.BottomCenter)
                        ) {
                            Column {
                                NextButton { viewModel.onNext() }
                            }
                        }
                    }
                }
            }

            is TriviaScreenUiState.Error -> {
                Text(
                    text = (triviaUiState as TriviaScreenUiState.Error).errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(all = 16.dp)
                )
            }
        }
    }
}

@Composable
fun TriviaQuestion(
    triviaQuestion: TriviaQuestion,
    modifier: Modifier = Modifier,
    selectedChoice: String? = null,
    onChoiceSelected: (String) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        Text(
            text = triviaQuestion.show,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = triviaQuestion.question,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        ChoiceButtons(
            question = triviaQuestion,
            selectedChoice = selectedChoice
        ) { answer ->
            onChoiceSelected(answer)
        }
    }
}

@Composable
fun ChoiceButtons(
    question: TriviaQuestion,
    modifier: Modifier = Modifier,
    selectedChoice: String? = null,
    onChoiceSelected: (String) -> Unit
) {
    Column(modifier = modifier) {
        question.choices.forEach { choiceText ->
            ChoiceButton(
                buttonText = choiceText,
                answer = question.answer,
                selectedChoice = selectedChoice
            ) { answer ->
                onChoiceSelected(answer)
            }
        }
    }
}

@Composable
fun ChoiceButton(
    buttonText: String,
    answer: String,
    selectedChoice: String?,
    modifier: Modifier = Modifier,
    onChoiceSelected: (String) -> Unit,
) {
    val buttonBackground =
        if ((selectedChoice != answer) && (buttonText == selectedChoice)) {
            ButtonDefaults.buttonColors(
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                containerColor = TriviaScreenConfig.getAnimatedErrorState(hasAnswered = selectedChoice.isNotEmpty()).value
            )
        } else if (buttonText == answer && !selectedChoice.isNullOrEmpty()) {
            ButtonDefaults.buttonColors(
                contentColor = MaterialTheme.colorScheme.onBackground,
                containerColor = getAnimatedAnsweredState(hasAnswered = !selectedChoice.isNullOrEmpty()).value
            )
        } else {
            ButtonDefaults.buttonColors(
                contentColor = MaterialTheme.colorScheme.onSurface,
                containerColor = getAnimatedDefaultState(hasAnswered = !selectedChoice.isNullOrEmpty()).value
            )
        }

    Button(
        colors = buttonBackground,
        onClick = { onChoiceSelected(buttonText) },
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = buttonText,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun Loading(
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("animation/trivia_loading1711807763787.json"))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(8.dp)
    ) {

        LottieAnimation(
            composition = composition,
            iterations = 10,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // TODO Extract to string resource
        Text(
            text = "Please wait while Gemini is preparing the quiz questions",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(4.dp)
                .align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(16.dp))

        CircularProgressIndicator()
    }
}

@Composable
fun TriviaComplete(
    correctAnswers: Int,
    modifier: Modifier = Modifier
) {

    // TODO switch between the success and sad animation if no questions are answered correctly
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("animation/trivia_celebration1711806163916.json"))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(16.dp)
    ) {

        LottieAnimation(
            composition = composition,
            iterations = 10
        )

        Spacer(modifier = Modifier.height(8.dp))

        // TODO use the correctAnswers count to formulate the right message

        // TODO Extract to string resource
        Text(
            text = "Well done!",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(4.dp)
                .align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(4.dp))

        // TODO Extract to string resource
        Text(
            text = "You had $correctAnswers correct answers",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.CenterHorizontally),
        )
    }
}

@Composable
fun NextButton(
    modifier: Modifier = Modifier,
    onNextClick: () -> Unit
) {
    Button(
        onClick = { onNextClick() },
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = "Next",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@ReferenceDevices
@Composable
fun TriviaQuestionPreview() {
    TriviaQuestion(
        triviaQuestion = TriviaQuestion(
            show = "Stranger Things",
            imageUrl = "",
            question = "What is the name of the alternate dimension in Stranger Things?",
            choices = listOf(
                "The Upside Down",
                "The Netherworld",
                "The Shadow Realm",
                "The Void"
            ),
            answer = "The Upside Down",
        ),
        onChoiceSelected = {}
    )
}

@Preview
@Composable
fun TriviaQuestionButtonPreview() {
    ChoiceButton(
        buttonText = "This is an option",
        answer = "This is an option",
        selectedChoice = "This is the answer",
        modifier = Modifier.fillMaxWidth(),
        onChoiceSelected = {})
}

@ReferenceDevices
@Composable
fun TriviaCompletePreview() {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxSize()
    ) {
        TriviaComplete(
            correctAnswers = 5,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
