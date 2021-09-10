package com.skydoves.moviecompose.accounts

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.NetworkErrorException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import java.lang.UnsupportedOperationException


public class Authenticator(private val context: Context, private val odooAccountManager: OdooAccountManager) :
    AbstractAccountAuthenticator(context) {
    // android.accounts.AbstractAccountAuthenticator
    override fun confirmCredentials(
        accountAuthenticatorResponse: AccountAuthenticatorResponse,
        account: Account,
        bundle: Bundle
    ): Bundle? {
        return null
    }

    @Throws(NetworkErrorException::class)  // android.accounts.AbstractAccountAuthenticator
    override fun getAccountRemovalAllowed(
        accountAuthenticatorResponse: AccountAuthenticatorResponse,
        account: Account
    ): Bundle {
        val accountRemovalAllowed =
            super.getAccountRemovalAllowed(accountAuthenticatorResponse, account)
        if (accountRemovalAllowed != null && accountRemovalAllowed.containsKey("booleanResult") && !accountRemovalAllowed.containsKey(
                "intent"
            ) && accountRemovalAllowed.getBoolean("booleanResult")
        ) {
//            odooAccountManager.getAccount(account.name).getSession(context).removeSession()
        }
        return accountRemovalAllowed
    }

    override fun editProperties(
        accountAuthenticatorResponse: AccountAuthenticatorResponse,
        str: String
    ): Bundle {
        throw UnsupportedOperationException()
    }

    override fun addAccount(
        accountAuthenticatorResponse: AccountAuthenticatorResponse?,
        str: String?,
        str2: String?,
        strArr: Array<String?>?,
        bundle: Bundle?
    ): Bundle? {
//        val intent = Intent(context, Login::class.java)
//        intent.putExtra("force_new_login", true)
        val bundle2 = Bundle()
//        bundle2.putParcelable("intent", intent)
        return bundle2
    }

    // android.accounts.AbstractAccountAuthenticator
    override fun getAuthToken(
        accountAuthenticatorResponse: AccountAuthenticatorResponse,
        account: Account,
        str: String,
        bundle: Bundle
    ): Bundle {
        throw UnsupportedOperationException()
    }

    override fun getAuthTokenLabel(str: String): String {
        throw UnsupportedOperationException()
    }

    // android.accounts.AbstractAccountAuthenticator
    override fun updateCredentials(
        accountAuthenticatorResponse: AccountAuthenticatorResponse,
        account: Account,
        str: String,
        bundle: Bundle
    ): Bundle {
        throw UnsupportedOperationException()
    }

    // android.accounts.AbstractAccountAuthenticator
    override fun hasFeatures(
        accountAuthenticatorResponse: AccountAuthenticatorResponse,
        account: Account,
        strArr: Array<String>
    ): Bundle {
        throw UnsupportedOperationException()
    }
}
