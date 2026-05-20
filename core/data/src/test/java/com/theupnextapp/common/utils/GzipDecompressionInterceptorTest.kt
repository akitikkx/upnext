package com.theupnextapp.common.utils

import com.theupnextapp.di.GzipDecompressionInterceptor
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

class GzipDecompressionInterceptorTest {

    @Mock
    private lateinit var mockChain: Interceptor.Chain

    private lateinit var interceptor: GzipDecompressionInterceptor

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        interceptor = GzipDecompressionInterceptor()
    }

    @Test
    fun `test interceptor decompresses gzipped response and removes headers`() {
        // Arrange
        val originalText = "Hello, world! This is a decompressed response."
        
        // Gzip the text
        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).use { gzos ->
            gzos.write(originalText.toByteArray())
        }
        val gzippedBytes = bos.toByteArray()

        val request = Request.Builder()
            .url("https://api.example.com/data")
            .build()

        val body = gzippedBytes.toResponseBody("application/json".toMediaType())
        val mockResponse = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .header("Content-Encoding", "gzip")
            .header("Content-Length", gzippedBytes.size.toString())
            .body(body)
            .build()

        `when`(mockChain.request()).thenReturn(request)
        `when`(mockChain.proceed(org.mockito.kotlin.any())).thenReturn(mockResponse)

        // Act
        val response = interceptor.intercept(mockChain)

        // Assert
        assertNotNull(response)
        assertEquals(null, response.header("Content-Encoding"))
        assertEquals(null, response.header("Content-Length"))
        
        val responseBodyString = response.body.string()
        assertEquals(originalText, responseBodyString)
    }

    @Test
    fun `test interceptor handles trailing bytes in gzipped response gracefully`() {
        // Arrange
        val originalText = "Lenient Gzip Decompression works!"
        
        // Gzip the text
        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).use { gzos ->
            gzos.write(originalText.toByteArray())
        }
        // Append trailing newline bytes (this would normally fail Okio's GzipSource.read check)
        val gzippedBytes = bos.toByteArray() + "\n\n".toByteArray()

        val request = Request.Builder()
            .url("https://api.example.com/data")
            .build()

        val body = gzippedBytes.toResponseBody("application/json".toMediaType())
        val mockResponse = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .header("Content-Encoding", "gzip")
            .header("Content-Length", gzippedBytes.size.toString())
            .body(body)
            .build()

        `when`(mockChain.request()).thenReturn(request)
        `when`(mockChain.proceed(org.mockito.kotlin.any())).thenReturn(mockResponse)

        // Act
        val response = interceptor.intercept(mockChain)

        // Assert
        assertNotNull(response)
        assertEquals(null, response.header("Content-Encoding"))
        
        val responseBodyString = response.body.string()
        assertEquals(originalText, responseBodyString)
    }

    @Test
    fun `test interceptor passes non-gzipped response unmodified`() {
        // Arrange
        val originalText = "Plain text response."
        val request = Request.Builder()
            .url("https://api.example.com/data")
            .build()

        val body = originalText.toResponseBody("text/plain".toMediaType())
        val mockResponse = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .header("Content-Type", "text/plain")
            .body(body)
            .build()

        `when`(mockChain.request()).thenReturn(request)
        `when`(mockChain.proceed(org.mockito.kotlin.any())).thenReturn(mockResponse)

        // Act
        val response = interceptor.intercept(mockChain)

        // Assert
        assertNotNull(response)
        assertEquals("text/plain", response.header("Content-Type"))
        assertEquals(null, response.header("Content-Encoding"))
        
        val responseBodyString = response.body.string()
        assertEquals(originalText, responseBodyString)
    }
}
