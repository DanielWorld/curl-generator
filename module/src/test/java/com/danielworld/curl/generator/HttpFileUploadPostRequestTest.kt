package com.danielworld.curl.generator

import android.os.Build
import android.os.Environment
import com.danielworld.curl.generator.CurlInterceptor
import com.danielworld.curl.generator.internal.CurlGenerator
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@Config(
    manifest = Config.NONE,
    sdk = [Build.VERSION_CODES.R]
)
@RunWith(RobolectricTestRunner::class)
class HttpFileUploadPostRequestTest  {

    private val mUrl = "https://custom.test.com/hello"

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
    fun testUploadImageFileWithMultipart() {

        val file = File(Environment.DIRECTORY_DOWNLOADS, "test_image.jpg")

        println("file path : ${file.absolutePath}")

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("title", "file upload test")
            .addFormDataPart("file", file.name,
                file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url(mUrl)
            .post(requestBody)
            .build()

        val expected = "curl \\\n" +
                "-X POST \\\n" +
                "-H \"Content-Type:${MultipartBody.FORM.toString()}; boundary=${requestBody.boundary}\" \\\n" +
                "-F \"title=file upload test\" \\\n" +
                "-F \"file=@${file.name}\" \\\n" +
                "\"$mUrl\""

        val actual = CurlGenerator(
            request,
            CurlInterceptor.Delimiter.BACKSLASH_NEW_LINE
        ).build()

        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testUploadBinaryFile() {
        val file = File(Environment.DIRECTORY_DOWNLOADS, "clova_20200624_154227_desk_encoded.mp4")

        println("file path : ${file.absolutePath}")

        val requestBody = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(mUrl)
            .post(requestBody)
            .build()

        val expected = "curl \\\n" +
                "-X POST \\\n" +
                "-H \"Content-Type:application/octet-stream\" \\\n" +
                "--data-binary @filename \\\n" +
                "\"$mUrl\""

        val actual = CurlGenerator(
            request,
            CurlInterceptor.Delimiter.BACKSLASH_NEW_LINE
        ).build()

        Assert.assertEquals(expected, actual)
    }
}