package com.skydoves.moviecompose.sandwich

import com.skydoves.moviecompose.models.network.JsonRpcResponse
import com.skydoves.moviecompose.utils.AnnotationUtils
import okhttp3.ResponseBody
import org.apache.commons.lang3.reflect.TypeUtils
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

// 参考：https://gist.github.com/naturalwarren/56b54759b0f690622938caa91f037be6
class JsonRpcConverterFactory private constructor()// Private constructor.
    : Converter.Factory() {

    override fun responseBodyConverter(type: Type,
                                       annotations: Array<Annotation>,
                                       retrofit: Retrofit): Converter<ResponseBody, *>? {
        if (!AnnotationUtils.isAnnotationPresent(annotations, JsonRpcCall::class.java)) {
            return null
        }

        val rpcType = TypeUtils.parameterize(JsonRpcResponse::class.java, type)
        val delegate = retrofit.nextResponseBodyConverter<JsonRpcResponse<ResponseBody>>(this, rpcType, annotations)

        return JsonRpcConverterFactoryConverter(delegate)
    }

    internal class JsonRpcConverterFactoryConverter<T>(private val delegate: Converter<ResponseBody, JsonRpcResponse<T>>) :
        Converter<ResponseBody, T> {

//        @Throws(IOException::class, ApiException::class)
        override fun convert(responseBody: ResponseBody): T? {
            val response = delegate.convert(responseBody)
//            if (response.error != null) {
//                val error = response.error as Map<String, Any>
//                val code: Int = error["code"].toString().toInt()
//                val message = error["message"].toString()
//                throw ApiException(code, message)
//            }
            return response?.result
        }
    }
//
//    override fun requestBodyConverter(type: Type?, annotations: Array<Annotation>?,
//                                      methodAnnotations: Array<Annotation>?, retrofit: Retrofit?): Converter<*, RequestBody>? {
//        val methodAnnotation = TypeUtils.findAnnotation(methodAnnotations, JsonRpcCall::class.java)
//            ?: return null
//        val method = methodAnnotation.value
//
//        val delegate = retrofit!!.nextRequestBodyConverter<Any>(this, JsonRPCRequest::class.java, annotations!!,
//            methodAnnotations!!)
//
//        return JsonRPCRequestBodyConverter<BaseRPCRequest>(method, delegate)
//    }
//
//    internal class JsonRPCRequestBodyConverter<T>(private val method: String, private val delegate: Converter<JsonRPCRequest, RequestBody>) :
//        Converter<T, RequestBody> {
//
//        @Throws(IOException::class)
//        override fun convert(value: T): RequestBody {
//            return delegate.convert(JsonRPCRequest.create(method, value as BaseRPCRequest))
//        }
//    }

    companion object {
        @JvmStatic
        fun create() = JsonRpcConverterFactory()
    }
}
