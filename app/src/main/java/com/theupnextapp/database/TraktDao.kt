package com.theupnextapp.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TraktDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllTraktPopular(vararg traktPopularShows: DatabaseTraktPopularShows)

    @Query("delete from trakt_popular")
    fun deleteAllTraktPopular()

    @Query("select * from trakt_popular")
    fun getTraktPopular(): LiveData<List<DatabaseTraktPopularShows>>

    // TRAKT TRENDING SHOWS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllTraktTrending(vararg traktTrendingShows: DatabaseTraktTrendingShows)

    @Query("delete from trakt_trending")
    fun deleteAllTraktTrending()

    @Query("select * from trakt_trending")
    fun getTraktTrending(): LiveData<List<DatabaseTraktTrendingShows>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllTraktMostAnticipated(vararg traktMostAnticipatedShows: DatabaseTraktMostAnticipated)

    @Query("delete from trakt_most_anticipated")
    fun deleteAllTraktMostAnticipated()

    @Query("select * from trakt_most_anticipated")
    fun getTraktMostAnticipated(): LiveData<List<DatabaseTraktMostAnticipated>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllFavoriteShows(vararg databaseFavoriteShows: DatabaseFavoriteShows)

    @Query("delete from favorite_shows")
    fun deleteAllFavoriteShows()

    @Query("select * from favorite_shows")
    fun getFavoriteShows(): LiveData<List<DatabaseFavoriteShows>>

    @Query("select * from favorite_shows")
    fun getFavoriteShowsRaw(): List<DatabaseFavoriteShows>

    @Query("select * from favorite_shows where imdbID = :imdbID")
    fun getFavoriteShow(imdbID: String): DatabaseFavoriteShows?

    @Query("delete from trakt_access")
    fun deleteTraktAccessData()

    @Query("select * from trakt_access")
    fun getTraktAccessData(): LiveData<DatabaseTraktAccess?>

    @Query("select * from trakt_access")
    fun getTraktAccessDataRaw(): DatabaseTraktAccess?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllTraktAccessData(databaseTraktAccess: DatabaseTraktAccess)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllFavoriteNextEpisodes(vararg databaseFavoriteNextEpisode: DatabaseFavoriteNextEpisode)

    @Update(entity = DatabaseFavoriteShows::class)
    fun updateFavoriteEpisode(databaseFavoriteShows: DatabaseFavoriteShows)

    @Query("delete from favorite_next_episodes")
    fun deleteAllFavoriteEpisodes()

    @Query("select * from favorite_next_episodes")
    fun getFavoriteEpisodes(): LiveData<List<DatabaseFavoriteNextEpisode>>

    @Query("select * from favorite_shows where tvMazeID = :tvMazeId")
    fun getFavoriteShowRaw(tvMazeId: Int): DatabaseFavoriteShows

    @Query("delete from favorite_next_episodes where imdb = :imdbID")
    fun deleteFavoriteEpisode(imdbID: String)
}