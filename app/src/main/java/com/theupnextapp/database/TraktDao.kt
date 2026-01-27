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
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TraktDao {
    // TRAKT ACCESS
    @Query("delete from trakt_access")
    fun deleteTraktAccessData()

    @Query("select * from trakt_access")
    fun getTraktAccessData(): Flow<DatabaseTraktAccess?>

    @Query("select * from trakt_access")
    fun getTraktAccessDataRaw(): DatabaseTraktAccess?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllTraktAccessData(databaseTraktAccess: DatabaseTraktAccess)

    // TRAKT FAVORITE SHOWS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFavoriteShows(vararg shows: DatabaseFavoriteShows)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteShow(databaseFavoriteShows: DatabaseFavoriteShows)

    @Query("DELETE FROM favorite_shows")
    suspend fun deleteAllFavoriteShows()

    @Query("select * from favorite_shows")
    fun getFavoriteShows(): Flow<List<DatabaseFavoriteShows>>

    @Query("select * from favorite_shows")
    fun getFavoriteShowsRaw(): List<DatabaseFavoriteShows>

    @Query("select * from favorite_shows where imdbID = :imdbID")
    fun getFavoriteShowFlow(imdbID: String): Flow<DatabaseFavoriteShows?>

    @Query("select * from favorite_shows where imdbID = :imdbID")
    suspend fun getFavoriteShow(imdbID: String): DatabaseFavoriteShows?

    @Update(entity = DatabaseFavoriteShows::class)
    fun updateFavoriteShowWithAirStamp(databaseFavoriteShows: DatabaseFavoriteShows)

    @Query("select * from favorite_shows where tvMazeID = :tvMazeId")
    fun getFavoriteShowRawByTvMazeId(tvMazeId: Int): DatabaseFavoriteShows

    @Query("SELECT * FROM favorite_shows WHERE traktID = :traktId LIMIT 1")
    suspend fun getFavoriteShowByTraktId(traktId: Int): DatabaseFavoriteShows?

    @Query("SELECT traktID FROM favorite_shows")
    suspend fun getAllFavoriteShowTraktIds(): List<Int>

    @Query("DELETE FROM favorite_shows WHERE traktID IN (:traktIds)")
    suspend fun deleteFavoriteShowsByTraktIds(traktIds: List<Int>): Int // returns number of rows deleted

    @Query("DELETE FROM favorite_shows WHERE traktID = :traktId")
    suspend fun deleteFavoriteShowByTraktId(traktId: Int): Int

    // TRAKT POPULAR SHOWS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTraktPopular(vararg traktPopularShows: DatabaseTraktPopularShows)

    @Query("select * from trakt_popular")
    fun getTraktPopular(): Flow<List<DatabaseTraktPopularShows>>

    @Query("SELECT * FROM trakt_popular")
    fun getTraktPopularRaw(): List<DatabaseTraktPopularShows>

    @Query("DELETE FROM trakt_popular")
    suspend fun clearPopularShows()

    @Query("SELECT COUNT(id) == 0 FROM trakt_popular")
    suspend fun checkIfPopularShowsIsEmpty(): Boolean

    @Query("DELETE FROM trakt_popular WHERE id IN (:showIds)")
    suspend fun deleteSpecificPopularShows(showIds: List<Int>)

    // TRAKT TRENDING SHOWS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTraktTrending(vararg traktTrendingShows: DatabaseTraktTrendingShows)

    @Query("select * from trakt_trending")
    fun getTraktTrending(): Flow<List<DatabaseTraktTrendingShows>>

    @Query("SELECT * FROM trakt_trending")
    fun getTraktTrendingRaw(): List<DatabaseTraktTrendingShows>

    @Query("DELETE FROM trakt_trending")
    suspend fun clearTrendingShows()

    @Query("SELECT COUNT(id) == 0 FROM trakt_trending")
    suspend fun checkIfTrendingShowsIsEmpty(): Boolean

    @Query("DELETE FROM trakt_trending WHERE id IN (:showIds)")
    suspend fun deleteSpecificTrendingShows(showIds: List<Int>)

    // TRAKT MOST ANTICIPATED SHOWS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTraktMostAnticipated(vararg traktMostAnticipatedShows: DatabaseTraktMostAnticipated)

    @Query("select * from trakt_most_anticipated")
    fun getTraktMostAnticipated(): Flow<List<DatabaseTraktMostAnticipated>>

    @Query("SELECT * FROM trakt_most_anticipated")
    fun getTraktMostAnticipatedRaw(): List<DatabaseTraktMostAnticipated>

    @Query("DELETE FROM trakt_most_anticipated")
    suspend fun clearMostAnticipatedShows()

    @Query("SELECT COUNT(id) == 0 FROM trakt_most_anticipated")
    suspend fun checkIfMostAnticipatedShowsIsEmpty(): Boolean

    @Query("DELETE FROM trakt_most_anticipated WHERE id IN (:showIds)")
    suspend fun deleteSpecificMostAnticipatedShows(showIds: List<Int>)

    // WATCHED EPISODES
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchedEpisode(episode: DatabaseWatchedEpisode)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchedEpisodes(episodes: List<DatabaseWatchedEpisode>)

    @Query(
        "DELETE FROM watched_episodes WHERE showTraktId = :showTraktId AND seasonNumber = :season AND episodeNumber = :episode"
    )
    suspend fun deleteWatchedEpisode(showTraktId: Int, season: Int, episode: Int)

    @Query("SELECT * FROM watched_episodes WHERE showTraktId = :showTraktId")
    fun getWatchedEpisodesForShow(showTraktId: Int): Flow<List<DatabaseWatchedEpisode>>

    @Query(
        "SELECT * FROM watched_episodes WHERE showTraktId = :showTraktId AND seasonNumber = :season AND episodeNumber = :episode LIMIT 1"
    )
    suspend fun getWatchedEpisode(showTraktId: Int, season: Int, episode: Int): DatabaseWatchedEpisode?

    @Query("SELECT COUNT(*) FROM watched_episodes WHERE showTraktId = :showTraktId AND syncStatus = 0")
    suspend fun getWatchedCountForShow(showTraktId: Int): Int

    @Query("SELECT * FROM watched_episodes WHERE syncStatus != 0")
    suspend fun getPendingSyncEpisodes(): List<DatabaseWatchedEpisode>

    @Query(
        "UPDATE watched_episodes SET syncStatus = :status WHERE showTraktId = :showTraktId AND seasonNumber = :season AND episodeNumber = :episode"
    )
    suspend fun updateSyncStatus(showTraktId: Int, season: Int, episode: Int, status: Int)

    @Query(
        "DELETE FROM watched_episodes WHERE syncStatus = 2 AND showTraktId = :showTraktId AND seasonNumber = :season AND episodeNumber = :episode"
    )
    suspend fun confirmRemoval(showTraktId: Int, season: Int, episode: Int)

    @Query("DELETE FROM watched_episodes")
    suspend fun clearAllWatchedEpisodes()
}
