package com.theupnextapp.ui.explore

import android.view.ViewGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.DiffUtil
import com.google.android.material.composethemeadapter.MdcTheme
import com.theupnextapp.domain.TraktTrendingShows
import com.theupnextapp.domain.TrendingShowItemDiffCallback
import com.theupnextapp.ui.widgets.ListPosterCard
import com.theupnextapp.ui.common.ComposeAdapter
import com.theupnextapp.ui.common.ComposeViewHolder

class TrendingShowsAdapter : ComposeAdapter<TraktTrendingShows, TrendingShowsAdapter.ViewHolder>(){

    override var list: List<TraktTrendingShows> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ComposeView(parent.context))
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

    class ViewHolder(composeView: ComposeView) :
        ComposeViewHolder<TraktTrendingShows>(composeView) {

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