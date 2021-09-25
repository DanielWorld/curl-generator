package com.danielworld.curl.generator

import android.os.Build
import com.danielworld.curl.generator.CurlInterceptor
import com.danielworld.curl.generator.internal.CurlGenerator
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(
    manifest = Config.NONE,
    sdk = [Build.VERSION_CODES.R]
)
@RunWith(RobolectricTestRunner::class)
class HttpPostRequestTest {

    // key
    private val CONTENT_TYPE = "Content-Type"
    private val ACCEPT_ENCODING = "Accept-Encoding"
    private val AUTHORIZATION = "Authorization"

    // value
    private val mUrl = "https://custom.test.com/hello"
    private val mJsonContentType = "application/json"
    private val mGzip = "gzip"
    private val mAuthorization = "Bearer SM-nxaYwc_-AXMeGTCHC8WY5QSa3eLTlBx8D0TQlZJA"

    private lateinit var closeable : AutoCloseable

    @Before
    fun setUp() {
        // initialize mock, before executing each test
        closeable = MockitoAnnotations.openMocks(this)
    }

    @After
    fun shutdown() {
        closeable.close()
    }

    @Test
    fun testJsonRequestPost() {
        val body = JSONObject()
        body.put("UserId", "19d920f")

        val contentType = mJsonContentType.toMediaTypeOrNull()
        val requestBody = body.toString().toRequestBody(contentType)

        val request = Request.Builder()
            .url(mUrl)
            .addHeader(AUTHORIZATION, mAuthorization)
            .post(requestBody)
            .build()

        val expected = "curl -X POST -H \"$AUTHORIZATION:$mAuthorization\" -H \"$CONTENT_TYPE:$mJsonContentType; charset=utf-8\" -d '$body' \"$mUrl\""

        val actual = CurlGenerator(
            request,
            CurlInterceptor.Delimiter.EMPTY_SPACE
        ).build()

        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testGzipJsonRequestPost() {
        val body = JSONObject()
        body.put("UserId", "19d920f")

        val contentType = mJsonContentType.toMediaTypeOrNull()
        val requestBody = body.toString().toRequestBody(contentType)

        val request = Request.Builder()
            .url(mUrl)
            .addHeader(ACCEPT_ENCODING, mGzip)
            .addHeader(AUTHORIZATION, mAuthorization)
            .post(requestBody)
            .build()

        val expected = "curl -X POST -H \"$ACCEPT_ENCODING:$mGzip\" -H \"$AUTHORIZATION:$mAuthorization\" -H \"$CONTENT_TYPE:$mJsonContentType; charset=utf-8\" -d '$body' --compressed \"$mUrl\""

        val actual = CurlGenerator(
            request,
            CurlInterceptor.Delimiter.EMPTY_SPACE
        ).build()

        Assert.assertEquals(expected, actual)
    }
}