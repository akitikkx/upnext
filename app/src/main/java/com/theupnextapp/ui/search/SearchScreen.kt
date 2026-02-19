/*
 * MIT License
 *
 * Copyright (c) 2022 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.theupnextapp.R
import com.theupnextapp.domain.RecentSearch
import com.theupnextapp.domain.ShowSearch
import com.theupnextapp.navigation.Destinations
import com.theupnextapp.ui.widgets.SearchListCard

@ExperimentalMaterial3WindowSizeClassApi
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    navController: NavController,
) {
    val searchResultsList = viewModel.searchResponse.observeAsState()

    val isLoading = viewModel.isLoading.observeAsState()

    val recentSearches = viewModel.recentSearches.observeAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(8.dp),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                SearchArea(
                    searchResultsList = searchResultsList.value,
                    recentSearches = recentSearches.value,
                    onResultClick = {
                        it.name?.let { name ->
                            viewModel.onQuerySaved(name)
                        }
                        navController.navigate(
                            Destinations.ShowDetail(
                                source = "search",
                                showId = it.id.toString(),
                                showTitle = it.name,
                                showImageUrl = it.originalImageUrl,
                                showBackgroundUrl = it.mediumImageUrl,
                                imdbID = null,
                                isAuthorizedOnTrakt = null,
                                showTraktId = null,
                            ),
                        )
                    },
                    onRecentSearchClick = {
                        viewModel.onQueryTextSubmit(it)
                    },
                    onTextSubmit = {
                        viewModel.onQueryTextSubmit(it)
                    },
                    onClearRecentSearches = {
                        viewModel.onClearRecentSearches()
                    },
                )

                if (isLoading.value == true) {
                    LinearProgressIndicator(
                        modifier =
                            Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@Composable
fun SearchArea(
    searchResultsList: List<ShowSearch>?,
    recentSearches: List<RecentSearch>?,
    onTextSubmit: (query: String) -> Unit,
    onResultClick: (item: ShowSearch) -> Unit,
    onRecentSearchClick: (query: String) -> Unit,
    onClearRecentSearches: () -> Unit,
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        SearchForm {
            onTextSubmit(it)
        }

        if (!searchResultsList.isNullOrEmpty()) {
            SearchResultsList(list = searchResultsList) {
                onResultClick(it)
            }
        } else if (!recentSearches.isNullOrEmpty()) {
            RecentSearchesList(
                list = recentSearches,
                onRecentSearchClick = onRecentSearchClick,
                onClearRecentSearches = onClearRecentSearches,
            )
        }
    }
}

@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@Composable
fun SearchForm(onSearch: (String) -> Unit) {
    val searchQueryState = rememberSaveable { mutableStateOf("") }

    SearchInputField(
        inputLabel = stringResource(id = R.string.search_input_hint),
        valueState = searchQueryState,
        onValueChange = {
            onSearch(searchQueryState.value.trim())
        },
    )
}

@ExperimentalMaterial3Api
@Composable
fun SearchInputField(
    modifier: Modifier = Modifier,
    inputLabel: String,
    valueState: MutableState<String>,
    onValueChange: (value: String) -> Unit,
) {
    OutlinedTextField(
        value = valueState.value,
        onValueChange = {
            valueState.value = it
            onValueChange(valueState.value)
        },
        label = { Text(inputLabel) },
        singleLine = true,
        modifier =
            modifier
                .padding(8.dp)
                .fillMaxWidth(),
    )
}

@ExperimentalMaterial3Api
@Composable
fun SearchResultsList(
    list: List<ShowSearch>,
    onClick: (item: ShowSearch) -> Unit,
) {
    LazyColumn {
        items(list) { result ->
            SearchListCard(item = result) {
                onClick(result)
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun RecentSearchesList(
    list: List<RecentSearch>,
    onRecentSearchClick: (query: String) -> Unit,
    onClearRecentSearches: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = R.string.search_recent_searches),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onClearRecentSearches) {
                Text(
                    text = stringResource(id = R.string.search_clear_recent),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        LazyColumn {
            items(list) { recentSearch ->
                ListItem(
                    headlineContent = { Text(recentSearch.query) },
                    leadingContent = { Icon(Icons.Default.History, contentDescription = null) },
                    modifier = Modifier.clickable { onRecentSearchClick(recentSearch.query) }
                )
            }
        }
    }
}
