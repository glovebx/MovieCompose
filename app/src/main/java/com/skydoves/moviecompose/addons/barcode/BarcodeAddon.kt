package com.skydoves.moviecompose.addons.barcode

import android.Manifest
import android.content.Context
import com.skydoves.moviecompose.core.WebAddon
import com.skydoves.moviecompose.core.WebAddonArgs
import com.skydoves.moviecompose.core.WebAddonCallback

class BarcodeAddon(override val context: Context):
    WebAddon(context = context, "barcode", listOf(Manifest.permission.CAMERA)) {

    fun scanBarcode(webPluginArgs: WebAddonArgs?, webPluginCallback: WebAddonCallback?) {

    }
}
