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

package com.theupnextapp.ui.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.theupnextapp.ui.components.SectionHeadingText
import com.theupnextapp.ui.theme.UpnextTheme
import com.theupnextapp.ui.widgets.ListPosterCard

@ExperimentalMaterial3WindowSizeClassApi
@ExperimentalMaterial3Api
@Composable
fun StatsScreen() {
    UpnextTheme {
        Surface {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(8.dp),
            ) {
                SectionHeadingText(text = "Summary")
                Card(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "Total Time Watched")
                        Text(text = "120 hours")
                    }
                }

                SectionHeadingText(text = "Top Genres")
                Card(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "Chart placeholder")
                    }
                }

                SectionHeadingText(text = "Recently Watched")
                LazyRow {
                    items(
                        items =
                            listOf(
                                "The Last of Us" to "https://image.tmdb.org/t/p/w342/uKvVjHNqB5VmOrdxqAt2F72kflB.jpg",
                                "Succession" to "https://image.tmdb.org/t/p/w342/jZgOkzD65Rj2vopSjWl2W3zP5Y.jpg",
                                "The Mandalorian" to "https://image.tmdb.org/t/p/w342/eU1i6eHX6g7C0a5eL2DprqQ0q4.jpg",
                                "Ted Lasso" to "https://image.tmdb.org/t/p/w342/v9ie7tI5K2h9eQh4g3o4r6bS7u.jpg",
                            ),
                    ) {
                        ListPosterCard(
                            itemName = it.first,
                            itemUrl = it.second,
                            onClick = { },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Preview
@Composable
fun StatsScreenPreview() {
    StatsScreen()
}
