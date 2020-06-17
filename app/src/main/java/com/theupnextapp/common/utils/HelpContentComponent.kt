package com.theupnextapp.common.utils

import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.theupnextapp.ui.helpContent.collection.CollectionInfoBottomSheetFragment
import com.theupnextapp.ui.helpContent.connectToTrakt.ConnectToTraktInfoBottomSheetFragment
import com.theupnextapp.ui.helpContent.watchlist.WatchlistInfoBottomSheetFragment

object HelpContentComponent {

    fun showContent(
        fragmentType: HelpContentType,
        fragmentManager: FragmentManager?
    ) {
        val fragment = fragmentType.getFragment()
        fragmentManager?.let { fragment.show(it, fragmentType.getFragmentTag()) }
    }


}

enum class HelpContentType {
    CONNECT_TO_TRAKT_INFO {
        override fun getFragment() =
            ConnectToTraktInfoBottomSheetFragment()

        override fun getFragmentTag(): String? = getFragment().tag
    },

    WATCHLIST_INFO {
        override fun getFragment(): BottomSheetDialogFragment = WatchlistInfoBottomSheetFragment()
        override fun getFragmentTag(): String? = getFragment().tag
    },

    COLLECTION_INFO {
        override fun getFragment(): BottomSheetDialogFragment = CollectionInfoBottomSheetFragment()
        override fun getFragmentTag(): String? = getFragment().tag
    };

    abstract fun getFragment(): BottomSheetDialogFragment

    abstract fun getFragmentTag(): String?
}