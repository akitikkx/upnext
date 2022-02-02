package com.theupnextapp.ui.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import com.theupnextapp.R
import com.theupnextapp.ui.components.PosterAttributionItem
import com.theupnextapp.ui.components.PosterImage
import com.theupnextapp.ui.components.PosterTitleTextItem

@ExperimentalMaterialApi
@Composable
fun ListPosterCard(
    itemName: String?,
    itemUrl: String?,
    onClick: () -> Unit
) {
    Card(
        elevation = 4.dp,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.padding(4.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.width(dimensionResource(id = R.dimen.compose_poster_frame_width))) {
            itemUrl?.let {
                PosterImage(
                    url = it,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(id = R.dimen.compose_shows_list_poster_height))
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                itemName?.let {
                    PosterTitleTextItem(title = it)
                }
                PosterAttributionItem()
            }
        }
    }
}