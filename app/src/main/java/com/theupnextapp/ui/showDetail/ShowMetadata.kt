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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.theupnextapp.R
import com.theupnextapp.domain.ShowDetailSummary
import com.theupnextapp.extensions.ReferenceDevices
import com.theupnextapp.ui.components.HeadingAndItemText
import com.theupnextapp.ui.previewdata.ShowDetailSummaryProvider

@Composable
fun ShowMetadata(
    showSummary: ShowDetailSummary?,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = modifier,
    ) {
        showSummary?.airDays?.let {
            if (it.isNotEmpty()) {
                HeadingAndItemText(
                    item = it,
                    heading =
                        stringResource(
                            id = R.string.show_detail_air_days_heading,
                        ),
                )
            }
        }

        showSummary?.genres?.let {
            if (it.isNotEmpty()) {
                HeadingAndItemText(
                    item = it,
                    heading =
                        stringResource(
                            id = R.string.show_detail_genres_heading,
                        ),
                )
            }
        }

        if (!showSummary?.originalImageUrl.isNullOrEmpty()) {
            Text(
                text = stringResource(id = R.string.tv_maze_creative_commons_attribution_text_multiple),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}

@ReferenceDevices
@Composable
fun ShowMetadataPreview(
    @PreviewParameter(ShowDetailSummaryProvider::class)
    showDetailSummary: ShowDetailSummary,
) {
    ShowMetadata(showSummary = showDetailSummary)
}
