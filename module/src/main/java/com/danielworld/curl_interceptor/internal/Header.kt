package com.danielworld.curl_interceptor.internal

internal class Header(private val name: String?, private val value: String?) {
    fun name(): String? {
        return name
    }

    fun value(): String? {
        return value
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val header =
            o as Header
        if (if (name != null) name != header.name else header.name != null) return false
        return if (value != null) value == header.value else header.value == null
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + (value?.hashCode() ?: 0)
        return result
    }

}