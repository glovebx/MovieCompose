package com.skydoves.moviecompose.sandwich

import com.skydoves.moviecompose.exceptions.ApiException
import com.skydoves.moviecompose.exceptions.NO_EXPECTED_DATA_EXCEPTION
import com.skydoves.moviecompose.models.network.JsonRpcResponse
import com.skydoves.moviecompose.utils.AnnotationUtils
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.apache.commons.lang3.reflect.TypeUtils
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.IOException
import java.lang.reflect.Type

// 参考：https://gist.github.com/naturalwarren/56b54759b0f690622938caa91f037be6
class JsonRpcConverterFactory private constructor()// Private constructor.
    : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        if (!AnnotationUtils.isAnnotationPresent(annotations, JsonRpcRespond::class.java) &&
            !AnnotationUtils.isAnnotationPresent(annotations, JsonRpcCall::class.java)
        ) {
            return null
        }

        val rpcType = TypeUtils.parameterize(JsonRpcResponse::class.java, type)
        val delegate = retrofit.nextResponseBodyConverter<JsonRpcResponse<ResponseBody>>(
            this,
            rpcType,
            annotations
        )

        return JsonRpcConverterFactoryConverter(delegate)
    }

    internal class JsonRpcConverterFactoryConverter<T>(private val delegate: Converter<ResponseBody, JsonRpcResponse<T>>) :
        Converter<ResponseBody, T> {

        @Throws(IOException::class, ApiException::class)
        override fun convert(responseBody: ResponseBody): T? {
            val response = delegate.convert(responseBody)
            // TODO: response 为null？？
            if (response?.error != null) {
                throw ApiException(response.error!!.code, response.error!!.message)
            }
//            if (response?.result == null) {
//                throw ApiException(NO_EXPECTED_DATA_EXCEPTION, "数据没有正常返回")
//            }
            // TODO: 如果result是null，那一定是错了，抛出异常
            return response?.result
        }
    }

    override fun requestBodyConverter(
        type: Type, annotations: Array<Annotation>,
        methodAnnotations: Array<Annotation>, retrofit: Retrofit
    ): Converter<*, RequestBody>? {
        if (!AnnotationUtils.isAnnotationPresent(methodAnnotations, JsonRpcCall::class.java)) {
            return null
        }
        val method = "call"

        val delegate = retrofit.nextRequestBodyConverter<JsonRpcRequest>(
            this, JsonRpcRequest::class.java, annotations, methodAnnotations
        )

        return JsonRpcRequestBodyConverter<JsonRpcRequest>(method, delegate)
    }

    internal class JsonRpcRequestBodyConverter<F>(
        private val method: String,
        private val delegate: Converter<JsonRpcRequest, RequestBody>
    ) :
        Converter<F, RequestBody> {

        @Throws(IOException::class)
        override fun convert(value: F): RequestBody? {
            return delegate.convert(JsonRpcRequest.create(method, value))
        }
    }

    companion object {
        @JvmStatic
        fun create() = JsonRpcConverterFactory()
    }
}
