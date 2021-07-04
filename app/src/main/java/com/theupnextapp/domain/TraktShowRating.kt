package com.theupnextapp.domain

data class TraktShowRating(
    val rating: Int?,
    val votes: Int?,
    val distribution: HashMap<String, Int>?
)