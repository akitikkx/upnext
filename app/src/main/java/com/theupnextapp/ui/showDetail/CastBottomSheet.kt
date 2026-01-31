/*
 * MIT License
 *
 * Copyright (c) 2024 Ahmed Tikiwa
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

package com.theupnextapp.ui.showDetail

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.theupnextapp.network.models.trakt.NetworkTraktPersonShowCastCredit
import com.theupnextapp.ui.components.PosterImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CastBottomSheet(
    uiState: ShowDetailViewModel.CastBottomSheetUiState,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    onCreditClick: (NetworkTraktPersonShowCastCredit) -> Unit,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        val context = LocalContext.current

        LazyColumn(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            item {
                if (uiState.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                uiState.errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp),
                    )
                }

                uiState.traktCast?.let { cast ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PosterImage(
                            url = cast.originalImageUrl ?: "",
                            modifier =
                                Modifier
                                    .width(80.dp)
                                    .height(120.dp)
                                    .padding(end = 16.dp),
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = cast.name ?: "",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            if (!cast.character.isNullOrEmpty()) {
                                Text(
                                    text = "as ${cast.character}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontStyle = FontStyle.Italic,
                                )
                            }
                        }
                    }
                }
            }

            item {
                uiState.personSummary?.let { person ->
                    val biography = person.biography
                    if (!biography.isNullOrEmpty()) {
                        Text(
                            text = "Bio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                        Text(
                            text = biography,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            uiState.personCredits?.cast?.let { credits ->
                if (credits.isNotEmpty()) {
                    item {
                        Text(
                            text = "Filmography (Shows)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }

                    items(credits) { credit ->
                        val showTitle = credit.show?.title ?: "Unknown Show"
                        val character = credit.character ?: credit.characters?.joinToString(", ") ?: "Unknown Character"
                        val year = credit.show?.year?.toString() ?: ""

                        val isClickable = !credit.show?.ids?.imdb.isNullOrEmpty()

                        Card(
                            onClick = {
                                if (isClickable) {
                                    onCreditClick(credit)
                                } else {
                                    Toast.makeText(context, "Show details unavailable", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                            enabled = true, // Always enabled to capture click for Toast
                        ) {
                            Column(
                                modifier =
                                    Modifier
                                        .padding(12.dp)
                                        .alpha(if (isClickable) 1f else 0.38f), // Visually dim disabled items
                            ) {
                                Text(
                                    text = "$showTitle ($year)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(text = "as $character", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
