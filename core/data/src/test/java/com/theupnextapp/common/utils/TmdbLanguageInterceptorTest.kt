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

class TmdbLanguageInterceptorTest {

    @Mock
    private lateinit var mockChain: Interceptor.Chain

    @Captor
    private lateinit var requestCaptor: ArgumentCaptor<Request>

    private lateinit var interceptor: TmdbLanguageInterceptor

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        interceptor = TmdbLanguageInterceptor()
    }

    @Test
    fun `test interceptor adds api_key and language query parameters`() {
        // Arrange
        val originalRequest = Request.Builder()
            .url("https://api.themoviedb.org/3/movie/popular")
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
            // Verify original method is maintained
            assertEquals("GET", capturedRequest.method)
            
            // Verify the URL query parameters
            val capturedUrl = capturedRequest.url
            assertEquals(BuildConfig.TMDB_ACCESS_TOKEN, capturedUrl.queryParameter("api_key"))
            assertEquals(Locale.getDefault().toLanguageTag(), capturedUrl.queryParameter("language"))
            
            // Verify original path is maintained
            assertEquals(listOf("3", "movie", "popular"), capturedUrl.pathSegments)
        })
    }
}
