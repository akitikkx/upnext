/*
 * MIT License
 *
 * Copyright (c) 2022 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.repository.fakes

import com.theupnextapp.database.DatabaseTableUpdate
import com.theupnextapp.database.UpnextDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeUpnextDao : UpnextDao {
    private val tableUpdates = mutableMapOf<String, DatabaseTableUpdate>()
    private val tableUpdatesFlow =
        MutableStateFlow<Map<String, DatabaseTableUpdate>>(emptyMap())

    override fun insertTableUpdateLog(vararg databaseTableUpdate: DatabaseTableUpdate) {
        databaseTableUpdate.forEach { update ->
            tableUpdates[update.table_name] = update
        }
        tableUpdatesFlow.value = HashMap(tableUpdates)
    }

    override fun getTableLastUpdate(tableName: String): Flow<DatabaseTableUpdate?> {
        return tableUpdatesFlow.map { it[tableName] }
    }

    override fun getTableLastUpdateTime(tableName: String): DatabaseTableUpdate? {
        return tableUpdates[tableName]
    }

    override fun deleteRecentTableUpdate(tableName: String) {
        tableUpdates.remove(tableName)
        tableUpdatesFlow.value = HashMap(tableUpdates) // Emit new state
    }

    // Helper for tests
    fun clearAll() {
        tableUpdates.clear()
        tableUpdatesFlow.value = emptyMap()
    }

    fun addTableUpdate(update: DatabaseTableUpdate) {
        insertTableUpdateLog(update)
    }
}
