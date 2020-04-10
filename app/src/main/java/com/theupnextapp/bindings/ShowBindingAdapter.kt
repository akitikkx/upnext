package com.theupnextapp.bindings

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.theupnextapp.R
import com.theupnextapp.domain.ShowInfo
import com.theupnextapp.domain.ShowSearch
import org.jsoup.Jsoup

@BindingAdapter("imageUrl")
fun setImageUrl(imageView: ImageView, url: String?) {
    Glide.with(imageView.context)
        .load(url)
        .placeholder(R.drawable.loading_list_placeholder)
        .error(R.drawable.ic_filter_b_and_w_grey600_48dp)
        .into(imageView)
}

@BindingAdapter("goneIfNotNull")
fun goneIfNotNull(view: View, it: Any?) {
    view.visibility = if (it != null) View.GONE else View.VISIBLE
}

@BindingAdapter("showHideView")
fun showHideView(view: View, show : Boolean) {
    if (show) {
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("showHideStringContent")
fun showHideStringContent(view: View, content: String?) {
    view.visibility = if (!content.isNullOrEmpty()) View.VISIBLE else View.GONE
}

@BindingAdapter("fromHtml")
fun fromHtml(view: TextView, html: String?) {
    if (!html.isNullOrEmpty()) {
        view.text = Jsoup.parse(html).text()
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

@BindingAdapter("nextEpisodeInfo")
fun nextEpisodeInfo(view: TextView, showInfo: ShowInfo?) {
    if (!showInfo?.nextEpisodeSummary.isNullOrEmpty()) {
        view.text = view.resources.getString(
            R.string.show_detail_episode_season_info,
            showInfo?.nextEpisodeSeason,
            showInfo?.nextEpisodeNumber,
            showInfo?.nextEpisodeName
        )
    }
}

@BindingAdapter("previousEpisodeInfo")
fun previousEpisodeInfo(view: TextView, showInfo: ShowInfo?) {
    if (!showInfo?.previousEpisodeSummary.isNullOrEmpty()) {
        view.text = view.resources.getString(
            R.string.show_detail_episode_season_info,
            showInfo?.previousEpisodeSeason,
            showInfo?.previousEpisodeNumber,
            showInfo?.previousEpisodeName
        )
    }
}

@BindingAdapter("showNameAndReleaseYear")
fun showNameAndReleaseYear(view: TextView, showSearch: ShowSearch) {
    if (!showSearch.status.isNullOrEmpty()) {
        if (showSearch.status != "Ended") {
            view.text = view.resources.getString(
                R.string.search_item_not_ended,
                showSearch.name,
                showSearch.premiered?.substring(0,4)
            )
        } else {
            view.text = view.resources.getString(
                R.string.search_item_ended,
                showSearch.name,
                showSearch.premiered?.substring(0,4)
            )
        }
    } else {
        view.text = view.resources.getString(
            R.string.search_item_ended,
            showSearch.name,
            showSearch.premiered?.substring(0,4)
        )
    }
}