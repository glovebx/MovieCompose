package com.skydoves.moviecompose.core

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.webkit.WebStorage.QuotaUpdater
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import com.skydoves.moviecompose.addons.WebAddonsRepository
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.lang.ref.WeakReference
import javax.inject.Inject


class WVJBWebView : WebView {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    //    lateinit var APP_CACHE_DIRNAME: String
//    private var javascriptCloseWindowListener: JavascriptCloseWindowListener? = null
    private var WVJBCoreJs = ""

    private val outerHandlerWeakReference = WeakReference(this)
    lateinit var mainThreadHandler: MyHandler

    inner class MyHandler(private val handlerWeakReference: WeakReference<WVJBWebView>) :
        Handler(Looper.getMainLooper()) {
        //  Using WeakReference to avoid memory leak
        override fun handleMessage(msg: Message) {
            handlerWeakReference.get()?.let {
                when (msg.what) {
                    EXEC_SCRIPT -> _evaluateJavascriptOnMain(msg.obj as String)
                    LOAD_URL -> super@WVJBWebView.loadUrl((msg.obj as String))
                    LOAD_URL_WITH_HEADERS -> {
                        (msg.obj as UrlRequestWithHeaders)?.let {
                            super@WVJBWebView.loadUrl(it.url, it.headers)
                        }
                    }
                    HANDLE_MESSAGE -> (msg.obj as UrlRequestWithOdoo)?.let {
                        this@WVJBWebView.handleOdooMessage(it.name, it.args, it.id)
                    }
                }
            }
        }
    }

    private inner class UrlRequestWithHeaders(
        val url: String,
        val headers: Map<String, String>
    )

    private inner class UrlRequestWithOdoo(
        val name: String,
        val args: String?,
        val id: String?
    )

    inner class WVJBMessage {
        var data: Any? = null
        var callbackId: String? = null
        var handlerName: String? = null
        var responseId: String? = null
        var responseData: Any? = null
    }

    interface WVJBResponseCallback<T> {
        fun onResult(data: T)
    }

    interface WVJBHandler<T, R> {
        fun handler(data: T, callback: WVJBResponseCallback<R>?)
    }

    interface WVJBMethodExistCallback {
        fun onResult(exist: Boolean)
    }
//
//    interface JavascriptCloseWindowListener {
//        /**
//         * @return If true, close the current activity, otherwise, do nothing.
//         */
//        fun onClose(): Boolean
//    }
    @Inject
    lateinit var webAddonsRepository: WebAddonsRepository

    private var startupMessageQueue: ArrayList<WVJBMessage>? = null
    private var uniqueId: Long = 0
    private var alertBoxBlock = true

    lateinit var responseCallbacks: MutableMap<String, WVJBResponseCallback<*>>
    lateinit var messageHandlers: MutableMap<String, WVJBHandler<*, *>>

    fun disableJavascriptAlertBoxSafetyTimeout(disable: Boolean) {
        alertBoxBlock = !disable
    }

    @JvmOverloads
    fun <T> callHandler(
        handlerName: String?, data: Any? = null,
        responseCallback: WVJBResponseCallback<Any>? = null
    ) {
        sendData(data, responseCallback, handlerName)
    }

    /**
     * Test whether the handler exist in javascript
     *
     * @param handlerName
     * @param callback
     */
    fun hasJavascriptMethod(handlerName: String?, callback: WVJBMethodExistCallback) {
        callHandler<Any>("_hasJavascriptMethod", handlerName, object : WVJBResponseCallback<Any> {
            override fun onResult(data: Any) {
                callback.onResult(data as Boolean)
            }
        })
    }
//
//    /**
//     * set a listener for javascript closing the current activity.
//     */
//    fun setJavascriptCloseWindowListener(listener: JavascriptCloseWindowListener?) {
//        javascriptCloseWindowListener = listener
//    }

    fun <T, R> registerHandler(handlerName: String, handler: WVJBHandler<T, R>?) {
        if (handlerName.isNullOrEmpty() || handler == null) {
            return
        }
        messageHandlers[handlerName] = handler
    }

    fun unregisterAllHandlers() {
        if (messageHandlers.isNotEmpty()) {
            messageHandlers.clear()
        }
    }

    // send the onResult message to javascript
    private fun sendData(
        data: Any?,
        responseCallback: WVJBResponseCallback<Any>?,
        handlerName: String?
    ) {
        if (data == null && (handlerName.isNullOrEmpty())) {
            return
        }
        val message = WVJBMessage()
        if (data != null) {
            message.data = data
        }
        if (responseCallback != null) {
            val callbackId = "java_cb_" + ++uniqueId
            responseCallbacks[callbackId] = responseCallback
            message.callbackId = callbackId
        }
        handlerName?.let {
            message.handlerName = it
        }
        queueMessage(message)
    }

    @Synchronized
    private fun queueMessage(message: WVJBMessage) {
        startupMessageQueue?.add(message) ?: dispatchMessage(message)
    }

    private fun dispatchMessage(message: WVJBMessage) {
        val messageJSON = message2JSONObject(message).toString()
        evaluateJavascript(
            String.format(
                "WebViewJavascriptBridge._handleMessageFromJava(%s)",
                messageJSON
            )
        )
    }

    private fun handleOdooMessage(name: String, args: String?, id: String?) {
        Timber.d("$JS_BRIDGE_NAME, Executing: $name; Args: $args, Callback: $id")
        try {
            // base.crashManager(...)
            // barcode.scanBarcode(...)
            val split = name.split("\\.")
            if (split.size > 1) {
                webAddonsRepository.exec(this, split[0], split[1], JSONObject(args), id)
            }
        } catch (e: JSONException) {
            Timber.e(e)
        }
    }

    // handle the onResult message from javascript
    private fun handleMessage(info: String) {
        try {
            val jsonObject = JSONObject(info)
            val message = JSONObject2WVJBMessage(jsonObject)
            if (message.responseId != null) {
                val responseCallback =
                    responseCallbacks.remove(message.responseId) as WVJBResponseCallback<Any>
                responseCallback?.let {
                    it.onResult(message.responseData ?: "")
                }

            } else {
                var responseCallback: WVJBResponseCallback<Any>? = null
                if (message.callbackId != null) {
                    val callbackId = message.callbackId
                    responseCallback = object : WVJBResponseCallback<Any> {
                        override fun onResult(data: Any) {
                            val msg = WVJBMessage()
                            msg.responseId = callbackId
                            msg.responseData = data
                            dispatchMessage(msg)
                        }
                    }
                }
                val handler = messageHandlers[message.handlerName] as WVJBHandler<Any, Any>?
                handler?.handler(message.data ?: "", responseCallback)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun message2JSONObject(message: WVJBMessage): JSONObject {
        val jsonObject = JSONObject()
        try {
            message.callbackId?.let {
                jsonObject.put("callbackId", it)
            }
            message.data?.let {
                jsonObject.put("data", it)
            }
            message.handlerName?.let {
                jsonObject.put("handlerName", it)
            }
            message.responseId?.let {
                jsonObject.put("responseId", message.responseId)
            }
            message.responseData?.let {
                jsonObject.put("responseData", message.responseData)
            }
//            if (message.callbackId != null) {
//                jo.put("callbackId", message.callbackId)
//            }
//            if (message.data != null) {
//                jo.put("data", message.data)
//            }
//            if (message.handlerName != null) {
//                jo.put("handlerName", message.handlerName)
//            }
//            if (message.responseId != null) {
//                jo.put("responseId", message.responseId)
//            }
//            if (message.responseData != null) {
//                jo.put("responseData", message.responseData)
//            }
        } catch (e: JSONException) {
            Timber.e(e)
        }
        return jsonObject
    }

    private fun JSONObject2WVJBMessage(jsonObject: JSONObject): WVJBMessage {
        val message = WVJBMessage()
        try {
            if (jsonObject.has("callbackId")) {
                message.callbackId = jsonObject.getString("callbackId")
            }
            if (jsonObject.has("data")) {
                message.data = jsonObject["data"]
            }
            if (jsonObject.has("handlerName")) {
                message.handlerName = jsonObject.getString("handlerName")
            }
            if (jsonObject.has("responseId")) {
                message.responseId = jsonObject.getString("responseId")
            }
            if (jsonObject.has("responseData")) {
                message.responseData = jsonObject["responseData"]
            }
        } catch (e: JSONException) {
            Timber.e(e)
        }
        return message
    }

    @Keep
    fun init() {
//        APP_CACHE_DIRNAME = context.filesDir.absolutePath + "/webcache"
        mainThreadHandler = MyHandler(outerHandlerWeakReference)
        responseCallbacks = hashMapOf()
        messageHandlers = hashMapOf()
        startupMessageQueue = arrayListOf()


//        final WebSettings webSettings = this.getSettings();
//        webSettings.setDomStorageEnabled(true);
//        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
//        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
//        webSettings.setJavaScriptEnabled(true);
//        webSettings.setSaveFormData(false);
//        webSettings.setLoadsImagesAutomatically(true);
//
//        final CookieManager cookieManager = CookieManager.getInstance();
//        cookieManager.setAcceptCookie(true);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            cookieManager.setAcceptThirdPartyCookies(this, true);
////            cookieManager.acceptThirdPartyCookies(this);
//        }

        // 注意这里调用的是super的方法！！！！！
        // 本类里面的继承的这两个方法是来设置代理的！！！
        super.setWebChromeClient(mWebChromeClient)
        super.setWebViewClient(mWebViewClient)
        registerHandler("_hasNativeMethod", object : WVJBHandler<Any, Any> {
            override fun handler(data: Any, callback: WVJBResponseCallback<Any>?) {
                callback?.onResult(messageHandlers[data] != null)
            }
        })
//        registerHandler("_closePage", object : WVJBHandler<Any, Any> {
//            override fun handler(data: Any, callback: WVJBResponseCallback<Any>?) {
//                if (javascriptCloseWindowListener == null
//                    || javascriptCloseWindowListener!!.onClose()
//                ) {
////                    (context as Activity).onBackPressed()
//                }
//            }
//        })
        registerHandler(
            "_disableJavascriptAlertBoxSafetyTimeout",
            object : WVJBHandler<Any, Any> {
                override fun handler(data: Any, callback: WVJBResponseCallback<Any>?) {
                    disableJavascriptAlertBoxSafetyTimeout(data as Boolean)
                }
            })

        super.addJavascriptInterface(InJavaScriptLocalObj(), BRIDGE_NAME)

        // 从资源文件获取js
        try {
            val inputStream: InputStream = context.assets.open(
                "WebViewJavascriptBridge.js"
            )
            val buffer = ByteArray(inputStream.available())
            inputStream.read(buffer)
            inputStream.close()
            WVJBCoreJs = String(buffer)
        } catch (e: IOException) {
            Timber.e(e)
        }
    }

    private fun _evaluateJavascriptOnMain(script: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            super@WVJBWebView.evaluateJavascript(script, null)
        } else {
            super@WVJBWebView.loadUrl("javascript:$script")
        }
    }

    /**
     * This method can be called in any thread, and if it is not called in the main thread,
     * it will be automatically distributed to the main thread.
     *
     * @param script
     */
    fun evaluateJavascript(script: String) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            _evaluateJavascriptOnMain(script)
        } else {
            val msg = Message()
            msg.what = EXEC_SCRIPT
            msg.obj = script
            mainThreadHandler.sendMessage(msg)
        }
    }

    /**
     * This method can be called in any thread, and if it is not called in the main thread,
     * it will be automatically distributed to the main thread.
     *
     * @param url
     */
    override fun loadUrl(url: String) {
        val msg = Message()
        msg.what = LOAD_URL
        msg.obj = url
        mainThreadHandler.sendMessage(msg)
    }

    /**
     * This method can be called in any thread, and if it is not called in the main thread,
     * it will be automatically distributed to the main thread.
     *
     * @param url
     * @param additionalHttpHeaders
     */
    override fun loadUrl(url: String, additionalHttpHeaders: Map<String, String>) {
        val msg = Message()
        msg.what = LOAD_URL_WITH_HEADERS
        msg.obj = UrlRequestWithHeaders(url, additionalHttpHeaders)
        mainThreadHandler.sendMessage(msg)
    }

    // proxy client
    var newWebChromeClient: WebChromeClient? = null
    var newWebViewClient: WebViewClient? = null
    override fun setWebChromeClient(client: WebChromeClient?) {
        newWebChromeClient = client
    }

    override fun setWebViewClient(client: WebViewClient) {
        newWebViewClient = client
    }

    private val mWebChromeClient: WebChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            newWebChromeClient?.onProgressChanged(view, newProgress)
                ?: super.onProgressChanged(view, newProgress)
        }

        override fun onReceivedTitle(view: WebView, title: String) {
            newWebChromeClient?.onReceivedTitle(view, title) ?: super.onReceivedTitle(view, title)
        }

        override fun onReceivedIcon(view: WebView, icon: Bitmap) {
            newWebChromeClient?.onReceivedIcon(view, icon) ?: super.onReceivedIcon(view, icon)
        }

        override fun onReceivedTouchIconUrl(view: WebView, url: String, precomposed: Boolean) {
            newWebChromeClient?.onReceivedTouchIconUrl(view, url, precomposed)
                ?: super.onReceivedTouchIconUrl(view, url, precomposed)
        }

        override fun onShowCustomView(view: View, callback: CustomViewCallback?) {
            newWebChromeClient?.onShowCustomView(view, callback) ?: super.onShowCustomView(
                view,
                callback
            )
        }

        override fun onShowCustomView(
            view: View, requestedOrientation: Int,
            callback: CustomViewCallback?
        ) {
            newWebChromeClient?.onShowCustomView(view, requestedOrientation, callback)
                ?: super.onShowCustomView(view, requestedOrientation, callback)
        }

        override fun onHideCustomView() {
            newWebChromeClient?.onHideCustomView() ?: super.onHideCustomView()
        }

        override fun onCreateWindow(
            view: WebView, isDialog: Boolean,
            isUserGesture: Boolean, resultMsg: Message?
        ): Boolean {
            return newWebChromeClient?.onCreateWindow(
                view, isDialog,
                isUserGesture, resultMsg
            ) ?: super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
        }

        override fun onRequestFocus(view: WebView) {
            newWebChromeClient?.onRequestFocus(view) ?: super.onRequestFocus(view)
        }

        override fun onCloseWindow(window: WebView) {
            newWebChromeClient?.onCloseWindow(window) ?: super.onCloseWindow(window)
        }
//
//        override fun onJsAlert(
//            view: WebView,
//            url: String,
//            message: String,
//            result: JsResult
//        ): Boolean {
//            if (!alertBoxBlock) {
//                result.confirm()
//            }
//            if (newWebChromeClient != null) {
//                if (newWebChromeClient!!.onJsAlert(view, url, message, result)) {
//                    return true
//                }
//            }
//            val alertDialog: Dialog =
//                AlertDialog.Builder(context).setMessage(message).setCancelable(false).setPositiveButton(
//                    R.string.ok
//                ) { dialog, _ ->
//                    dialog.dismiss()
//                    if (alertBoxBlock) {
//                        result.confirm()
//                    }
//                }.create()
//            alertDialog.show()
//            return true
//        }
//
//        override fun onJsConfirm(
//            view: WebView, url: String, message: String,
//            result: JsResult
//        ): Boolean {
//            if (!alertBoxBlock) {
//                result.confirm()
//            }
//            return if (newWebChromeClient != null && newWebChromeClient!!.onJsConfirm(
//                    view,
//                    url,
//                    message,
//                    result
//                )
//            ) {
//                true
//            } else {
//                val listener =
//                    DialogInterface.OnClickListener { _, which ->
//                        if (alertBoxBlock) {
//                            if (which == Dialog.BUTTON_POSITIVE) {
//                                result.confirm()
//                            } else {
//                                result.cancel()
//                            }
//                        }
//                    }
//                AlertDialog.Builder(context)
//                    .setMessage(message)
//                    .setCancelable(false)
//                    .setPositiveButton(R.string.ok, listener)
//                    .setNegativeButton(R.string.cancel, listener).show()
//                true
//            }
//        }
//
//        override fun onJsPrompt(
//            view: WebView, url: String, message: String,
//            defaultValue: String, result: JsPromptResult
//        ): Boolean {
//            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
//                val prefix = "_wvjbxx"
//                if (message == prefix) {
//                    val msg = Message()
//                    msg.what = HANDLE_MESSAGE
//                    msg.obj = defaultValue
//                    mainThreadHandler.sendMessage(msg)
//                }
//                return true
//            }
//            if (!alertBoxBlock) {
//                result.confirm()
//            }
//            return if (newWebChromeClient != null && newWebChromeClient!!.onJsPrompt(
//                    view,
//                    url,
//                    message,
//                    defaultValue,
//                    result
//                )
//            ) {
//                true
//            } else {
//                val editText = EditText(context)
//                editText.setText(defaultValue)
//                if (defaultValue != null) {
//                    editText.setSelection(defaultValue.length)
//                }
//                val dpi = context.resources.displayMetrics.density
//                val listener =
//                    DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
//                        if (alertBoxBlock) {
//                            if (which == Dialog.BUTTON_POSITIVE) {
//                                result.confirm(editText.text.toString())
//                            } else {
//                                result.cancel()
//                            }
//                        }
//                    }
//                AlertDialog.Builder(context)
//                    .setTitle(message)
//                    .setView(editText)
//                    .setCancelable(false)
//                    .setPositiveButton(R.string.ok, listener)
//                    .setNegativeButton(R.string.cancel, listener)
//                    .show()
//                val layoutParams = FrameLayout.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT
//                )
//                val t = (dpi * 16).toInt()
//                layoutParams.setMargins(t, 0, t, 0)
//                layoutParams.gravity = Gravity.CENTER_HORIZONTAL
//                editText.layoutParams = layoutParams
//                val padding = (15 * dpi).toInt()
//                editText.setPadding(padding - (5 * dpi).toInt(), padding, padding, padding)
//                true
//            }
//        }

        override fun onJsBeforeUnload(
            view: WebView,
            url: String,
            message: String,
            result: JsResult
        ): Boolean {
            return newWebChromeClient?.onJsBeforeUnload(view, url, message, result)
                ?: super.onJsBeforeUnload(view, url, message, result)
        }

        override fun onExceededDatabaseQuota(
            url: String, databaseIdentifier: String, quota: Long,
            estimatedDatabaseSize: Long,
            totalQuota: Long,
            quotaUpdater: QuotaUpdater
        ) {
            newWebChromeClient?.onExceededDatabaseQuota(
                url, databaseIdentifier, quota,
                estimatedDatabaseSize, totalQuota, quotaUpdater
            ) ?: super.onExceededDatabaseQuota(
                url, databaseIdentifier, quota,
                estimatedDatabaseSize, totalQuota, quotaUpdater
            )
        }

        override fun onReachedMaxAppCacheSize(
            requiredStorage: Long,
            quota: Long,
            quotaUpdater: QuotaUpdater
        ) {
            newWebChromeClient?.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater)
                ?: super.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater)
        }

        override fun onGeolocationPermissionsHidePrompt() {
            newWebChromeClient?.onGeolocationPermissionsHidePrompt()
                ?: super.onGeolocationPermissionsHidePrompt()
        }

        override fun onJsTimeout(): Boolean {
            return newWebChromeClient?.onJsTimeout() ?: super.onJsTimeout()
        }

        override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
            return newWebChromeClient?.onConsoleMessage(consoleMessage) ?: super.onConsoleMessage(
                consoleMessage
            )
        }

        override fun getDefaultVideoPoster(): Bitmap? {
            return newWebChromeClient?.defaultVideoPoster ?: super.getDefaultVideoPoster()
        }

        override fun getVideoLoadingProgressView(): View? {
            return newWebChromeClient?.videoLoadingProgressView
                ?: super.getVideoLoadingProgressView()
        }

        override fun getVisitedHistory(callback: ValueCallback<Array<String>>) {
            newWebChromeClient?.getVisitedHistory(callback) ?: super.getVisitedHistory(callback)
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onShowFileChooser(
            webView: WebView?, filePathCallback: ValueCallback<Array<Uri?>?>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            return newWebChromeClient?.onShowFileChooser(
                webView,
                filePathCallback,
                fileChooserParams
            )
                ?: super.onShowFileChooser(webView, filePathCallback, fileChooserParams)
        }
    }
    private val mWebViewClient: WebViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            return newWebViewClient?.shouldOverrideUrlLoading(view, url)
                ?: super.shouldOverrideUrlLoading(view, url)
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            newWebViewClient?.onPageStarted(view, url, favicon) ?: super.onPageStarted(
                view,
                url,
                favicon
            )
        }

        override fun onPageFinished(view: WebView, url: String) {
            WVJBCoreJs?.let {
                evaluateJavascript(it)
            }
            synchronized(this@WVJBWebView) {
                if (startupMessageQueue != null) {
                    for (i in 0 until startupMessageQueue!!.size) {
                        dispatchMessage(startupMessageQueue!![i])
                    }
                    startupMessageQueue = null
                }
            }
            newWebViewClient?.onPageFinished(view, url) ?: super.onPageFinished(view, url)
        }

        override fun onLoadResource(view: WebView, url: String) {
            newWebViewClient?.onLoadResource(view, url) ?: super.onLoadResource(view, url)
        }

        @Deprecated("")
        override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
            return newWebViewClient?.shouldInterceptRequest(view, url)
                ?: super.shouldInterceptRequest(view, url)
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        override fun shouldInterceptRequest(
            view: WebView,
            request: WebResourceRequest
        ): WebResourceResponse? {
            return newWebViewClient?.shouldInterceptRequest(view, request)
                ?: super.shouldInterceptRequest(view, request)
        }

        @Deprecated("")
        override fun onTooManyRedirects(
            view: WebView?,
            cancelMsg: Message?,
            continueMsg: Message?
        ) {
            newWebViewClient?.onTooManyRedirects(view, cancelMsg, continueMsg)
                ?: super.onTooManyRedirects(view, cancelMsg, continueMsg)
        }

        @Deprecated("")
        override fun onReceivedError(
            view: WebView,
            errorCode: Int,
            description: String,
            failingUrl: String
        ) {
            newWebViewClient?.onReceivedError(view, errorCode, description, failingUrl)
                ?: super.onReceivedError(view, errorCode, description, failingUrl)
        }

        override fun onFormResubmission(view: WebView?, dontResend: Message?, resend: Message?) {
            newWebViewClient?.onFormResubmission(view, dontResend, resend)
                ?: super.onFormResubmission(view, dontResend, resend)
        }

        override fun doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
            newWebViewClient?.doUpdateVisitedHistory(view, url, isReload)
                ?: super.doUpdateVisitedHistory(view, url, isReload)
        }

        override fun shouldOverrideKeyEvent(view: WebView?, event: KeyEvent?): Boolean {
            return newWebViewClient?.shouldOverrideKeyEvent(view, event)
                ?: super.shouldOverrideKeyEvent(view, event)
        }

        @Deprecated("")
        override fun onUnhandledKeyEvent(view: WebView?, event: KeyEvent?) {
            newWebViewClient?.onUnhandledKeyEvent(view, event) ?: super.onUnhandledKeyEvent(
                view,
                event
            )
        }

        override fun onScaleChanged(view: WebView, oldScale: Float, newScale: Float) {
            newWebViewClient?.onScaleChanged(view, oldScale, newScale) ?: super.onScaleChanged(
                view,
                oldScale,
                newScale
            )
        }
    }

    override fun destroy() {
        release()
        super.destroy()
    }

    fun injectCookie(url: String, cookie: String) {
        val cookieManager = CookieManager.getInstance()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(this, true)

            cookieManager.setCookie(Uri.parse(url).host, cookie)

            cookieManager.flush()
        } else {
            val cookieSyncManager = CookieSyncManager.createInstance(context)
            cookieSyncManager.startSync()

            cookieManager.setCookie(Uri.parse(url).host, cookie)

            cookieSyncManager.stopSync()
            cookieSyncManager.sync()
        }
    }

    fun clearCookie() {
        val cookieManager = CookieManager.getInstance()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeAllCookies(null)
            cookieManager.removeSessionCookies(null)
            cookieManager.flush()
        } else {
            val cookieSyncManager = CookieSyncManager.createInstance(this.context)
            cookieSyncManager.startSync()
            cookieManager.removeAllCookie()
            cookieManager.removeSessionCookie()
            cookieManager.removeExpiredCookie()
            cookieSyncManager.stopSync()
            // flush
            cookieSyncManager.sync()
        }
    }

    // 程序通用的初始设置
    fun setUp() {
        this.settings.let {
            it.domStorageEnabled = true
            it.cacheMode = WebSettings.LOAD_NO_CACHE
            it.javaScriptCanOpenWindowsAutomatically = true
            it.javaScriptEnabled = true
            //设置true,才能让Webivew支持<meta>标签的viewport属性
            it.useWideViewPort = true

            //设置可以支持缩放
            it.setSupportZoom(true)
            //设置出现缩放工具
            it.builtInZoomControls = true
            //设定缩放控件隐藏
            it.displayZoomControls = false
            it.loadsImagesAutomatically = true
            // 不保存Form框条的数据
            it.saveFormData = false
        }

////         改变ua
//        webSettings.setUserAgentString("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36");
//        webSettings.setUserAgentString("Mozilla/5.0 (iPhone; CPU iPhone OS 12_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.0 Mobile/15E148 Safari/604.1");

//        final String appCachePath
//                = this.getContext().getApplicationContext().getCacheDir().getAbsolutePath();
//        webSettings.setAppCachePath(appCachePath);
//        webSettings.setAppCacheEnabled(true);

//        webSettings.setAllowFileAccessFromFileURLs(true);
//        webSettings.setAllowUniversalAccessFromFileURLs(true);

        // 5.0 以后的WebView加载的链接为Https开头，但是链接里面的内容，比如图片为Http链接，这时候，图片就会加载不出来
        // 原因是5.0之后不支持Https和Http的混合模式
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            webSettings.setMixedContentMode(webSettings.getMixedContentMode());
//        }
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(this, true)
        }
    }

    fun release() {
        unregisterAllHandlers()
        stopLoading()
//        setWebViewClient(null)
        webChromeClient = null
        this.settings.let {
            it.javaScriptEnabled = false
            it.blockNetworkImage = false
        }
        clearHistory()
        clearCache(true)
        WebStorage.getInstance().deleteAllData()
        // clear cookies
        clearCookie()
        removeAllViews()
        try {
            val viewParent = this.parent
            if (viewParent != null && viewParent is ViewGroup) {
                viewParent.removeView(this)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    internal inner class InJavaScriptLocalObj() {
        @Keep
        @JavascriptInterface
        fun execute(name: String, args: String?, id: String?) {
            val msg = Message()
            msg.what = HANDLE_MESSAGE
            msg.obj = UrlRequestWithOdoo(name, args, id)
            mainThreadHandler.sendMessage(msg)
        }

        @Keep
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
        }
    }

    companion object {
        private const val BRIDGE_NAME = "OdooDeviceUtility"
        private const val EXEC_SCRIPT = 1
        private const val LOAD_URL = 2
        private const val LOAD_URL_WITH_HEADERS = 3
        private const val HANDLE_MESSAGE = 4
    }
}
