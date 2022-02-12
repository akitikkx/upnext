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
import com.theupnextapp.databinding.MostAnticipatedShowListItemBinding
import com.theupnextapp.domain.TraktMostAnticipated

class MostAnticipatedShowsAdapter(val listener: MostAnticipatedShowsAdapterListener) :
    RecyclerView.Adapter<MostAnticipatedShowsAdapter.ViewHolder>() {

    private var mostAnticipatedShowsList: List<TraktMostAnticipated> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val withDataBinding: MostAnticipatedShowListItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            ViewHolder.LAYOUT,
            parent,
            false
        )
        return ViewHolder(withDataBinding)
    }

    override fun getItemCount(): Int = mostAnticipatedShowsList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewDataBinding.also {
            it.listener = listener

            it.show = mostAnticipatedShowsList[position]
        }
    }

    interface MostAnticipatedShowsAdapterListener {
        fun onMostAnticipatedShowClick(view: View, mostAnticipated: TraktMostAnticipated)
    }

    fun submitList(anticipatedShowsList: List<TraktMostAnticipated>){
        val oldAnticipatedShowsList = mostAnticipatedShowsList
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            MostAnticipatedItemDiffCallback(
                oldAnticipatedShowsList,
                anticipatedShowsList
            )
        )
        mostAnticipatedShowsList = anticipatedShowsList
        diffResult.dispatchUpdatesTo(this)
    }

    class MostAnticipatedItemDiffCallback(
        private val oldAnticipatedShowsList: List<TraktMostAnticipated>,
        private val newAnticipatedShowsList: List<TraktMostAnticipated>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldAnticipatedShowsList[oldItemPosition].imdbID == newAnticipatedShowsList[newItemPosition].imdbID
        }

        override fun getOldListSize(): Int = oldAnticipatedShowsList.size

        override fun getNewListSize(): Int = newAnticipatedShowsList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldAnticipatedShowsList[oldItemPosition].equals(newAnticipatedShowsList[newItemPosition])
        }

    }

    class ViewHolder(val viewDataBinding: MostAnticipatedShowListItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.most_anticipated_show_list_item
        }
    }
}