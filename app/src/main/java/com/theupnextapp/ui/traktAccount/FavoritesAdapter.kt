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
import com.theupnextapp.domain.TraktUserListItem

class FavoritesAdapter(val listener: FavoritesAdapterListener) :
    RecyclerView.Adapter<FavoritesAdapter.ViewHolder>() {

    private var favoriteShows: List<TraktUserListItem> = ArrayList()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.also { binding ->
            binding.show = favoriteShows[position]
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