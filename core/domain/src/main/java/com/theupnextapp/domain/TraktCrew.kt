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

package com.theupnextapp.domain

data class TraktCrew(
    val job: String?,
    val name: String?,
    val originalImageUrl: String?,
    val mediumImageUrl: String?,
    val traktId: Int?,
    val imdbId: String?,
    val tmdbId: Int?,
    val slug: String?
)
