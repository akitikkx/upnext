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
    // TRAKT ACCESS
    @Query("delete from trakt_access")
    fun deleteTraktAccessData()

    @Query("select * from trakt_access")
    fun getTraktAccessData(): Flow<DatabaseTraktAccess?>

    @Query("select * from trakt_access")
    fun getTraktAccessDataRaw(): DatabaseTraktAccess?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllTraktAccessData(databaseTraktAccess: DatabaseTraktAccess)

    // TRAKT WATCHLIST SHOWS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllWatchlistShows(vararg shows: DatabaseWatchlistShows)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlistShow(databaseWatchlistShows: DatabaseWatchlistShows)

    @Query("DELETE FROM favorite_shows")
    suspend fun deleteAllWatchlistShows()

    @Query("select * from favorite_shows")
    fun getWatchlistShows(): Flow<List<DatabaseWatchlistShows>>

    @Query("select * from favorite_shows")
    fun getWatchlistShowsRaw(): List<DatabaseWatchlistShows>

    @Query("select * from favorite_shows where imdbID = :imdbID")
    fun getWatchlistShowFlow(imdbID: String): Flow<DatabaseWatchlistShows?>

    @Query("select * from favorite_shows where imdbID = :imdbID")
    suspend fun getWatchlistShow(imdbID: String): DatabaseWatchlistShows?

    @Update(entity = DatabaseWatchlistShows::class)
    fun updateWatchlistShowWithAirStamp(databaseWatchlistShows: DatabaseWatchlistShows)

    @Query("select * from favorite_shows where tvMazeID = :tvMazeId")
    fun getWatchlistShowRawByTvMazeId(tvMazeId: Int): DatabaseWatchlistShows

    @Query("SELECT * FROM favorite_shows WHERE traktID = :traktId LIMIT 1")
    suspend fun getWatchlistShowByTraktId(traktId: Int): DatabaseWatchlistShows?

    @Query("SELECT traktID FROM favorite_shows")
    suspend fun getAllWatchlistShowTraktIds(): List<Int>

    @Query("DELETE FROM favorite_shows WHERE traktID IN (:traktIds)")
    suspend fun deleteWatchlistShowsByTraktIds(traktIds: List<Int>): Int // returns number of rows deleted

    @Query("DELETE FROM favorite_shows WHERE traktID = :traktId")
    suspend fun deleteWatchlistShowByTraktId(traktId: Int): Int

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

    // TRENDING SHOWS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTrending(vararg trendingShows: DatabaseTrendingShows)

    @Query("select * from trending_shows where providerId = :providerId")
    fun getTrendingShows(providerId: String): Flow<List<DatabaseTrendingShows>>

    @Query("SELECT * FROM trending_shows where providerId = :providerId")
    fun getTrendingShowsRaw(providerId: String): List<DatabaseTrendingShows>

    @Query("DELETE FROM trending_shows where providerId = :providerId")
    suspend fun clearTrendingShows(providerId: String)

    @Query("SELECT COUNT(id) == 0 FROM trending_shows where providerId = :providerId")
    suspend fun checkIfTrendingShowsIsEmpty(providerId: String): Boolean

    @Query("DELETE FROM trending_shows WHERE id IN (:showIds) AND providerId = :providerId")
    suspend fun deleteSpecificTrendingShows(showIds: List<Int>, providerId: String)

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

    @Query("SELECT * FROM watched_episodes WHERE showTraktId = :showTraktId AND seasonNumber = :season")
    suspend fun getWatchedEpisodesForSeason(showTraktId: Int, season: Int): List<DatabaseWatchedEpisode>

    @Query(
        "UPDATE watched_episodes SET syncStatus = :status WHERE showTraktId = :showTraktId AND seasonNumber = :season"
    )
    suspend fun updateSyncStatusForSeason(showTraktId: Int, season: Int, status: Int)

    @Query(
        "DELETE FROM watched_episodes WHERE showTraktId = :showTraktId AND seasonNumber = :season"
    )
    suspend fun deleteWatchedEpisodesForSeason(showTraktId: Int, season: Int)

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

    @Query(
        "DELETE FROM watched_episodes WHERE syncStatus = 2 AND showTraktId = :showTraktId AND seasonNumber = :season"
    )
    suspend fun confirmRemovalForSeason(showTraktId: Int, season: Int)

    @Query("DELETE FROM watched_episodes")
    suspend fun clearAllWatchedEpisodes()

    // WATCH HISTORY
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchHistory(history: List<DatabaseWatchHistory>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchHistoryItem(item: DatabaseWatchHistory)

    @Query("SELECT * FROM watch_history ORDER BY watchedAtEpochMillis DESC")
    fun getWatchHistoryFlow(): Flow<List<DatabaseWatchHistory>>

    @Query("SELECT * FROM watch_history ORDER BY watchedAtEpochMillis DESC")
    suspend fun getWatchHistoryRaw(): List<DatabaseWatchHistory>

    @Query("SELECT watchedAt FROM watch_history ORDER BY watchedAtEpochMillis DESC LIMIT 1")
    suspend fun getLatestWatchedTimestamp(): String?

    @Query("SELECT watchedAt FROM watch_history ORDER BY watchedAtEpochMillis ASC LIMIT 1")
    suspend fun getOldestWatchedTimestamp(): String?

    @Query(
        "UPDATE watch_history SET episodeImageUrl = :episodeImageUrl, showPosterUrl = :showPosterUrl WHERE historyId = :historyId"
    )
    suspend fun updateWatchHistoryImages(historyId: Long, episodeImageUrl: String?, showPosterUrl: String?)

    @Query(
        "UPDATE watch_history SET showPosterUrl = :showPosterUrl WHERE showTraktId = :showTraktId AND showPosterUrl IS NULL"
    )
    suspend fun updateShowPosterForShow(showTraktId: Int, showPosterUrl: String)

    @Query("SELECT COUNT(*) FROM watch_history")
    suspend fun getWatchHistoryCount(): Int

    @Query("SELECT * FROM watch_history WHERE episodeImageUrl IS NULL OR showPosterUrl IS NULL LIMIT :limit")
    suspend fun getWatchHistoryItemsMissingImages(limit: Int = 50): List<DatabaseWatchHistory>

    @Query("DELETE FROM watch_history WHERE historyId = :historyId")
    suspend fun deleteWatchHistoryItem(historyId: Long)

    @Query("DELETE FROM watch_history")
    suspend fun clearWatchHistory()

    // TRAKT CUSTOM LISTS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomLists(lists: List<DatabaseCustomList>)

    @Query("SELECT * FROM trakt_custom_lists ORDER BY name ASC")
    fun getCustomListsFlow(): Flow<List<DatabaseCustomList>>

    @Query("SELECT * FROM trakt_custom_lists ORDER BY name ASC")
    suspend fun getCustomListsRaw(): List<DatabaseCustomList>

    @Query("SELECT * FROM trakt_custom_lists WHERE traktId = :traktId LIMIT 1")
    suspend fun getCustomListByTraktId(traktId: Int): DatabaseCustomList?

    @Query("DELETE FROM trakt_custom_lists WHERE traktId NOT IN (:activeTraktIds)")
    suspend fun deleteMissingCustomLists(activeTraktIds: List<Int>)

    @Query("DELETE FROM trakt_custom_lists WHERE traktId = :traktId")
    suspend fun deleteCustomList(traktId: Int)

    @Query("DELETE FROM trakt_custom_lists")
    suspend fun clearCustomLists()

    // TRAKT CUSTOM LIST ITEMS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomListItems(items: List<DatabaseCustomListItem>)

    @Query("SELECT * FROM trakt_custom_list_items WHERE listTraktId = :listTraktId ORDER BY rank ASC, title ASC")
    fun getCustomListItemsFlow(listTraktId: Int): Flow<List<DatabaseCustomListItem>>

    @Query("SELECT * FROM trakt_custom_list_items WHERE listTraktId = :listTraktId ORDER BY rank ASC, title ASC")
    suspend fun getCustomListItemsRaw(listTraktId: Int): List<DatabaseCustomListItem>

    @Query("DELETE FROM trakt_custom_list_items WHERE listTraktId = :listTraktId")
    suspend fun clearCustomListItems(listTraktId: Int)

    @Query("DELETE FROM trakt_custom_list_items WHERE listTraktId = :listTraktId AND traktID NOT IN (:activeTraktIds)")
    suspend fun deleteMissingCustomListItems(listTraktId: Int, activeTraktIds: List<Int>)

    @Query("UPDATE trakt_custom_list_items SET originalImageUrl = :posterUrl, mediumImageUrl = :heroImageUrl, tvMazeID = :tvMazeId WHERE traktID = :showTraktId")
    suspend fun updateCustomListItemImages(showTraktId: Int, posterUrl: String?, heroImageUrl: String?, tvMazeId: Int?)

    @Query("DELETE FROM trakt_custom_list_items")
    suspend fun clearAllCustomListItems()
}
