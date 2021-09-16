package com.skydoves.moviecompose.addons

import android.webkit.WebView
import com.skydoves.moviecompose.core.WebAddon
import com.skydoves.moviecompose.core.WebAddonArgs
import com.skydoves.moviecompose.core.WebAddonCallback
import com.skydoves.moviecompose.core.extensions.asWebAddonArgs
import org.json.JSONObject

class WebAddonsRepository(
    val addons: Array<WebAddon>
) {
    init {
        addons.forEach {
            // 将addons的方法提取到map里面
            it.cacheSelfMethods()
        }
    }

    fun exec(webView: WebView, aliasName: String, name: String, jsonObject: JSONObject?, id: String?) {
        addons.firstOrNull { it.aliasName == aliasName }?.let { webAddon ->
            {
                if (webAddon.permissions?.isNotEmpty() == true) {
                    // TODO: 检查权限，没有则动态申请
                }
                webAddon.methodMetas[name]?.let { methodMeta ->
                    {
                        val args = jsonObject?.asWebAddonArgs() ?: WebAddonArgs()
                        val callback =
                            if (id.isNullOrEmpty()) null else WebAddonCallback(webView, id)
                        invokeMethod(webAddon, methodMeta, args, callback)
                    }
                }
            }
        }
    }

    private fun invokeMethod(webAddon: WebAddon, methodMeta: WebAddonMethodMeta,
                             args: WebAddonArgs, callback: WebAddonCallback?) {
        when(methodMeta.type) {
            AddonMethodTypes.EXECUTE_VOID -> {
                methodMeta.method.invoke(webAddon, args)
                callback?.success(true)
            }
            AddonMethodTypes.EXECUTE_RETURN -> {
                callback?.success(methodMeta.method.invoke(webAddon, args)?.toString() ?: "")
            }
            AddonMethodTypes.EXECUTE_CALLBACK -> {
                callback?.let {
                    methodMeta.method.invoke(webAddon, args, it)
                }
            }
        }
    }
}
