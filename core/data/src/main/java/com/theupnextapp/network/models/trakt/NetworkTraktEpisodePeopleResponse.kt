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

import com.theupnextapp.domain.EpisodePeople
import com.theupnextapp.domain.TraktCast
import com.theupnextapp.domain.TraktCrew

data class NetworkTraktEpisodePeopleResponse(
    val cast: List<NetworkTraktCast>?,
    val guest_stars: List<NetworkTraktCast>?,
    val crew: NetworkTraktCrew?
)

fun NetworkTraktEpisodePeopleResponse.asDomainModel(): EpisodePeople {
    return EpisodePeople(
        guestStars = guest_stars?.map {
            TraktCast(
                character = it.characters?.firstOrNull(),
                name = it.person?.name,
                originalImageUrl = null,
                mediumImageUrl = null,
                traktId = it.person?.ids?.trakt,
                imdbId = it.person?.ids?.imdb,
                slug = it.person?.ids?.slug
            )
        },
        crew = crew?.let { crewOpt ->
            val crewList = mutableListOf<TraktCrew>()
            crewOpt.directing?.forEach { member ->
                crewList.add(TraktCrew(job = member.jobs?.firstOrNull(), name = member.person?.name, originalImageUrl = null, mediumImageUrl = null, traktId = member.person?.ids?.trakt, imdbId = member.person?.ids?.imdb, slug = member.person?.ids?.slug))
            }
            crewOpt.writing?.forEach { member ->
                 crewList.add(TraktCrew(job = member.jobs?.firstOrNull(), name = member.person?.name, originalImageUrl = null, mediumImageUrl = null, traktId = member.person?.ids?.trakt, imdbId = member.person?.ids?.imdb, slug = member.person?.ids?.slug))
            }
            crewList
        }
    )
}
