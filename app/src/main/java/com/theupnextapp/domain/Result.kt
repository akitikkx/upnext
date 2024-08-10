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
import java.io.IOException

private val moshi = Moshi.Builder().build()

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class ApiError(val code: Int, val errorResponse: ErrorResponse?) : Result<Nothing>()
    data class ParseError(val exception: Exception) : Result<Nothing>()
    data class UnknownError(val exception: Throwable) : Result<Nothing>()
    data class Loading(val isLoading: Boolean) : Result<Nothing>()
    data object NetworkError : Result<Nothing>()
}

suspend fun <T> safeApiCall(dispatcher: CoroutineDispatcher, apiCall: suspend () -> T): Result<T> {
    return withContext(dispatcher) {
        try {
            Result.Success(apiCall())
        } catch (throwable: Throwable) {
            when (throwable) {
                is IOException -> Result.NetworkError
                is HttpException -> {
                    val errorCode = throwable.code()
                    val errorResponse = try {
                        parseException(throwable)
                    } catch (e: Exception) {
                        return@withContext Result.ParseError(e)
                    }
                    Result.ApiError(errorCode, errorResponse)
                }
                else -> Result.UnknownError(throwable)
            }
        }
    }
}

private fun parseException(throwable: HttpException): ErrorResponse? {
    return throwable.response()?.errorBody()?.source()?.let {
        moshi.adapter(ErrorResponse::class.java).fromJson(it)
    }
}
