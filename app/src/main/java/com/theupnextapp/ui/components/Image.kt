package com.theupnextapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import coil.compose.rememberImagePainter
import com.theupnextapp.R

@Composable
fun PosterImage(
    url: String,
    modifier: Modifier = Modifier,
    height: Dp = dimensionResource(id = R.dimen.compose_shows_list_poster_height),
) {
    Image(
        painter = rememberImagePainter(
            data = url,
            builder = {
                crossfade(true)
                placeholder(R.drawable.poster_placeholder)
                error(R.drawable.poster_placeholder)
                fallback(R.drawable.poster_placeholder)
            }),
        contentScale = ContentScale.Crop,
        contentDescription = null,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    )
}