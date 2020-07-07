package com.theupnextapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.theupnextapp.domain.TableUpdate

@Entity(tableName = "table_updates")
data class DatabaseTableUpdate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val table_name: String,
    val last_updated: Long
)

fun DatabaseTableUpdate.asDomainModel() : TableUpdate {
    return TableUpdate(
        id = id,
        tableName = table_name,
        lastUpdated = last_updated
    )
}