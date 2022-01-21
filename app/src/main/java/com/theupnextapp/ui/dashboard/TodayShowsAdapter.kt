package com.theupnextapp.ui.dashboard

import android.view.ViewGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.DiffUtil
import com.google.android.material.composethemeadapter.MdcTheme
import com.theupnextapp.domain.ScheduleShow
import com.theupnextapp.domain.ScheduleShowItemDiffCallback
import com.theupnextapp.ui.common.ComposeAdapter
import com.theupnextapp.ui.common.ComposeViewHolder
import com.theupnextapp.ui.widgets.ListPosterCard

class TodayShowsAdapter : ComposeAdapter<ScheduleShow, TodayShowsAdapter.ViewHolder>() {

    override var list: List<ScheduleShow> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ComposeView(parent.context))
    }

    override fun submitList(updateList: List<ScheduleShow>) {
        val oldItems = list
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            ScheduleShowItemDiffCallback(
                oldItems,
                updateList
            )
        )
        list = updateList
        diffResult.dispatchUpdatesTo(this)
    }

    class ViewHolder(composeView: ComposeView) :
        ComposeViewHolder<ScheduleShow>(composeView) {

        override val source: String = "today"

        @OptIn(ExperimentalMaterialApi::class)
        @Composable
        override fun ComposableContainer(item: ScheduleShow) {
            MdcTheme {
                ListPosterCard(
                    itemName = item.name,
                    itemUrl = item.originalImage
                ) {
                    navigateFromDashboardToShowDetail(item, composeView)
                }
            }
        }
    }
}