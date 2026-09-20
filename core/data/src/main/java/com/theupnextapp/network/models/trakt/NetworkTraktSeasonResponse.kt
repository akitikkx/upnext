/*
 * MIT License
 *
 * Copyright (c) 2026 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 */

package com.theupnextapp.network.models.trakt

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.theupnextapp.domain.TraktSeason

@Keep
data class NetworkTraktSeasonResponse(
    val number: Int?,
    val title: String? = null,
    @SerializedName("episode_count")
    val episodeCount: Int? = null,
    @SerializedName("aired_episodes")
    val airedEpisodes: Int? = null,
)

fun NetworkTraktSeasonResponse.asDomainModel(): TraktSeason {
    return TraktSeason(
        number = number ?: 0,
        title = title,
        episodeCount = episodeCount,
        airedEpisodes = airedEpisodes,
    )
}
