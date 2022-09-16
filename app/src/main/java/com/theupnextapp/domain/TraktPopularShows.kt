/*
 * MIT License
 *
 * Copyright (c) 2022 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.domain

data class TraktPopularShows(
    val id: Int?,
    val title: String?,
    val year: String?,
    val mediumImageUrl: String?,
    val originalImageUrl: String?,
    val imdbID: String?,
    val slug: String?,
    val tmdbID: Int?,
    val traktID: Int?,
    val tvdbID: Int?,
    val tvMazeID: Int?
) {
    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) {
            return false
        }

        other as TraktPopularShows

        if (id != other.id) {
            return false
        }

        if (title != other.title) {
            return false
        }

        if (year != other.year) {
            return false
        }

        if (mediumImageUrl != other.mediumImageUrl) {
            return false
        }

        if (originalImageUrl != other.originalImageUrl) {
            return false
        }

        if (imdbID != other.imdbID) {
            return false
        }

        if (slug != other.slug) {
            return false
        }

        if (tmdbID != other.tmdbID) {
            return false
        }

        if (traktID != other.traktID) {
            return false
        }

        if (tvdbID != other.tvdbID) {
            return false
        }

        if (tvMazeID != other.tvMazeID) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (year?.hashCode() ?: 0)
        result = 31 * result + (mediumImageUrl?.hashCode() ?: 0)
        result = 31 * result + (originalImageUrl?.hashCode() ?: 0)
        result = 31 * result + (imdbID?.hashCode() ?: 0)
        result = 31 * result + (slug?.hashCode() ?: 0)
        result = 31 * result + (tmdbID ?: 0)
        result = 31 * result + (traktID ?: 0)
        result = 31 * result + (tvdbID ?: 0)
        result = 31 * result + (tvMazeID ?: 0)
        return result
    }
}
