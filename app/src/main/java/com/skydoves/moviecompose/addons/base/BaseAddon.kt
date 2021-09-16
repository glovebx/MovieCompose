package com.skydoves.moviecompose.addons.base

import android.content.Context
import android.content.Intent
import android.util.Log
import com.skydoves.moviecompose.core.WebAddon

class BaseAddon(override val context: Context): WebAddon(context = context, "base") {

//    fun crashManager(webAddonArgs: WebAddonArgs) {
//        val str: String = WebPlugin.TAG
//        Log.e(str, "crashManager() ERROR CODE : " + webAddonArgs.getString("code"))
//        if (webAddonArgs.containsKey("data")) {
//            val map: WebAddonArgs = webAddonArgs.getMap("data")
//            if (map.containsKey("name")) {
//                val string: String = map.getString("name")
//                Log.d("$str:crashManager()", string)
//                string.hashCode()
//                if (string == "odoo.http.SessionExpiredException") {
//                    getUser().getSession(getContext()).removeSession()
//                    val intent = Intent(getContext(), UserLoginActivity::class.java)
//                    intent.putExtra("user_name", getUser().getAccountName())
//                    intent.putExtra("session_exprire_redirect", true)
//                    getContext().startActivity(intent)
//                    getWebView().getActivity().finish()
//                }
//            }
//        }
//    }
}