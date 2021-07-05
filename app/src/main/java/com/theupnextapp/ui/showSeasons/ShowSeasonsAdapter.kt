package com.theupnextapp.ui.showSeasons

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.ShowSeasonItemBinding
import com.theupnextapp.domain.ShowSeason

class ShowSeasonsAdapter :
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
            return oldShowSeasonsList[oldItemPosition].equals(newShowSeasonsList[newItemPosition])
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