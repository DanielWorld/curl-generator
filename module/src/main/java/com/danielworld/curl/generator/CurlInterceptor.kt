package com.danielworld.curl.generator

import android.util.Log
import com.danielworld.curl.generator.internal.CurlGenerator
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response


class CurlInterceptor(private val enableLog: Boolean,  private val delimiter: String) : Interceptor  {
    constructor(enableLog: Boolean) : this(enableLog,
        Delimiter.BACKSLASH_NEW_LINE
    )

    private val TAG = CurlInterceptor::class.simpleName

    // 중첩 클래스 (내부 클래스 x)
    class Delimiter {
        companion object {
            const val EMPTY_SPACE = " "
            const val BACKSLASH_NEW_LINE = " \\\n"
        }
    }

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

    private fun printCurl(curl: String) {
        if (enableLog) {
            Log.d(TAG, "╭---- cURL -----------------------------------------------")
            Log.i(TAG, curl)
            Log.d(TAG, "╰---- (copy and paste the above line) ---------------------")
        }
    }
}