package com.theupnextapp.ui.traktAccount

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.theupnextapp.R
import com.theupnextapp.domain.TraktUserListItem
import com.theupnextapp.ui.widgets.ListPosterCard

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun TraktAccountScreen(
    viewModel: TraktAccountViewModel = hiltViewModel(),
    onConnectToTraktClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    val isAuthorizedOnTrakt = viewModel.isAuthorizedOnTrakt.observeAsState()

    val favoriteShowsList = viewModel.favoriteShows.observeAsState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .scrollable(scrollState, orientation = Orientation.Vertical)
    ) {
        if (isAuthorizedOnTrakt.value == true) {
            favoriteShowsList.value?.let {
                FavoritesList(favoriteShows = it)
            }
        } else {
            ConnectToTrakt {
                onConnectToTraktClick()
            }
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
fun FavoritesList(favoriteShows: List<TraktUserListItem>) {
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
                .padding(4.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.body1
        )
        LazyVerticalGrid(cells = GridCells.Fixed(3)) {
            items(favoriteShows) { favoriteShow ->
                ListPosterCard(
                    itemName = favoriteShow.title,
                    itemUrl = favoriteShow.originalImageUrl
                ) {

                }
            }
        }
    }
}