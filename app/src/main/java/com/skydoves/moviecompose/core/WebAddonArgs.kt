package com.skydoves.moviecompose.core

class WebAddonArgs: HashMap<String, Any>() {
    fun asString(key: String): String {
        return get(key)?.toString() ?: ""
    }

    fun asInt(key: String): Int? {
        return get(key)?.toString()?.toInt()
    }

    fun asBoolean(key: String): Boolean {
        return get(key)?.toString()?.toBoolean() ?: false
    }

    fun asWebAddonArgs(key: String): WebAddonArgs {
        val value = get(key) ?: return WebAddonArgs()
        return if (value is WebAddonArgs) value else WebAddonArgs()
    }

    fun asStringMap(): Map<String, String> {
        val stringMap = mutableMapOf<String, String>()
        this.forEach {
            when (it.value) {
                null -> {
                    stringMap[it.key] = ""
                }
                is List<*> -> {
                    stringMap[it.key] = (it.value as List<*>).joinToString(", ")
                }
                is Map<*, *> -> {
                    // ??
                }
                else -> {
                    stringMap[it.key] = asString(it.key)
                }
            }
        }
        return stringMap
    }
}