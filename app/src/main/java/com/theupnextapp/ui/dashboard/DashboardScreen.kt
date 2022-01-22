package com.theupnextapp.ui.dashboard

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.theupnextapp.domain.ScheduleShow
import com.theupnextapp.ui.widgets.ListPosterCard

@ExperimentalMaterialApi
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onShowClick: (item: ScheduleShow) -> Unit
) {
    val yesterdayShowsList = viewModel.yesterdayShowsList.observeAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        yesterdayShowsList.value?.let { list ->
            YesterdayShows(list = list) {
                onShowClick(it)
            }
        }
    }

}

@ExperimentalMaterialApi
@Composable
fun YesterdayShows(
    list: List<ScheduleShow>,
    onClick: (item: ScheduleShow) -> Unit
) {
    LazyRow {
        items(list) { show ->
            ListPosterCard(
                itemName = show.name,
                itemUrl = show.originalImage
            ) {
                onClick(show)
            }
        }
    }
}
