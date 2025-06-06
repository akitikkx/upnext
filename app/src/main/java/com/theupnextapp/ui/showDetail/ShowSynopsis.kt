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

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.theupnextapp.domain.ShowDetailSummary
import com.theupnextapp.extensions.ReferenceDevices
import com.theupnextapp.ui.previewdata.ShowDetailSummaryProvider
import org.jsoup.Jsoup

@Composable
fun ShowSynopsis(
    showSummary: ShowDetailSummary?,
    modifier: Modifier = Modifier
) {
    showSummary?.summary?.let { summary ->
        Text(
            text = Jsoup.parse(summary).text(),
            modifier = modifier,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@ReferenceDevices
@Composable
fun ShowSynopsisPreview(
    @PreviewParameter(ShowDetailSummaryProvider::class)
    showSummary: ShowDetailSummary?
) {
    ShowSynopsis(showSummary = showSummary)
}