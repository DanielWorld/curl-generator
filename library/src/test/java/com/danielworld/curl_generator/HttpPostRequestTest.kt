package com.danielworld.curl_generator

import android.os.Build
import com.danielworld.curl_generator.internal.CurlGenerator
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// For Robolectrics 4.3.x, Android SDK 29 requires Java 9 (have Java 8). so set sdk = 28 immediately
@Config(
    manifest = Config.NONE,
    sdk = [Build.VERSION_CODES.P]
)
// Why use Robolectric ? : Because it contains many mocks of Android class which running on local JVM. (No need Android emulator or Device.// So. Use RobolectricTestRunner.class instead of MockitoJUnitRunner.class. Robolectric handle Android API.
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

    @Before
    fun setUp() {
        // initialize mock, before executing each test
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun testJsonRequestPost() {
        val body = JSONObject()
        body.put("UserId", "19d920f")

        val contentType = mJsonContentType.toMediaTypeOrNull()
        val requestBody = RequestBody.create(contentType, body.toString())

        val request = Request.Builder()
            .url(mUrl)
            .addHeader(AUTHORIZATION, mAuthorization)
            .post(requestBody)
            .build()

        val expected = "curl -X POST -H \"$AUTHORIZATION:$mAuthorization\" -H \"$CONTENT_TYPE:$mJsonContentType; charset=utf-8\" -d '$body' \"$mUrl\""

        val actual = CurlGenerator(request, CurlInterceptor.Delimiter.EMPTY_SPACE).build()

        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testGzipJsonRequestPost() {
        val body = JSONObject()
        body.put("UserId", "19d920f")

        val contentType = mJsonContentType.toMediaTypeOrNull()
        val requestBody = RequestBody.create(contentType, body.toString())

        val request = Request.Builder()
            .url(mUrl)
            .addHeader(ACCEPT_ENCODING, mGzip)
            .addHeader(AUTHORIZATION, mAuthorization)
            .post(requestBody)
            .build()

        val expected = "curl -X POST -H \"$ACCEPT_ENCODING:$mGzip\" -H \"$AUTHORIZATION:$mAuthorization\" -H \"$CONTENT_TYPE:$mJsonContentType; charset=utf-8\" -d '$body' --compressed \"$mUrl\""

        val actual = CurlGenerator(request, CurlInterceptor.Delimiter.EMPTY_SPACE).build()

        Assert.assertEquals(expected, actual)
    }
}