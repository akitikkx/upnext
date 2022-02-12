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

package com.theupnextapp.ui.showSeasonEpisodes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.ShowSeasonEpisodeItemBinding
import com.theupnextapp.domain.ShowSeasonEpisode

class ShowSeasonEpisodesAdapter(
    val listener: ShowSeasonEpisodesAdapterListener,
    val isAuthorizedOnTrakt: Boolean = false,
    val imdbID: String? = null
) : RecyclerView.Adapter<ShowSeasonEpisodesAdapter.ViewHolder>() {

    private var showSeasonEpisodes: List<ShowSeasonEpisode> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val withDataBinding = ShowSeasonEpisodeItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(withDataBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.also {
            val showSeasonEpisode = showSeasonEpisodes[position]

            it.episode = showSeasonEpisode
            it.listener = listener
            it.isAuthorizedOnTrakt = isAuthorizedOnTrakt
            it.imdbID = imdbID
        }
    }

    override fun getItemCount() = showSeasonEpisodes.size

    fun submitSeasonEpisodesList(episodesList: List<ShowSeasonEpisode>) {
        val oldEpisodesList = showSeasonEpisodes

        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            ShowSeasonEpisodeItemDiffCallback(
                oldEpisodesList,
                episodesList
            )
        )
        showSeasonEpisodes = episodesList
        diffResult.dispatchUpdatesTo(this)
    }

    interface ShowSeasonEpisodesAdapterListener {
        fun onCheckInClick(showSeasonEpisode: ShowSeasonEpisode, imdbID: String?)
    }

    class ShowSeasonEpisodeItemDiffCallback(
        private val oldSeasonEpisodesList: List<ShowSeasonEpisode>,
        private val newSeasonEpisodesList: List<ShowSeasonEpisode>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldSeasonEpisodesList.size

        override fun getNewListSize() = newSeasonEpisodesList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldSeasonEpisodesList[oldItemPosition].id == newSeasonEpisodesList[newItemPosition].id

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldSeasonEpisodesList[oldItemPosition] == newSeasonEpisodesList[newItemPosition]
    }

    class ViewHolder(val binding: ShowSeasonEpisodeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.show_season_episode_item
        }
    }
}