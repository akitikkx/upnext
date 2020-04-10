package com.theupnextapp.network

class NetworkTraktCollectionResponse : ArrayList<NetworkTraktCollectionResponseItem>()

data class NetworkTraktCollectionResponseItem(
    val last_collected_at: String?,
    val last_updated_at: String?,
    val seasons: List<TraktSeason>?,
    val show: TraktCollectionShow?
)

data class TraktSeason(
    val episodes: List<TraktEpisode>?,
    val number: Int?
)

data class TraktCollectionShow(
    val ids: TraktCollectionIds?,
    val title: String?,
    val year: Int?
)

data class TraktEpisode(
    val collected_at: String?,
    val number: Int?
)

data class TraktCollectionIds(
    val imdb: String?,
    val slug: String?,
    val tmdb: Int?,
    val trakt: Int?,
    val tvdb: Int?,
    val tvrage: Int?
)