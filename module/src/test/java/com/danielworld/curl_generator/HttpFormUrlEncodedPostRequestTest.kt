package com.danielworld.curl_generator

import android.os.Build
import com.danielworld.curl_generator.internal.CurlGenerator
import okhttp3.FormBody
import okhttp3.Request
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.net.URLEncoder

// For Robolectrics 4.3.x, Android SDK 29 requires Java 9 (have Java 8). so set sdk = 28 immediately
@Config(
    manifest = Config.NONE,
    sdk = [Build.VERSION_CODES.P]
)
// Why use Robolectric ? : Because it contains many mocks of Android class which running on local JVM. (No need Android emulator or Device.// So. Use RobolectricTestRunner.class instead of MockitoJUnitRunner.class. Robolectric handle Android API.
@RunWith(RobolectricTestRunner::class)
class HttpFormUrlEncodedPostRequestTest {

    // key
    private val CONTENT_TYPE = "Content-Type"
    private val ACCEPT_ENCODING = "Accept-Encoding"
    private val AUTHORIZATION = "Authorization"

    private val GRANT_TYPE = "grant_type"
    private val USER_NAME = "username"
    private val PASSWORD = "password"

    // value
    private val mUrl = "https://custom.test.com/hello"
    private val mFormUrlEncodedContentType = "application/x-www-form-urlencoded"
    private val mGzip = "gzip"
    private val mAuthorization = "Bearer SM-nxaYwc_-AXMeGTCHC8WY5QSa3eLTlBx8D0TQlZJA"

    private val mGrantType = "password"
    private val mUserName = "DanielPark!@#$%%^&*()-+"
    private val mPassword = "abc\"d\"e123+="

    @Before
    fun setUp() {
        // initialize mock, before executing each test
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun testFormUrlEncoded() {

        val requestBody = FormBody.Builder()
            // Encoded 된 데이터가 아님.
//            .addEncoded(GRANT_TYPE, mGrantType)
//            .addEncoded(USER_NAME, mUserName)
//            .addEncoded(PASSWORD, mPassword)

                // add 시 알아서 encoded 해줌
            .add(GRANT_TYPE, mGrantType)
            .add(USER_NAME, mUserName)
            .add(PASSWORD, mPassword)
            .build()

        val request = Request.Builder()
            .url(mUrl)
            .post(requestBody)
            .build()

        val expected = "curl -X POST -H \"$CONTENT_TYPE:$mFormUrlEncodedContentType\" --data-urlencode \"$GRANT_TYPE=${URLEncoder.encode(mGrantType)}\" --data-urlencode \"$USER_NAME=${URLEncoder.encode(mUserName)}\" --data-urlencode \"$PASSWORD=${URLEncoder.encode(mPassword)}\" \"$mUrl\""

        val actual = CurlGenerator(request, CurlInterceptor.Delimiter.EMPTY_SPACE).build()

        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testGzipFormUrlEncoded() {

        val requestBody = FormBody.Builder()
            // add 시 알아서 encoded 해줌
            .add(GRANT_TYPE, mGrantType)
            .add(USER_NAME, mUserName)
            .add(PASSWORD, mPassword)
            .build()

        val request = Request.Builder()
            .url(mUrl)
            .addHeader(ACCEPT_ENCODING, mGzip)
            .post(requestBody)
            .build()

        val expected = "curl -X POST -H \"$ACCEPT_ENCODING:$mGzip\" -H \"$CONTENT_TYPE:$mFormUrlEncodedContentType\" --data-urlencode \"$GRANT_TYPE=${URLEncoder.encode(mGrantType)}\" --data-urlencode \"$USER_NAME=${URLEncoder.encode(mUserName)}\" --data-urlencode \"$PASSWORD=${URLEncoder.encode(mPassword)}\" --compressed \"$mUrl\""

        val actual = CurlGenerator(request, CurlInterceptor.Delimiter.EMPTY_SPACE).build()

        Assert.assertEquals(expected, actual)
    }
}