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

package com.theupnextapp.ui.traktAccount

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.theupnextapp.R
import com.theupnextapp.domain.TraktUserListItem
import com.theupnextapp.extensions.ReferenceDevices
import com.theupnextapp.ui.components.SectionHeadingText
import com.theupnextapp.ui.widgets.ListPosterCard

@ExperimentalMaterial3WindowSizeClassApi
@ExperimentalMaterial3Api
@ExperimentalFoundationApi
@Composable
fun FavoritesListContent(
    favoriteShows: List<TraktUserListItem>,
    widthSizeClass: WindowWidthSizeClass?,
    modifier: Modifier = Modifier,
    lazyGridState: LazyGridState = rememberLazyGridState(),
    header: @Composable () -> Unit = {},
    onFavoriteClick: (item: TraktUserListItem) -> Unit,
) {
    val columns: GridCells =
        when (widthSizeClass) {
            WindowWidthSizeClass.Compact -> GridCells.Fixed(3)
            WindowWidthSizeClass.Medium -> GridCells.Fixed(4)
            else -> GridCells.Adaptive(minSize = 140.dp)
        }

    LazyVerticalGrid(
        columns = columns,
        modifier = modifier.testTag("favorites_grid"),
        state = lazyGridState,
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            header()
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            SectionHeadingText(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .padding(top = 8.dp, bottom = 8.dp),
                text = stringResource(id = R.string.title_favorites_list),
            )
        }

        items(
            items = favoriteShows,
            key = { item -> item.traktID ?: item.id ?: item.hashCode() }
        ) { favoriteShow ->
            ListPosterCard(
                itemName = favoriteShow.title,
                itemUrl = favoriteShow.originalImageUrl,
            ) {
                onFavoriteClick(favoriteShow)
            }
        }
    }
}

@ReferenceDevices
@Composable
fun FavoritesListPreview() {
}
