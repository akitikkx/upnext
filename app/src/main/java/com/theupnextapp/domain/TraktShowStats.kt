package com.theupnextapp.domain

data class TraktShowStats(
    val collected_episodes: Int?,
    val collectors: Int?,
    val comments: Int?,
    val lists: Int?,
    val plays: Int?,
    val votes: Int?,
    val watchers: Int?
)