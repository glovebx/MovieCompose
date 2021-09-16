package com.skydoves.moviecompose.extensions

import org.json.JSONArray

inline fun LinkedHashMap<String, Any>.emptyContext() {
    if (!this.containsKey("context")) {
        this["context"] = mapOf<String, String>()
    }
}

inline fun JSONArray.asList(): List<Any> {
    val jsonList = mutableListOf<Any>()
    for (i in 0 until this.length()) {
        jsonList[i] = this.get(i)
    }
    return jsonList
}
