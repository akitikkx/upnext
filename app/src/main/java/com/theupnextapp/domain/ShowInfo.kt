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

import android.os.Parcel
import android.os.Parcelable

data class ShowInfo(
    val airDays: String?,
    val averageRating: String?,
    var id: Int,
    val imdbID: String?,
    val genres: String?,
    val language: String?,
    val mediumImageUrl: String?,
    val name: String?,
    val originalImageUrl: String?,
    val summary: String?,
    val time: String?,
    val status: String?,
    val nextEpisodeLinkedId: Int?,
    val previousEpisodeLinkedId: Int?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(airDays)
        parcel.writeString(averageRating)
        parcel.writeInt(id)
        parcel.writeString(imdbID)
        parcel.writeString(genres)
        parcel.writeString(language)
        parcel.writeString(mediumImageUrl)
        parcel.writeString(name)
        parcel.writeString(originalImageUrl)
        parcel.writeString(summary)
        parcel.writeString(time)
        parcel.writeString(status)
        parcel.writeValue(nextEpisodeLinkedId)
        parcel.writeValue(previousEpisodeLinkedId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ShowInfo> {
        override fun createFromParcel(parcel: Parcel): ShowInfo {
            return ShowInfo(parcel)
        }

        override fun newArray(size: Int): Array<ShowInfo?> {
            return arrayOfNulls(size)
        }
    }
}