package com.danielworld.curl_generator

import android.os.Build
import com.danielworld.curl_generator.internal.CurlGenerator
import okhttp3.Request
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
class HttpGetRequestTest {

    // key
    private val USER_AGENT = "User-Agent"
    private val CONTENT_TYPE = "Content-Type"
    private val AUTHORIZATION = "Authorization"

    // value
    private val mUrl = "https://custom.test.com/hello?id=dk13l11"
    private val mWebUserAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0 Safari/605.1.15"
    private val mJSonContentType = "application/json"
    private val mAuthorization = "Bearer SM-nxaYwc_-AXMeGTCHC8WY5QSa3eLTlBx8D0TQlZJA"


    @Before
    fun setUp() {
        // initialize mock, before executing each test
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun testWithoutHeaderRequestGet() {

        val request = Request.Builder()
            .url(mUrl)
            .get()
            .build()

        val expected =
            "curl -X GET \"$mUrl\""

        val actual = CurlGenerator(request, CurlInterceptor.Delimiter.EMPTY_SPACE).build()

        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testOneHeaderRequestGet() {

        val request = Request.Builder()
            .url(mUrl)
            .addHeader(USER_AGENT, mWebUserAgent)
            .get()
            .build()

        val expected =
            "curl -X GET -H \"$USER_AGENT:$mWebUserAgent\" \"$mUrl\""

        val actual = CurlGenerator(request, CurlInterceptor.Delimiter.EMPTY_SPACE).build()

        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testHeadersRequestGet() {

        val request = Request.Builder()
            .url(mUrl)
            .addHeader(USER_AGENT, mWebUserAgent)
            .addHeader(AUTHORIZATION, mAuthorization)
            .get()
            .build()

        val expected =
            "curl -X GET -H \"$USER_AGENT:$mWebUserAgent\" -H \"$AUTHORIZATION:$mAuthorization\" \"$mUrl\""

        val actual = CurlGenerator(request, CurlInterceptor.Delimiter.EMPTY_SPACE).build()

        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testSameHeadersRequestGet() {
        val request = Request.Builder()
            .url(mUrl)
            .addHeader(USER_AGENT, mWebUserAgent)
            .addHeader(USER_AGENT, mWebUserAgent)
            .get()
            .build()

        // @namgyu.park (2020-06-24) : 중복된 Header 값도 Http request 에서는 허용함.
        val expected =
            "curl -X GET -H \"$USER_AGENT:$mWebUserAgent\" -H \"$USER_AGENT:$mWebUserAgent\" \"$mUrl\""

        val actual = CurlGenerator(request, CurlInterceptor.Delimiter.EMPTY_SPACE).build()

        Assert.assertEquals(expected, actual)
    }
}