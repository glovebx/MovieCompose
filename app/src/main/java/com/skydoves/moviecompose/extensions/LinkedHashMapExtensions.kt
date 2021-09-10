package com.skydoves.moviecompose.extensions

inline fun LinkedHashMap<String, Any>.emptyContext() {
    if (!this.containsKey("context")) {
        this["context"] = mapOf<String, String>()
    }
}