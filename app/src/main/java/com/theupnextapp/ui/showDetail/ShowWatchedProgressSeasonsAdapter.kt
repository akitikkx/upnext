package com.theupnextapp.ui.showDetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.TraktWatchedProgressSeasonListItemBinding
import com.theupnextapp.domain.TraktWatchedShowProgressSeason

class ShowWatchedProgressSeasonsAdapter :
    RecyclerView.Adapter<ShowWatchedProgressSeasonsAdapter.ViewHolder>() {

    var seasons: List<TraktWatchedShowProgressSeason>? = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val withDataBinding: TraktWatchedProgressSeasonListItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            ViewHolder.LAYOUT,
            parent,
            false
        )
        return ViewHolder(withDataBinding)
    }

    override fun getItemCount(): Int {
        return if (seasons.isNullOrEmpty()) 0 else seasons!!.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewDataBinding.also {
            it.season = seasons?.get(position)
        }
    }

    class ViewHolder(val viewDataBinding: TraktWatchedProgressSeasonListItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {

        companion object {
            @LayoutRes
            val LAYOUT = R.layout.trakt_watched_progress_season_list_item
        }
    }
}