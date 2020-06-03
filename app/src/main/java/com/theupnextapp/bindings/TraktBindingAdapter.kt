package com.theupnextapp.bindings

import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.R
import com.theupnextapp.domain.TraktHistory
import com.theupnextapp.domain.TraktWatchedShowProgressSeason
import com.theupnextapp.domain.TraktWatchlist
import java.text.SimpleDateFormat
import java.util.*

@BindingAdapter("showListedAt")
fun showListedAt(view: TextView, watchlist: TraktWatchlist) {
    if (!watchlist.listed_at.isNullOrEmpty()) {
        try {
            val format =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.000Z'", Locale.getDefault())
            val formattedDate: Date? = format.parse(watchlist.listed_at)

            view.text = view.resources.getString(
                R.string.watchlist_item_listed_at,
                formattedDate
            )
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
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

@BindingAdapter("watchedSeasonProgressBarLength")
fun watchedSeasonProgressBarLength(view: ProgressBar, season: TraktWatchedShowProgressSeason?) {
    if (season != null) {
        val completedEpisodes = season.aired.toFloat()
        val watchedEpisodes = season.completed.toFloat()
        val completedProgress = (watchedEpisodes * 100.0f) / completedEpisodes

        view.progress = completedProgress.toInt()
    }
}

@BindingAdapter("watchedSeasonProgress")
fun watchedSeasonProgress(view: TextView, season: TraktWatchedShowProgressSeason?) {
    if (season != null) {
        val completedEpisodes = season.aired.toFloat()
        val watchedEpisodes = season.completed.toFloat()
        val completedProgress = (watchedEpisodes * 100.0f) / completedEpisodes

        view.text = view.resources.getString(R.string.trakt_watched_progress_season_percentage, completedProgress.toInt())
    }
}