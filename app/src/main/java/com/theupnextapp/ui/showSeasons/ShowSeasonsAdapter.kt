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

package com.theupnextapp.ui.showSeasons

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.ShowSeasonItemBinding
import com.theupnextapp.domain.ShowSeason

class ShowSeasonsAdapter(
    val listener: ShowSeasonsAdapterListener,
    val showId: Int?
) :
    RecyclerView.Adapter<ShowSeasonsAdapter.ViewHolder>() {

    private var showSeasons: List<ShowSeason> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val withDataBinding: ShowSeasonItemBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                ViewHolder.LAYOUT,
                parent,
                false
            )
        return ViewHolder(withDataBinding)
    }

    override fun getItemCount(): Int = showSeasons.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewDataBinding.also {
            val showSeason = showSeasons[position]
            it.showSeason = showSeason
            it.showId = showId
            it.listener = listener
        }
    }

    fun submitShowSeasonsList(showSeasonsList: List<ShowSeason>) {
        val oldShowSeasonsList = showSeasons
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            ShowSeasonItemDiffCallback(
                oldShowSeasonsList,
                showSeasonsList
            )
        )
        showSeasons = showSeasonsList
        diffResult.dispatchUpdatesTo(this)
    }

    interface ShowSeasonsAdapterListener {
        fun onSeasonClick(
            view: View,
            showId: Int,
            seasonNumber: Int
        )
    }

    class ShowSeasonItemDiffCallback(
        private val oldShowSeasonsList: List<ShowSeason>,
        private val newShowSeasonsList: List<ShowSeason>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldShowSeasonsList[oldItemPosition].id == newShowSeasonsList[newItemPosition].id
        }

        override fun getOldListSize(): Int = oldShowSeasonsList.size

        override fun getNewListSize(): Int = newShowSeasonsList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldShowSeasonsList[oldItemPosition] == newShowSeasonsList[newItemPosition]
        }
    }

    class ViewHolder(val viewDataBinding: ShowSeasonItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {

        companion object {
            @LayoutRes
            val LAYOUT = R.layout.show_season_item
        }
    }
}