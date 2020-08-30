package com.theupnextapp.ui.traktRecommendations

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.TraktRecommendationsListItemBinding
import com.theupnextapp.domain.TraktRecommendations

class TraktRecommendationsAdapter(val listener: TraktRecommendationsAdapterListener) :
    RecyclerView.Adapter<TraktRecommendationsAdapter.ViewHolder>() {

    private var recommendedShows: List<TraktRecommendations> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val withDataBinding: TraktRecommendationsListItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            ViewHolder.LAYOUT,
            parent,
            false
        )
        return ViewHolder(
            withDataBinding
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewDataBinding.also {
            it.show = recommendedShows[position]
            it.listener = listener
        }
    }

    override fun getItemCount(): Int = recommendedShows.size

    interface TraktRecommendationsAdapterListener {
        fun onRecommendedShowClick(view: View, traktRecommendations: TraktRecommendations)
    }

    fun submitList(recommendationsList: List<TraktRecommendations>) {
        val oldRecommendationsList = recommendedShows
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            TraktRecommendationsItemCallback(
                oldRecommendationsList,
                recommendationsList
            )
        )
        recommendedShows = recommendationsList
        diffResult.dispatchUpdatesTo(this)
    }

    class TraktRecommendationsItemCallback(
        private val oldRecommendationsList: List<TraktRecommendations>,
        private val newRecommendationsList: List<TraktRecommendations>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldRecommendationsList[oldItemPosition].id == newRecommendationsList[newItemPosition].id
        }

        override fun getOldListSize(): Int = oldRecommendationsList.size

        override fun getNewListSize(): Int = newRecommendationsList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldRecommendationsList[oldItemPosition].equals(newRecommendationsList[newItemPosition])
        }

    }

    class ViewHolder(val viewDataBinding: TraktRecommendationsListItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.trakt_recommendations_list_item
        }
    }
}