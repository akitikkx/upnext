package com.theupnextapp.ui.traktAccount

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.FavoriteItemBinding
import com.theupnextapp.domain.FavoriteNextEpisode
import com.theupnextapp.domain.TraktUserListItem

class FavoritesAdapter(val listener: FavoritesAdapterListener) :
    RecyclerView.Adapter<FavoritesAdapter.ViewHolder>() {

    private var favoriteShows: List<TraktUserListItem> = ArrayList()

    private var favoriteEpisodes: List<FavoriteNextEpisode> = ArrayList()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.also { binding ->
            val favoriteShow = favoriteShows[position]
            binding.show = favoriteShow
            if (favoriteEpisodes.isNotEmpty()) {
                binding.nextEpisode = favoriteEpisodes.find { it.tvMazeID == favoriteShow.tvMazeID }
            }
            binding.listener = listener
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val withDataBinding: FavoriteItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            ViewHolder.LAYOUT,
            parent,
            false
        )
        return ViewHolder(withDataBinding)
    }

    override fun getItemCount() = favoriteShows.size

    fun submitFavoriteShowsList(list: List<TraktUserListItem>) {
        val oldFavoritesList = favoriteShows

        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            FavoriteItemDiffCallback(
                oldFavoritesList,
                list
            )
        )
        favoriteShows = list
        diffResult.dispatchUpdatesTo(this)
    }

    fun submitFavoriteNextEpisodes(episodesList: List<FavoriteNextEpisode>) {
        this.favoriteEpisodes = episodesList
    }

    class FavoriteItemDiffCallback(
        private val oldFavoritesList: List<TraktUserListItem>,
        private val newFavoritesList: List<TraktUserListItem>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldFavoritesList.size

        override fun getNewListSize() = newFavoritesList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldFavoritesList[oldItemPosition].id == newFavoritesList[newItemPosition].id

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldFavoritesList[oldItemPosition] == newFavoritesList[newItemPosition]
    }

    interface FavoritesAdapterListener {
        fun onFavoriteItemClick(view: View, favoriteShows: TraktUserListItem)
    }


    class ViewHolder(val binding: FavoriteItemBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.favorite_item
        }
    }
}