package com.danielworld.curl.generator

import android.os.Build
import com.danielworld.curl.generator.CurlInterceptor
import com.danielworld.curl.generator.internal.CurlGenerator
import okhttp3.FormBody
import okhttp3.Request
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.net.URLEncoder

@Config(
    manifest = Config.NONE,
    sdk = [Build.VERSION_CODES.R]
)
@RunWith(RobolectricTestRunner::class)
class DelimiterTest {

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
    private val mPassword = "abc\"d\"e123+"

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
    fun testBackslashNewLine() {

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

        val expected = "curl \\\n" +
                "-X POST \\\n" +
                "-H \"$ACCEPT_ENCODING:$mGzip\" \\\n" +
                "-H \"$CONTENT_TYPE:$mFormUrlEncodedContentType\" \\\n" +
                "--data-urlencode \"$GRANT_TYPE=${URLEncoder.encode(mGrantType, "UTF-8")}\" \\\n" +
                "--data-urlencode \"$USER_NAME=${URLEncoder.encode(mUserName, "UTF-8")}\" \\\n" +
                "--data-urlencode \"$PASSWORD=${URLEncoder.encode(mPassword, "UTF-8")}\" \\\n" +
                "--compressed \\\n" +
                "\"$mUrl\""

        val actual = CurlGenerator(
            request,
            CurlInterceptor.Delimiter.BACKSLASH_NEW_LINE
        ).build()

        Assert.assertEquals(expected, actual)
    }
}