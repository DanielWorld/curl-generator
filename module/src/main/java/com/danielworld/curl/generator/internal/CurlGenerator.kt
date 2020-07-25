package com.danielworld.curl.generator.internal

import okhttp3.*
import okio.Buffer
import java.io.IOException
import java.nio.charset.Charset
import java.util.*

/**
 * 0. curl Options : https://curl.haxx.se/docs/manpage.html <br>
 * 1. curl change log : https://curl.haxx.se/changes.html <br>
 * 2. How to HTTP with curl : https://ec.haxx.se/http
 */
internal class CurlGenerator(private val request: Request, private val delimiter : String) {
    constructor(request: Request) : this (request, " ")


    private val url = request.url.toString()
    private val method : String = request.method
    private val headers: MutableList<Header> = LinkedList()     // Allowed same name's header like 'Set-Cookie' (https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2)
    private var contentType: String? = null
    private var body : String? = null
    private var formUrlEncodedBody: FormBody? = null
    private var multipartFormBody: MultipartBody? = null
    private var binaryBody : RequestBody? = null
    private var compressed: Boolean = false

    companion object {
        private const val FORMAT_URL = "\"%1\$s\""
        private const val FORMAT_METHOD = "-X %1\$s"
        private const val FORMAT_HEADER = "-H \"%1\$s:%2\$s\""
        private const val FORMAT_BODY = "-d '%1\$s'"
        private const val FORMAT_URL_ENCODED_BODY = "--data-urlencode \"%1\$s=%2\$s\""
        private const val FORMAT_MULTIPART_FORM_BODY = "-F \"%1\$s=%2\$s\""
        private const val FORMAT_BINARY_BODY = "--data-binary @%1\$s"

        private const val CONTENT_TYPE = "Content-Type"
        private const val ACCEPT_ENCODING = "Accept-Encoding"
    }

    init {
        val requestBody = request.body
        requestBody?.let { rb ->
            this.contentType = getContentType(rb)
            if (contentType?.contains("application/x-www-form-urlencoded") == true && rb is FormBody) {
                formUrlEncodedBody = rb
            }
            else if (contentType?.contains("multipart/form-data") == true && rb is MultipartBody) {
                // @namgyu.park (2020-07-13) : multipart/form-data 의 경우에만 대응할 것.
                multipartFormBody = rb
            }
            else if (contentType?.contains("application/octet-stream") == true) {
                binaryBody = rb
            }
            else {
                this.body = getBodyAsString(rb)
            }
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

        formUrlEncodedBody?.let {
            val size = it.size
            for (index in 0 until size) {
                parts.add(String.format(FORMAT_URL_ENCODED_BODY, it.encodedName(index), it.encodedValue(index)))
            }
        } ?: multipartFormBody?.let {
            it.parts.map { part ->
                part.headers?.last()?.second?.let {
                    val isFile = it.contains("filename=")
                    val array = it.split(";")
                    var name: String = ""
                    var value : String? = ""

                    array.map {
                        val newStr = it.trim()
                        if (newStr.startsWith("name=\"")) {
                            name = newStr.removePrefix("name=\"").removeSuffix("\"").trim()
                        }
                        else if (isFile && newStr.startsWith("filename=\"")) {
                            value = "@" + newStr.removePrefix("filename=\"").removeSuffix("\"").trim()
                        }
                    }

                    if (!isFile) {
                        value = getBodyAsString(part.body)
                    }

                    parts.add(String.format(FORMAT_MULTIPART_FORM_BODY, name, value))
                }
            }
        } ?: binaryBody?.let {

//            val buffer = Buffer()
//            it.writeTo(buffer)

            // 임의의 파일네임 적용
            parts.add(String.format(FORMAT_BINARY_BODY, "filename"))

        } ?: run {
            if (body != null) {
                parts.add(String.format(FORMAT_BODY, body))
            }
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