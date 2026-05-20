package com.theupnextapp.ui.simklAccount

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.theupnextapp.R

@Composable
fun SimklProfileHeader(modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        val logoResId = if (isSystemInDarkTheme()) {
            R.drawable.ic_simkl_logo_white
        } else {
            R.drawable.ic_simkl_logo_black
        }

        Image(
            painter = painterResource(id = logoResId),
            contentDescription = "SIMKL Logo",
            modifier = Modifier.height(33.dp),
        )
    }
}
