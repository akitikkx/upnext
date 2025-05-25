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

package com.theupnextapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TraktDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTraktPopular(vararg traktPopularShows: DatabaseTraktPopularShows)

    @Query("select * from trakt_popular")
    fun getTraktPopular(): Flow<List<DatabaseTraktPopularShows>>

    // TRAKT TRENDING SHOWS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTraktTrending(vararg traktTrendingShows: DatabaseTraktTrendingShows)

    @Query("select * from trakt_trending")
    fun getTraktTrending(): Flow<List<DatabaseTraktTrendingShows>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTraktMostAnticipated(vararg traktMostAnticipatedShows: DatabaseTraktMostAnticipated)

    @Query("select * from trakt_most_anticipated")
    fun getTraktMostAnticipated(): Flow<List<DatabaseTraktMostAnticipated>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFavoriteShows(vararg databaseFavoriteShows: DatabaseFavoriteShows)

    @Query("delete from favorite_shows")
    fun deleteAllFavoriteShows()

    @Query("select * from favorite_shows")
    fun getFavoriteShows(): Flow<List<DatabaseFavoriteShows>>

    @Query("select * from favorite_shows")
    fun getFavoriteShowsRaw(): List<DatabaseFavoriteShows>

    @Query("select * from favorite_shows where imdbID = :imdbID")
    fun getFavoriteShow(imdbID: String): DatabaseFavoriteShows?

    @Query("delete from trakt_access")
    fun deleteTraktAccessData()

    @Query("select * from trakt_access")
    fun getTraktAccessData(): Flow<DatabaseTraktAccess?>

    @Query("select * from trakt_access")
    fun getTraktAccessDataRaw(): DatabaseTraktAccess?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllTraktAccessData(databaseTraktAccess: DatabaseTraktAccess)

    @Update(entity = DatabaseFavoriteShows::class)
    fun updateFavoriteShowWithAirStamp(databaseFavoriteShows: DatabaseFavoriteShows)

    @Query("select * from favorite_shows where tvMazeID = :tvMazeId")
    fun getFavoriteShowRawByTvMazeId(tvMazeId: Int): DatabaseFavoriteShows

    @Query("SELECT * FROM trakt_popular") // Check if at least one row exists
    fun getTraktPopularRaw(): List<DatabaseTraktPopularShows> // Or return a single item or count

    @Query("SELECT * FROM trakt_trending")
    fun getTraktTrendingRaw(): List<DatabaseTraktTrendingShows>

    @Query("SELECT * FROM trakt_most_anticipated")
    fun getTraktMostAnticipatedRaw(): List<DatabaseTraktMostAnticipated>

    @Query("DELETE FROM trakt_popular")
    suspend fun clearPopularShows()

    @Query("DELETE FROM trakt_trending")
    suspend fun clearTrendingShows()

    @Query("DELETE FROM trakt_most_anticipated")
    suspend fun clearMostAnticipatedShows()

    @Query("SELECT COUNT(id) == 0 FROM trakt_popular")
    suspend fun checkIfPopularShowsIsEmpty(): Boolean

    @Query("DELETE FROM trakt_popular WHERE id IN (:showIds)")
    suspend fun deleteSpecificPopularShows(showIds: List<Int>)

    @Query("SELECT COUNT(id) == 0 FROM trakt_trending")
    suspend fun checkIfTrendingShowsIsEmpty(): Boolean

    @Query("DELETE FROM trakt_trending WHERE id IN (:showIds)")
    suspend fun deleteSpecificTrendingShows(showIds: List<Int>)

    @Query("SELECT COUNT(id) == 0 FROM trakt_most_anticipated")
    suspend fun checkIfMostAnticipatedShowsIsEmpty(): Boolean

    @Query("DELETE FROM trakt_most_anticipated WHERE id IN (:showIds)")
    suspend fun deleteSpecificMostAnticipatedShows(showIds: List<Int>)
}
