package com.theupnextapp.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.theupnextapp.R
import com.theupnextapp.common.utils.models.getNameAndReleaseYearResource
import com.theupnextapp.domain.ShowSearch
import com.theupnextapp.ui.components.PosterImage

@ExperimentalMaterialApi
@Composable
fun SeasonListCard(
    item: ShowSearch,
    onClick: () -> Unit
) {
    Card(
        elevation = 4.dp,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            item.originalImageUrl?.let { url ->
                PosterImage(
                    url = url,
                    modifier = Modifier
                        .width(dimensionResource(id = R.dimen.compose_search_poster_width))
                        .height(dimensionResource(id = R.dimen.compose_search_poster_height))
                )
            }
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = stringResource(
                        getNameAndReleaseYearResource(item),
                        item.name.toString(),
                        item.premiered?.substring(0, 4).toString()
                    ),
                    modifier = Modifier.padding(4.dp),
                    style = MaterialTheme.typography.h6
                )
                Text(
                    text = item.status.toString(),
                    modifier = Modifier.padding(4.dp),
                    style = MaterialTheme.typography.caption
                )
            }
        }
    }
}