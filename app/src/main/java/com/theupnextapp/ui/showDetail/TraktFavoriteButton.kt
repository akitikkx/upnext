/*
 * MIT License
 *
 * Copyright (c) 2022 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.ui.showDetail

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.theupnextapp.R

@Composable
fun TraktFavoriteButton(
    isAuthorizedOnTrakt: Boolean?,
    isFavorite: Boolean?,
    onFavoriteClick: () -> Unit
) {
    if (isAuthorizedOnTrakt == true) {
        if (isFavorite == true) {
            OutlinedButton(
                onClick = { onFavoriteClick() },
                modifier = Modifier.padding(4.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.btn_show_detail_remove_from_favorites)
                )
            }
        } else {
            Button(
                onClick = { onFavoriteClick() },
                modifier = Modifier.padding(4.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.btn_show_detail_add_to_favorites)
                )
            }
        }
    }
}