package com.theupnextapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UpnextDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTableUpdateLog(vararg databaseTableUpdate: DatabaseTableUpdate)

    @Query("select * from table_updates where table_name = :tableName")
    fun getTableLastUpdate(tableName: String): Flow<DatabaseTableUpdate?>

    @Query("select * from table_updates where table_name = :tableName")
    fun getTableLastUpdateTime(tableName: String): DatabaseTableUpdate?

    @Query("delete from table_updates where table_name = :tableName")
    fun deleteRecentTableUpdate(tableName: String)
}