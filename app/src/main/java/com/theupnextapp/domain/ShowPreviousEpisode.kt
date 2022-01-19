package com.theupnextapp.domain

data class ShowPreviousEpisode(
    val previousEpisodeId: Int?,
    val previousEpisodeAirdate: String?,
    val previousEpisodeAirstamp: String?,
    val previousEpisodeAirtime: String?,
    val previousEpisodeMediumImageUrl: String?,
    val previousEpisodeOriginalImageUrl: String?,
    val previousEpisodeName: String?,
    val previousEpisodeNumber: String?,
    val previousEpisodeRuntime: String?,
    val previousEpisodeSeason: String?,
    val previousEpisodeSummary: String?,
    val previousEpisodeUrl: String?
)