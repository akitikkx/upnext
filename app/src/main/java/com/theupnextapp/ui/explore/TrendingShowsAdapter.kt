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

package com.theupnextapp.ui.explore

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.TrendingShowListItemBinding
import com.theupnextapp.domain.TraktTrendingShows

class TrendingShowsAdapter(val listener: TrendingShowsAdapterListener) :
    RecyclerView.Adapter<TrendingShowsAdapter.ViewHolder>() {

    private var trendingList: List<TraktTrendingShows> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val withDataBinding: TrendingShowListItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            ViewHolder.LAYOUT,
            parent,
            false
        )
        return ViewHolder(withDataBinding)
    }

    override fun getItemCount(): Int = trendingList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewDataBinding.also {
            it.listener = listener

            it.show = trendingList[position]
        }
    }

    interface TrendingShowsAdapterListener {
        fun onTrendingShowClick(view: View, traktTrending: TraktTrendingShows)
    }

    fun submitList(trendingShowsList: List<TraktTrendingShows>) {
        val oldTrendingShowsList = trendingList
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            TrendingShowItemDiffCallback(
                oldTrendingShowsList,
                trendingShowsList
            )
        )
        trendingList = trendingShowsList
        diffResult.dispatchUpdatesTo(this)
    }

    class TrendingShowItemDiffCallback(
        private val oldTrendingShowsList: List<TraktTrendingShows>,
        private val newTrendingShowsList: List<TraktTrendingShows>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldTrendingShowsList[oldItemPosition].imdbID == newTrendingShowsList[newItemPosition].imdbID
        }

        override fun getOldListSize(): Int = oldTrendingShowsList.size

        override fun getNewListSize(): Int = newTrendingShowsList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldTrendingShowsList[oldItemPosition].equals(newTrendingShowsList[newItemPosition])
        }

    }

    class ViewHolder(val viewDataBinding: TrendingShowListItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.trending_show_list_item
        }
    }
}