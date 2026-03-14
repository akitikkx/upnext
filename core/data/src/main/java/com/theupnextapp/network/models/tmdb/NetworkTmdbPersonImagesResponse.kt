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

package com.theupnextapp.network.models.tmdb

import com.squareup.moshi.Json

data class NetworkTmdbPersonImagesResponse(
    @Json(name = "id") val id: Int?,
    @Json(name = "profiles") val profiles: List<NetworkTmdbProfileImage>?
)

data class NetworkTmdbProfileImage(
    @Json(name = "aspect_ratio") val aspectRatio: Double?,
    @Json(name = "height") val height: Int?,
    @Json(name = "iso_639_1") val iso6391: String?,
    @Json(name = "file_path") val filePath: String?,
    @Json(name = "vote_average") val voteAverage: Double?,
    @Json(name = "vote_count") val voteCount: Int?,
    @Json(name = "width") val width: Int?
)
