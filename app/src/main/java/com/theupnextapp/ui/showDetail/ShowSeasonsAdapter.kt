package com.theupnextapp.ui.showDetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.ShowSeasonItemBinding
import com.theupnextapp.domain.ShowSeason
import com.theupnextapp.domain.TraktWatchedShowProgressSeason

class ShowSeasonsAdapter(val listener: ShowSeasonsAdapterListener) : RecyclerView.Adapter<ShowSeasonsAdapter.ViewHolder>() {

    var showSeasons: List<ShowSeason> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var watchedProgress: List<TraktWatchedShowProgressSeason>? = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val withDataBinding: com.theupnextapp.databinding.ShowSeasonItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            ViewHolder.LAYOUT,
            parent,
            false
        )
        return ViewHolder(withDataBinding)
    }

    override fun getItemCount(): Int = showSeasons.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewDataBinding.also {
            val showSeason = showSeasons[position]
            var seasonWatched: Boolean = false

            it.listener = listener

            it.showSeason = showSeason

            if (watchedProgress.isNullOrEmpty()) {
                it.seasonAddToggle.visibility = View.GONE
                it.seasonRemoveToggle.visibility = View.GONE
            } else {
                watchedProgress?.forEach { season ->
                    if (season.number == showSeason.seasonNumber && season.aired == season.completed) {
                        seasonWatched = true
                    }
                }

                if (seasonWatched) {
                    it.seasonRemoveToggle.visibility = View.VISIBLE
                    it.seasonAddToggle.visibility = View.GONE
                } else {
                    it.seasonRemoveToggle.visibility = View.GONE
                    it.seasonAddToggle.visibility = View.VISIBLE
                }
            }
        }
    }

    interface ShowSeasonsAdapterListener {

        fun onShowSeasonAddClick(view: View, showSeason: ShowSeason)

        fun onShowSeasonRemoveClick(view: View, showSeason: ShowSeason)
    }

    class ViewHolder(val viewDataBinding: ShowSeasonItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {

        companion object {
            @LayoutRes
            val LAYOUT = R.layout.show_season_item
        }

    }
}