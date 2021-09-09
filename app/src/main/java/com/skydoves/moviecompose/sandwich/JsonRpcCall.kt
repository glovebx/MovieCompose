package com.skydoves.moviecompose.sandwich

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy


/** Make a JSON-RPC request.  */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(RetentionPolicy.RUNTIME)
annotation class JsonRpcCall(
    /** The name of RPC method being invoked by this call.  */
    val value: String = "")
