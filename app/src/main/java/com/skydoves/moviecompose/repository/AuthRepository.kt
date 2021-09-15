/*
 * Designed and developed by 2021 skydoves (Jaewoong Eum)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.skydoves.moviecompose.repository

import androidx.annotation.WorkerThread
import com.skydoves.moviecompose.accounts.OdooManager
import com.skydoves.moviecompose.models.OdooLogin
import com.skydoves.moviecompose.network.service.AuthService
import com.skydoves.moviecompose.persistence.AuthDao
import com.skydoves.moviecompose.ui.login.AuthenticateResult
import com.skydoves.sandwich.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import timber.log.Timber

class AuthRepository constructor(
    private val authService: AuthService,
    private val authDao: AuthDao
) : Repository {

    init {
        Timber.d("Injection AuthRepository")
    }

    @WorkerThread
    fun loadVersionAndDatabaseInfo(url: String, success: () -> Unit, error: () -> Unit) = flow {
        OdooManager.serverUrl = url
        val params = mapOf<String, Any>()
        val version = authService.fetchVersionInfo(params).getOrNull()
        if (version == null) {
            error()
        } else {
            // TODO: 判断是不是企业版
            val databaseList = authService.fetchDatabaseList(params).getOrElse(listOf())
            emit(databaseList)
        }
    }.onCompletion { success() }.flowOn(Dispatchers.IO)

    @WorkerThread
    fun setupDatabaseName(db: String) = flow {
        OdooManager.db = db
        emit(db)
    }.flowOn(Dispatchers.IO)

    @WorkerThread
    fun authenticate(
        odooLogin: OdooLogin,
        success: () -> Unit,
        error: () -> Unit
    ) = flow {
        val params = mapOf<String, Any>("db" to odooLogin.db,
            "login" to odooLogin.login, "password" to odooLogin.password)
        val response = authService.authenticate(params)
        response.suspendOnSuccess {
            OdooManager.sessionId = headers["Set-Cookie"]?.split(";").let {
                var sessionId: String = ""
                it?.forEach { it2 ->
                   val kv = it2.split("=")
                    if (kv[0] == "session_id") {
                        sessionId = kv[1]
                        return@forEach
                    }
                }
                sessionId
            }
            data.afterAuthenticated(OdooManager.serverUrl!!, OdooManager.sessionId)

            // TODO: 保存到数据库
            val odooAuthenticate = authDao.getCurrentOdooAuthenticate()
            odooAuthenticate?.apply {
                if (this.serverUrl != data.serverUrl
                    || this.db != data.db
                    || this.uid != data.uid
                ) {
                    this.active = 0
                    authDao.updateOdooAuthenticate(this)
                }
            }
            authDao.insertOdooAuthenticate(data)

            emit(AuthenticateResult.AUTHENTICATED)
//        }.onError {
//            error()
//        }.onException { error()
        }
            .suspendOnError {
                emit(AuthenticateResult.AUTHENTICATE_FAILED)
            }
            .suspendOnException {
                emit(AuthenticateResult.AUTHENTICATE_FAILED)
            }
    }.onCompletion { success() }.flowOn(Dispatchers.IO)

    // 验证当前账号的sessionId是否还有效
    @WorkerThread
    fun authenticate(
        success: () -> Unit
    ) = flow {
        val params = mapOf<String, Any>()
        val odooAuthenticate = authDao.getCurrentOdooAuthenticate()
        if (odooAuthenticate != null) {
            OdooManager.serverUrl = odooAuthenticate.serverUrl
            OdooManager.db = odooAuthenticate.db
            OdooManager.sessionId = odooAuthenticate.sessionId
            val response = authService.fetchSessionInfo(params)
            response.suspendOnSuccess {
                // 当前账号有效，更新本地数据库？？
                data.afterAuthenticated(odooAuthenticate.serverUrl, odooAuthenticate.sessionId!!)
                authDao.insertOdooAuthenticate(data)

                emit(AuthenticateResult.AUTHENTICATED)
            }.suspendOnError {
                emit(AuthenticateResult.SESSION_EXPIRED)
            }
        } else {
            emit(AuthenticateResult.ACCOUNT_NOT_EXISTS)
        }
    }.onCompletion { success() }.flowOn(Dispatchers.IO)
}
