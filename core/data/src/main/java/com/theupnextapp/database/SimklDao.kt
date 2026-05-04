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
}
