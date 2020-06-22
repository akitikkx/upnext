package com.theupnextapp.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import com.theupnextapp.domain.ShowInfo

@Dao
interface UpnextDao {

    // Recommended Shows
    @Query("select * from recommended_shows")
    fun getRecommendedShows(): LiveData<List<DatabaseRecommendedShows>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllRecommendedShows(vararg recommendedShows: DatabaseRecommendedShows)

    @Query("delete from recommended_shows")
    fun deleteAllRecommendedShows()

    // New shows
    @Query("select * from new_shows")
    fun getNewShows(): LiveData<List<DatabaseNewShows>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllNewShows(vararg newShows: DatabaseNewShows)

    @Query("delete from new_shows")
    fun deleteAllNewShows()

    // Schedule shows
    @Query("select * from schedule_yesterday")
    fun getYesterdayShows(): LiveData<List<DatabaseYesterdaySchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllYesterdayShows(vararg newShows: DatabaseYesterdaySchedule)

    @Query("delete from schedule_yesterday")
    fun deleteAllYesterdayShows()

    @Query("select * from schedule_today")
    fun getTodayShows(): LiveData<List<DatabaseTodaySchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllTodayShows(vararg newShows: DatabaseTodaySchedule)

    @Query("delete from schedule_today")
    fun deleteAllTodayShows()

    @Query("select * from schedule_tomorrow")
    fun getTomorrowShows(): LiveData<List<DatabaseTomorrowSchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllTomorrowShows(vararg newShows: DatabaseTomorrowSchedule)

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

    @Query("select * from trakt_collection_episodes where imdbID = :imdbID")
    fun getTraktCollectionSeasonEpisodes(imdbID: String): LiveData<List<DatabaseTraktCollectionEpisode>>
}

@Database(
    entities = [
        DatabaseRecommendedShows::class,
        DatabaseNewShows::class,
        DatabaseYesterdaySchedule::class,
        DatabaseTodaySchedule::class,
        DatabaseTomorrowSchedule::class,
        DatabaseShowInfo::class,
        DatabaseTraktWatchlist::class,
        DatabaseTraktHistory::class,
        DatabaseTraktCollection::class,
        DatabaseTraktCollectionSeason::class,
        DatabaseTraktCollectionEpisode::class
    ],
    version = 10,
    exportSchema = true
)
abstract class UpnextDatabase : RoomDatabase() {
    abstract val upnextDao: UpnextDao
}

private lateinit var INSTANCE: UpnextDatabase

fun getDatabase(context: Context): UpnextDatabase {
    synchronized(UpnextDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                UpnextDatabase::class.java,
                "upnext"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
    return INSTANCE
}