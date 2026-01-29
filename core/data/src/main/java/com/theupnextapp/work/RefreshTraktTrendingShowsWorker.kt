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

package com.theupnextapp.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.theupnextapp.repository.TraktRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

/**
 * This worker runs daily to refresh the Trakt trending shows in the database.
 * It is scheduled by [RefreshTraktTrendingShowsWorker].
 * It extends [BaseWorker] which handles the foreground notification.
 *
 * @param appContext The application context.
 * @param workerParameters The worker parameters.
 * @param traktRepository The Trakt repository.
 */
@HiltWorker
class RefreshTraktTrendingShowsWorker
@AssistedInject
constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val traktRepository: TraktRepository,
) : BaseWorker(appContext, workerParameters) {
    override val notificationId: Int = NOTIFICATION_ID
    override val contentTitleText: String = "Refreshing Trakt Trending shows"

    override suspend fun doWork(): Result {
        Timber.d("$WORK_NAME: Starting worker.")
        return try {
            Timber.d("$WORK_NAME: Refreshing Trakt trending shows from repository.")
            // Pass forceRefresh = true because the worker is triggered when an update is desired
            traktRepository.refreshTraktTrendingShows(forceRefresh = true)
            Timber.i("$WORK_NAME: Worker completed successfully.")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "$WORK_NAME: Worker failed.")
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "RefreshTraktTrendingShowsWorker"
        private const val NOTIFICATION_ID = 1008
    }
}
