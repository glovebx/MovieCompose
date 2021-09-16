package com.skydoves.moviecompose.exceptions

sealed class BizError(val code: Int, val message: String, var data: Any?)