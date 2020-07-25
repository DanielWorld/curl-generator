package com.danielworld.curl_generator

import android.os.Build
import android.os.Environment
import com.danielworld.curl.generator.CurlInterceptor
import com.danielworld.curl.generator.internal.CurlGenerator
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

// For Robolectrics 4.3.x, Android SDK 29 requires Java 9 (have Java 8). so set sdk = 28 immediately
@Config(
    manifest = Config.NONE,
    sdk = [Build.VERSION_CODES.P]
)
// Why use Robolectric ? : Because it contains many mocks of Android class which running on local JVM. (No need Android emulator or Device.// So. Use RobolectricTestRunner.class instead of MockitoJUnitRunner.class. Robolectric handle Android API.
@RunWith(RobolectricTestRunner::class)
class HttpFileUploadPostRequestTest  {

    private val mUrl = "https://custom.test.com/hello"

    @Before
    fun setUp() {
        // initialize mock, before executing each test
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun testUploadImageFileWithMultipart() {

        val file = File(Environment.DIRECTORY_DOWNLOADS, "test_image.jpg")

        println("file path : ${file.absolutePath}")

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("title", "file upload test")
            .addFormDataPart("file", file.name, RequestBody.create("application/octet-stream".toMediaTypeOrNull(), file))
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

        val requestBody = RequestBody.create("application/octet-stream".toMediaTypeOrNull(), file)

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