package com.theupnextapp.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PosterTitleTextItem(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.caption,
        maxLines = 1,
        modifier = Modifier.padding(
            start = 4.dp,
            top = 8.dp,
            bottom = 8.dp,
            end = 4.dp
        )
    )
}