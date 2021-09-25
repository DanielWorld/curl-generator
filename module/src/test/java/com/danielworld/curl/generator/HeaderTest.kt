package com.danielworld.curl.generator

import android.os.Build
import com.danielworld.curl.generator.internal.Header
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
class HeaderTest {

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