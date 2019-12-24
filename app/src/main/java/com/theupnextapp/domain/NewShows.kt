package com.theupnextapp.domain

data class NewShows(
    var id: Int,
    val url: String?,
    val name: String?,
    val status: String?,
    val airTime: String?,
    val runtime: String?,
    val premiered: String?,
    val trailerUrl: String?,
    val mediumImageUrl: String?,
    val originalImageUrl: String?,
    val createDate: String? = null,
    val updateDate: String?,
    val localImageUrl: String?
)