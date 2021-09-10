package com.skydoves.moviecompose.exceptions

const val UNKNOWN_EXCEPTION = -1001L
const val NO_EXPECTED_DATA_EXCEPTION = -1002L

class ApiException: Throwable {
    val code: Long

    constructor(message:String?): super(message) {
        this.code = UNKNOWN_EXCEPTION
    }

    constructor(code:Long, message:String?): super(message) {
        this.code = code
    }
}