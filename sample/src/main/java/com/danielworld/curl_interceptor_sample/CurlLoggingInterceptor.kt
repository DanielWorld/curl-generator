package com.danielworld.curl_interceptor_sample

import android.util.Log
import androidx.annotation.VisibleForTesting
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset

/**
 * Original Code : https://gist.github.com/jgilfelt/42d1c020cc66d3f0a0d7
 * <br>
 * Intercept OkHttp request to log generated cURL
 * <br>
 * Modified by Daniel Park on 2019-12-23
 */
class CurlLoggingInterceptor(private val ENABLE_LOG: Boolean) : Interceptor {
    private val TAG = CurlLoggingInterceptor::class.java.name

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val curlCmd = cURLGenerator(request)

        if (ENABLE_LOG) {
            Log.d(TAG, "╭---- cURL (" + request.url() + ") ------------------------")
            Log.i(TAG, curlCmd)
            Log.d(TAG, "╰---- (copy and paste the above line) ---------------------")
        }
        return chain.proceed(request)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun cURLGenerator(request : Request) : String {

        var compressed = false
        var contentTypeFound = false

        // @namgyu.park (2019-12-23) : In Postman, url must be ahead of -X.
        var curlCmd = "curl \'" + request.url() + "\'"
        curlCmd += " -X " + request.method()

        // 1. Header
        val headers = request.headers()
        var index = 0
        val count = headers.size()
        while (index < count) {
            val name = headers.name(index)
            val value = headers.value(index)
            if ("Accept-Encoding".equals(name, ignoreCase = true) && "gzip".equals(value, ignoreCase = true)) {
                compressed = true
            }
            if ("Content-Type".equals(name, ignoreCase = true)) {
                contentTypeFound = true
            }
            curlCmd += " -H \'$name: $value\'"
            index++
        }

        // 2. Body
        val requestBody = request.body()
        if (requestBody != null) {

            // 1) Decide charset
            var charset = UTF8
            val contentType = requestBody.contentType()

            contentType?.let {
                charset = it.charset(UTF8) ?: UTF8
            }

            // 2) Multipart/form-data
            if (requestBody is MultipartBody) {
                val multipartBody = requestBody
                val boundary = multipartBody.boundary()
                val contentLength = multipartBody.contentLength()
                val parts = multipartBody.parts()

                if (ENABLE_LOG) {
                    Log.v(TAG, "This is multipartBody. boundary : $boundary, contentLength : $contentLength")
                }

                // TODO: @namgyu.park (2019-11-27) : Sorry. We don't support multipart-file upload yet.
                // Forbidden generating cURL if contentLength is larger than 100kb (100 * 1024 byte).
                if (contentLength > 100 * 1024) {
                    if (ENABLE_LOG) {
                        Log.v(TAG, "contentLength is larger than 100kb. Sorry we don't generate cURL!")
                    }
//                    return chain.proceed(request)
                    return "---"
                }

                for (p in parts) {
                    // get Parts name
                    var partsName = ""
                    val h = p.headers()
                    if (h != null) {
                        val contentDisposition = h["Content-Disposition"]
                        if (contentDisposition != null) {
                            if (ENABLE_LOG) {
                                Log.d(TAG, "Content-Disposition: $contentDisposition")
                            }
                            val cd = contentDisposition.split(";").toTypedArray()
                            for (s in cd) {
                                if (s.trim { it <= ' ' }.startsWith("name=")) {
                                    partsName = s.trim { it <= ' ' }.replace("name=", "").replace("\"", "")
                                    break
                                }
                            }
                        }
                    }

                    if (ENABLE_LOG) {
                        Log.d(TAG, "p.name() : $partsName")
                    }

                    // get Parts body
                    val partsRequestBody = p.body()
                    val pb = Buffer()
                    partsRequestBody.writeTo(pb)
                    val partsBody = pb.readString(charset)

                    if (ENABLE_LOG) {
                        Log.d(TAG, "p.body() : $partsBody")
                    }

                    // apply to cURL
                    curlCmd += " -F \'$partsName=$partsBody\'"

                    // Buffer flush
                    pb.flush()
                }
            }
            // 3) Others
            else {
                val contentTypeString: String = contentType?.toString() ?: ""

                if (contentTypeString.contains("application/octet-stream") ) {
                    // it(body) could be unknown binary file.

                    if (ENABLE_LOG) {
                        Log.v(TAG, "\'application/octet-stream\' content type. We customize body() to print log: @attach_your_file_in_postman_or_file_binary_data")
                        Log.v(TAG, "Make sure to find original file and attach it in Postman. or get file's binary data and replace @attach_your_file_in_postman_or_file_binary_data")
                    }

                    // apply to cURL
                    curlCmd += " --data-binary @attach_your_file_in_postman_or_file_binary_data"
                }
                else {
                    // @namgyu.park (2019-11-27) : print all requestBody.
                    val buffer = Buffer()
                    requestBody.writeTo(buffer)

                    val body = buffer.readString(charset)

                    if (ENABLE_LOG) {
                        Log.d(TAG, "body() : $body")
                    }

                    // @namgyu.park (2019-11-27) : If no found contentType, we need to add Content-Type on Header in cURL.
                    if (!contentTypeFound) {

                        if (ENABLE_LOG) {
                            Log.v(TAG, "No found Content-Type. Try to find out...")
                        }

                        // TODO: @namgyu.park (2019-11-27) : We need to figure out a better solution.
                        try {
                            JSONObject(body)
                            curlCmd += " -H " + "\'" + "Content-Type: application/json" + "\'"
                        } catch (e: JSONException) {
                            try {
                                JSONArray(body)
                                curlCmd += " -H " + "\'" + "Content-Type: application/json" + "\'"
                            } catch (ex: JSONException) {
                            }
                        }
                    }

                    // apply to cURL
                    curlCmd += " -d \'$body\'"

                    // Buffer flush
                    buffer.flush()
                }
            }
        }

        if (compressed) {
            curlCmd += " --compressed"
        }

        return curlCmd
    }

    companion object {
        private val UTF8 = Charset.forName("UTF-8")
    }
}