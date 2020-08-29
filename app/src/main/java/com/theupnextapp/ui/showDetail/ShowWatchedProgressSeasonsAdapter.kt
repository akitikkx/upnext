package com.theupnextapp.ui.showDetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.TraktWatchedProgressSeasonListItemBinding
import com.theupnextapp.domain.TraktWatchedShowProgressSeason

class ShowWatchedProgressSeasonsAdapter :
    RecyclerView.Adapter<ShowWatchedProgressSeasonsAdapter.ViewHolder>() {

    private var seasons: List<TraktWatchedShowProgressSeason> = ArrayList()

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
        return if (seasons.isNullOrEmpty()) 0 else seasons.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewDataBinding.also {
            it.season = seasons[position]
        }
    }

    fun submitWatchedProgressSeasonsList(watchedProgressSeasonsList: List<TraktWatchedShowProgressSeason>) {
        val oldWatchedProgressSeasonsList = seasons
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            WatchedProgressSeasonItemCallback(
                oldWatchedProgressSeasonsList,
                watchedProgressSeasonsList
            )
        )
        seasons = watchedProgressSeasonsList
        diffResult.dispatchUpdatesTo(this)
    }

    class WatchedProgressSeasonItemCallback(
        private val oldWatchedProgressSeasonsList: List<TraktWatchedShowProgressSeason>,
        private val newWatchedProgressSeasonsList: List<TraktWatchedShowProgressSeason>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldWatchedProgressSeasonsList[oldItemPosition].number == newWatchedProgressSeasonsList[newItemPosition].number
        }

        override fun getOldListSize(): Int = oldWatchedProgressSeasonsList.size

        override fun getNewListSize(): Int = newWatchedProgressSeasonsList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldWatchedProgressSeasonsList[oldItemPosition].equals(
                newWatchedProgressSeasonsList?.get(newItemPosition)
            )
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