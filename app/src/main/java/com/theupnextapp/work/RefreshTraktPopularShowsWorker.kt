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
 * A [androidx.work.Worker] that fetches popular shows from Trakt.tv and stores them
 * in the local database.
 * This worker is scheduled to run periodically using [androidx.work.PeriodicWorkRequest].
 * It extends [BaseWorker] to handle common worker functionalities like showing notifications.
 *
 * @param appContext The application context.
 * @param workerParameters Parameters for the worker.
 * @param traktRepository The repository for accessing Trakt.tv data.
 */
@HiltWorker
class RefreshTraktPopularShowsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val traktRepository: TraktRepository
) : BaseWorker(appContext, workerParameters) {

    override val notificationId: Int = NOTIFICATION_ID
    override val contentTitleText: String = "Refreshing Trakt Popular shows"

    override suspend fun doWork(): Result {
        Timber.d("$WORK_NAME: Starting worker.")
        return try {
            Timber.d("$WORK_NAME: Refreshing Trakt popular shows from repository.")
            // Pass forceRefresh = true because the worker is triggered when an update is desired
            traktRepository.refreshTraktPopularShows(forceRefresh = true)
            Timber.i("$WORK_NAME: Worker completed successfully.")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "$WORK_NAME: Worker failed.")
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "RefreshTraktPopularShowsWorker"
        private const val NOTIFICATION_ID = 1007
    }
}
