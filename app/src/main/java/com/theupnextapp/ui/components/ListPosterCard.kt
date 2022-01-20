package com.theupnextapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.theupnextapp.R
import com.theupnextapp.domain.ScheduleShow

@ExperimentalMaterialApi
@Composable
fun ListPosterCard(
    item: ScheduleShow,
    onClick: () -> Unit
) {
    Card(
        elevation = 4.dp,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.padding(4.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.width(dimensionResource(id = R.dimen.compose_poster_frame_width))) {
            Image(
                painter = rememberImagePainter(
                    data = item.originalImage,
                    builder = {
                        crossfade(true)
                    }),
                contentScale = ContentScale.Crop,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(id = R.dimen.compose_shows_list_poster_height))
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                item.name?.let {
                    Text(
                        text = it,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier.padding(4.dp)
                    )
                }
                Text(
                    text = stringResource(id = R.string.tv_maze_creative_commons_attribution_text_single),
                    modifier = Modifier.padding(4.dp),
                    style = MaterialTheme.typography.caption
                )
            }
        }
    }
}