package com.theupnextapp.bindings

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.theupnextapp.R
import org.jsoup.Jsoup

@BindingAdapter("dynamicText")
fun setDynamicText(textView: TextView, isAlreadyConnected : Boolean) {
    if (isAlreadyConnected) {
        textView.setText(R.string.trakt_button_text_is_connected)
    } else {
        textView.setText(R.string.trakt_button_text_not_connected)
    }
}

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