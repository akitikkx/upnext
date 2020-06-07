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

@BindingAdapter("watchedProgressBottomSheetTitle")
fun watchedProgressBottomSheetTitle(view: TextView, showName: String?) {
    if (!showName.isNullOrEmpty()) {
        view.text = view.resources.getString(
            R.string.trakt_watched_progress_sheet_title_with_show_name,
            showName
        )
    } else {
        view.text = view.resources.getString(R.string.trakt_watched_progress_sheet_title)
    }
}