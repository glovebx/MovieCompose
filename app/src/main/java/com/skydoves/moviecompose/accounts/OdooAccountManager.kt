package com.skydoves.moviecompose.accounts

import android.accounts.Account
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.content.Context
import com.skydoves.moviecompose.R

public class OdooAccountManager constructor(
    private val context: Context,
    private val accountManager: AccountManager
) {

    @SuppressLint("MissingPermission")
    private fun getAccounts(): Array<Account> {
        return accountManager.getAccountsByType(context.getString(R.string.account_type))
    }

    private fun findAccount(accountName: String): Account? {
        val accounts = getAccounts()
        for (account in accounts) {
            if (account.name == accountName) {
                return account
            }
        }
        return null
    }

    fun getUserAccounts(): List<OdooUser> {
        val arrayList = mutableListOf<OdooUser>()
        for (account in getAccounts()) {
            arrayList.add(OdooUser.fromBundle(accountManager, account))
        }
        return arrayList
    }

    fun getAccount(accountName: String): OdooUser? {
        val findAccount: Account? = findAccount(accountName)
        return if (findAccount != null) {
            OdooUser.fromBundle(accountManager, findAccount)
        } else null
    }
}
