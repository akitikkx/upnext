/*
 * MIT License
 *
 * Copyright (c) 2026 Ahmed Tikiwa
 */

package com.theupnextapp.network.models.trakt

import com.google.gson.annotations.SerializedName

/**
 * Response model for GET /sync/ratings/shows.
 * Each entry represents a show the user has rated.
 */
data class NetworkTraktUserRatingResponse(
    @SerializedName("rating") val rating: Int?,
    @SerializedName("show") val show: NetworkTraktUserRatingShow?,
)

data class NetworkTraktUserRatingShow(
    @SerializedName("title") val title: String?,
    @SerializedName("ids") val ids: NetworkTraktUserRatingShowIds?,
)

data class NetworkTraktUserRatingShowIds(
    @SerializedName("trakt") val trakt: Int?,
    @SerializedName("imdb") val imdb: String?,
    @SerializedName("tmdb") val tmdb: Int?,
    @SerializedName("slug") val slug: String?,
)
