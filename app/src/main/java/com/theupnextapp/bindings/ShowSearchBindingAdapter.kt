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

package com.theupnextapp.bindings

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.theupnextapp.R
import com.theupnextapp.domain.ShowSearch

@BindingAdapter("showNameAndReleaseYear")
fun showNameAndReleaseYear(view: TextView, showSearch: ShowSearch) {
    if (!showSearch.status.isNullOrEmpty()) {
        if (showSearch.status != "Ended") {
            view.text = view.resources.getString(
                R.string.search_item_not_ended,
                showSearch.name,
                showSearch.premiered?.substring(0, 4)
            )
        } else {
            view.text = view.resources.getString(
                R.string.search_item_ended,
                showSearch.name,
                showSearch.premiered?.substring(0, 4)
            )
        }
    } else {
        view.text = view.resources.getString(
            R.string.search_item_ended,
            showSearch.name,
            showSearch.premiered?.substring(0, 4)
        )
    }
}