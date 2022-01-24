package com.theupnextapp.ui.traktAccount

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Button
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.theupnextapp.R

@Composable
fun TraktAccountScreen(
    viewModel: TraktAccountViewModel = hiltViewModel(),
    onConnectToTraktClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    val isAuthorizedOnTrakt = viewModel.isAuthorizedOnTrakt.observeAsState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .scrollable(scrollState, orientation = Orientation.Vertical)
    ) {
        if (isAuthorizedOnTrakt.value == true) {
            FavoritesList()
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

@Composable
fun FavoritesList() {

}