/*
 * MIT License
 *
 * Copyright (c) 2024 Ahmed Tikiwa
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

package com.theupnextapp.repository

import com.theupnextapp.network.SimklService
import com.theupnextapp.network.models.simkl.NetworkSimklLibraryResponse
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimklSyncManager @Inject constructor(
    private val simklService: SimklService,
    private val providerManager: ProviderManager,
    private val simklRepository: SimklRepository
) {

    /**
     * Executes the SIMKL Sync Loop.
     * Phase 1: If no previous sync date exists, fetches the entire TV show library.
     * Phase 2: If a sync date exists, fetches only the delta since that date.
     *
     * @param token The OAuth Bearer token to authorize the sync requests.
     */
    suspend fun sync(token: String): Result<Unit> {
        return try {
            val formattedToken = "Bearer $token"
            
            // 1. Check activities to see if sync is needed
            val activitiesResponse = simklService.getActivities(formattedToken)
            if (!activitiesResponse.isSuccessful || activitiesResponse.body() == null) {
                Timber.e("SIMKL Sync failed: Unable to fetch activities.")
                return Result.failure(Exception("Unable to fetch activities from SIMKL"))
            }

            val tvShowsUpdatedAt = activitiesResponse.body()?.tvShows?.updatedAt
            val lastActivityHash = providerManager.simklLastActivityHash.firstOrNull()

            if (tvShowsUpdatedAt != null && tvShowsUpdatedAt == lastActivityHash) {
                Timber.d("SIMKL Sync short-circuited: No new activities.")
                return Result.success(Unit) // No new updates, short-circuit
            }

            // 2. Determine Phase (Initial vs Delta)
            val lastSyncDate = providerManager.simklLastSyncDate.firstOrNull()

            if (lastSyncDate == null) {
                // Phase 1: Initial Sync (TV Shows only)
                Timber.d("SIMKL Sync Phase 1: Initial Sync for TV Shows.")
                val showsResponse = simklService.getSyncShows(formattedToken)
                
                if (showsResponse.isSuccessful && showsResponse.body() != null) {
                    processLibraryResponse(showsResponse.body()!!)
                    
                    // Update sync date to current UTC timestamp (ISO-8601 string compatible with SIMKL date_from)
                    val currentSyncDate = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                    providerManager.setSimklLastSyncDate(currentSyncDate)
                } else {
                    Timber.e("SIMKL Sync Phase 1 failed: Unable to fetch shows library.")
                    return Result.failure(Exception("Unable to fetch shows library from SIMKL"))
                }
            } else {
                // Phase 2: Delta Sync
                Timber.d("SIMKL Sync Phase 2: Delta Sync since $lastSyncDate.")
                val allItemsResponse = simklService.getAllItems(formattedToken, lastSyncDate)
                
                if (allItemsResponse.isSuccessful && allItemsResponse.body() != null) {
                    val tvShowsDelta = allItemsResponse.body()?.tvShows
                    if (!tvShowsDelta.isNullOrEmpty()) {
                        processLibraryResponse(tvShowsDelta)
                    }
                    
                    // Update sync date to current UTC timestamp
                    val currentSyncDate = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                    providerManager.setSimklLastSyncDate(currentSyncDate)
                } else {
                    Timber.e("SIMKL Sync Phase 2 failed: Unable to fetch deltas.")
                    return Result.failure(Exception("Unable to fetch deltas from SIMKL"))
                }
            }

            // 3. Refresh Watched History
            Timber.d("SIMKL Sync Phase 3: Push pending history and refreshing watched history.")
            simklRepository.pushPendingWatchedHistory(token)
            simklRepository.refreshWatchedHistory(token)

            // Update activity hash to prevent redundant syncs
            if (tvShowsUpdatedAt != null) {
                providerManager.setSimklLastActivityHash(tvShowsUpdatedAt)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "SIMKL Sync Loop encountered an exception.")
            Result.failure(e)
        }
    }

    private suspend fun processLibraryResponse(shows: List<NetworkSimklLibraryResponse>) {
        Timber.d("SIMKL Sync processing ${shows.size} TV shows.")
        
        val mappedShows = shows.mapNotNull { it.show }
        simklRepository.saveSyncShows(mappedShows)
    }
}
