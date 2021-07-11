package com.theupnextapp.network.models.trakt

import com.theupnextapp.domain.TraktUserListItem

class NetworkTraktUserListItemResponse : ArrayList<NetworkTraktUserListItemResponseItem>()

data class NetworkTraktUserListItemResponseItem(
    val id: Int?,
    val listed_at: String?,
    val rank: Int?,
    val show: NetworkTraktUserListItemResponseItemShow?,
    val type: String?
)

data class NetworkTraktUserListItemResponseItemShow(
    val ids: NetworkTraktUserListItemResponseItemShowIds?,
    val title: String?,
    val year: Int?,
    var mediumImageUrl: String?,
    var originalImageUrl: String?
)

data class NetworkTraktUserListItemResponseItemShowIds(
    val imdb: String?,
    val slug: String?,
    val tmdb: Int?,
    val trakt: Int?,
    val tvdb: Int?,
    var tvMazeID: Int?
)

fun List<NetworkTraktUserListItemResponseItem>.asDomainModel(): List<TraktUserListItem> {
    return map {
        TraktUserListItem(
            id = it.id,
            title = it.show?.title,
            slug = it.show?.ids?.slug,
            year = it.show?.year.toString(),
            mediumImageUrl = it.show?.mediumImageUrl,
            originalImageUrl = it.show?.originalImageUrl,
            imdbID = it.show?.ids?.imdb,
            tmdbID = it.show?.ids?.tmdb,
            traktID = it.show?.ids?.trakt,
            tvdbID = it.show?.ids?.tvdb,
            tvMazeID = it.show?.ids?.tvMazeID
        )
    }
}