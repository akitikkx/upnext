package com.theupnextapp.ui.explore

import android.view.ViewGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.DiffUtil
import com.google.android.material.composethemeadapter.MdcTheme
import com.theupnextapp.domain.TraktTrendingShows
import com.theupnextapp.domain.TrendingShowItemDiffCallback
import com.theupnextapp.ui.components.ListPosterCard
import com.theupnextapp.ui.dashboard.DashboardAdapter
import com.theupnextapp.ui.dashboard.DashboardViewHolder

class TrendingShowsAdapter : DashboardAdapter<TraktTrendingShows, TrendingShowsAdapter.ComposeViewHolder>(){

    override var list: List<TraktTrendingShows> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComposeViewHolder {
        return ComposeViewHolder(ComposeView(parent.context))
    }

    override fun submitList(updateList: List<TraktTrendingShows>) {
        val oldItems = list
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            TrendingShowItemDiffCallback(
                oldItems,
                updateList
            )
        )
        list = updateList
        diffResult.dispatchUpdatesTo(this)
    }

    class ComposeViewHolder(composeView: ComposeView) :
        DashboardViewHolder<TraktTrendingShows>(composeView) {

        override val source: String = "trending"

        @OptIn(ExperimentalMaterialApi::class)
        @Composable
        override fun ComposableContainer(item: TraktTrendingShows) {
            MdcTheme {
                ListPosterCard(
                    itemName = item.title,
                    itemUrl = item.originalImageUrl
                ) {
                    navigateFromTrendingToShowDetail(item, composeView)
                }
            }
        }
    }
}