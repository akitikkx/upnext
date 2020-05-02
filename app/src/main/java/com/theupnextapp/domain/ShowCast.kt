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
}