package com.skydoves.moviecompose.core

import android.content.Context
import com.skydoves.moviecompose.addons.WebAddonMethodMeta
import java.lang.reflect.Modifier

open class WebAddon(open val context: Context, open val aliasName: String, open val permissions: List<String>? = null) {
    val methodMetas = mutableMapOf<String, WebAddonMethodMeta>()

    fun cacheSelfMethods() {
        javaClass.declaredMethods.forEach {
            it.isAccessible = true
            if (Modifier.isPublic(it.modifiers)) {
                val parameterTypes = it.parameterTypes
                if (parameterTypes.isNotEmpty()
                    && parameterTypes[0].isAssignableFrom(WebAddonArgs::class.java)) {
                    val methodMeta = WebAddonMethodMeta(method = it)
                    methodMetas[methodMeta.name] = methodMeta
                }
            }
        }
    }
}