package com.theupnextapp.datasource

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.core.data.BuildConfig
import com.theupnextapp.database.SimklDao
import com.theupnextapp.database.UpnextDao
import com.theupnextapp.domain.SimklAccessToken
import com.theupnextapp.network.SimklService
import com.theupnextapp.network.TvMazeService
import com.theupnextapp.network.models.simkl.NetworkSimklAccessTokenRequest
import com.theupnextapp.network.models.simkl.asDatabaseModel
import com.theupnextapp.network.models.simkl.asDomainModel
import javax.inject.Inject

open class SimklAuthDataSource
@Inject
constructor(
    private val simklService: SimklService,
    private val simklDao: SimklDao,
    upnextDao: UpnextDao,
    tvMazeService: TvMazeService,
    firebaseCrashlytics: FirebaseCrashlytics,
) : BaseTraktDataSource(upnextDao, tvMazeService, firebaseCrashlytics) {

    suspend fun getAccessToken(code: String): Result<SimklAccessToken> {
        if (code.isEmpty()) {
            val message = "Attempted to get access token with empty code."
            logTraktException(message) // Reusing logTraktException from base class for simplicity
            return Result.failure(IllegalArgumentException(message))
        }

        return safeApiCall {
            val request =
                NetworkSimklAccessTokenRequest(
                    code = code,
                    clientId = BuildConfig.SIMKL_CLIENT_ID,
                    redirectUri = BuildConfig.SIMKL_REDIRECT_URI,
                    grantType = "authorization_code",
                )
            val response = simklService.getAccessTokenAsync(request).await()
            simklDao.deleteSimklAccessData()
            simklDao.insertAllSimklAccessData(response.asDatabaseModel())
            response.asDomainModel()
        }
    }
}
