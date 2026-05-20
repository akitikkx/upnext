package com.theupnextapp.ui.account

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.theupnextapp.repository.ProviderManager
import com.theupnextapp.ui.simklAccount.SimklAccountScreen
import com.theupnextapp.ui.traktAccount.TraktAccountScreen

@Composable
fun AccountScreen(
    viewModel: AccountViewModel = hiltViewModel(),
    navController: NavController,
    code: String? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val activeProvider by viewModel.activeProvider.collectAsState()

    if (activeProvider == ProviderManager.PROVIDER_SIMKL) {
        SimklAccountScreen(
            navController = navController,
            code = code,
            contentPadding = contentPadding
        )
    } else {
        TraktAccountScreen(
            navController = navController,
            code = code,
            contentPadding = contentPadding
        )
    }
}
