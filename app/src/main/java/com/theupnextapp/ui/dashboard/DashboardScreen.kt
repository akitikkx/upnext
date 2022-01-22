package com.theupnextapp.ui.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.theupnextapp.R
import com.theupnextapp.domain.ScheduleShow
import com.theupnextapp.ui.components.SectionHeadingText
import com.theupnextapp.ui.widgets.ListPosterCard

@ExperimentalMaterialApi
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onShowClick: (item: ScheduleShow) -> Unit
) {
    val yesterdayShowsList = viewModel.yesterdayShowsList.observeAsState()

    val todayShowsList = viewModel.todayShowsList.observeAsState()

    val tomorrowShowsList = viewModel.tomorrowShowsList.observeAsState()

    val scrollState = rememberScrollState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.verticalScroll(scrollState)
        ) {
            yesterdayShowsList.value?.let { list ->
                if (list.isNotEmpty()) {
                    ShowsRow(
                        list = list,
                        rowTitle = stringResource(id = R.string.title_yesterday_shows)
                    ) {
                        onShowClick(it)
                    }
                }
            }

            todayShowsList.value?.let { list ->
                if (list.isNotEmpty()) {
                    ShowsRow(
                        list = list,
                        rowTitle = stringResource(id = R.string.title_today_shows)
                    ) {
                        onShowClick(it)
                    }
                }
            }

            tomorrowShowsList.value?.let { list ->
                if (list.isNotEmpty()) {
                    ShowsRow(
                        list = list,
                        rowTitle = stringResource(id = R.string.title_tomorrow_shows)
                    ) {
                        onShowClick(it)
                    }
                }
            }
        }
    }

}

@ExperimentalMaterialApi
@Composable
fun ShowsRow(
    list: List<ScheduleShow>,
    rowTitle: String,
    onClick: (item: ScheduleShow) -> Unit
) {
    Column {
        SectionHeadingText(text = rowTitle)

        LazyRow(modifier = Modifier.padding(8.dp)) {
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
}
