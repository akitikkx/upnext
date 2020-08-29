package com.theupnextapp.domain

import android.os.Parcel
import android.os.Parcelable

data class TraktCollection(
    var id: Long,
    val title: String?,
    val mediumImageUrl: String?,
    val originalImageUrl: String?,
    val year: Int?,
    val slug: String?,
    val imdbID: String?,
    val tmdbID: Int?,
    val traktID: Int?,
    val tvdbID: Int?,
    val tvrageID: Int?,
    val tvMazeID: Int?,
    val lastCollectedAt: String?,
    val lastUpdatedAt: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(title)
        parcel.writeString(mediumImageUrl)
        parcel.writeString(originalImageUrl)
        parcel.writeValue(year)
        parcel.writeString(slug)
        parcel.writeString(imdbID)
        parcel.writeValue(tmdbID)
        parcel.writeValue(traktID)
        parcel.writeValue(tvdbID)
        parcel.writeValue(tvrageID)
        parcel.writeValue(tvMazeID)
        parcel.writeString(lastCollectedAt)
        parcel.writeString(lastUpdatedAt)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TraktCollection> {
        override fun createFromParcel(parcel: Parcel): TraktCollection {
            return TraktCollection(parcel)
        }

        override fun newArray(size: Int): Array<TraktCollection?> {
            return arrayOfNulls(size)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) {
            return false
        }

        other as TraktCollection

        if (id != other.id) {
            return false
        }

        if (title != other.title) {
            return false
        }

        if (mediumImageUrl != other.mediumImageUrl) {
            return false
        }

        if (originalImageUrl != other.originalImageUrl) {
            return false
        }

        if (year != other.year) {
            return false
        }

        if (slug != other.slug) {
            return false
        }

        if (imdbID != other.imdbID) {
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

        if (tvrageID != other.tvrageID) {
            return false
        }

        if (tvMazeID != other.tvMazeID) {
            return false
        }

        if (lastCollectedAt != other.lastCollectedAt) {
            return false
        }

        if (lastUpdatedAt != other.lastUpdatedAt) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (mediumImageUrl?.hashCode() ?: 0)
        result = 31 * result + (originalImageUrl?.hashCode() ?: 0)
        result = 31 * result + (year ?: 0)
        result = 31 * result + (slug?.hashCode() ?: 0)
        result = 31 * result + (imdbID?.hashCode() ?: 0)
        result = 31 * result + (tmdbID ?: 0)
        result = 31 * result + (traktID ?: 0)
        result = 31 * result + (tvdbID ?: 0)
        result = 31 * result + (tvrageID ?: 0)
        result = 31 * result + (tvMazeID ?: 0)
        result = 31 * result + (lastCollectedAt?.hashCode() ?: 0)
        result = 31 * result + (lastUpdatedAt?.hashCode() ?: 0)
        return result
    }
}