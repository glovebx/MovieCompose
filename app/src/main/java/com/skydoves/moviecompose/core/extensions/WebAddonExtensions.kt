package com.skydoves.moviecompose.core.extensions

import com.skydoves.moviecompose.core.WebAddonArgs
import com.skydoves.moviecompose.extensions.asList
import org.json.JSONArray
import org.json.JSONObject

inline fun JSONObject.asWebAddonArgs(): WebAddonArgs {
    val args = WebAddonArgs()
    this.keys().forEach {
        when(val value = this[it]) {
            is JSONObject -> {
                args[it] = value
            }
            is JSONArray -> {
                args[it] = value.asList()
            }
            else -> {
                args[it] = value
            }
        }
    }
    return args
}