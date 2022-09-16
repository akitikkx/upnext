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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import com.theupnextapp.R
import com.theupnextapp.ui.components.PosterAttributionItem
import com.theupnextapp.ui.components.PosterImage
import com.theupnextapp.ui.components.PosterTitleTextItem

@ExperimentalMaterial3Api
@Composable
fun ListPosterCard(
    itemName: String?,
    itemUrl: String?,
    onClick: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.padding(4.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.width(dimensionResource(id = R.dimen.compose_poster_frame_width))) {
            itemUrl?.let {
                PosterImage(
                    url = it,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(id = R.dimen.compose_shows_list_poster_height))
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
