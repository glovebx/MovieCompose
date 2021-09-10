package com.skydoves.moviecompose.accounts

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import kotlinx.parcelize.Parcelize
import java.lang.StringBuilder
import java.util.*

@Parcelize
class OdooUser : Parcelable {
    var account: Account? = null
    var active: String? = null
    var avatar: String? = null
    var database: String? = null
    var fcmToken: String? = null
    var host: String? = null
    var httpUserCredentials: String? = null
    var id = 0
    var name: String? = null
    var ocnToken: String? = null
    var sessionId: String? = null
    var username: String? = null

    companion object {
        fun fromBundle(accountManager: AccountManager, account2: Account): OdooUser {
            val odooUser = OdooUser()
//        odooUser.id = accountManager.getUserData(account2, "uid").toInt()
//        odooUser.avatar = accountManager.getUserData(account2, "avatar")
//        odooUser.name = accountManager.getUserData(account2, "name")
//        odooUser.host = accountManager.getUserData(account2, "host")
//        odooUser.username = accountManager.getUserData(account2, "username")
//        odooUser.database = accountManager.getUserData(account2, "database")
//        odooUser.active = accountManager.getUserData(account2, "active")
//        odooUser.account = account2
//        odooUser.http_user_credentials =
//            accountManager.getUserData(account2, "http_user_credentials")
//        odooUser.ocn_token = accountManager.getUserData(account2, FCMPlugin.OCN_TOKEN)
//        odooUser.fcm_token = accountManager.getUserData(account2, "fcm_token")
            return odooUser
        }
    }
}
