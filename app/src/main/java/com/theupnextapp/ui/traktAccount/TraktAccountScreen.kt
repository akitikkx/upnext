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
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator // Changed from Linear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.ShowDetailScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.theupnextapp.BuildConfig
import com.theupnextapp.R
import com.theupnextapp.common.utils.getWindowSizeClass
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.TraktUserListItem
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3WindowSizeClassApi::class,
    ExperimentalFoundationApi::class
)
@Destination<RootGraph>
@Composable
fun TraktAccountScreen(
    viewModel: TraktAccountViewModel = hiltViewModel(),
    navigator: DestinationsNavigator,
    code: String? = null
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isAuthorizedOnTrakt by viewModel.isAuthorizedOnTrakt.collectAsStateWithLifecycle()
    val favoriteShowsList by viewModel.favoriteShows.collectAsStateWithLifecycle()
    val isLoadingConnection by viewModel.isLoadingConnection.collectAsStateWithLifecycle()
    val isLoadingFavorites by viewModel.isLoadingFavoriteShows.collectAsStateWithLifecycle()
    val favoriteShowsError by viewModel.favoriteShowsError.collectAsStateWithLifecycle()
    val isFavoriteShowsEmpty by viewModel.favoriteShowsEmpty.collectAsStateWithLifecycle()


    // Handle deep link code for authorization
    LaunchedEffect(key1 = code, key2 = isAuthorizedOnTrakt) {
        if (!code.isNullOrEmpty() && !isAuthorizedOnTrakt) {
            viewModel.onCodeReceived(code)
        }
    }

    // Handle opening custom tab for Trakt authorization
    LaunchedEffect(key1 = uiState.openCustomTab) {
        if (uiState.openCustomTab) {
            openCustomTab(context = context)
            viewModel.onCustomTabOpened() // Reset the flag
        }
    }

    // Show connection error snackbar
    LaunchedEffect(uiState.connectionError) {
        uiState.connectionError?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = error,
                    duration = SnackbarDuration.Long
                )
            }
            viewModel.clearConnectionError()
        }
    }

    // Show disconnection error snackbar
    LaunchedEffect(uiState.disconnectionError) {
        uiState.disconnectionError?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = error,
                    duration = SnackbarDuration.Long
                )
            }
            viewModel.clearDisconnectionError()
        }
    }

    // Show favorite shows error snackbar
    LaunchedEffect(favoriteShowsError) {
        favoriteShowsError?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Error loading favorites: $error",
                    duration = SnackbarDuration.Long
                )
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { localScaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(localScaffoldPadding)
                .scrollable(scrollState, orientation = Orientation.Vertical)
        ) {
            AccountContent(
                isAuthorizedOnTrakt = isAuthorizedOnTrakt,
                favoriteShowsList = favoriteShowsList,
                isFavoriteShowsEmpty = isFavoriteShowsEmpty,
                isLoadingConnection = isLoadingConnection,
                isLoadingFavorites = isLoadingFavorites,
                isDisconnecting = uiState.isDisconnecting,
                onConnectToTraktClick = { viewModel.onConnectToTraktClick() },
                onFavoriteClick = { item ->
                    navigator.navigate(
                        ShowDetailScreenDestination(
                            ShowDetailArg(
                                source = "favorites",
                                showId = item.tvMazeID.toString(),
                                showTitle = item.title,
                                showImageUrl = item.originalImageUrl,
                                showBackgroundUrl = item.mediumImageUrl
                            )
                        )
                    )
                },
                onLogoutClick = { viewModel.onDisconnectFromTraktClick() }
            )

            if (uiState.confirmDisconnectFromTrakt) {
                DisconnectTraktDialog(
                    onDismissed = { viewModel.onDisconnectFromTraktRefused() },
                    onConfirmed = { viewModel.onDisconnectConfirm() }
                )
            }
        }
    }
}

@ExperimentalMaterial3WindowSizeClassApi
@ExperimentalMaterial3Api
@ExperimentalFoundationApi
@Composable
private fun AccountContent(
    isAuthorizedOnTrakt: Boolean,
    favoriteShowsList: List<TraktUserListItem>,
    isFavoriteShowsEmpty: Boolean,
    isLoadingConnection: Boolean,
    isLoadingFavorites: Boolean,
    isDisconnecting: Boolean,
    onConnectToTraktClick: () -> Unit,
    onFavoriteClick: (item: TraktUserListItem) -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoadingConnection || isDisconnecting) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            if (isLoadingConnection) Text(text = stringResource(R.string.trakt_connecting_text))
            if (isDisconnecting) Text(text = stringResource(R.string.trakt_disconnecting_text))
        } else {
            if (isAuthorizedOnTrakt) {
                TraktProfileHeader(onLogoutClick = onLogoutClick)
                Spacer(modifier = Modifier.height(16.dp))

                if (isLoadingFavorites) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    Text(text = stringResource(R.string.trakt_loading_favorites))
                } else if (isFavoriteShowsEmpty) {
                    EmptyFavoritesContent()
                } else {
                    FavoritesListContent(
                        favoriteShows = favoriteShowsList,
                        widthSizeClass = getWindowSizeClass()?.widthSizeClass,
                        onFavoriteClick = onFavoriteClick,
                    )
                }
            } else {
                ConnectToTrakt(onClick = onConnectToTraktClick)
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
            TextButton(onClick = { onConfirmed() }) { // Changed to TextButton for Material 3
                Text(text = stringResource(id = R.string.disconnect_from_trakt_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismissed() }) { // Changed to TextButton for Material 3
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

    val chromePackageInfo = try {
        context.packageManager.getPackageInfo(packageName, 0)
    } catch (_: PackageManager.NameNotFoundException) {
        null
    }

    if (chromePackageInfo != null) {
        customBuilder.intent.setPackage(packageName)
        customBuilder.launchUrl(context, traktUrl.toUri())
    } else {
        try {
            val intent = Intent(Intent.ACTION_VIEW, traktUrl.toUri())
            if (intent.resolveActivity(context.packageManager) != null) {
                activity?.startActivity(intent)
                    ?: context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } else {
                Toast.makeText(context, "No browser found to open Trakt.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open browser: ${e.message}", Toast.LENGTH_LONG)
                .show()
        }
    }
}

@ExperimentalMaterial3WindowSizeClassApi
@ExperimentalMaterial3Api
@ExperimentalFoundationApi
@Composable
fun ConnectToTrakt(
    onClick: () -> Unit
) {
    val image: Painter = painterResource(id = R.drawable.ic_trakt_wide_red_white)
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Image(
            painter = image,
            stringResource(id = R.string.trakt_logo_description),
            modifier = Modifier.height(dimensionResource(id = R.dimen.trakt_account_not_authorized_logo_height))
        )
        Text(
            text = stringResource(id = R.string.trakt_connect_description),
            modifier = Modifier.padding(16.dp), // Increased padding
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 8.dp), // Adjusted padding
            onClick = { onClick() }
        ) {
            Text(text = stringResource(id = R.string.connect_to_trakt_button))
        }
    }
}
