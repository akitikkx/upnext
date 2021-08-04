package com.theupnextapp.repository

import com.theupnextapp.common.utils.DateUtils
import com.theupnextapp.database.UpnextDao

abstract class BaseRepository(private val upnextDao: UpnextDao) {

    /**
     * Determine whether this particular update request may proceed
     * based on the last update timestamp
     */
    protected fun canProceedWithUpdate(tableName: String, intervalMinutes: Long): Boolean {
        var canProceedWithUpdate = false

        val lastUpdateTime =
            upnextDao.getTableLastUpdateTime(tableName)

        val diffInMinutes = lastUpdateTime?.last_updated?.let {
            DateUtils.dateDifference(
                endTime = it,
                type = DateUtils.MINUTES
            )
        }
        if (diffInMinutes != null) {
            if (diffInMinutes > intervalMinutes) {
                canProceedWithUpdate = true
            }
        }

        if (diffInMinutes == null) {
            canProceedWithUpdate = true
        }

        return canProceedWithUpdate
    }
}