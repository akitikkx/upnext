package com.theupnextapp.network.models.trakt

import com.theupnextapp.domain.TraktShowRating

data class NetworkTraktShowRatingResponse(
    val distribution: HashMap<String, Int>?,
    val rating: Double?,
    val votes: Int?
)

data class Distribution(
    val score: String,
    val value: Int
)

fun NetworkTraktShowRatingResponse.asDomainModel(): TraktShowRating {
    return TraktShowRating(
        rating = rating?.toInt(),
        votes = votes,
        distribution = distribution
    )
}