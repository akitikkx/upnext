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

package com.theupnextapp.ui.showDetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.theupnextapp.common.utils.getWindowSizeClass
import com.theupnextapp.domain.ShowDetailSummary
import com.theupnextapp.ui.components.PosterImage
import com.theupnextapp.ui.showDetail.SynopsisAreaConfig.posterHeight
import com.theupnextapp.ui.showDetail.SynopsisAreaConfig.posterWidth

@ExperimentalMaterial3WindowSizeClassApi
@Composable
fun SynopsisArea(
    showSummary: ShowDetailSummary?,
    widthSizeClass: WindowWidthSizeClass?,
    modifier: Modifier = Modifier
) {
    when (widthSizeClass) {
        WindowWidthSizeClass.Compact,
        WindowWidthSizeClass.Medium -> SynopsisAreaCompact(
            showSummary = showSummary,
            modifier = modifier
        )

        else -> SynopsisAreaExpanded(
            showSummary = showSummary,
            modifier = modifier
        )
    }
}

@ExperimentalMaterial3WindowSizeClassApi
@Composable
private fun SynopsisAreaCompact(
    showSummary: ShowDetailSummary?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            showSummary?.originalImageUrl?.let {
                PosterImage(
                    url = it,
                    modifier = Modifier
                        .width(posterWidth)
                        .height(posterHeight)
                )
            }

            ShowMetadata(showSummary = showSummary)
        }
        ShowSynopsis(
            showSummary = showSummary,
            modifier = Modifier.padding(
                top = 4.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 4.dp
            )
        )
    }
}

@ExperimentalMaterial3WindowSizeClassApi
@Composable
private fun SynopsisAreaExpanded(
    showSummary: ShowDetailSummary?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        showSummary?.originalImageUrl?.let {
            PosterImage(
                url = it,
                modifier = Modifier
                    .width(posterWidth)
                    .height(posterHeight)
            )
        }

        ShowMetadata(
            showSummary = showSummary,
            modifier = Modifier
                .width(150.dp)
                .padding(start = 16.dp)
        )

        ShowSynopsis(
            showSummary = showSummary,
            modifier = Modifier.padding(
                top = 4.dp,
                start = 16.dp,
                end = 16.dp,
            )
        )
    }
}

@ExperimentalMaterial3WindowSizeClassApi
object SynopsisAreaConfig {
    val posterWidth: Dp
        @Composable get() {
            return when (getWindowSizeClass()?.widthSizeClass) {
                WindowWidthSizeClass.Compact -> 130.dp
                WindowWidthSizeClass.Medium -> 130.dp
                else -> 200.dp
            }
        }

    val posterHeight: Dp
        @Composable get() {
            return when (getWindowSizeClass()?.widthSizeClass) {
                WindowWidthSizeClass.Compact -> 180.dp
                WindowWidthSizeClass.Medium -> 180.dp
                else -> 250.dp
            }
        }

}

@Preview(name = "phone", device = Devices.PHONE, showBackground = true)
@ExperimentalMaterial3WindowSizeClassApi
@Composable
fun SynopsisAreaCompactPreview(@PreviewParameter(ShowDetailSummaryPreviewProvider::class) showDetailSummary: ShowDetailSummary) {
    SynopsisAreaCompact(showSummary = showDetailSummary)
}

@Preview(name = "foldable", device = Devices.FOLDABLE, showBackground = true)
@Preview(name = "custom", device = "spec:width=1280dp,height=800dp,dpi=480", showBackground = true)
@Preview("desktop", device = "id:desktop_medium", showBackground = true)
@ExperimentalMaterial3WindowSizeClassApi
@Composable
fun SynopsisAreaExpandedPreview(@PreviewParameter(ShowDetailSummaryPreviewProvider::class) showDetailSummary: ShowDetailSummary) {
    SynopsisAreaExpanded(showSummary = showDetailSummary)
}