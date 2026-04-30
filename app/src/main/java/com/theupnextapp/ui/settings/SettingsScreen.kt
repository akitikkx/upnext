package com.theupnextapp.ui.settings

import android.Manifest
import android.net.Uri
import android.os.Build
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.theupnextapp.R
import com.theupnextapp.common.utils.TraktConstants
import com.theupnextapp.domain.Theme

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val theme by viewModel.themeStream.collectAsState()
    val dataSaver by viewModel.dataSaverStream.collectAsState()
    val notificationsEnabled by viewModel.areNotificationsEnabled.collectAsState()
    val traktToken by viewModel.traktAccessToken.collectAsState()
    val context = LocalContext.current

    val notificationsPermissionState =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            null
        }

    LaunchedEffect(notificationsPermissionState?.status) {
        if (notificationsPermissionState?.status?.isGranted == false && notificationsEnabled) {
            viewModel.onNotificationsToggled(false)
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
    ) {
        // General Section
        SettingsSectionHeader(title = stringResource(id = R.string.settings_general))

        ListItem(
            headlineContent = { Text(stringResource(id = R.string.settings_episode_notifications)) },
            supportingContent = { Text(stringResource(id = R.string.settings_episode_notifications_desc)) },
            trailingContent = {
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { isChecked ->
                        if (isChecked) {
                            if (notificationsPermissionState != null && !notificationsPermissionState.status.isGranted) {
                                notificationsPermissionState.launchPermissionRequest()
                            } else {
                                viewModel.onNotificationsToggled(true)
                            }
                        } else {
                            viewModel.onNotificationsToggled(false)
                        }
                    },
                )
            },
        )

        // Theme Setting
        var showThemeDialog by remember { mutableStateOf(false) }
        ListItem(
            headlineContent = { Text(stringResource(id = R.string.settings_app_theme)) },
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
                                .minimumInteractiveComponentSize()
                                .clickable {
                                    viewModel.onThemeSelected(themeOption)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp),
                    ) {
                        Text(text = themeOption.name, modifier = Modifier.weight(1f))
                        if (theme == themeOption) {
                            Icon(Icons.Default.Check, contentDescription = stringResource(id = R.string.settings_theme_selected), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        ListItem(
            headlineContent = { Text(stringResource(id = R.string.settings_data_saver)) },
            supportingContent = { Text(stringResource(id = R.string.settings_data_saver_desc)) },
            trailingContent = {
                Switch(checked = dataSaver, onCheckedChange = { viewModel.onDataSaverToggled(it) })
            },
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Account Section
        SettingsSectionHeader(title = stringResource(id = R.string.settings_account))

        // Disconnect confirmation state
        var showDisconnectDialog by remember { mutableStateOf(false) }

        if (traktToken != null) {
            ListItem(
                headlineContent = { Text(stringResource(id = R.string.settings_trakt_connection)) },
                supportingContent = { Text(stringResource(id = R.string.settings_connected)) },
                trailingContent = {
                    Text(
                        text = stringResource(id = R.string.settings_disconnect),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.minimumInteractiveComponentSize().clickable { showDisconnectDialog = true },
                    )
                },
            )

            if (showDisconnectDialog) {
                DisconnectTraktDialog(
                    onDismissed = { showDisconnectDialog = false },
                    onConfirmed = {
                        viewModel.onDisconnectTrakt()
                        showDisconnectDialog = false
                    },
                )
            }
        } else {
            ListItem(
                headlineContent = { Text(stringResource(id = R.string.settings_trakt_connection)) },
                supportingContent = { Text(stringResource(id = R.string.settings_trakt_not_connected)) },
                trailingContent = {
                    Text(
                        text = stringResource(id = R.string.settings_connect),
                        color = MaterialTheme.colorScheme.primary,
                        modifier =
                            Modifier.minimumInteractiveComponentSize().clickable {
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
        SettingsSectionHeader(title = stringResource(id = R.string.settings_about))
        ListItem(
            headlineContent = { Text(stringResource(id = R.string.settings_version)) },
            supportingContent = { Text(com.theupnextapp.BuildConfig.VERSION_NAME) },
        )
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
            TextButton(onClick = { onConfirmed() }) {
                Text(text = stringResource(id = R.string.disconnect_from_trakt_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismissed() }) {
                Text(text = stringResource(id = R.string.disconnect_from_trakt_dialog_cancel))
            }
        },
    )
}
