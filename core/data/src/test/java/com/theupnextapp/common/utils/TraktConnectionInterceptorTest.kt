package com.theupnextapp.common.utils

import com.theupnextapp.core.data.BuildConfig
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.util.Locale

class TraktConnectionInterceptorTest {

    @Mock
    private lateinit var mockChain: Interceptor.Chain

    @Captor
    private lateinit var requestCaptor: ArgumentCaptor<Request>

    private lateinit var interceptor: TraktConnectionInterceptor

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        interceptor = TraktConnectionInterceptor()
    }

    @Test
    fun `test interceptor adds default trakt headers and accept-language`() {
        // Arrange
        val originalRequest = Request.Builder()
            .url("https://api.trakt.tv/shows/trending")
            // Intentionally add a dummy method to ensure it's preserved
            .post(okhttp3.RequestBody.create(null, ""))
            .build()

        val mockResponse = Response.Builder()
            .request(originalRequest)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .build()

        `when`(mockChain.request()).thenReturn(originalRequest)
        `when`(mockChain.proceed(org.mockito.kotlin.any())).thenReturn(mockResponse)

        // Act
        val response = interceptor.intercept(mockChain)

        // Assert
        assertNotNull(response)

        // Capture the request passed to proceed()
        verify(mockChain).proceed(org.mockito.kotlin.check { capturedRequest ->
            // Verify the HTTP method and body are preserved
            assertEquals("POST", capturedRequest.method)
            assertNotNull(capturedRequest.body)
    
            // Verify headers are correctly added
            assertEquals("2", capturedRequest.header("trakt-api-version"))
            assertEquals(BuildConfig.TRAKT_CLIENT_ID, capturedRequest.header("trakt-api-key"))
            assertEquals("application/json", capturedRequest.header("Content-Type"))
            assertEquals(Locale.getDefault().language, capturedRequest.header("Accept-Language"))
        })
    }
}
