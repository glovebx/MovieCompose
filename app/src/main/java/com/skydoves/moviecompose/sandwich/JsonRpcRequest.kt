package com.skydoves.moviecompose.sandwich

import com.skydoves.moviecompose.extensions.emptyContext
import java.util.Random
import kotlin.math.abs

internal class JsonRpcRequest(val method: String
                              , val params: Any?
                              , val id: Long
                              , val jsonrpc: String = "2.0") {

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val that = o as JsonRpcRequest?

        if (id != that!!.id) return false
        return if (method != that.method) false else params == that.params
    }

    override fun hashCode(): Int {
        var result = method.hashCode()
        result = 31 * result + params.hashCode()
        result = 31 * result + (id xor id.ushr(32)).toInt()
        return result
    }

    companion object {
        private val RANDOM = Random()

        fun create(method: String, args: Any?): JsonRpcRequest {
            val id = abs(RANDOM.nextLong())
            if (args is LinkedHashMap<*, *>) {
                (args as LinkedHashMap<String, Any>).emptyContext()
            }
            return JsonRpcRequest(method, args, id)
        }
    }
}