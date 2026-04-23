package com.theupnextapp.ui.showDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import com.theupnextapp.R
import com.valentinilk.shimmer.shimmer

@Composable
fun SummaryPlaceholder() {
    Column(modifier = Modifier.shimmer()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.LightGray.copy(alpha = 0.3f)),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(24.dp)
                .padding(horizontal = 16.dp)
                .background(Color.LightGray.copy(alpha = 0.3f)),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(16.dp)
                .padding(horizontal = 16.dp)
                .background(Color.LightGray.copy(alpha = 0.3f)),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(Modifier.padding(horizontal = 16.dp)) {
            repeat(4) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f)),
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp, 40.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f)),
            )
            Box(
                modifier = Modifier
                    .size(100.dp, 40.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f)),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun CastListPlaceholder() {
    LazyRow(
        modifier = Modifier
            .testTag("cast_loading")
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shimmer(),
    ) {
        items(5) {
            Column(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .width(dimensionResource(id = R.dimen.compose_show_detail_poster_width)),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .width(dimensionResource(id = R.dimen.compose_show_detail_poster_width))
                        .height(dimensionResource(id = R.dimen.compose_show_detail_poster_height))
                        .background(Color.LightGray.copy(alpha = 0.3f)),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(12.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f)),
                )
            }
        }
    }
}

@Composable
fun EpisodePlaceholder() {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shimmer(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(20.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f)),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f)),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(14.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f)),
            )
        }
    }
}
