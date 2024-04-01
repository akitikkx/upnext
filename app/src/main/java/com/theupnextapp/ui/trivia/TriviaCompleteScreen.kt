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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.theupnextapp.R
import com.theupnextapp.extensions.ReferenceDevices

@Composable
fun TriviaCompleteScreen(
    correctAnswers: Int,
    modifier: Modifier = Modifier
) {
    val compositionCongratulate by rememberLottieComposition(LottieCompositionSpec.Asset("animation/trivia_celebration1711806163916.json"))
    val compositionEncourage by rememberLottieComposition(LottieCompositionSpec.Asset("animation/trivia_sad1711989856335.json"))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(16.dp)
    ) {

        LottieAnimation(
            composition = if (correctAnswers == 0) {
                compositionEncourage
            } else {
                compositionCongratulate
            },
            iterations = 10
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (correctAnswers == 0) {
                stringResource(id = R.string.trivia_confirmation_heading_encourage)
            } else {
                stringResource(id = R.string.trivia_confirmation_heading_congratulate)
            },
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(4.dp)
                .align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(
                id = R.string.trivia_confirmation_correct_answer_count,
                correctAnswers,
                if (correctAnswers == 1) {
                    stringResource(R.string.trivia_confirmation_correct_answer_count_singular)
                } else {
                    stringResource(R.string.trivia_confirmation_correct_answer_count_many)
                }
            ),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.CenterHorizontally),
        )
    }
}

@ReferenceDevices
@Composable
fun TriviaCompletePreview() {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxSize()
    ) {
        TriviaCompleteScreen(
            correctAnswers = 5,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}