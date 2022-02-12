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
    val voice: Boolean?
) : Parcelable {
    constructor(parcel: Parcel) : this(
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

    override fun writeToParcel(parcel: Parcel, flags: Int) {
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

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ShowCast> {
        override fun createFromParcel(parcel: Parcel): ShowCast {
            return ShowCast(parcel)
        }

        override fun newArray(size: Int): Array<ShowCast?> {
            return arrayOfNulls(size)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) {
            return false
        }

        other as ShowCast

        if (id != other.id) {
            return false
        }

        if (name != other.name) {
            return false
        }

        if (country != other.country) {
            return false
        }

        if (birthday != other.birthday) {
            return false
        }

        if (deathday != other.deathday) {
            return false
        }

        if (gender != other.gender) {
            return false
        }

        if (originalImageUrl != other.originalImageUrl) {
            return false
        }

        if (mediumImageUrl != other.mediumImageUrl) {
            return false
        }

        if (characterId != other.characterId) {
            return false
        }

        if (characterUrl != other.characterUrl) {
            return false
        }

        if (characterName != other.characterName) {
            return false
        }

        if (characterMediumImageUrl != other.characterMediumImageUrl) {
            return false
        }

        if (characterOriginalImageUrl != other.characterOriginalImageUrl) {
            return false
        }

        if (self != other.self) {
            return false
        }

        if (voice != other.voice) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = id ?: 0
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (country?.hashCode() ?: 0)
        result = 31 * result + (birthday?.hashCode() ?: 0)
        result = 31 * result + (deathday?.hashCode() ?: 0)
        result = 31 * result + (gender?.hashCode() ?: 0)
        result = 31 * result + (originalImageUrl?.hashCode() ?: 0)
        result = 31 * result + (mediumImageUrl?.hashCode() ?: 0)
        result = 31 * result + (characterId ?: 0)
        result = 31 * result + (characterUrl?.hashCode() ?: 0)
        result = 31 * result + (characterName?.hashCode() ?: 0)
        result = 31 * result + (characterMediumImageUrl?.hashCode() ?: 0)
        result = 31 * result + (characterOriginalImageUrl?.hashCode() ?: 0)
        result = 31 * result + (self?.hashCode() ?: 0)
        result = 31 * result + (voice?.hashCode() ?: 0)
        return result
    }
}