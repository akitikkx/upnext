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

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.theupnextapp.domain.TriviaQuestion
import com.theupnextapp.extensions.ReferenceDevices
import com.theupnextapp.ui.trivia.TriviaScreenConfig.answerButtonBgColor
import com.theupnextapp.ui.trivia.TriviaScreenConfig.defaultButtonBgColor
import com.theupnextapp.ui.trivia.TriviaScreenConfig.errorButtonBgColor

@Destination
@Composable
fun TriviaScreen(
    viewModel: TriviaViewModel = hiltViewModel()
) {
    val triviaUiState: TriviaScreenUiState by viewModel.uiState.collectAsState()

    val scrollState = rememberScrollState()

    when (triviaUiState) {
        is TriviaScreenUiState.Initial -> {}

        is TriviaScreenUiState.Loading -> {
            LinearProgressIndicator(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            )
        }

        is TriviaScreenUiState.Success -> {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .verticalScroll(scrollState)
            ) {

                Text(
                    text = "Correct answers: ${(triviaUiState as TriviaScreenUiState.Success).correctAnswers}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(all = 16.dp)
                )

                (triviaUiState as TriviaScreenUiState.Success).currentQuestion?.let { question ->
                    TriviaQuestion(
                        triviaQuestion = question,
                        selectedChoice = (triviaUiState as TriviaScreenUiState.Success).selectedAnswer
                    ) { answer ->
                        viewModel.onQuestionAnswered(answer)
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

@Composable
fun TriviaQuestion(
    triviaQuestion: TriviaQuestion,
    modifier: Modifier = Modifier,
    selectedChoice: String? = null,
    onChoiceSelected: (String) -> Unit
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
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

            TriviaButtons(
                question = triviaQuestion,
                selectedChoice = selectedChoice
            ) { answer ->
                onChoiceSelected(answer)
            }
        }
    }
}

@Composable
fun TriviaButtons(
    question: TriviaQuestion,
    modifier: Modifier = Modifier,
    selectedChoice: String? = null,
    onChoiceSelected: (String) -> Unit
) {
    Column(modifier = modifier) {
        if (!question.hasAnswered) {
            question.choices.forEach { choiceText ->
                ChoiceButton(
                    choiceText = choiceText,
                    onChoiceSelected = { answer ->
                        onChoiceSelected(answer)
                    }
                )
            }
        } else {
            question.choices.forEach { choiceText ->
                ResultButton(
                    buttonText = choiceText,
                    answer = question.answer,
                    selectedChoice = selectedChoice
                )
            }
        }
    }
}

@Composable
fun ResultButton(
    buttonText: String,
    answer: String,
    selectedChoice: String?,
    modifier: Modifier = Modifier
) {
    val buttonBackground =
        if ((selectedChoice != answer) && (buttonText == selectedChoice)) {
            ButtonDefaults.buttonColors(
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                containerColor = getAnimatedErrorState(hasAnswered = selectedChoice.isNotEmpty()).value
            )
        } else if (buttonText == answer) {
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
        onClick = {},
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
fun ChoiceButton(
    choiceText: String,
    modifier: Modifier = Modifier,
    onChoiceSelected: (String) -> Unit
) {
    Button(
        colors = ButtonDefaults.buttonColors(
            contentColor = MaterialTheme.colorScheme.onSurface,
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        onClick = { onChoiceSelected(choiceText) },
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = choiceText,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
private fun getAnimatedErrorState(hasAnswered: Boolean) = animateColorAsState(
    targetValue = if (hasAnswered) errorButtonBgColor else defaultButtonBgColor,
    label = "Animated Error Background Color",
    animationSpec = repeatable(
        iterations = 3,
        animation = tween(durationMillis = 20000),
        repeatMode = RepeatMode.Reverse
    )
)

@Composable
private fun getAnimatedAnsweredState(hasAnswered: Boolean) = animateColorAsState(
    targetValue = if (hasAnswered) answerButtonBgColor else defaultButtonBgColor,
    label = "Animated Default Background Color",
    animationSpec = tween(5000, 4000, LinearOutSlowInEasing)
)

@Composable
private fun getAnimatedDefaultState(hasAnswered: Boolean) = animateColorAsState(
    targetValue = if (hasAnswered) defaultButtonBgColor else defaultButtonBgColor,
    label = "Animated Default Background Color",
    animationSpec = tween(5000, 0, LinearEasing)
)

object TriviaScreenConfig {
    val errorButtonBgColor: Color
        @Composable get() = MaterialTheme.colorScheme.errorContainer

    val defaultButtonBgColor: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceContainer

    val answerButtonBgColor: Color
        @Composable get() = MaterialTheme.colorScheme.inversePrimary
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
        choiceText = "This is an option",
        modifier = Modifier.fillMaxWidth(),
        onChoiceSelected = {})
}