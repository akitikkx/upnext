package com.theupnextapp.ui.showSeasonEpisodes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.ShowSeasonEpisodeItemBinding
import com.theupnextapp.domain.ShowSeasonEpisode

class ShowSeasonEpisodesAdapter(val listener: ShowSeasonEpisodesAdapterListener) : RecyclerView.Adapter<ShowSeasonEpisodesAdapter.ViewHolder>() {

    private var showSeasonEpisodes: List<ShowSeasonEpisode> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val withDataBinding = ShowSeasonEpisodeItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(withDataBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.also {
            val showSeasonEpisode = showSeasonEpisodes[position]

            it.episode = showSeasonEpisode
            it.listener = listener
        }
    }

    override fun getItemCount() = showSeasonEpisodes.size

    fun submitSeasonEpisodesList(episodesList: List<ShowSeasonEpisode>) {
        val oldEpisodesList = showSeasonEpisodes

        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            ShowSeasonEpisodeItemDiffCallback(
                oldEpisodesList,
                episodesList
            )
        )
        showSeasonEpisodes = episodesList
        diffResult.dispatchUpdatesTo(this)
    }

    interface ShowSeasonEpisodesAdapterListener {
        fun onCheckInClick(showSeasonEpisode: ShowSeasonEpisode)
    }

    class ShowSeasonEpisodeItemDiffCallback(
        private val oldSeasonEpisodesList: List<ShowSeasonEpisode>,
        private val newSeasonEpisodesList: List<ShowSeasonEpisode>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldSeasonEpisodesList.size

        override fun getNewListSize() = newSeasonEpisodesList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldSeasonEpisodesList[oldItemPosition].id == newSeasonEpisodesList[newItemPosition].id

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldSeasonEpisodesList[oldItemPosition] == newSeasonEpisodesList[newItemPosition]
    }

    class ViewHolder(val binding: ShowSeasonEpisodeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.show_season_episode_item
        }
    }
}