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
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal object TriviaScreenConfig {

    private val errorButtonBgColor: Color
        @Composable get() = MaterialTheme.colorScheme.errorContainer

    private val defaultButtonBgColor: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceContainer

    private val answerButtonBgColor: Color
        @Composable get() = MaterialTheme.colorScheme.inversePrimary

    @Composable
    fun getAnimatedErrorState(hasAnswered: Boolean) = animateColorAsState(
        targetValue = if (hasAnswered) errorButtonBgColor else defaultButtonBgColor,
        label = "Animated Error Background Color",
        animationSpec = repeatable(
            iterations = 3,
            animation = tween(durationMillis = 200),
            repeatMode = RepeatMode.Reverse
        )
    )

    @Composable
    fun getAnimatedAnsweredState(hasAnswered: Boolean) = animateColorAsState(
        targetValue = if (hasAnswered) answerButtonBgColor else defaultButtonBgColor,
        label = "Animated Default Background Color",
        animationSpec = tween(5000, 0, LinearOutSlowInEasing)
    )

    @Composable
    fun getAnimatedDefaultState(hasAnswered: Boolean) = animateColorAsState(
        targetValue = if (hasAnswered) defaultButtonBgColor else defaultButtonBgColor,
        label = "Animated Default Background Color",
        animationSpec = tween(5000, 0, LinearOutSlowInEasing)
    )
}