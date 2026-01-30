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

import com.theupnextapp.database.DatabaseFavoriteShows
import com.theupnextapp.database.DatabaseTraktAccess
import com.theupnextapp.database.DatabaseTraktMostAnticipated
import com.theupnextapp.database.DatabaseTraktPopularShows
import com.theupnextapp.database.DatabaseTraktTrendingShows
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
    override suspend fun insertAllFavoriteShows(vararg shows: DatabaseFavoriteShows) {}

    override suspend fun insertFavoriteShow(databaseFavoriteShows: DatabaseFavoriteShows) {}

    override suspend fun deleteAllFavoriteShows() {}

    override fun getFavoriteShows(): Flow<List<DatabaseFavoriteShows>> = flowOf(emptyList())

    override fun getFavoriteShowsRaw(): List<DatabaseFavoriteShows> = emptyList()

    override fun getFavoriteShowFlow(imdbID: String): Flow<DatabaseFavoriteShows?> = flowOf(getFavoriteShowRaw(imdbID))

    // Helper for non-suspend access in tests if needed, or just standard lookup
    private fun getFavoriteShowRaw(imdbID: String): DatabaseFavoriteShows? = null

    override suspend fun getFavoriteShow(imdbID: String): DatabaseFavoriteShows? = getFavoriteShowRaw(imdbID)

    override fun updateFavoriteShowWithAirStamp(databaseFavoriteShows: DatabaseFavoriteShows) {}

    override fun getFavoriteShowRawByTvMazeId(tvMazeId: Int): DatabaseFavoriteShows = TODO()

    override suspend fun getFavoriteShowByTraktId(traktId: Int): DatabaseFavoriteShows? = null

    override suspend fun getAllFavoriteShowTraktIds(): List<Int> = emptyList()

    override suspend fun deleteFavoriteShowsByTraktIds(traktIds: List<Int>): Int = 0

    override suspend fun deleteFavoriteShowByTraktId(traktId: Int): Int = 0

    override suspend fun insertAllTraktPopular(vararg traktPopularShows: DatabaseTraktPopularShows) {}

    override fun getTraktPopular(): Flow<List<DatabaseTraktPopularShows>> = flowOf(emptyList())

    override fun getTraktPopularRaw(): List<DatabaseTraktPopularShows> = emptyList()

    override suspend fun clearPopularShows() {}

    override suspend fun checkIfPopularShowsIsEmpty(): Boolean = true

    override suspend fun deleteSpecificPopularShows(showIds: List<Int>) {}

    override suspend fun insertAllTraktTrending(vararg traktTrendingShows: DatabaseTraktTrendingShows) {}

    override fun getTraktTrending(): Flow<List<DatabaseTraktTrendingShows>> = flowOf(emptyList())

    override fun getTraktTrendingRaw(): List<DatabaseTraktTrendingShows> = emptyList()

    override suspend fun clearTrendingShows() {}

    override suspend fun checkIfTrendingShowsIsEmpty(): Boolean = true

    override suspend fun deleteSpecificTrendingShows(showIds: List<Int>) {}

    override suspend fun insertAllTraktMostAnticipated(vararg traktMostAnticipatedShows: DatabaseTraktMostAnticipated) {}

    override fun getTraktMostAnticipated(): Flow<List<DatabaseTraktMostAnticipated>> = flowOf(emptyList())

    override fun getTraktMostAnticipatedRaw(): List<DatabaseTraktMostAnticipated> = emptyList()

    override suspend fun clearMostAnticipatedShows() {}

    override suspend fun checkIfMostAnticipatedShowsIsEmpty(): Boolean = true

    override suspend fun deleteSpecificMostAnticipatedShows(showIds: List<Int>) {}

    // Watched Episodes
    override suspend fun insertWatchedEpisode(episode: com.theupnextapp.database.DatabaseWatchedEpisode) {}

    override suspend fun insertWatchedEpisodes(episodes: List<com.theupnextapp.database.DatabaseWatchedEpisode>) {}

    override suspend fun deleteWatchedEpisode(
        showTraktId: Int,
        season: Int,
        episode: Int,
    ) {}

    override fun getWatchedEpisodesForShow(showTraktId: Int): Flow<List<com.theupnextapp.database.DatabaseWatchedEpisode>> =
        flowOf(emptyList())

    override suspend fun getWatchedEpisode(
        showTraktId: Int,
        season: Int,
        episode: Int,
    ): com.theupnextapp.database.DatabaseWatchedEpisode? = null

    override suspend fun getWatchedCountForShow(showTraktId: Int): Int = 0

    override suspend fun getPendingSyncEpisodes(): List<com.theupnextapp.database.DatabaseWatchedEpisode> = emptyList()

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
}
