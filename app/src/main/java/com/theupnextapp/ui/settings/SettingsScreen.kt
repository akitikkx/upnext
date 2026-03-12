package com.theupnextapp.ui.settings

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.theupnextapp.common.utils.TraktConstants
import com.theupnextapp.domain.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
) {
    val theme by viewModel.themeStream.collectAsState()
    val dataSaver by viewModel.dataSaverStream.collectAsState()
    val traktToken by viewModel.traktAccessToken.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Settings", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
        ) {
            // General Section
            SettingsSectionHeader(title = "General")

            // Theme Setting
            var showThemeDialog by remember { mutableStateOf(false) }
            ListItem(
                headlineContent = { Text("App Theme") },
                supportingContent = { Text(theme.name) },
                modifier = Modifier.clickable { showThemeDialog = !showThemeDialog },
            )

            if (showThemeDialog) {
                Column(modifier = Modifier.padding(start = 32.dp, end = 16.dp)) {
                    Theme.values().forEach { themeOption ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.onThemeSelected(themeOption)
                                        showThemeDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                        ) {
                            Text(text = themeOption.name, modifier = Modifier.weight(1f))
                            if (theme == themeOption) {
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            ListItem(
                headlineContent = { Text("Data Saver Mode") },
                supportingContent = { Text("Limit image quality to save bandwidth") },
                trailingContent = {
                    Switch(checked = dataSaver, onCheckedChange = { viewModel.onDataSaverToggled(it) })
                },
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Account Section
            SettingsSectionHeader(title = "Account")

            if (traktToken != null) {
                ListItem(
                    headlineContent = { Text("Trakt.tv Connection") },
                    supportingContent = { Text("Connected") },
                    trailingContent = {
                        Text(
                            text = "Disconnect",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.clickable { viewModel.onDisconnectTrakt() },
                        )
                    },
                )
            } else {
                ListItem(
                    headlineContent = { Text("Trakt.tv Connection") },
                    supportingContent = { Text("Not connected. Track progress and sync history.") },
                    trailingContent = {
                        Text(
                            text = "Connect",
                            color = MaterialTheme.colorScheme.primary,
                            modifier =
                                Modifier.clickable {
                                    val builder = CustomTabsIntent.Builder()
                                    val customTabsIntent = builder.build()
                                    customTabsIntent.launchUrl(context, Uri.parse(TraktConstants.TRAKT_AUTH_URL))
                                },
                        )
                    },
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // About Section
            SettingsSectionHeader(title = "About")
            ListItem(
                headlineContent = { Text("Version") },
                supportingContent = { Text("3.10.1") },
            )
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp),
    )
}
