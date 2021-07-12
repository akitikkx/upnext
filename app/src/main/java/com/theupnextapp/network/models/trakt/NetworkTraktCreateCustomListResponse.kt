package com.theupnextapp.network.models.trakt

data class NetworkTraktCreateCustomListResponse(
    val allow_comments: Boolean?,
    val comment_count: Int?,
    val created_at: String?,
    val description: String?,
    val display_numbers: Boolean?,
    val ids: NetworkTraktCreateCustomListResponseIds?,
    val item_count: Int?,
    val likes: Int?,
    val name: String?,
    val privacy: String?,
    val sort_by: String?,
    val sort_how: String?,
    val updated_at: String?
)

data class NetworkTraktCreateCustomListResponseIds(
    val slug: String?,
    val trakt: Int?
)