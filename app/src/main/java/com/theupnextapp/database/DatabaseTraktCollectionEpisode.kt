package com.theupnextapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.theupnextapp.domain.TraktCollectionSeasonEpisode

@Entity(tableName = "trakt_collection_episodes")
data class DatabaseTraktCollectionEpisode(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var imdbID: String?,
    val seasonNumber: Int?,
    val episodeNumber: Int?,
    val collectedAt: String?
)

fun List<DatabaseTraktCollectionEpisode>.asDomainModel(): List<TraktCollectionSeasonEpisode> {
    return map {
        TraktCollectionSeasonEpisode(
            id = it.id,
            imdbID = it.imdbID,
            seasonNumber = it.seasonNumber,
            episodeNumber = it.episodeNumber,
            collectedAt = it.collectedAt
        )
    }
}