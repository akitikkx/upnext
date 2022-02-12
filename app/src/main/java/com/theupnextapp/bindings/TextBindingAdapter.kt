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
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.theupnextapp.R
import com.theupnextapp.common.utils.models.TimeDifferenceForDisplay
import com.theupnextapp.domain.TraktShowRating
import org.jsoup.Jsoup

@BindingAdapter("fromHtml")
fun fromHtml(view: TextView, html: String?) {
    if (!html.isNullOrEmpty()) {
        view.text = Jsoup.parse(html).text()
    }
}

@BindingAdapter("showRatingVotes")
fun showRatingVotes(view: TextView, votes: Int?) {
    if (votes != null) {
        view.text = view.resources.getString(
            R.string.show_detail_rating_votes,
            votes
        )
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("showHideStringContent")
fun showHideStringContent(view: View, content: String?) {
    view.visibility = if (!content.isNullOrEmpty()) View.VISIBLE else View.GONE
}

@BindingAdapter("showHideIntContent")
fun showHideIntContent(view: View, content: Int?) {
    view.visibility = if (content == null) View.GONE else View.VISIBLE
}

@BindingAdapter("showHideDoubleContent")
fun showHideDoubleContent(view: View, content: Double?) {
    view.visibility = if (content == null) View.GONE else View.VISIBLE
}

@BindingAdapter("showRating")
fun showRating(view: TextView, rating: Int?) {
    if (rating != null) {
        view.text = view.resources.getString(
            R.string.show_detail_rating,
            rating
        )
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("showRatingNumerator")
fun showRatingNumerator(view: TextView, rating: Int?) {
    if (rating != null) {
        view.text = view.resources.getString(
            R.string.show_detail_rating_numerator,
            rating
        )
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("lastUpdate")
fun lastUpdate(view: TextView, timeDifferenceForDisplay: TimeDifferenceForDisplay?) {
    if (timeDifferenceForDisplay != null) {
        view.text = view.resources.getString(
            R.string.library_table_last_update,
            timeDifferenceForDisplay.difference,
            timeDifferenceForDisplay.type
        )
    } else {
        view.text = view.resources.getString(
            R.string.library_table_last_update_null
        )
    }
}

@BindingAdapter("showRatingAndVotes")
fun showRatingAndVotes(view: TextView, showRating: TraktShowRating?) {
    if (showRating?.rating != null && showRating.votes != null) {
        view.text = view.resources.getString(
            R.string.show_detail_rating_and_votes,
            showRating.rating,
            showRating.votes
        )
        view.visibility = View.VISIBLE
    } else if (showRating != null && showRating.votes == null) {
        view.text = view.resources.getString(
            R.string.show_detail_rating,
            showRating.rating
        )
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}