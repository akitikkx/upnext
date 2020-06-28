package com.theupnextapp.ui.collectionSeasonEpisodes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.TraktCollectionSeasonEpisodeItemBinding
import com.theupnextapp.domain.TraktCollectionArg
import com.theupnextapp.domain.TraktCollectionSeason
import com.theupnextapp.domain.TraktCollectionSeasonEpisode

class CollectionSeasonEpisodesAdapter(val listener: CollectionSeasonEpisodesAdapterListener) :
    RecyclerView.Adapter<CollectionSeasonEpisodesAdapter.ViewHolder>() {

    var episodesList: List<TraktCollectionSeasonEpisode> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

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

        fun onCollectionSeasonEpisodeRemoveClick(view: View, traktCollectionSeasonEpisode: TraktCollectionSeasonEpisode)
    }

    class ViewHolder(val viewDataBinding: TraktCollectionSeasonEpisodeItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.trakt_collection_season_episode_item
        }
    }
}