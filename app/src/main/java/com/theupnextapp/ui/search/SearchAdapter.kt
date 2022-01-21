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
import com.theupnextapp.ui.widgets.SearchListItem
import com.theupnextapp.ui.dashboard.DashboardViewHolder

class SearchAdapter :
    ComposeAdapter<ShowSearch, SearchAdapter.ComposeViewHolder>() {

    override var list: List<ShowSearch> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComposeViewHolder {
        return ComposeViewHolder(ComposeView(parent.context))
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

    class ComposeViewHolder(composeView: ComposeView) :
        DashboardViewHolder<ShowSearch>(composeView) {

        override val source: String = "search"

        @OptIn(ExperimentalMaterialApi::class)
        @Composable
        override fun ComposableContainer(item: ShowSearch) {
            MdcTheme {
                SearchListItem(item = item) {
                    navigateFromSearchToDetail(item, composeView)
                }
            }
        }
    }
}