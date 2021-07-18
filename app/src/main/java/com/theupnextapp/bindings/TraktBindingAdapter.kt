package com.theupnextapp.bindings

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.theupnextapp.R
import com.theupnextapp.domain.TraktUserListItem

@BindingAdapter("favoriteTitleAndYear")
fun favoriteTitleAndYear(view: TextView, show: TraktUserListItem) {
    if (show.year.isNullOrEmpty()) {
        view.text = view.resources.getString(R.string.favorite_title_no_year, show.title)
    } else {
        view.text = view.resources.getString(
            R.string.favorite_title_and_release_year,
            show.title,
            show.year
        )
    }
}
@BindingAdapter("addRemoveFavoriteBtnText")
fun addRemoveFavoriteBtnText(view: TextView, traktUserListItem: TraktUserListItem?) {
    if (traktUserListItem == null) {
        view.text = view.resources.getString(R.string.btn_show_detail_add_to_favorites)
    } else {
        view.text = view.resources.getString(R.string.btn_show_detail_remove_from_favorites)
    }
}