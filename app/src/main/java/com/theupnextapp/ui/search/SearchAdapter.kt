package com.theupnextapp.ui.search

import android.view.ViewGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.DiffUtil
import com.google.android.material.composethemeadapter.MdcTheme
import com.theupnextapp.domain.SearchItemDiffCallback
import com.theupnextapp.domain.ShowSearch
import com.theupnextapp.ui.common.ComposeAdapter
import com.theupnextapp.ui.common.ComposeViewHolder
import com.theupnextapp.ui.widgets.SearchListCard

class SearchAdapter :
    ComposeAdapter<ShowSearch, SearchAdapter.ViewHolder>() {

    override var list: List<ShowSearch> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ComposeView(parent.context))
    }

    override fun submitList(updateList: List<ShowSearch>) {
        val oldSearchResultsList = list
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            SearchItemDiffCallback(
                oldSearchResultsList,
                updateList
            )
        )
        list = updateList
        diffResult.dispatchUpdatesTo(this)
    }

    class ViewHolder(composeView: ComposeView) :
        ComposeViewHolder<ShowSearch>(composeView) {

        override val source: String = "search"

        @OptIn(ExperimentalMaterialApi::class)
        @Composable
        override fun ComposableContainer(item: ShowSearch) {
            MdcTheme {
                SearchListCard(item = item) {
                    navigateFromSearchToDetail(item, composeView)
                }
            }
        }
    }
}