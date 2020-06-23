package com.danielworld.curl_interceptor.internal

import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import okio.Buffer
import java.io.IOException
import java.nio.charset.Charset
import java.util.*

internal class CurlGenerator
constructor(private val request: Request, delimiter : String){

    private val url = request.url.toString()
    private val method : String = request.method
    private var contentType: String? = null
    private var body : String? = null
    private val headers: MutableList<Header> = LinkedList()
    private val delimiter: String = delimiter

    companion object {
        private val FORMAT_URL = "\"%1\$s\""
        private val FORMAT_METHOD = "-X %1\$s"
        private val FORMAT_HEADER = "-H \"%1\$s:%2\$s\""
        private val FORMAT_BODY = "-d '%1\$s'"
        private val CONTENT_TYPE = "Content-Type"
    }

    init {
        val requestBody = request.body
        requestBody?.let { rb ->
            this.contentType = getContentType(rb)
            this.body = getBodyAsString(rb)
        }

        val headers = request.headers
        headers.map {
            val header =
                Header(
                    it.first,
                    it.second
                )
            this.headers.add(header)
        }
    }

    fun build() : String {
        val parts: MutableList<String> = ArrayList()
        parts.add("curl")
        parts.add(String.format(FORMAT_URL, url)) // In Postman, url must be ahead of -X.
        parts.add(String.format(FORMAT_METHOD, method.toUpperCase()))

        for (header in headers) {
            val headerPart = String.format(FORMAT_HEADER, header.name(), header.value())
            parts.add(headerPart)
        }

        if (contentType != null && !containsName(CONTENT_TYPE, headers)) {
            parts.add(java.lang.String.format(
                FORMAT_HEADER,
                CONTENT_TYPE, contentType))
        }

        if (body != null) {
            parts.add(java.lang.String.format(FORMAT_BODY, body))
        }

        return join(delimiter, parts)
    }

    // ------------------------------------------------------------------------------------
    // private methods
    // ------------------------------------------------------------------------------------

    private fun getContentType(body: RequestBody): String? {
        return body.contentType()?.toString()
    }

    private fun getBodyAsString(body: RequestBody): String? {
        return try {
            val sink = Buffer()
            val mediaType: MediaType? = body.contentType()
            val charset: Charset = getCharset(mediaType)
            body.writeTo(sink);
            sink.readString(charset)
        } catch (e: IOException) {
            "Error while reading body: $e"
        }
    }

    private fun getCharset(mediaType: MediaType?): Charset {
        return mediaType?.charset(Charset.defaultCharset()) ?: Charset.defaultCharset()
    }

    private fun containsName(
        name: String,
        headers: List<Header>
    ): Boolean {
        for (header in headers) {
            if (header.name() == name) {
                return true
            }
        }
        return false
    }

    /**
     * Returns a string containing the tokens joined by delimiters.
     * @param tokens an array objects to be joined. Strings will be formed from
     * the objects by calling object.toString().
     */
    private fun join(
        delimiter: CharSequence?,
        tokens: Iterable<*>
    ): String {
        val sb = StringBuilder()
        var firstTime = true
        for (token in tokens) {
            if (firstTime) {
                firstTime = false
            } else {
                sb.append(delimiter)
            }
            sb.append(token)
        }
        return sb.toString()
    }
}