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

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.theupnextapp.BuildConfig
import com.theupnextapp.R
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.TraktUserListItem
import com.theupnextapp.ui.components.SectionHeadingText
import com.theupnextapp.ui.destinations.ShowDetailScreenDestination
import com.theupnextapp.ui.widgets.ListPosterCard

@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@Destination
@Composable
fun TraktAccountScreen(
    viewModel: TraktAccountViewModel = hiltViewModel(),
    navigator: DestinationsNavigator,
    code: String? = null
) {
    val scrollState = rememberScrollState()

    val isAuthorizedOnTrakt = viewModel.isAuthorizedOnTrakt.observeAsState()

    val favoriteShowsList = viewModel.favoriteShows.observeAsState()

    val confirmDisconnectFromTrakt = viewModel.confirmDisconnectFromTrakt.observeAsState()

    val isLoading = viewModel.isLoading.observeAsState()

    val context = LocalContext.current

    if (!code.isNullOrEmpty() && isAuthorizedOnTrakt.value == false) {
        viewModel.onCodeReceived(code)
    }

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
                    onConnectToTraktClick = { openCustomTab(context = context) },
                    onFavoriteClick = {
                        navigator.navigate(
                            ShowDetailScreenDestination(
                                ShowDetailArg(
                                    source = "favorites",
                                    showId = it.tvMazeID.toString(),
                                    showTitle = it.title,
                                    showImageUrl = it.originalImageUrl,
                                    showBackgroundUrl = it.mediumImageUrl
                                )
                            )
                        )
                    },
                    onLogoutClick = { viewModel.onDisconnectFromTraktClick() }
                )

                if (isLoading.value == true) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                    )
                }

                if (confirmDisconnectFromTrakt.value == true) {
                    DisconnectTraktDialog(
                        onDismissed = { viewModel.onDisconnectFromTraktRefused() },
                        onConfirmed = { viewModel.onDisconnectConfirm() }
                    )
                }
            }
        }
    }
}

@Composable
private fun DisconnectTraktDialog(
    onDismissed: () -> Unit,
    onConfirmed: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismissed() },
        title = {
            Text(
                text = stringResource(id = R.string.disconnect_from_trakt_dialog_title)
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.disconnect_from_trakt_dialog_message)
            )
        },
        confirmButton = {
            Button(onClick = { onConfirmed() }) {
                Text(text = stringResource(id = R.string.disconnect_from_trakt_dialog_confirm))
            }
        },
        dismissButton = {
            Button(onClick = { onDismissed() }) {
                Text(text = stringResource(id = R.string.disconnect_from_trakt_dialog_cancel))
            }
        }
    )
}

fun openCustomTab(context: Context) {
    val packageName = "com.android.chrome"

    val traktUrl =
        "https://trakt.tv/oauth/authorize?response_type=code&client_id=" +
                "${BuildConfig.TRAKT_CLIENT_ID}&redirect_uri=" +
                BuildConfig.TRAKT_REDIRECT_URI

    val activity = (context as? Activity)

    val builder = CustomTabsIntent.Builder()
    builder.setShowTitle(true)
    builder.setInstantAppsEnabled(true)

    val customBuilder = builder.build()

    if (packageName != null) {
        customBuilder.intent.setPackage(packageName)
        customBuilder.launchUrl(context, traktUrl.toUri())
    } else {
        val intent = Intent(Intent.ACTION_VIEW, traktUrl.toUri())
        activity?.startActivity(intent)
    }
}

@ExperimentalMaterial3Api
@ExperimentalFoundationApi
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

@ExperimentalMaterial3Api
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
            style = MaterialTheme.typography.bodyMedium
        )

        SectionHeadingText(
            modifier = Modifier
                .padding(top = 8.dp, bottom = 8.dp)
                .align(Alignment.CenterHorizontally),
            text = stringResource(id = R.string.title_favorites_list),
        )

        LazyVerticalGrid(columns = GridCells.Fixed(3)) {
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
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(0.7f),
            text = stringResource(id = R.string.trakt_account_favorites_empty),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview
@Composable
fun EmptyStateFavoritesListPreview() {
    EmptyFavoritesList()
}
