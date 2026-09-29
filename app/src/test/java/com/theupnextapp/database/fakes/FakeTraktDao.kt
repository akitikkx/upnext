/*
 * MIT License
 *
 * Copyright (c) 2022 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.database.fakes

import com.theupnextapp.database.DatabaseTraktAccess
import com.theupnextapp.database.DatabaseTraktMostAnticipated
import com.theupnextapp.database.DatabaseTraktPopularShows
import com.theupnextapp.database.DatabaseTrendingShows
import com.theupnextapp.database.DatabaseWatchHistory
import com.theupnextapp.database.DatabaseWatchedEpisode
import com.theupnextapp.database.DatabaseWatchlistShows
import com.theupnextapp.database.TraktDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeTraktDao : TraktDao {
    var currentToken: DatabaseTraktAccess? = null

    override fun deleteTraktAccessData() {
        currentToken = null
    }

    override fun getTraktAccessData(): Flow<DatabaseTraktAccess?> {
        return flowOf(currentToken)
    }

    override fun getTraktAccessDataRaw(): DatabaseTraktAccess? {
        return currentToken
    }

    override fun insertAllTraktAccessData(databaseTraktAccess: DatabaseTraktAccess) {
        currentToken = databaseTraktAccess
    }

    // Unimplemented methods
    override suspend fun insertAllWatchlistShows(vararg shows: DatabaseWatchlistShows) {}

    override suspend fun insertWatchlistShow(databaseWatchlistShows: DatabaseWatchlistShows) {}

    override suspend fun deleteAllWatchlistShows() {}

    override fun getWatchlistShows(): Flow<List<DatabaseWatchlistShows>> = flowOf(emptyList())

    override fun getWatchlistShowsRaw(): List<DatabaseWatchlistShows> = emptyList()

    override fun getWatchlistShowFlow(imdbID: String): Flow<DatabaseWatchlistShows?> = flowOf(getWatchlistShowRaw(imdbID))

    // Helper for non-suspend access in tests if needed, or just standard lookup
    private fun getWatchlistShowRaw(imdbID: String): DatabaseWatchlistShows? = null

    override suspend fun getWatchlistShow(imdbID: String): DatabaseWatchlistShows? = getWatchlistShowRaw(imdbID)

    override fun updateWatchlistShowWithAirStamp(databaseWatchlistShows: DatabaseWatchlistShows) {}

    override fun getWatchlistShowRawByTvMazeId(tvMazeId: Int): DatabaseWatchlistShows = TODO()

    override suspend fun getWatchlistShowByTraktId(traktId: Int): DatabaseWatchlistShows? = null

    override suspend fun getAllWatchlistShowTraktIds(): List<Int> = emptyList()

    override suspend fun deleteWatchlistShowsByTraktIds(traktIds: List<Int>): Int = 0

    override suspend fun deleteWatchlistShowByTraktId(traktId: Int): Int = 0

    override suspend fun insertAllTraktPopular(vararg traktPopularShows: DatabaseTraktPopularShows) {}

    override fun getTraktPopular(): Flow<List<DatabaseTraktPopularShows>> = flowOf(emptyList())

    override fun getTraktPopularRaw(): List<DatabaseTraktPopularShows> = emptyList()

    override suspend fun clearPopularShows() {}

    override suspend fun checkIfPopularShowsIsEmpty(): Boolean = true

    override suspend fun deleteSpecificPopularShows(showIds: List<Int>) {}

    override suspend fun insertAllTrending(vararg trendingShows: DatabaseTrendingShows) {}

    override fun getTrendingShows(providerId: String): Flow<List<DatabaseTrendingShows>> = flowOf(emptyList())

    override fun getTrendingShowsRaw(providerId: String): List<DatabaseTrendingShows> = emptyList()

    override suspend fun clearTrendingShows(providerId: String) {}

    override suspend fun checkIfTrendingShowsIsEmpty(providerId: String): Boolean = true

    override suspend fun deleteSpecificTrendingShows(showIds: List<Int>, providerId: String) {}

    override suspend fun insertAllTraktMostAnticipated(vararg traktMostAnticipatedShows: DatabaseTraktMostAnticipated) {}

    override fun getTraktMostAnticipated(): Flow<List<DatabaseTraktMostAnticipated>> = flowOf(emptyList())

    override fun getTraktMostAnticipatedRaw(): List<DatabaseTraktMostAnticipated> = emptyList()

    override suspend fun clearMostAnticipatedShows() {}

    override suspend fun checkIfMostAnticipatedShowsIsEmpty(): Boolean = true

    override suspend fun deleteSpecificMostAnticipatedShows(showIds: List<Int>) {}

    // Watched Episodes
    override suspend fun insertWatchedEpisode(episode: DatabaseWatchedEpisode) {}

    override suspend fun insertWatchedEpisodes(episodes: List<DatabaseWatchedEpisode>) {}

    override suspend fun deleteWatchedEpisode(
        showTraktId: Int,
        season: Int,
        episode: Int,
    ) {}

    override fun getWatchedEpisodesForShow(showTraktId: Int): Flow<List<DatabaseWatchedEpisode>> =
        flowOf(emptyList())

    override suspend fun getWatchedEpisode(
        showTraktId: Int,
        season: Int,
        episode: Int,
    ): DatabaseWatchedEpisode? = null

    override suspend fun getWatchedCountForShow(showTraktId: Int): Int = 0

    override suspend fun getPendingSyncEpisodes(): List<DatabaseWatchedEpisode> = emptyList()

    override suspend fun updateSyncStatus(
        showTraktId: Int,
        season: Int,
        episode: Int,
        status: Int,
    ) {}

    override suspend fun confirmRemoval(
        showTraktId: Int,
        season: Int,
        episode: Int,
    ) {}

    override suspend fun clearAllWatchedEpisodes() {}

    override suspend fun getWatchedEpisodesForSeason(
        showTraktId: Int,
        season: Int,
    ): List<DatabaseWatchedEpisode> = emptyList()

    override suspend fun updateSyncStatusForSeason(
        showTraktId: Int,
        season: Int,
        status: Int,
    ) {}

    override suspend fun deleteWatchedEpisodesForSeason(
        showTraktId: Int,
        season: Int,
    ) {}

    override suspend fun confirmRemovalForSeason(
        showTraktId: Int,
        season: Int,
    ) {}

    // WATCH HISTORY
    private val _watchHistory = mutableListOf<DatabaseWatchHistory>()

    override suspend fun insertWatchHistory(history: List<DatabaseWatchHistory>) {
        _watchHistory.removeAll { existing -> history.any { it.historyId == existing.historyId } }
        _watchHistory.addAll(history)
    }

    override suspend fun insertWatchHistoryItem(item: DatabaseWatchHistory) {
        _watchHistory.removeAll { it.historyId == item.historyId }
        _watchHistory.add(item)
    }

    override fun getWatchHistoryFlow(): Flow<List<DatabaseWatchHistory>> = flowOf(_watchHistory)

    override suspend fun getWatchHistoryRaw(): List<DatabaseWatchHistory> = _watchHistory.toList()

    override suspend fun getLatestWatchedTimestamp(): String? =
        _watchHistory.maxByOrNull { it.watchedAtEpochMillis }?.watchedAt

    override suspend fun getOldestWatchedTimestamp(): String? =
        _watchHistory.minByOrNull { it.watchedAtEpochMillis }?.watchedAt

    override suspend fun updateWatchHistoryImages(historyId: Long, episodeImageUrl: String?, showPosterUrl: String?) {
        val index = _watchHistory.indexOfFirst { it.historyId == historyId }
        if (index != -1) {
            val item = _watchHistory[index]
            _watchHistory[index] = item.copy(episodeImageUrl = episodeImageUrl, showPosterUrl = showPosterUrl)
        }
    }

    override suspend fun updateShowPosterForShow(showTraktId: Int, showPosterUrl: String) {
        for (i in _watchHistory.indices) {
            if (_watchHistory[i].showTraktId == showTraktId && _watchHistory[i].showPosterUrl == null) {
                _watchHistory[i] = _watchHistory[i].copy(showPosterUrl = showPosterUrl)
            }
        }
    }

    override suspend fun getWatchHistoryCount(): Int = _watchHistory.size

    override suspend fun getWatchHistoryItemsMissingImages(limit: Int): List<DatabaseWatchHistory> =
        _watchHistory.filter { it.episodeImageUrl == null || it.showPosterUrl == null }.take(limit)

    override suspend fun deleteWatchHistoryItem(historyId: Long) {
        _watchHistory.removeAll { it.historyId == historyId }
    }

    override suspend fun clearWatchHistory() {
        _watchHistory.clear()
    }
}
