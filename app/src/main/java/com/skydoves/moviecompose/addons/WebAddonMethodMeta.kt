package com.skydoves.moviecompose.addons

import java.lang.reflect.Method

enum class AddonMethodTypes {
    EXECUTE_VOID,
    EXECUTE_RETURN,
    EXECUTE_CALLBACK
}
class WebAddonMethodMeta(val method: Method) {
    val name: String = method.name
    var type = AddonMethodTypes.EXECUTE_VOID
    init {
        if (method.parameterTypes.size > 1) {
            // 有异步回调
            type = AddonMethodTypes.EXECUTE_CALLBACK
        } else if (method.returnType != Void.TYPE) {
            // 有返回值
            type = AddonMethodTypes.EXECUTE_RETURN
        }
    }

    override fun toString(): String {
        return "WebAddonMethodMeta{methodName=$name, methodType=$type}"
    }
}