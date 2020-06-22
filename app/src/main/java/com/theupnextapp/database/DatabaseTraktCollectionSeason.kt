package com.theupnextapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.theupnextapp.domain.TraktCollectionSeason

@Entity(tableName = "trakt_collection_seasons")
data class DatabaseTraktCollectionSeason(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var imdbID: String?,
    val seasonNumber: Int?
)

fun List<DatabaseTraktCollectionSeason>.asDomainModel() : List<TraktCollectionSeason> {
    return map {
        TraktCollectionSeason(
            id = it.id,
            imdbID = it.imdbID,
            seasonNumber = it.seasonNumber
        )
    }
}