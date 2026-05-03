package com.theupnextapp.ui.simklAccount

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.theupnextapp.R
import kotlinx.coroutines.launch

@Composable
fun SimklAccountScreen(
    viewModel: SimklAccountViewModel = hiltViewModel(),
    navController: NavController,
    code: String? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val simklAccessToken by viewModel.simklAccessToken.collectAsStateWithLifecycle()
    val isAuthorized = simklAccessToken != null

    LaunchedEffect(viewModel, context) {
        viewModel.openCustomTab.collect { url ->
            launchCustomTab(context, url)
        }
    }

    // Handle deep link code for authorization
    LaunchedEffect(key1 = code, key2 = isAuthorized) {
        if (!code.isNullOrEmpty() && !isAuthorized) {
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

    Scaffold(
        topBar = {}, // Empty TopAppBar as MainScreen handles the title
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(contentPadding),
        ) {
            if (uiState.isConnecting) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (isAuthorized) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "SIMKL Account Connected")
                }
            } else {
                ConnectToSimkl(onClick = { viewModel.onConnectToSimklClick() })
            }
        }
    }
}

@Composable
fun ConnectToSimkl(onClick: () -> Unit) {
    // Reusing the trakt icon for now until we add a SIMKL icon
    val image: Painter = painterResource(id = R.drawable.ic_trakt_wide_red_white)
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors =
                CardDefaults.cardColors(
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
                    contentDescription = "SIMKL Logo",
                    modifier = Modifier.height(56.dp),
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Connect to SIMKL",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Connect your SIMKL account to sync watch history securely.",
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
                        text = "Connect SIMKL account",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

private fun launchCustomTab(
    context: Context,
    url: String,
) {
    val builder = CustomTabsIntent.Builder()
    val customTabsIntent = builder.build()
    customTabsIntent.launchUrl(context, Uri.parse(url))
}
