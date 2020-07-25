package com.danielworld.curl_generator.model

import android.os.Build
import com.danielworld.curl.generator.internal.Header
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
class HeaderTest {

    @Before
    fun setUp() {
        // initialize mock, before executing each test
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun testHeaderEqualsSameNameDifferentValue() {

        val header1 =
            Header("LINE", "ab22lf")
        val header2 =
            Header("LINE", "bd2f2f")

        Assert.assertNotEquals(header1, header2)
    }

    @Test
    fun testHeaderEqualsSameNameSameValue() {

        val header1 =
            Header("LINE", "ab22lf")
        val header2 =
            Header("LINE", "ab22lf")

        Assert.assertEquals(header1, header2)
    }

    @Test
    fun testHeaderEqualsDifferentNameSameValue() {

        val header1 =
            Header("NAVER", "ab22lf")
        val header2 =
            Header("LINE", "ab22lf")

        Assert.assertNotEquals(header1, header2)
    }

    @Test
    fun testHeaderEqualsDifferentAll() {

        val header1 =
            Header("NAVER", "bd2lf")
        val header2 =
            Header("LINE", "ab22lf")

        Assert.assertNotEquals(header1, header2)
    }
}