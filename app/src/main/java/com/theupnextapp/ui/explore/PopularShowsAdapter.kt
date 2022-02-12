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
import com.theupnextapp.databinding.PopularShowListItemBinding
import com.theupnextapp.domain.TraktPopularShows

class PopularShowsAdapter(val listener: PopularShowsAdapterListener) :
    RecyclerView.Adapter<PopularShowsAdapter.ViewHolder>() {

    private var popularList: List<TraktPopularShows> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val withDataBinding: PopularShowListItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            ViewHolder.LAYOUT,
            parent,
            false
        )
        return ViewHolder(withDataBinding)
    }

    override fun getItemCount(): Int = popularList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewDataBinding.also {
            it.listener = listener

            it.show = popularList[position]
        }
    }

    interface PopularShowsAdapterListener {
        fun onPopularShowClick(view: View, popularShows: TraktPopularShows)
    }

    fun submitList(popularShowList: List<TraktPopularShows>){
        val oldList = popularList
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            PopularShowItemDiffCallback(
                oldList,
                popularShowList
            )
        )
        popularList = popularShowList
        diffResult.dispatchUpdatesTo(this)
    }

    class PopularShowItemDiffCallback(
        private val oldPopularShowsList: List<TraktPopularShows>,
        private val newPopularShowsList: List<TraktPopularShows>
    ): DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldPopularShowsList[oldItemPosition].imdbID == newPopularShowsList[newItemPosition].imdbID
        }

        override fun getOldListSize(): Int = oldPopularShowsList.size

        override fun getNewListSize(): Int  = newPopularShowsList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldPopularShowsList[oldItemPosition].equals(newPopularShowsList[newItemPosition])
        }

    }

    class ViewHolder(val viewDataBinding: PopularShowListItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.popular_show_list_item
        }
    }
}