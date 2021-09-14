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
import com.skydoves.moviecompose.models.entities.Database
import com.skydoves.moviecompose.network.service.AuthService
import com.skydoves.moviecompose.persistence.MovieDao
import com.skydoves.sandwich.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import timber.log.Timber

class AuthRepository constructor(
  private val authService: AuthService
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
    fun authenticate(db: String, login: String, password: String, success: () -> Unit, error: () -> Unit) = flow {
        val params = mapOf<String, Any>("db" to db, "login" to login, "password" to password)
        val response = authService.authenticate(params)
        response.suspendOnSuccess {
        // TODO: 保存到数据库
         emit(data)
        }.onError {
            error()
        }.onException { error() }
    }.onCompletion { success() }.flowOn(Dispatchers.IO)
}
