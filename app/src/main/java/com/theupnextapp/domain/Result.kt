package com.theupnextapp.domain

import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()

    data class GenericError(val code: Int? = null, val error: ErrorResponse? = null) :
        Result<Nothing>()

    object NetworkError : Result<Nothing>()

    object Loading : Result<Nothing>()
}

suspend fun <T> safeApiCall(dispatcher: CoroutineDispatcher, apiCall: suspend () -> T): Result<T> {
    return withContext(dispatcher) {
        try {
            Result.Success(apiCall.invoke())
        } catch (throwable: Throwable) {
            when (throwable) {
                is IOException -> Result.NetworkError
                is HttpException -> {
                    val errorCode = throwable.code()
                    val errorResponse = parseException(throwable)
                    Result.GenericError(errorCode, errorResponse)
                }
                else -> Result.GenericError(null, null)
            }
        }
    }
}

private fun parseException(throwable: HttpException): ErrorResponse? {
    return try {
        throwable.response()?.errorBody()?.source()?.let {
            val moshiAdapter = Moshi.Builder().build().adapter(ErrorResponse::class.java)
            moshiAdapter.fromJson(it)
        }
    } catch (exception: Exception) {
        null
    }
}
