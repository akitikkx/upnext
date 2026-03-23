package com.theupnextapp.network.models.trakt

import com.google.gson.annotations.SerializedName

class NetworkTraktWatchlistResponse : ArrayList<NetworkTraktWatchlistResponseItem>()

data class NetworkTraktWatchlistResponseItem(
    @SerializedName("id")
    val id: Long,
    @SerializedName("listed_at")
    val listedAt: String,
    @SerializedName("notes")
    val notes: String?,
    @SerializedName("rank")
    val rank: Int,
    @SerializedName("show")
    val show: NetworkTraktWatchlistResponseItemShow,
    @SerializedName("type")
    val type: String
)

data class NetworkTraktWatchlistResponseItemShow(
    @SerializedName("ids")
    val ids: NetworkTraktWatchlistResponseItemShowIds,
    @SerializedName("title")
    val title: String,
    @SerializedName("year")
    val year: Int,
    @SerializedName("network")
    val network: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("rating")
    val rating: Double?
)

data class NetworkTraktWatchlistResponseItemShowIds(
    @SerializedName("imdb")
    val imdb: String?,
    @SerializedName("slug")
    val slug: String,
    @SerializedName("tmdb")
    val tmdb: Int?,
    @SerializedName("trakt")
    val trakt: Int,
    @SerializedName("tvdb")
    val tvdb: Int?,
    @SerializedName("tvrage")
    val tvMazeID: Int?
)

fun NetworkTraktWatchlistResponseItem.asDatabaseModel(): com.theupnextapp.database.DatabaseWatchlistShows {
    return com.theupnextapp.database.DatabaseWatchlistShows(
        id = id.toInt(),
        title = show.title,
        slug = show.ids.slug,
        year = show.year?.toString(),
        imdbID = show.ids.imdb,
        tvdbID = show.ids.tvdb,
        tmdbID = show.ids.tmdb,
        traktID = show.ids.trakt,
        tvMazeID = show.ids.tvMazeID,
        rating = show.rating,
        network = show.network,
        status = show.status,
        originalImageUrl = null,
        mediumImageUrl = null
    )
}
