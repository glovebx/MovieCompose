package com.skydoves.moviecompose.addons.base

import android.content.Context
import com.skydoves.moviecompose.core.WebAddon
import com.skydoves.moviecompose.core.WebAddonArgs
import timber.log.Timber

class BaseAddon(override val context: Context): WebAddon(context = context, "base") {

    fun crashManager(webAddonArgs: WebAddonArgs) {
//        val str: String = WebPlugin.TAG
//        Log.e(str, "crashManager() ERROR CODE : " + webAddonArgs.getString("code"))
        Timber.d("CrashManager error code: ${webAddonArgs.asString("code")}")
        val map: WebAddonArgs = webAddonArgs.asWebAddonArgs("data")
        if (map.containsKey("name")) {
            val exceptionName: String = map.asString("name")
//                Log.d("$str:crashManager()", string)
            Timber.d("CrashManager exception name: $exceptionName")
//            string.hashCode()
            if (exceptionName == "odoo.http.SessionExpiredException") {
//                getUser().getSession(getContext()).removeSession()
//                val intent = Intent(getContext(), UserLoginActivity::class.java)
//                intent.putExtra("user_name", getUser().getAccountName())
//                intent.putExtra("session_exprire_redirect", true)
//                getContext().startActivity(intent)
//                getWebView().getActivity().finish()
            }
        }
    }
}
