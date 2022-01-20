package com.theupnextapp.ui.dashboard

import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.composethemeadapter.MdcTheme
import com.theupnextapp.R
import com.theupnextapp.databinding.TodayShowListItemBinding
import com.theupnextapp.domain.ScheduleShow
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.ui.components.ListPosterCard

class TodayShowsAdapter(val listener: TodayShowsAdapterListener) :
    RecyclerView.Adapter<TodayShowsAdapter.ComposeViewHolder>() {

    var todayShows: List<ScheduleShow> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComposeViewHolder {
        return ComposeViewHolder(ComposeView(parent.context))
    }

    override fun getItemCount(): Int = todayShows.size

    interface TodayShowsAdapterListener {
        fun onTodayShowClick(view: View, scheduleShow: ScheduleShow)
    }

    fun submitList(todayShowsList: List<ScheduleShow>) {
        val oldItems = todayShows
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            TodayShowItemDiffCallback(
                oldItems,
                todayShowsList
            )
        )
        todayShows = todayShowsList
        diffResult.dispatchUpdatesTo(this)
    }

    class TodayShowItemDiffCallback(
        private val oldTodayShowsList: List<ScheduleShow>,
        private val newTodayShowsList: List<ScheduleShow>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldTodayShowsList[oldItemPosition].id == newTodayShowsList[newItemPosition].id
        }

        override fun getOldListSize(): Int = oldTodayShowsList.size

        override fun getNewListSize(): Int = newTodayShowsList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldTodayShowsList[oldItemPosition].equals(newTodayShowsList[newItemPosition])
        }

    }

    class ViewHolder(val viewDataBinding: TodayShowListItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        companion object {
            @LayoutRes
            val LAYOUT = R.layout.today_show_list_item
        }
    }

    override fun onViewRecycled(holder: ComposeViewHolder) {
        holder.composeView.disposeComposition()
    }

    @OptIn(ExperimentalMaterialApi::class)
    override fun onBindViewHolder(holder: ComposeViewHolder, position: Int) {
        val item = todayShows[position]
        holder.bind(item)
    }

    class ComposeViewHolder(val composeView: ComposeView) : RecyclerView.ViewHolder(composeView) {
        init {
            composeView.setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
        }

        @ExperimentalMaterialApi
        fun bind(item: ScheduleShow) {
            composeView.setContent {
                MdcTheme {
                    ListPosterCard(item) {
                        navigateToShow(item, composeView)
                    }
                }
            }
        }

        private fun navigateToShow(item: ScheduleShow, view: View) {
            val direction = DashboardFragmentDirections.actionDashboardFragmentToShowDetailFragment(
                ShowDetailArg(
                    source = "today",
                    showId = item.id,
                    showTitle = item.name,
                    showImageUrl = item.originalImage,
                    showBackgroundUrl = item.mediumImage
                )
            )

            view.findNavController().navigate(direction)
        }
    }
}