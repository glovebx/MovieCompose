package com.skydoves.moviecompose.core

import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.skydoves.moviecompose.addons.WebAddonsRepository
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

const val JS_BRIDGE_NAME = "OdooDeviceUtility"

@Deprecated("nouse")
class WebJsBridge (private val webView: WebView, private val webAddonsRepository: WebAddonsRepository) {

    @JavascriptInterface
    fun execute(name: String, args: String?, id: String?) {
        Timber.d("$JS_BRIDGE_NAME, Executing: $name; Args: $args, Callback: $id")
        try {
            // base.crashManager(...)
            // barcode.scanBarcode(...)
            val split = name.split("\\.")
            if (split.size >= 2) {
                webAddonsRepository.exec(webView, split[0], split[1], JSONObject(args), id)
            }
        } catch (e: JSONException) {
            Timber.e(e)
        }
    }

    @JavascriptInterface
    fun list_plugins(): String? {
        val jsonArray = JSONArray()
        webAddonsRepository.addons.forEach { webAddon ->
            webAddon.methodMetas.values.forEach { methodMeta ->
                val jsonObject = JSONObject()
                jsonObject.put("name", methodMeta.name)
                    .put("action", "${webAddon.aliasName}.${methodMeta.name}")

                jsonArray.put(jsonObject)
            }
        }
        return jsonArray.toString()
//        val jSONArray = JSONArray()
//        for (pluginMeta in this.pluginUtils.getPlugins().values()) {
//            for (pluginMethodMeta in pluginMeta.getMethods().values()) {
//                val jSONObject = JSONObject()
//                try {
//                    jSONObject.put(
//                        "action",
//                        java.lang.String.format(
//                            "%s.%s",
//                            pluginMeta.getAlias(),
//                            pluginMethodMeta.getMethodName()
//                        )
//                    )
//                    jSONObject.put("name", pluginMethodMeta.getMethodName())
//                    jSONArray.put(jSONObject)
//                } catch (e: JSONException) {
//                    e.printStackTrace()
//                }
//            }
//        }
//        return jSONArray.toString()
    }
}
