package com.theupnextapp.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.theupnextapp.domain.ShowInfo

@Dao
interface UpnextDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTableUpdateLog(vararg databaseTableUpdate: DatabaseTableUpdate)

    @Query("select * from table_updates where table_name = :tableName")
    fun getTableLastUpdate(tableName: String): LiveData<DatabaseTableUpdate?>

    @Query("delete from table_updates where table_name = :tableName")
    fun deleteRecentTableUpdate(tableName: String)

    // Schedule shows
    @Query("select * from schedule_yesterday")
    fun getYesterdayShows(): LiveData<List<DatabaseYesterdaySchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllYesterdayShows(vararg yesterdayShows: DatabaseYesterdaySchedule)

    @Query("delete from schedule_yesterday")
    fun deleteAllYesterdayShows()

    @Query("select * from schedule_today")
    fun getTodayShows(): LiveData<List<DatabaseTodaySchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllTodayShows(vararg todayShows: DatabaseTodaySchedule)

    @Query("delete from schedule_today")
    fun deleteAllTodayShows()

    @Query("select * from schedule_tomorrow")
    fun getTomorrowShows(): LiveData<List<DatabaseTomorrowSchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllTomorrowShows(vararg tomorrowShows: DatabaseTomorrowSchedule)

    @Query("delete from schedule_tomorrow")
    fun deleteAllTomorrowShows()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllShowInfo(showInfo: DatabaseShowInfo)

    @Query("delete from shows_info where id = :id")
    fun deleteAllShowInfo(id: Int)

    @Query("select * from shows_info where id = :id")
    fun getShowWithId(id: Int): ShowInfo

    @Query("delete from trakt_watchlist")
    fun deleteAllTraktWatchlist()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllTraktWatchlist(vararg traktWatchlist: DatabaseTraktWatchlist)

    @Query("select * from trakt_watchlist")
    fun getTraktWatchlist(): LiveData<List<DatabaseTraktWatchlist>>

    @Query("delete from trakt_history")
    fun deleteAllTraktHistory()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllTraktHistory(vararg traktHistory: DatabaseTraktHistory)

    @Query("select * from trakt_history order by watchedAt desc")
    fun getTraktHistory(): LiveData<List<DatabaseTraktHistory>>

    @Query("select * from trakt_watchlist where imdbID = :imdbID")
    fun checkIfInTraktWatchlist(imdbID: String): LiveData<DatabaseTraktHistory?>

    @Query("delete from trakt_watchlist where imdbID = :imdbID")
    fun deleteWatchlistItem(imdbID: String)

    // Trakt Collection
    @Query("delete from trakt_collection")
    fun deleteAllTraktCollection()

    @Query("delete from trakt_collection where imdbID = :imdbID")
    fun deleteAllTraktCollectionByImdbId(imdbID: String)

    @Query("delete from trakt_collection_seasons")
    fun deleteAllTraktCollectionSeasons()

    @Query("delete from trakt_collection_seasons where seasonNumber = :seasonNumber and imdbID = :imdbID")
    fun deleteAllTraktCollectionSeasonsBySeasonNumber(seasonNumber: Int, imdbID: String)

    @Query("delete from trakt_collection_seasons where imdbID = :imdbID")
    fun deleteAllTraktCollectionSeasonsByImdbId(imdbID: String)

    @Query("delete from trakt_collection_episodes")
    fun deleteAllTraktCollectionSeasonEpisodes()

    @Query("delete from trakt_collection_episodes where imdbID in(:collectedShows)")
    fun deleteAllTraktCollectionSeasonEpisodesButInclude(collectedShows: List<String>)

    @Query("delete from trakt_collection_episodes where seasonNumber = :seasonNumber and imdbID = :imdbID")
    fun deleteAllTraktCollectionEpisodesBySeasonNumber(seasonNumber: Int, imdbID: String)

    @Query("delete from trakt_collection_episodes where imdbID = :imdbID")
    fun deleteAllTraktCollectionEpisodesByImdbId(imdbID: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllTraktCollection(vararg traktCollection: DatabaseTraktCollection)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllTraktCollectionSeasons(vararg traktCollectionSeason: DatabaseTraktCollectionSeason)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllTraktCollectionEpisodes(vararg traktCollectionResponseItemEpisode: DatabaseTraktCollectionEpisode)

    @Query("select * from trakt_collection")
    fun getTraktCollection(): LiveData<List<DatabaseTraktCollection>>

    @Query("select * from trakt_collection_seasons where imdbID = :imdbID")
    fun getTraktCollectionSeasons(imdbID: String): LiveData<List<DatabaseTraktCollectionSeason>>

    @Query("select * from trakt_collection_episodes where imdbID = :imdbID and seasonNumber = :seasonNumber")
    fun getTraktCollectionSeasonEpisodes(
        imdbID: String,
        seasonNumber: Int
    ): LiveData<List<DatabaseTraktCollectionEpisode>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllTraktRecommendations(vararg traktRecommendations: DatabaseTraktRecommendations)

    @Query("delete from trakt_recommendations")
    fun deleteAllTraktRecommendations()

    @Query("select * from trakt_recommendations")
    fun getTraktRecommendations(): LiveData<List<DatabaseTraktRecommendations>>

    // TRAKT POPULAR SHOWS
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

    // TRAKT MOST ANTICIPATED SHOWS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllTraktMostAnticipated(vararg traktMostAnticipatedShows: DatabaseTraktMostAnticipated)

    @Query("delete from trakt_most_anticipated")
    fun deleteAllTraktMostAnticipated()

    @Query("select * from trakt_most_anticipated")
    fun getTraktMostAnticipated(): LiveData<List<DatabaseTraktMostAnticipated>>
}