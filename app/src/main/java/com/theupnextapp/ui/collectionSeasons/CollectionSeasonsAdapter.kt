package com.theupnextapp.ui.collectionSeasons

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.TraktCollectionSeasonItemBinding
import com.theupnextapp.domain.TraktCollectionArg
import com.theupnextapp.domain.TraktCollectionSeason

class CollectionSeasonsAdapter(val listener: CollectionSeasonsAdapterListener) :
    RecyclerView.Adapter<CollectionSeasonsAdapter.ViewHolder>() {

    var traktCollectionSeasons: List<TraktCollectionSeason> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var traktCollection: TraktCollectionArg? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val withDataBinding: TraktCollectionSeasonItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            ViewHolder.LAYOUT,
            parent,
            false
        )
        return ViewHolder(withDataBinding)
    }

    override fun getItemCount(): Int = traktCollectionSeasons.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewDataBinding.also {
            it.showSeason = traktCollectionSeasons[position]

            it.show = traktCollection

            it.listener = listener
        }
    }

    interface CollectionSeasonsAdapterListener {

        fun onCollectionSeasonClick(view: View, traktCollectionSeason: TraktCollectionSeason)

        fun onCollectionSeasonRemoveClick(view: View, traktCollectionSeason: TraktCollectionSeason)
    }

    class ViewHolder(val viewDataBinding: TraktCollectionSeasonItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.trakt_collection_season_item
        }
    }
}