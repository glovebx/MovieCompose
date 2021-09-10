package com.skydoves.moviecompose.accounts

import android.app.Service
import android.content.Intent
import android.os.IBinder

public class AuthenticatorService(
    private val odooAccountManager: OdooAccountManager
) : Service() {
    private val authenticator: Authenticator by lazy { Authenticator(this, odooAccountManager) }

    override fun onBind(intent: Intent): IBinder? {
        return authenticator.iBinder
    }
}
