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

package com.theupnextapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.theupnextapp.R
import com.valentinilk.shimmer.shimmer

val shimmerBackgroundColor = Color.LightGray.copy(alpha = 0.2f)

@Composable
fun ShimmerPosterCard(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .padding(end = 8.dp)
                .width(130.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(shimmerBackgroundColor),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(0.8f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBackgroundColor),
        )
    }
}

@Composable
fun ShimmerPosterCardRow(
    modifier: Modifier = Modifier,
    numberOfItems: Int = 5,
) {
    Column(modifier = modifier.shimmer()) {
        Box(
            modifier =
                Modifier
                    .padding(start = 8.dp, top = 16.dp, bottom = 8.dp)
                    .width(150.dp)
                    .height(20.dp)
                    .background(shimmerBackgroundColor),
        )
        LazyRow(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            items(numberOfItems) {
                ShimmerPosterCard()
            }
        }
    }
}

@Composable
fun ShimmerSeasons(
    modifier: Modifier = Modifier,
    numberOfItems: Int = 5,
) {
    Column(modifier = modifier.shimmer()) {
        repeat(numberOfItems) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .width(dimensionResource(id = R.dimen.compose_search_poster_width))
                            .height(dimensionResource(id = R.dimen.compose_search_poster_height))
                            .clip(RoundedCornerShape(4.dp))
                            .background(shimmerBackgroundColor),
                )

                Column(
                    modifier =
                        Modifier
                            .padding(start = 8.dp)
                            .fillMaxWidth(),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .width(100.dp)
                                .height(16.dp)
                                .background(shimmerBackgroundColor),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier =
                            Modifier
                                .width(150.dp)
                                .height(12.dp)
                                .background(shimmerBackgroundColor),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier =
                            Modifier
                                .width(120.dp)
                                .height(12.dp)
                                .background(shimmerBackgroundColor),
                    )
                }
            }
        }
    }
}

@Composable
fun ShimmerSeasonEpisodes(
    modifier: Modifier = Modifier,
    numberOfItems: Int = 5,
) {
    Column(modifier = modifier.shimmer()) {
        repeat(numberOfItems) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(SHIMMER_SEASON_EPISODE_POSTER_WIDTH_FACTOR)
                            .height(dimensionResource(id = R.dimen.show_season_episode_poster_height))
                            .clip(RoundedCornerShape(4.dp))
                            .background(shimmerBackgroundColor),
                )

                Column(
                    modifier =
                        Modifier
                            .padding(start = 8.dp)
                            .fillMaxWidth(),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth(SHIMMER_SEASON_EPISODE_TITLE_WIDTH_FACTOR)
                                .height(16.dp)
                                .background(shimmerBackgroundColor),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth(SHIMMER_SEASON_EPISODE_DATE_WIDTH_FACTOR)
                                .height(12.dp)
                                .background(shimmerBackgroundColor),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth(SHIMMER_SEASON_EPISODE_RATING_WIDTH_FACTOR)
                                .height(12.dp)
                                .background(shimmerBackgroundColor),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ShimmerPosterCardPreview() {
    MaterialTheme {
        ShimmerPosterCard()
    }
}

@Preview(showBackground = true)
@Composable
fun ShimmerPosterCardRowPreview() {
    MaterialTheme {
        ShimmerPosterCardRow()
    }
}
