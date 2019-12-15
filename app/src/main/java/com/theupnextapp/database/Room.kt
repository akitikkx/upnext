package com.theupnextapp.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface UpnextDao {
    @Query("select * from recommended_shows")
    fun getRecommendedShows(): LiveData<List<DatabaseRecommendedShows>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllRecommendedShows(vararg recommendedShows: DatabaseRecommendedShows)

    @Query("delete from recommended_shows")
    fun deleteAllRecommendedShows()

    @Query("select * from new_shows")
    fun getNewShows(): LiveData<List<DatabaseNewShows>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllNewShows(vararg newShows: DatabaseNewShows)

    @Query("delete from new_shows")
    fun deleteAllNewShows()
}

@Database(
    entities = [DatabaseRecommendedShows::class, DatabaseNewShows::class],
    version = 1,
    exportSchema = false
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
            ).build()
        }
    }
    return INSTANCE
}