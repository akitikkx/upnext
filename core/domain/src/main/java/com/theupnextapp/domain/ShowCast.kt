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

import android.os.Parcelable


data class ShowCast(
    val id: Int?,
    val name: String?,
    val country: String?,
    val birthday: String?,
    val deathday: String?,
    val gender: String?,
    val originalImageUrl: String?,
    val mediumImageUrl: String?,
    val characterId: Int?,
    val characterUrl: String?,
    val characterName: String?,
    val characterMediumImageUrl: String?,
    val characterOriginalImageUrl: String?,
    val self: Boolean?,
    val voice: Boolean?,
) : Parcelable {
    constructor(parcel: android.os.Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean
    )

    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(name)
        parcel.writeString(country)
        parcel.writeString(birthday)
        parcel.writeString(deathday)
        parcel.writeString(gender)
        parcel.writeString(originalImageUrl)
        parcel.writeString(mediumImageUrl)
        parcel.writeValue(characterId)
        parcel.writeString(characterUrl)
        parcel.writeString(characterName)
        parcel.writeString(characterMediumImageUrl)
        parcel.writeString(characterOriginalImageUrl)
        parcel.writeValue(self)
        parcel.writeValue(voice)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : android.os.Parcelable.Creator<ShowCast> {
        override fun createFromParcel(parcel: android.os.Parcel): ShowCast {
            return ShowCast(parcel)
        }

        override fun newArray(size: Int): Array<ShowCast?> {
            return arrayOfNulls(size)
        }
    }
}
