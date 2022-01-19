package com.theupnextapp.domain

data class ShowNextEpisode(
    val nextEpisodeId: Int?,
    val nextEpisodeAirdate: String?,
    val nextEpisodeAirstamp: String?,
    val nextEpisodeAirtime: String?,
    val nextEpisodeMediumImageUrl: String?,
    val nextEpisodeOriginalImageUrl: String?,
    val nextEpisodeName: String?,
    val nextEpisodeNumber: String?,
    val nextEpisodeRuntime: String?,
    val nextEpisodeSeason: String?,
    val nextEpisodeSummary: String?,
    val nextEpisodeUrl: String?
)