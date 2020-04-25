package com.theupnextapp.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.R
import com.theupnextapp.databinding.RecommendedShowListItemBinding
import com.theupnextapp.domain.RecommendedShows

class RecommendedShowsAdapter(val listener: RecommendedShowsAdapterListener) :
    RecyclerView.Adapter<RecommendedShowsAdapter.ViewHolder>() {

    var recommendedShows: List<RecommendedShows> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val withDataBinding: RecommendedShowListItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            ViewHolder.LAYOUT,
            parent,
            false
        )
        return ViewHolder(withDataBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewDataBinding.also {
            it.recommendedShowPoster.transitionName = "recommended_${recommendedShows[position].originalImageUrl}"
            it.show = recommendedShows[position]
            it.listener = listener
        }
    }

    override fun getItemCount(): Int = recommendedShows.size

    interface RecommendedShowsAdapterListener {
        fun onRecommendedShowClick(view : View, recommendedShow: RecommendedShows)
    }

    class ViewHolder(val viewDataBinding: RecommendedShowListItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.recommended_show_list_item
        }
    }
}