package com.danielworld.curl_generator.internal

import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import okio.Buffer
import java.io.IOException
import java.nio.charset.Charset
import java.util.*

/**
 * 0. curl Options : https://curl.haxx.se/docs/manpage.html
 * 1. curl change log : https://curl.haxx.se/changes.html <br>
 * 2. cURL 주요 옵션 : https://www.lesstif.com/software-architect/curl-http-get-post-rest-api-14745703.html
 */
internal class CurlGenerator(private val request: Request, private val delimiter : String) {
    constructor(request: Request) : this (request, " ")


    private val url = request.url.toString()
    private val method : String = request.method
    private var contentType: String? = null
    private var body : String? = null
    private val headers: MutableList<Header> = LinkedList()     // Allowed same name's header like 'Set-Cookie' (https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2)
    private var compressed: Boolean = false

    companion object {
        private const val FORMAT_URL = "\"%1\$s\""
        private const val FORMAT_METHOD = "-X %1\$s"
        private const val FORMAT_HEADER = "-H \"%1\$s:%2\$s\""
        private const val FORMAT_BODY = "-d '%1\$s'"

        private const val CONTENT_TYPE = "Content-Type"
        private const val ACCEPT_ENCODING = "Accept-Encoding"
    }

    init {
        val requestBody = request.body
        requestBody?.let { rb ->
            this.contentType = getContentType(rb)
            this.body = getBodyAsString(rb)
        }

        val headers = request.headers
        headers.map {
            if (ACCEPT_ENCODING.equals(it.first, ignoreCase = true) && "gzip".equals(it.second, ignoreCase = true)) {
                compressed = true
            }
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
        parts.add(String.format(FORMAT_METHOD, method.toUpperCase()))

        for (header in headers) {
            val headerPart = String.format(FORMAT_HEADER, header.name(), header.value())
            parts.add(headerPart)
        }

        if (contentType != null && !containsName(CONTENT_TYPE, headers)) {
            // @namgyu.park (2020-06-24) :
            // If body exists, a payload body should generate a Content-Type.
            // https://tools.ietf.org/html/rfc7231#section-3.1.1.5
            parts.add(String.format(
                FORMAT_HEADER,
                CONTENT_TYPE, contentType))
        }

        if (body != null) {
            parts.add(String.format(FORMAT_BODY, body))
        }

        if (compressed) {
            parts.add("--compressed")
        }

        parts.add(String.format(FORMAT_URL, url))

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