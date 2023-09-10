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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.ui.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.theupnextapp.R
import com.theupnextapp.common.utils.getWindowSizeClass
import com.theupnextapp.extensions.ReferenceDevices
import com.theupnextapp.ui.components.PosterAttributionItem
import com.theupnextapp.ui.components.PosterImage
import com.theupnextapp.ui.components.PosterTitleTextItem
import com.theupnextapp.ui.widgets.ListPosterCardConfig.listPosterHeight
import com.theupnextapp.ui.widgets.ListPosterCardConfig.listPosterWidth

@ExperimentalMaterial3WindowSizeClassApi
@ExperimentalMaterial3Api
@Composable
fun ListPosterCard(
    itemName: String?,
    itemUrl: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.large,
        modifier = modifier
            .width(listPosterWidth)
            .padding(4.dp),
        onClick = onClick
    ) {
        Column {
            itemUrl?.let {
                PosterImage(
                    url = it,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(listPosterHeight)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                itemName?.let {
                    PosterTitleTextItem(title = it)
                }
                PosterAttributionItem()
            }
        }
    }
}

@ExperimentalMaterial3WindowSizeClassApi
object ListPosterCardConfig {
    val listPosterWidth: Dp
        @Composable get() {
            return when (getWindowSizeClass()?.widthSizeClass) {
                WindowWidthSizeClass.Compact -> 140.dp
                WindowWidthSizeClass.Medium -> 140.dp
                else -> 140.dp
            }
        }

    val listPosterHeight: Dp
        @Composable get() {
            return when (getWindowSizeClass()?.widthSizeClass) {
                WindowWidthSizeClass.Compact -> 170.dp
                WindowWidthSizeClass.Medium -> 175.dp
                else -> 200.dp
            }
        }
}

@ExperimentalMaterial3WindowSizeClassApi
@ExperimentalMaterial3Api
@ReferenceDevices
@Composable
fun ListPosterCardPreview() {
    ListPosterCard(
        itemName = "List Poster",
        itemUrl = "https://www.theupnextapp.com",
        onClick = {}
    )
}
