package com.danielworld.curl_interceptor

import android.util.Log
import com.danielworld.curl_interceptor.internal.CurlGenerator
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.net.HttpURLConnection


class CurlInterceptor(private val enableLog: Boolean,  private val delimiter: String) : Interceptor  {
    constructor(enableLog: Boolean) : this(enableLog, " ")

    private val TAG = CurlInterceptor::class.simpleName

    /**
     * Support OkHttp3
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()

        val copy: Request = request.newBuilder().build()
        val curl: String = CurlGenerator(
            copy,
            delimiter
        ).build()

        printCurl(curl)

        return chain.proceed(request)
    }

    /**
     * TODO: Support HttpUrlConnection
     */
    fun intercept(connection: HttpURLConnection) {
        // TODO:
    }

    private fun printCurl(curl: String) {
        if (enableLog) {
            Log.d(TAG, "╭---- cURL -----------------------------------------------")
            Log.i(TAG, curl)
            Log.d(TAG, "╰---- (copy and paste the above line) ---------------------")
        }
    }
}