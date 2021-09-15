package com.skydoves.moviecompose.accounts

public class OdooManager {

    companion object {
        @Volatile
        var serverUrl: String? = null
        @Volatile
        var db: String? = null
        @Volatile
        var sessionId: String? = null
    }

}
