package com.theupnextapp.ui.dashboard

import android.view.ViewGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.DiffUtil
import com.google.android.material.composethemeadapter.MdcTheme
import com.theupnextapp.domain.ScheduleShow
import com.theupnextapp.domain.ScheduleShowItemDiffCallback
import com.theupnextapp.ui.components.ListPosterCard

class TodayShowsAdapter : DashboardAdapter<ScheduleShow, TodayShowsAdapter.ComposeViewHolder>() {

    override var list: List<ScheduleShow> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComposeViewHolder {
        return ComposeViewHolder(ComposeView(parent.context))
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

    class ComposeViewHolder(composeView: ComposeView) :
        DashboardViewHolder<ScheduleShow>(composeView) {

        override val source: String = "today"

        @OptIn(ExperimentalMaterialApi::class)
        @Composable
        override fun ComposableContainer(item: ScheduleShow) {
            MdcTheme {
                ListPosterCard(item) {
                    navigateToShow(item, composeView)
                }
            }
        }
    }
}