package com.theupnextapp.network

class NetworkTraktWatchedResponse : ArrayList<NetworkTraktWatchedItem>()

data class NetworkTraktWatchedItem(
    val last_updated_at: String,
    val last_watched_at: String,
    val plays: Int,
    val reset_at: Any,
    val seasons: List<TraktWatchedSeason>,
    val show: TraktWatchedShow
)

data class TraktWatchedSeason(
    val episodes: List<TraktWatchedEpisode>,
    val number: Int
)

data class TraktWatchedShow(
    val ids: TraktWatchedIds,
    val title: String,
    val year: Int
)

data class TraktWatchedEpisode(
    val last_watched_at: String,
    val number: Int,
    val plays: Int
)

data class TraktWatchedIds(
    val imdb: String,
    val slug: String,
    val tmdb: Int,
    val trakt: Int,
    val tvdb: Int
)