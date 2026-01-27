/*
 * MIT License
 *
 * Copyright (c) 2022 Ahmed Tikiwa
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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.ui.showDetail

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.theupnextapp.common.utils.getWindowSizeClass
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.ShowDetailSummary
import com.theupnextapp.ui.components.PosterImage
import com.theupnextapp.ui.showDetail.BackdropAndTitleConfig.backdropHeight

@ExperimentalMaterial3WindowSizeClassApi
@Composable
fun BackdropAndTitle(
    showDetailArgs: ShowDetailArg?,
    showSummary: ShowDetailSummary?,
) {
    val imageUrl: String? =
        showDetailArgs?.let { args -> // Use let to scope on non-null showDetailArgs
            if (!args.showBackgroundUrl.isNullOrEmpty()) {
                args.showBackgroundUrl
            } else if (!args.showImageUrl.isNullOrEmpty()) {
                args.showImageUrl
            } else {
                null
            }
        } ?: showSummary?.originalImageUrl

    imageUrl?.let {
        PosterImage(
            url = it,
            height = backdropHeight,
        )
    }

    showSummary?.name?.let { name ->
        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    top = 8.dp,
                    end = 16.dp,
                ),
        )
    }

    showSummary?.status?.let { status ->
        Text(
            text = status,
            style = MaterialTheme.typography.labelMedium,
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    top = 4.dp,
                    end = 16.dp,
                    bottom = 4.dp,
                ),
        )
    }
}

@ExperimentalMaterial3WindowSizeClassApi
object BackdropAndTitleConfig {
    val backdropHeight: Dp
        @Composable
        get() =
            when (getWindowSizeClass()?.widthSizeClass) {
                WindowWidthSizeClass.Compact -> 250.dp
                WindowWidthSizeClass.Medium -> 280.dp
                else -> 290.dp
            }
}
