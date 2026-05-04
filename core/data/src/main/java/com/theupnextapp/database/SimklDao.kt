package com.theupnextapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SimklDao {
    @Query("delete from simkl_access")
    suspend fun deleteSimklAccessData()

    @Query("select * from simkl_access")
    fun getSimklAccessData(): Flow<DatabaseSimklAccess?>

    @Query("select * from simkl_access")
    fun getSimklAccessDataRaw(): DatabaseSimklAccess?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSimklAccessData(databaseSimklAccess: DatabaseSimklAccess)

    // WATCHED EPISODES
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchedEpisode(episode: DatabaseSimklWatchedEpisode)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchedEpisodes(episodes: List<DatabaseSimklWatchedEpisode>)

    @Query(
        "DELETE FROM simkl_watched_episodes WHERE showSimklId = :showSimklId AND seasonNumber = :season AND episodeNumber = :episode"
    )
    suspend fun deleteWatchedEpisode(showSimklId: Int, season: Int, episode: Int)

    @Query("SELECT * FROM simkl_watched_episodes WHERE showSimklId = :showSimklId")
    fun getWatchedEpisodesForShow(showSimklId: Int): Flow<List<DatabaseSimklWatchedEpisode>>
    
    @Query("SELECT * FROM simkl_watched_episodes WHERE showTraktId = :showTraktId")
    fun getWatchedEpisodesForShowByTraktId(showTraktId: Int): Flow<List<DatabaseSimklWatchedEpisode>>

    @Query("SELECT * FROM simkl_watched_episodes WHERE showImdbId = :showImdbId")
    fun getWatchedEpisodesForShowByImdbId(showImdbId: String): Flow<List<DatabaseSimklWatchedEpisode>>

    @Query(
        "SELECT * FROM simkl_watched_episodes WHERE showSimklId = :showSimklId AND seasonNumber = :season AND episodeNumber = :episode LIMIT 1"
    )
    suspend fun getWatchedEpisode(showSimklId: Int, season: Int, episode: Int): DatabaseSimklWatchedEpisode?

    @Query("SELECT * FROM simkl_watched_episodes WHERE showSimklId = :showSimklId AND seasonNumber = :season")
    suspend fun getWatchedEpisodesForSeason(showSimklId: Int, season: Int): List<DatabaseSimklWatchedEpisode>

    @Query(
        "UPDATE simkl_watched_episodes SET syncStatus = :status WHERE showSimklId = :showSimklId AND seasonNumber = :season"
    )
    suspend fun updateSyncStatusForSeason(showSimklId: Int, season: Int, status: Int)

    @Query(
        "DELETE FROM simkl_watched_episodes WHERE showSimklId = :showSimklId AND seasonNumber = :season"
    )
    suspend fun deleteWatchedEpisodesForSeason(showSimklId: Int, season: Int)

    @Query("SELECT COUNT(*) FROM simkl_watched_episodes WHERE showSimklId = :showSimklId AND syncStatus = 0")
    suspend fun getWatchedCountForShow(showSimklId: Int): Int

    @Query("SELECT * FROM simkl_watched_episodes WHERE syncStatus != 0")
    suspend fun getPendingSyncEpisodes(): List<DatabaseSimklWatchedEpisode>

    @Query(
        "UPDATE simkl_watched_episodes SET syncStatus = :status WHERE showSimklId = :showSimklId AND seasonNumber = :season AND episodeNumber = :episode"
    )
    suspend fun updateSyncStatus(showSimklId: Int, season: Int, episode: Int, status: Int)

    @Query("DELETE FROM simkl_watched_episodes")
    suspend fun clearAllWatchedEpisodes()
}
