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

import android.os.Parcelable


data class EpisodeDetailArg(
    val showTraktId: Int,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val showTitle: String? = null,
    val showId: Int? = null,
    val imdbID: String? = null,
    val isAuthorizedOnTrakt: Boolean? = false,
    val showImageUrl: String? = null,
    val showBackgroundUrl: String? = null,
    val episodeImageUrl: String? = null,
) : Parcelable {
    constructor(parcel: android.os.Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
        parcel.writeInt(showTraktId)
        parcel.writeInt(seasonNumber)
        parcel.writeInt(episodeNumber)
        parcel.writeString(showTitle)
        parcel.writeValue(showId)
        parcel.writeString(imdbID)
        parcel.writeValue(isAuthorizedOnTrakt)
        parcel.writeString(showImageUrl)
        parcel.writeString(showBackgroundUrl)
        parcel.writeString(episodeImageUrl)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : android.os.Parcelable.Creator<EpisodeDetailArg> {
        override fun createFromParcel(parcel: android.os.Parcel): EpisodeDetailArg {
            return EpisodeDetailArg(parcel)
        }

        override fun newArray(size: Int): Array<EpisodeDetailArg?> {
            return arrayOfNulls(size)
        }
    }
}
