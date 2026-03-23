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

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.theupnextapp.R
import com.theupnextapp.domain.TraktAuthState
import com.theupnextapp.domain.TraktUserListItem
import com.theupnextapp.navigation.Destinations
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3WindowSizeClassApi::class,
    ExperimentalFoundationApi::class,
    ExperimentalAnimationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalCoroutinesApi::class,
)
@Composable
fun TraktAccountScreen(
    viewModel: TraktAccountViewModel = hiltViewModel(),
    navController: NavController,
    code: String? = null,
) {
    val watchlistLazyListState = androidx.compose.foundation.lazy.rememberLazyListState()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val traktAuthState by viewModel.traktAuthState.collectAsStateWithLifecycle()
    val isAuthorizedOnTrakt by viewModel.isAuthorizedOnTrakt.collectAsStateWithLifecycle()
    val watchlistShowsList by viewModel.watchlistShows.collectAsStateWithLifecycle()
    val isLoadingConnection by viewModel.isLoadingConnection.collectAsStateWithLifecycle()
    val isLoadingWatchlists by viewModel.isLoadingWatchlistShows.collectAsStateWithLifecycle()
    val watchlistShowsError by viewModel.watchlistShowsError.collectAsStateWithLifecycle()
    val isWatchlistShowsEmpty by viewModel.watchlistShowsEmpty.collectAsStateWithLifecycle()
    val watchlistSearchQuery by viewModel.watchlistSearchQuery.collectAsStateWithLifecycle()
    val watchlistSortOption by viewModel.watchlistSortOption.collectAsStateWithLifecycle()
    val isPullRefreshing by viewModel.isPullRefreshing.collectAsStateWithLifecycle()

    val onRefreshWatchlist = {
        viewModel.onRefreshWatchlist()
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.onSilentRefreshWatchlist()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(viewModel, context) {
        viewModel.openCustomTab.collect { url ->
            launchCustomTab(context, url)
        }
    }

    // Handle deep link code for authorization
    LaunchedEffect(key1 = code, key2 = isAuthorizedOnTrakt) {
        if (!code.isNullOrEmpty() && !isAuthorizedOnTrakt) {
            viewModel.onCodeReceived(code)
        }
    }

    // Show connection error snackbar
    LaunchedEffect(uiState.connectionError) {
        uiState.connectionError?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = error,
                    duration = SnackbarDuration.Long,
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
                    duration = SnackbarDuration.Long,
                )
            }
            viewModel.clearDisconnectionError()
        }
    }

    // Show watchlist shows error snackbar
    LaunchedEffect(watchlistShowsError) {
        watchlistShowsError?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Error loading watchlists: $error",
                    duration = SnackbarDuration.Long,
                )
            }
        }
    }

    Scaffold(
        topBar = {}, // Empty TopAppBar as MainScreen handles the title
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize(),
    ) { localScaffoldPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(localScaffoldPadding),
        ) {
            AccountContent(
                traktAuthState = traktAuthState,
                watchlistShowsList = watchlistShowsList,
                isWatchlistShowsEmpty = isWatchlistShowsEmpty,
                isLoadingConnection = isLoadingConnection,
                isLoadingWatchlists = isLoadingWatchlists,
                isPullRefreshing = isPullRefreshing,
                isDisconnecting = uiState.isDisconnecting,
                watchlistSearchQuery = watchlistSearchQuery,
                watchlistSortOption = watchlistSortOption,
                watchlistLazyListState = watchlistLazyListState,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onSortOptionChange = viewModel::onSortOptionChange,
                onRefreshWatchlist = onRefreshWatchlist,
                onConnectToTraktClick = {
                    viewModel.onConnectToTraktClick()
                },
                onWatchlistClick = { item ->
                    navController.navigate(
                        Destinations.ShowDetail(
                            source = "watchlists",
                            showId = item.tvMazeID.toString(),
                            showTitle = item.title,
                            showImageUrl = item.originalImageUrl,
                            showBackgroundUrl = item.mediumImageUrl,
                            imdbID = null,
                            isAuthorizedOnTrakt = null,
                            showTraktId = null,
                        ),
                    )
                },
                onRemoveItem = { item ->
                    item.traktID?.let { traktId ->
                        viewModel.onRemoveFromWatchlistClick(traktId)
                    }
                },
                onLogoutClick = { viewModel.onDisconnectFromTraktClick() },
            )

            if (uiState.confirmDisconnectFromTrakt) {
                DisconnectTraktDialog(
                    onDismissed = { viewModel.onDisconnectFromTraktRefused() },
                    onConfirmed = { viewModel.onDisconnectConfirm() },
                )
            }
        }
    }
}

@ExperimentalMaterial3WindowSizeClassApi
@ExperimentalMaterial3Api
@ExperimentalFoundationApi
@Composable
internal fun AccountContent(
    traktAuthState: TraktAuthState,
    watchlistShowsList: List<TraktUserListItem>,
    isWatchlistShowsEmpty: Boolean,
    isLoadingConnection: Boolean,
    isLoadingWatchlists: Boolean,
    isDisconnecting: Boolean,
    watchlistSearchQuery: String,
    watchlistSortOption: WatchlistSortOption,
    watchlistLazyListState: androidx.compose.foundation.lazy.LazyListState,
    isPullRefreshing: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSortOptionChange: (WatchlistSortOption) -> Unit,
    onRefreshWatchlist: () -> Unit,
    onConnectToTraktClick: () -> Unit,
    onWatchlistClick: (item: TraktUserListItem) -> Unit,
    onRemoveItem: (item: TraktUserListItem) -> Unit,
    onLogoutClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isLoadingConnection || isDisconnecting) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            if (isLoadingConnection) Text(text = stringResource(R.string.trakt_connecting_text))
            if (isDisconnecting) Text(text = stringResource(R.string.trakt_disconnecting_text))
        } else {
            when (traktAuthState) {
                TraktAuthState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }

                TraktAuthState.LoggedOut -> {
                    ConnectToTrakt(onClick = onConnectToTraktClick)
                }

                TraktAuthState.LoggedIn -> {
                    PullToRefreshBox(
                        isRefreshing = isPullRefreshing,
                        onRefresh = onRefreshWatchlist,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                    ) {
                        if (!isLoadingWatchlists && !isWatchlistShowsEmpty) {
                            WatchlistListContent(
                                watchlistItems = watchlistShowsList,
                                watchlistSearchQuery = watchlistSearchQuery,
                                watchlistSortOption = watchlistSortOption,
                                lazyListState = watchlistLazyListState,
                                onSearchQueryChange = onSearchQueryChange,
                                onSortOptionChange = onSortOptionChange,
                                modifier = Modifier.fillMaxSize(),
                                header = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        TraktProfileHeader(onLogoutClick = onLogoutClick)
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                },
                                onItemClick = onWatchlistClick,
                                onRemoveItem = onRemoveItem,
                            )
                        } else {
                            Column(
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                TraktProfileHeader(onLogoutClick = onLogoutClick)
                                Spacer(modifier = Modifier.height(16.dp))

                                if (isLoadingWatchlists && !isPullRefreshing) {
                                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                                    Text(text = stringResource(R.string.trakt_loading_favorites))
                                } else {
                                    EmptyWatchlistContent()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DisconnectTraktDialog(
    onDismissed: () -> Unit,
    onConfirmed: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onDismissed() },
        title = {
            Text(
                text = stringResource(id = R.string.disconnect_from_trakt_dialog_title),
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.disconnect_from_trakt_dialog_message),
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
        },
    )
}

@ExperimentalMaterial3WindowSizeClassApi
@ExperimentalMaterial3Api
@ExperimentalFoundationApi
@Composable
fun ConnectToTrakt(onClick: () -> Unit) {
    val image: Painter = painterResource(id = R.drawable.ic_trakt_wide_red_white)
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material3.Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors =
                androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
            ) {
                Image(
                    painter = image,
                    contentDescription = stringResource(id = R.string.trakt_logo_description),
                    modifier = Modifier.height(56.dp),
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Unlock Personalization",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Connect your Trakt account to automatically track your watch progress, sync your history securely, and manage your watchlists seamlessly.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onClick() },
                ) {
                    Text(
                        text = stringResource(id = R.string.connect_to_trakt_button),
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    )
                }
            }
        }
    }
}

private fun launchCustomTab(
    context: android.content.Context,
    url: String,
) {
    val builder = androidx.browser.customtabs.CustomTabsIntent.Builder()
    val customTabsIntent = builder.build()
    customTabsIntent.launchUrl(context, android.net.Uri.parse(url))
}
