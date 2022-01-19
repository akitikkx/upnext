package com.theupnextapp.repository

import com.theupnextapp.domain.Result
import com.theupnextapp.domain.ShowSearch
import com.theupnextapp.domain.safeApiCall
import com.theupnextapp.network.TvMazeService
import com.theupnextapp.network.models.tvmaze.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class SearchRepository(
    private val tvMazeService: TvMazeService,
) {

    suspend fun getShowSearchResults(name: String?): Flow<Result<List<ShowSearch>>?> {
        return flow {
            if (name.isNullOrEmpty().not()) {
                emit(Result.Loading(true))
                val response = name?.let {
                    safeApiCall(Dispatchers.IO) {
                        tvMazeService.getSuggestionListAsync(it).await().asDomainModel()
                    }
                }
                emit(Result.Loading(false))
                emit(response)
            } else {
                emit(Result.Success(emptyList()))
            }
        }.flowOn(Dispatchers.IO)
    }
}