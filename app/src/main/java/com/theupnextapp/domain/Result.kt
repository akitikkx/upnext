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

package com.theupnextapp.domain

import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()

    data class GenericError(val code: Int? = null, val error: ErrorResponse? = null, val exception: HttpException) : Result<Nothing>() // Modified

    data class NetworkError(val exception: IOException) : Result<Nothing>() // Modified

    data class Loading(val status: Boolean) : Result<Nothing>()

    data class Error(val exception: Throwable? = null, val message: String? = null) : Result<Nothing>()
}

suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher,
    apiCall: suspend () -> T,
): Result<T> {
    return withContext(dispatcher) {
        try {
            Result.Success(apiCall.invoke())
        } catch (throwable: Throwable) {
            when (throwable) {
                is IOException -> Result.NetworkError(throwable) // Modified
                is HttpException -> {
                    val code = throwable.code()
                    val errorResponse = parseHttpException(throwable)
                    Result.GenericError(code, errorResponse, throwable) // Modified
                }
                else -> {
                    Timber.tag("SafeApiCall")
                        .e(throwable, "Unexpected API call failure: ${throwable.localizedMessage}")
                    Result.Error(
                        throwable,
                        throwable.localizedMessage ?: "An unexpected error occurred",
                    )
                }
            }
        }
    }
}

private fun parseHttpException(httpException: HttpException): ErrorResponse? {
    return try {
        httpException.response()?.errorBody()?.source()?.let { errorSource ->
            val moshi = Moshi.Builder().build()
            val adapter = moshi.adapter(ErrorResponse::class.java)
            adapter.fromJson(errorSource)
        }
    } catch (exception: Exception) {
        Timber
            .tag("SafeApiCall")
            .e(exception, "Failed to parse HttpException error body")
        null
    }
}
