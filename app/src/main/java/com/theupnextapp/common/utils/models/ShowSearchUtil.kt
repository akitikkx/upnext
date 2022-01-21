package com.theupnextapp.common.utils.models

import com.theupnextapp.R
import com.theupnextapp.domain.ShowSearch

fun getNameAndReleaseYearResource(showSearch: ShowSearch): Int {
    return if (!showSearch.status.isNullOrEmpty()) {
        if (showSearch.status != "Ended") {
            R.string.search_item_not_ended
        } else {
            R.string.search_item_ended
        }
    } else {
        R.string.search_item_ended
    }
}