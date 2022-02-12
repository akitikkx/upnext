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

package com.theupnextapp.ui.showDetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.databinding.TraktShowRatingItemBinding
import com.theupnextapp.network.models.trakt.Distribution

class ShowRatingsAdapter : RecyclerView.Adapter<ShowRatingsAdapter.ViewHolder>() {

    private var distribution: List<Distribution> = ArrayList()

    private var votes: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val withDataBinding = TraktShowRatingItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(withDataBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        votes?.let { holder.bind(distribution[position], it) }
    }

    override fun getItemCount(): Int = distribution.size

    fun submitList(ratingsList: List<Distribution>) {
        distribution = ratingsList
        notifyDataSetChanged()
    }

    fun setVotes(votes: Int?) {
        this.votes = votes
    }

    inner class ViewHolder(val viewDataBinding: TraktShowRatingItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {

        fun bind(item: Distribution, votes: Int) {
            viewDataBinding.apply {
                val progressValue = (item.value.toFloat() / votes.toFloat()) * 100.0f
                ratingLevel.text = item.score
                ratingProgressIndicator.progress = progressValue.toInt()
            }
        }
    }
}