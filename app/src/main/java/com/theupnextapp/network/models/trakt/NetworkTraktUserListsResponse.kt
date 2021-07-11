package com.theupnextapp.network.models.trakt

import com.theupnextapp.domain.TraktUserList

class NetworkTraktUserListsResponse : ArrayList<NetworkTraktUserListsResponseItem>()

data class NetworkTraktUserListsResponseItem(
    val allow_comments: Boolean?,
    val comment_count: Int?,
    val created_at: String?,
    val description: String?,
    val display_numbers: Boolean?,
    val ids: NetworkTraktUserListsResponseItemIds?,
    val item_count: Int?,
    val likes: Int?,
    val name: String?,
    val privacy: String?,
    val sort_by: String?,
    val sort_how: String?,
    val updated_at: String?
)

data class NetworkTraktUserListsResponseItemIds(
    val slug: String?,
    val trakt: Int?
)

fun List<NetworkTraktUserListsResponseItem>.asDomainModel(): List<TraktUserList> {
    return map {
        TraktUserList(
            traktId = it.ids?.trakt,
            slug = it.ids?.slug
        )
    }
}