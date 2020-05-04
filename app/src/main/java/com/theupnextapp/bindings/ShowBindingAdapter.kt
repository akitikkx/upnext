package com.theupnextapp.bindings

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.crashlytics.android.Crashlytics
import com.theupnextapp.R
import com.theupnextapp.domain.*
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.*

@BindingAdapter("imageUrl")
fun setImageUrl(imageView: ImageView, url: String?) {
    Glide.with(imageView.context)
        .load(url)
        .placeholder(R.color.showBackdropBackground)
        .error(R.color.showBackdropBackground)
        .into(imageView)
}

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

@BindingAdapter("showListedAt")
fun showListedAt(view: TextView, watchlist: TraktWatchlist) {
    if (!watchlist.listed_at.isNullOrEmpty()) {
        try {
            val format =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.000Z'", Locale.getDefault())
            val formattedDate: Date = format.parse(watchlist.listed_at)

            view.text = view.resources.getString(
                R.string.watchlist_item_listed_at,
                formattedDate
            )
        } catch (e: Exception) {
            Crashlytics.logException(e)
        }
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("historyEpisodeDetails")
fun historyEpisodeDetails(view: TextView, show: TraktHistory) {
    if (show.episodeNumber != null && show.episodeSeasonNumber != null) {
        view.text = view.resources.getString(
            R.string.trakt_history_episode_details,
            show.episodeSeasonNumber,
            show.episodeNumber
        )
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("showDetailTransitionName")
fun getTransitionName(view: ImageView, showDetailArg: ShowDetailArg) {
    if (!showDetailArg.source.isNullOrEmpty() && !showDetailArg.showImageUrl.isNullOrEmpty()) {
        view.transitionName = "${showDetailArg.source}_${showDetailArg.showImageUrl}"
    }
}