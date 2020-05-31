package com.theupnextapp.network.models.trakt

import com.theupnextapp.domain.TraktShowRating

data class NetworkTraktShowRatingResponse(
    val distribution: Distribution?,
    val rating: Double?,
    val votes: Int?
)

data class Distribution(
    val `1`: Int,
    val `10`: Int,
    val `2`: Int,
    val `3`: Int,
    val `4`: Int,
    val `5`: Int,
    val `6`: Int,
    val `7`: Int,
    val `8`: Int,
    val `9`: Int
)

fun NetworkTraktShowRatingResponse.asDomainModel(): TraktShowRating {
    return TraktShowRating(
        rating = rating?.toInt(),
        votes = votes
    )
}