package com.theupnextapp.repository

import com.theupnextapp.common.utils.DateUtils
import com.theupnextapp.database.UpnextDao
import com.theupnextapp.network.TvMazeService
import com.theupnextapp.network.models.tvmaze.NetworkShowNextEpisodeResponse
import com.theupnextapp.network.models.tvmaze.NetworkShowPreviousEpisodeResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class BaseRepository(
    private val upnextDao: UpnextDao,
    private val tvMazeService: TvMazeService
) {

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

    suspend fun getNextEpisode(tvMazeID: Int): NetworkShowNextEpisodeResponse? {
        return try {
            withContext(Dispatchers.IO) {
                val showInfo =
                    tvMazeService.getShowSummaryAsync(tvMazeID.toString()).await()

                val nextEpisodeLink = showInfo._links?.nextepisode?.href?.substring(
                    showInfo._links.nextepisode.href.lastIndexOf("/") + 1,
                    showInfo._links.nextepisode.href.length
                )?.replace("/", "")

                getNextEpisodeInfo(nextEpisodeLink)
            }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun getNextEpisodeInfo(nextEpisodeLink: String?): NetworkShowNextEpisodeResponse? {
        var showNextEpisode: NetworkShowNextEpisodeResponse? = null
        if (!nextEpisodeLink.isNullOrEmpty()) {
            showNextEpisode = tvMazeService.getNextEpisodeAsync(
                nextEpisodeLink.replace("/", "")
            ).await()
        }
        return showNextEpisode
    }
}