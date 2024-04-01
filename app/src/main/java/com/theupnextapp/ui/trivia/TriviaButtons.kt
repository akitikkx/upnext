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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.theupnextapp.domain.TriviaQuestion
import com.theupnextapp.ui.trivia.TriviaButtonConfig.getCorrectChoiceColors
import com.theupnextapp.ui.trivia.TriviaButtonConfig.getDefaultChoiceColors
import com.theupnextapp.ui.trivia.TriviaButtonConfig.getErrorChoiceColors

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
            getErrorChoiceColors
        } else if (buttonText == answer && !selectedChoice.isNullOrEmpty()) {
            getCorrectChoiceColors
        } else {
            getDefaultChoiceColors
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

@Preview
@Composable
fun CorrectChoiceButtonPreview() {
    ChoiceButton(
        answer = "This is a choice",
        selectedChoice = "This is a choice",
        buttonText = "This is a choice",
    ) {}
}

@Preview
@Composable
fun IncorrectChoiceButton() {
    ChoiceButton(
        answer = "This is a choice",
        selectedChoice = "This is not a choice",
        buttonText = "This is not a choice",
    ) {}
}

@Preview
@Composable
fun DefaultChoiceButton() {
    ChoiceButton(
        answer = "This is a choice",
        selectedChoice = "",
        buttonText = "This is a choice",
    ) {}
}