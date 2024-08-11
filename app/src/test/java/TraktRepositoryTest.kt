/*
 * MIT License
 *
 * Copyright (c) 2022 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.BuildConfig
import com.theupnextapp.database.TraktDao
import com.theupnextapp.database.UpnextDao
import com.theupnextapp.domain.TraktAccessToken
import com.theupnextapp.network.TraktService
import com.theupnextapp.network.TvMazeService
import com.theupnextapp.network.models.trakt.NetworkTraktAccessTokenRequest
import com.theupnextapp.network.models.trakt.NetworkTraktAccessTokenResponse
import com.theupnextapp.network.models.trakt.NetworkTraktRevokeAccessTokenRequest
import com.theupnextapp.network.models.trakt.NetworkTraktRevokeAccessTokenResponse
import com.theupnextapp.network.models.trakt.asDatabaseModel
import com.theupnextapp.repository.TraktRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import kotlinx.coroutines.CompletableDeferred
import org.mockito.ArgumentMatchers.argThat
import org.mockito.ArgumentMatchers.eq

/*
 * MIT License
 *
 * Copyright (c) 2022 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class TraktRepositoryTest {

    private lateinit var traktRepository: TraktRepository

    private val upnextDao = mock(UpnextDao::class.java)
    private val traktDao = mock(TraktDao::class.java)
    private val tvMazeService = mock(TvMazeService::class.java)
    private val traktService = mock(TraktService::class.java)
    private val firebaseCrashlytics = mock(FirebaseCrashlytics::class.java)

    @Before
    fun setup() {
        traktRepository = TraktRepository(
            upnextDao, traktDao, tvMazeService, traktService, firebaseCrashlytics
        )
    }

    @Test
    fun `getTraktAccessToken success`() = runTest {
        val code = "test_code"
        val request = NetworkTraktAccessTokenRequest(
            code = code,
            client_id = BuildConfig.TRAKT_CLIENT_ID,
            client_secret = BuildConfig.TRAKT_CLIENT_SECRET,
            redirect_uri = BuildConfig.TRAKT_REDIRECT_URI,
            grant_type = "authorization_code"
        )
        val response = NetworkTraktAccessTokenResponse(
            access_token = "access_token",
            token_type = "token_type",
            expires_in = 123,
            refresh_token = "refresh_token",
            scope = "scope",
            created_at = 456
        )
        val deferredResponse = CompletableDeferred(response)
        `when`(traktService.getAccessTokenAsync(request)).thenReturn(deferredResponse)

        traktRepository.getTraktAccessToken(code)

        verify(traktService).getAccessTokenAsync(request)
        verify(traktDao).deleteTraktAccessData()
        verify(traktDao).insertAllTraktAccessData(response.asDatabaseModel())
    }

    @Test
    fun `revokeTraktAccessToken success`() = runTest {
        val accessToken = TraktAccessToken(
            access_token = "access_token",
            token_type = "token_type",
            expires_in = 123,
            refresh_token = "refresh_token",
            scope = "scope",
            created_at = 456
        )

        val response = NetworkTraktRevokeAccessTokenResponse()
        val deferredResponse = CompletableDeferred(response)

        `when`(
            traktService.revokeAccessTokenAsync(
                NetworkTraktRevokeAccessTokenRequest(
                    client_id = BuildConfig.TRAKT_CLIENT_ID,
                    client_secret = BuildConfig.TRAKT_CLIENT_SECRET,
                    token = accessToken.access_token!!
                )
            )
        ).thenReturn(deferredResponse)

        traktRepository.revokeTraktAccessToken(accessToken)

        verify(traktService).revokeAccessTokenAsync(
            NetworkTraktRevokeAccessTokenRequest(
                client_id = BuildConfig.TRAKT_CLIENT_ID,
                client_secret = BuildConfig.TRAKT_CLIENT_SECRET,
                token = accessToken.access_token!!
            )
        )
        verify(traktDao).deleteTraktAccessData()
    }

}