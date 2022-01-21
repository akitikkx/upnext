package com.theupnextapp.ui.explore

import android.view.ViewGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.DiffUtil
import com.google.android.material.composethemeadapter.MdcTheme
import com.theupnextapp.domain.PopularShowItemDiffCallback
import com.theupnextapp.domain.TraktPopularShows
import com.theupnextapp.ui.widgets.ListPosterCard
import com.theupnextapp.ui.common.ComposeAdapter
import com.theupnextapp.ui.common.ComposeViewHolder

class PopularShowsAdapter :
    ComposeAdapter<TraktPopularShows, PopularShowsAdapter.ViewHolder>() {

    override var list: List<TraktPopularShows> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ComposeView(parent.context))
    }

    override fun submitList(updateList: List<TraktPopularShows>){
        val oldList = list
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            PopularShowItemDiffCallback(
                oldList,
                updateList
            )
        )
        list = updateList
        diffResult.dispatchUpdatesTo(this)
    }

    class ViewHolder(composeView: ComposeView) :
        ComposeViewHolder<TraktPopularShows>(composeView) {

        override val source: String = "popular"

        @OptIn(ExperimentalMaterialApi::class)
        @Composable
        override fun ComposableContainer(item: TraktPopularShows) {
            MdcTheme {
                ListPosterCard(
                    itemName = item.title,
                    itemUrl = item.originalImageUrl
                ) {
                    navigateFromPopularToShowDetail(item, composeView)
                }
            }
        }
    }
}