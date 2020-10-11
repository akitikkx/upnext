package com.theupnextapp.domain

import androidx.navigation.NavDirections
import com.theupnextapp.common.utils.models.TimeDifferenceForDisplay

data class LibraryList(
    val leftIcon: Int,
    val title: String,
    val rightIcon: Int,
    val link: NavDirections,
    val transitionName: String,
    val lastUpdated: TimeDifferenceForDisplay?
)