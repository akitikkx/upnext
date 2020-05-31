package com.theupnextapp.network

import com.theupnextapp.domain.TraktShowStats

data class NetworkTraktShowStatsResponse(
    val collected_episodes: Int?,
    val collectors: Int?,
    val comments: Int?,
    val lists: Int?,
    val plays: Int?,
    val votes: Int?,
    val watchers: Int?
)

fun NetworkTraktShowStatsResponse.asDomainModel(): TraktShowStats {
    return TraktShowStats(
        collected_episodes = collected_episodes,
        collectors = collectors,
        comments = comments,
        lists = lists,
        plays = plays,
        votes = votes,
        watchers = watchers
    )
}