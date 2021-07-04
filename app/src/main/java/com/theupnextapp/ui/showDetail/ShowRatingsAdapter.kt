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