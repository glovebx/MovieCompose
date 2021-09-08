package com.skydoves.moviecompose.ui.components

import android.annotation.SuppressLint
import android.view.ViewTreeObserver
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.Toast

import android.view.ViewTreeObserver.OnPreDrawListener




@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebView(urlToRender: String, modifier: Modifier) {
    AndroidView(
        factory = { context ->
            val webView = WebView(context)
            webView.webViewClient = WebViewClient()
            val settings = webView.settings
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.setSupportZoom(true)
            settings.loadsImagesAutomatically = true

            val viewTreeObserver = webView.viewTreeObserver
            viewTreeObserver.addOnPreDrawListener(object : OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    val height = webView.measuredHeight
                    if (height != 0) {
                        modifier.height(height.dp)
                        viewTreeObserver.removeOnPreDrawListener(this)
                    }
                    return false
                }
            })

            webView.loadUrl(urlToRender)
            return@AndroidView webView
        },
        modifier = modifier
    )
}