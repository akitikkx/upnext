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