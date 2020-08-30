package com.theupnextapp.ui.collectionSeasonEpisodes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.TraktCollectionSeasonEpisodeItemBinding
import com.theupnextapp.domain.TraktCollectionArg
import com.theupnextapp.domain.TraktCollectionSeason
import com.theupnextapp.domain.TraktCollectionSeasonEpisode

class CollectionSeasonEpisodesAdapter(val listener: CollectionSeasonEpisodesAdapterListener) :
    RecyclerView.Adapter<CollectionSeasonEpisodesAdapter.ViewHolder>() {

    private var episodesList: List<TraktCollectionSeasonEpisode> = ArrayList()

    var traktCollection: TraktCollectionArg? = null

    var traktCollectionSeason: TraktCollectionSeason? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val withDataBinding: TraktCollectionSeasonEpisodeItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            ViewHolder.LAYOUT,
            parent,
            false
        )
        return ViewHolder(withDataBinding)
    }

    override fun getItemCount(): Int = episodesList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewDataBinding.also {
            it.showSeasonEpisode = episodesList[position]

            it.showSeason = traktCollectionSeason

            it.listener = listener
        }
    }

    interface CollectionSeasonEpisodesAdapterListener {

        fun onCollectionSeasonEpisodeRemoveClick(
            view: View,
            traktCollectionSeasonEpisode: TraktCollectionSeasonEpisode
        )
    }

    fun submitList(collectionSeasonEpisodesList: List<TraktCollectionSeasonEpisode>) {
        val oldCollectionSeasonEpisodesList = episodesList
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            CollectionSeasonEpisodeItemCallback(
                oldCollectionSeasonEpisodesList,
                collectionSeasonEpisodesList
            )
        )
        episodesList = collectionSeasonEpisodesList
        diffResult.dispatchUpdatesTo(this)
    }

    class CollectionSeasonEpisodeItemCallback(
        private val oldCollectionSeasonEpisodesList: List<TraktCollectionSeasonEpisode>,
        private val newCollectionSeasonEpisodesList: List<TraktCollectionSeasonEpisode>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldCollectionSeasonEpisodesList[oldItemPosition].id == newCollectionSeasonEpisodesList[newItemPosition].id
        }

        override fun getOldListSize(): Int = oldCollectionSeasonEpisodesList.size

        override fun getNewListSize(): Int = newCollectionSeasonEpisodesList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldCollectionSeasonEpisodesList[oldItemPosition].equals(
                newCollectionSeasonEpisodesList[newItemPosition]
            )
        }

    }

    class ViewHolder(val viewDataBinding: TraktCollectionSeasonEpisodeItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.trakt_collection_season_episode_item
        }
    }
}