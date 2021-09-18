package com.skydoves.moviecompose.addons

import android.widget.Toast
import com.permissionx.guolindev.PermissionX
import com.permissionx.guolindev.callback.ExplainReasonCallback
import com.permissionx.guolindev.callback.RequestCallback
import com.permissionx.guolindev.request.ExplainScope
import com.skydoves.moviecompose.core.WVJBWebView
import com.tencent.smtt.sdk.WebView
import com.skydoves.moviecompose.core.WebAddon
import com.skydoves.moviecompose.core.WebAddonArgs
import com.skydoves.moviecompose.core.WebAddonCallback
import com.skydoves.moviecompose.core.extensions.asWebAddonArgs
import com.skydoves.moviecompose.ui.main.MainActivity
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
        var webAddon = addons.firstOrNull { it.aliasName == aliasName } ?: return
        if (webAddon.permissions?.isNotEmpty() == true) {
            val requestPermissions = webAddon.permissions?.filter {
                // TODO: 检查权限，没有则动态申请
                !PermissionX.isGranted(webView.context, it)
            }
            if (requestPermissions!!.isNotEmpty()) {
                PermissionX.init(webView.context as MainActivity)
                    .permissions(requestPermissions)
                    .onExplainRequestReason { scope, deniedList ->
                        scope.showRequestReasonDialog(deniedList,
                            "同意以下权限才能正常使用",
                            "Allow", "Deny") }
                    .request { allGranted, grantedList, deniedList ->
                        if (allGranted) {
                            // 可以正常使用
                            Toast.makeText(webView.context, "您可以正常使用", Toast.LENGTH_LONG).show()
                        } else {
                            // 报错
                            Toast.makeText(webView.context, "您拒绝了权限: $deniedList", Toast.LENGTH_LONG).show()
                        }
                    }
                return
            }
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
