package com.theupnextapp.ui.common

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.theupnextapp.domain.ShowDetailArg
import com.theupnextapp.domain.ShowSearch
import com.theupnextapp.ui.search.SearchFragmentDirections

abstract class ComposeViewHolder<T>(val composeView: ComposeView) :
    RecyclerView.ViewHolder(composeView) {

    init {
        // necessary to make the Compose view holder work in all scenarios
        // https://developer.android.com/jetpack/compose/interop/compose-in-existing-ui#compose-recyclerview
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
    }

    abstract val source: String

    @Composable
    abstract fun ComposableContainer(item: T)

    fun bind(item: T) {
        composeView.setContent {
            ComposableContainer(item)
        }
    }

    fun navigateFromSearchToDetail(item: ShowSearch, view: View) {
        val directions = SearchFragmentDirections.actionSearchFragmentToShowDetailFragment(
            ShowDetailArg(
                source = source,
                showId = item.id,
                showTitle = item.name,
                showImageUrl = item.originalImageUrl,
                showBackgroundUrl = item.mediumImageUrl
            )
        )
        view.findNavController().navigate(directions)
    }
}