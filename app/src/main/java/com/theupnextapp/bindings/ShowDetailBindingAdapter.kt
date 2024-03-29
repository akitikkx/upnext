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
import com.theupnextapp.common.utils.DateUtils
import com.theupnextapp.domain.ShowNextEpisode
import com.theupnextapp.domain.ShowPreviousEpisode
import java.text.SimpleDateFormat
import java.util.Locale

@BindingAdapter("showHideNextEpisodeInfo")
fun showHideNextEpisodeInfo(view: TextView, showInfo: ShowNextEpisode?) {
    view.visibility =
        if (showInfo?.nextEpisodeSummary.isNullOrEmpty() && showInfo?.nextEpisodeAirstamp.isNullOrEmpty()) View.GONE else View.VISIBLE
}

@BindingAdapter("showHidePreviousEpisodeInfo")
fun showHidePreviousEpisodeInfo(view: TextView, showInfo: ShowPreviousEpisode?) {
    view.visibility =
        if (showInfo?.previousEpisodeSummary.isNullOrEmpty() && showInfo?.previousEpisodeAirstamp.isNullOrEmpty()) View.GONE else View.VISIBLE
}

@BindingAdapter("nextEpisodeInfo")
fun nextEpisodeInfo(view: TextView, showInfo: ShowNextEpisode?) {
    if (!showInfo?.nextEpisodeSummary.isNullOrEmpty()) {
        view.text = view.resources.getString(
            R.string.show_detail_episode_season_info,
            showInfo?.nextEpisodeSeason,
            showInfo?.nextEpisodeNumber,
            showInfo?.nextEpisodeName
        )
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("previousEpisodeInfo")
fun previousEpisodeInfo(view: TextView, showInfo: ShowPreviousEpisode?) {
    if (!showInfo?.previousEpisodeSummary.isNullOrEmpty()) {
        view.text = view.resources.getString(
            R.string.show_detail_episode_season_info,
            showInfo?.previousEpisodeSeason,
            showInfo?.previousEpisodeNumber,
            showInfo?.previousEpisodeName
        )
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("nextAirDate")
fun getNextAirDate(view: TextView, showInfo: ShowNextEpisode?) {
    if (!showInfo?.nextEpisodeAirstamp.isNullOrEmpty()) {
        val format =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+00:00'", Locale.getDefault())

        val date = showInfo?.nextEpisodeAirstamp?.let { format.parse(it) }
        if (date != null) {
            val difference =
                DateUtils.getTimeDifferenceForDisplay(endTime = date.time)
            view.text = view.resources.getString(
                R.string.show_detail_next_episode_airdate,
                difference?.difference?.unaryMinus(),
                difference?.type
            )
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("nextAirDate")
fun getNextAirDate(view: TextView, airStamp: String?) {
    if (!airStamp.isNullOrEmpty()) {
        val format =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+00:00'", Locale.getDefault())

        val date = airStamp.let { format.parse(it) }
        if (date != null) {
            val difference =
                DateUtils.getTimeDifferenceForDisplay(endTime = date.time)
            view.text = view.resources.getString(
                R.string.show_detail_next_episode_airdate,
                difference?.difference?.unaryMinus(),
                difference?.type
            )
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("previousAirDate")
fun getPreviousAirDate(view: TextView, showInfo: ShowPreviousEpisode?) {
    if (!showInfo?.previousEpisodeAirdate.isNullOrEmpty()) {
        val format =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+00:00'", Locale.getDefault())

        val date = showInfo?.previousEpisodeAirstamp?.let { format.parse(it) }
        if (date != null) {
            val difference =
                DateUtils.getTimeDifferenceForDisplay(endTime = date.time)
            view.text = view.resources.getString(
                R.string.show_detail_previous_episode_airdate,
                difference?.difference,
                difference?.type
            )
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("airDate")
fun airDate(view: TextView, airstamp: String?) {
    val date = airstamp?.let { DateUtils.getDisplayDateFromDateStamp(it) }
    view.text = view.resources.getString(R.string.show_detail_air_date_general, date)
}
