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

import com.google.gson.annotations.SerializedName

data class NetworkTmdbPersonImagesResponse(
    @SerializedName("id") val id: Int?,
    @SerializedName("profiles") val profiles: List<NetworkTmdbProfileImage>?
)

data class NetworkTmdbProfileImage(
    @SerializedName("aspect_ratio") val aspectRatio: Double?,
    @SerializedName("height") val height: Int?,
    @SerializedName("iso_639_1") val iso6391: String?,
    @SerializedName("file_path") val filePath: String?,
    @SerializedName("vote_average") val voteAverage: Double?,
    @SerializedName("vote_count") val voteCount: Int?,
    @SerializedName("width") val width: Int?
)
