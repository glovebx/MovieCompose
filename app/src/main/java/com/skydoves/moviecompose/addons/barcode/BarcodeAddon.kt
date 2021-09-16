package com.skydoves.moviecompose.addons.barcode

import android.Manifest
import android.content.Context
import com.skydoves.moviecompose.core.WebAddon

class BarcodeAddon(override val context: Context):
    WebAddon(context = context, "barcode", listOf(Manifest.permission.CAMERA)) {
}
