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

import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.theupnextapp.R

@BindingAdapter("goneIfNotNull")
fun goneIfNotNull(view: View, it: Any?) {
    view.visibility = if (it != null) View.GONE else View.VISIBLE
}

@BindingAdapter("showHideView")
fun showHideView(view: View, show: Boolean) {
    if (show) {
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("showProgress")
fun showProgress(view: ProgressBar, show: Boolean) {
    view.visibility = if (show) View.VISIBLE else View.GONE
}

@BindingAdapter("loadingVisibility")
fun loadingVisibility(view: View, isLoading: Boolean) {
    view.alpha = if (isLoading) .5f else 1f
}

@BindingAdapter("canClickItem")
fun canClickItem(view: View, isLoading: Boolean) {
    view.isClickable = !isLoading
    view.isFocusable = !isLoading
}

@BindingAdapter("seasonAndNumber")
fun seasonAndNumber(view: TextView, number: Int?) {
    view.text = view.resources.getString(
        R.string.show_detail_season_and_number,
        number
    )
}

@BindingAdapter("episodeAndNumber")
fun episodeAndNumber(view: TextView, number: Int?) {
    view.text = view.resources.getString(
        R.string.show_detail_episode_and_number,
        number
    )
}

@BindingAdapter("seasonNumber", "episodeNumber")
fun seasonAndEpisodeNumber(view: TextView, seasonNumber: Int?, episodeNumber: Int?) {
    view.text = view.resources.getString(
        R.string.show_detail_season_and_episode_number,
        seasonNumber,
        episodeNumber
    )
}

@BindingAdapter("seasonCount")
fun seasonCount(view: TextView, number: Int?) {
    if (number != null || number == 0) {
        view.text = view.resources.getString(
            R.string.show_detail_season_count,
            number
        )
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("seasonPremiereDate")
fun seasonPremiereDate(view: TextView, date: String?) {
    if (!date.isNullOrEmpty()) {
        view.text = view.resources.getString(
            R.string.show_detail_season_premiere_date,
            date
        )
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("seasonEndDate")
fun seasonEndDate(view: TextView, date: String?) {
    if (!date.isNullOrEmpty()) {
        view.text = view.resources.getString(
            R.string.show_detail_season_end_date,
            date
        )
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}