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

package com.theupnextapp.ui.traktAccount

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.theupnextapp.R
import com.theupnextapp.domain.TraktUserListItem
import com.theupnextapp.ui.components.SectionHeadingText
import com.theupnextapp.ui.widgets.ListPosterCard

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun TraktAccountScreen(
    viewModel: TraktAccountViewModel = hiltViewModel(),
    onConnectToTraktClick: () -> Unit,
    onFavoriteClick: (item: TraktUserListItem) -> Unit,
    onLogoutClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    val isAuthorizedOnTrakt = viewModel.isAuthorizedOnTrakt.observeAsState()

    val favoriteShowsList = viewModel.favoriteShows.observeAsState()

    val isLoading = viewModel.isLoading.observeAsState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .scrollable(scrollState, orientation = Orientation.Vertical)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxSize()) {
                AccountArea(
                    isAuthorizedOnTrakt = isAuthorizedOnTrakt.value,
                    favoriteShowsList = favoriteShowsList.value,
                    onConnectToTraktClick = { onConnectToTraktClick() },
                    onFavoriteClick = { onFavoriteClick(it) },
                    onLogoutClick = { onLogoutClick() }
                )

                if (isLoading.value == true) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }


    }
}

@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun AccountArea(
    isAuthorizedOnTrakt: Boolean?,
    favoriteShowsList: List<TraktUserListItem>?,
    onConnectToTraktClick: () -> Unit,
    onFavoriteClick: (item: TraktUserListItem) -> Unit,
    onLogoutClick: () -> Unit
) {
    if (isAuthorizedOnTrakt == true) {
        favoriteShowsList?.let { list ->
            if (list.isEmpty()) {
                EmptyFavoritesList()
            } else {
                FavoritesList(
                    favoriteShows = list,
                    onFavoriteClick = { favoriteShow ->
                        onFavoriteClick(favoriteShow)
                    },
                    onLogoutClick = { onLogoutClick() }
                )
            }
        }
    } else {
        ConnectToTrakt {
            onConnectToTraktClick()
        }
    }
}

@Composable
fun ConnectToTrakt(
    onClick: () -> Unit
) {
    val image: Painter = painterResource(id = R.drawable.ic_trakt_wide_red_white)
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Image(
            painter = image,
            stringResource(id = R.string.trakt_logo_description),
            modifier = Modifier.height(dimensionResource(id = R.dimen.trakt_account_not_authorized_logo_height))
        )
        Text(
            text = stringResource(id = R.string.trakt_connect_description),
            modifier = Modifier.padding(8.dp),
            textAlign = TextAlign.Center
        )
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            onClick = { onClick() }
        ) {
            Text(text = stringResource(id = R.string.connect_to_trakt_button))
        }
    }
}

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun FavoritesList(
    favoriteShows: List<TraktUserListItem>,
    onLogoutClick: () -> Unit,
    onFavoriteClick: (item: TraktUserListItem) -> Unit
) {
    val traktLogo: Painter = painterResource(id = R.drawable.ic_trakt_wide_red_white)
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Image(
            painter = traktLogo,
            contentDescription = stringResource(id = R.string.trakt_logo_description),
            modifier = Modifier
                .height(dimensionResource(id = R.dimen.trakt_account_authorized_logo_height))
                .fillMaxWidth()
        )
        Text(
            text = stringResource(id = R.string.trakt_connection_status_disconnect),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .clickable { onLogoutClick() },
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.body2
        )

        SectionHeadingText(
            modifier = Modifier
                .padding(top = 8.dp, bottom = 8.dp)
                .align(Alignment.CenterHorizontally),
            text = stringResource(id = R.string.title_favorites_list),
        )

        LazyVerticalGrid(cells = GridCells.Fixed(3)) {
            items(favoriteShows) { favoriteShow ->
                ListPosterCard(
                    itemName = favoriteShow.title,
                    itemUrl = favoriteShow.originalImageUrl
                ) {
                    onFavoriteClick(favoriteShow)

                }
            }
        }
    }
}

@Composable
fun EmptyFavoritesList() {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            modifier = Modifier.padding(8.dp),
            text = stringResource(id = R.string.trakt_account_favorites_empty),
            style = MaterialTheme.typography.body2
        )
    }
}

@Preview
@Composable
fun EmptyStateFavoritesListPreview() {
    EmptyFavoritesList()
}