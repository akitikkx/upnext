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

package com.theupnextapp.datasource

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.database.UpnextDao
import com.theupnextapp.network.TvMazeService
import com.theupnextapp.repository.BaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber

abstract class BaseTraktDataSource(
    upnextDao: UpnextDao,
    tvMazeService: TvMazeService,
    private val firebaseCrashlytics: FirebaseCrashlytics,
) : BaseRepository(upnextDao, tvMazeService) {
    protected suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
        return withContext(Dispatchers.IO) {
            try {
                Result.success(apiCall())
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val specificMessage = "HTTP ${e.code()}: ${e.message()}. Body: $errorBody"
                logTraktException(specificMessage, e)
                Result.failure(e)
            } catch (e: Exception) {
                logTraktException("API call failed: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    protected fun logTraktException(
        message: String,
        throwable: Throwable? = null,
    ) {
        if (throwable != null) {
            Timber.e(throwable, "Trakt Error: $message")
            firebaseCrashlytics.recordException(throwable)
        } else {
            Timber.e("Trakt Error: $message")
        }
    }
}
