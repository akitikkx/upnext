package com.theupnextapp.repository

import com.theupnextapp.database.SimklDao
import com.theupnextapp.domain.SimklAccessToken
import com.theupnextapp.network.models.simkl.asDomainModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SimklAuthManager @Inject constructor(
    private val simklDao: SimklDao
) {
    val simklAccessToken: Flow<SimklAccessToken?> =
        simklDao.getSimklAccessData().map {
            it?.let {
                SimklAccessToken(
                    accessToken = it.accessToken,
                    tokenType = it.tokenType,
                    scope = it.scope
                )
            }
        }

    suspend fun disconnect() {
        simklDao.deleteSimklAccessData()
    }
}
