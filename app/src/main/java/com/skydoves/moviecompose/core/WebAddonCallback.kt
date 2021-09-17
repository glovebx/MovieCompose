package com.skydoves.moviecompose.core

import com.tencent.smtt.sdk.WebView
import org.json.JSONObject

class WebAddonCallback(private val webView: WebView, private val callbackId: String) {

    fun success(args: Any?) {
        callback(args, true)
    }

    fun fail(args: Any?) {
        callback(args, false)
    }

    private fun callback(args: Any?, success: Boolean) {
        if (callbackId.isNotEmpty()) {
            val jsonObject = JSONObject()
            jsonObject.put("success", success)
            jsonObject.put("data", args)
            notifyOdoo(jsonObject)
        }
    }

    private fun notifyOdoo(args: JSONObject) {
        webView.evaluateJavascript("window.odoo.native_notify('$callbackId', $args);", null)
    }
}
